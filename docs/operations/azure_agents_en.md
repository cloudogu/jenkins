# Set up Jenkins agents in the Azure Cloud

This text describes how to create and configure Jenkins agents to run on Azure Cloud Virtual Machines.
It includes the installation of Docker to run containers inside Jenkins pipelines.

## Building an Azure image with Packer

- Install [Packer](https://www.packer.io/)
- Create the Packer template for the VM image, e.g. `azure-jenkins-agent.pkr.hcl`:

```hcl
packer {
  required_plugins {
    azure = {
      source  = "github.com/hashicorp/azure"
      version = "~> 1"
    }
  }
}

variable "client_id" {
  type = string
}

variable "client_secret" {
  type = string
}

variable "subscription_id" {
  type = string
}

variable "tenant_id" {
  type = string
}

source "azure-arm" "jenkins-agent" {
  client_id                         = "${var.client_id}"
  client_secret                     = "${var.client_secret}"
  image_offer                       = "0001-com-ubuntu-server-jammy"
  image_publisher                   = "canonical"
  image_sku                         = "22_04-lts"
  location                          = "WestEurope"
  managed_image_name                = "cesJenkinsDockerAgent"
  managed_image_resource_group_name = "ces_group"
  os_type                           = "Linux"
  subscription_id                   = "${var.subscription_id}"
  tenant_id                         = "${var.tenant_id}"
  vm_size                           = "Standard_D3_v2"
}

build {
  sources = ["source.azure-arm.jenkins-agent"]

  provisioner "shell" {
    inline = ["sudo apt-get update",
              "sudo apt-get install -y apt-transport-https openjdk-11-jre git ca-certificates curl software-properties-common",
              "curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg",
              "echo \"deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable\" | sudo tee /etc/apt/sources.list.d/docker.list",
              "sudo apt-get update",
              "sudo apt-get install -y docker-ce",
              "sudo adduser --disabled-password --gecos \"\" jenkinsagent",
              "sudo usermod -a -G docker jenkinsagent"]
  }

}
```

- Adapt the `managed_image_resource_group_name` to a resource group in your Azure environment
- Create the corresponding variables file, e.g. `azure-jenkins-agent-variables.auto.pkrvars.hcl`:

```hcl
client_id = ""
client_secret = ""
tenant_id = ""
subscription_id = ""
```

- Get the credentials needed in the variables file via Azure CLI commands:
  - Use `az login` to connect to your Azure environment
  - Get the subscription ID via `az account show --query "{ subscription_id: id }"`
  - Get the client ID, tenant ID and client secret via `az ad sp create-for-rbac --role Contributor --scopes /subscriptions/SUBSCRIPTION_ID --query "{ client_id: appId, client_secret: password, tenant_id: tenant }"`
    - Replace `SUBSCRIPTION_ID` with the subscription ID obtained in the step before

- Install required plugins via `packer init .`
- Build the VM image via `packer build .`
- You will get a VM image in your Azure environment called `cesJenkinsDockerAgent`

## Installing Azure VM Agents plugin

- Install the Azure VM Agents plugin (and, optionally, the Docker Pipeline plugin) with this script:

```groovy
import hudson.model.*;
import jenkins.model.*;
import groovy.json.JsonSlurper;
import hudson.util.VersionNumber
import hudson.PluginWrapper

def jenkins = Jenkins.instance;
def pluginManager = jenkins.pluginManager;
def updateCenter = jenkins.updateCenter;

def keyExists(String key) {
    String ip = new File("/etc/ces/node_master").getText("UTF-8").trim();
    URL url = new URL("http://${ip}:4001/v2/keys/${key}");
    try {
        def json = new JsonSlurper().parseText(url.text)
    } catch (FileNotFoundException) {
        return false
    }
    return true
}

def getValueFromEtcd(String key) {
    String ip = new File("/etc/ces/node_master").getText("UTF-8").trim();
    URL url = new URL("http://${ip}:4001/v2/keys/${key}");
    try {
        def json = new JsonSlurper().parseText(url.text)
        return json.node.value
    } catch (FileNotFoundException) {
        return false
    }
}

boolean isVersionSufficient(PluginWrapper plugin, VersionNumber versionNumber) {
    return plugin.getVersionNumber().isNewerThanOrEqualTo(versionNumber)
}

try {
    pluginManager.doCheckUpdatesServer();
} catch (IOException ex) {
    println "Plugin update server unreachable"
    println ex
}

// configuration
def plugins = [
        'azure-vm-agents',
        'docker-workflow'
];

def additionalPluginPath = "config/jenkins/additional.plugins";

if (keyExists(additionalPluginPath)) {
    println("Install additional plugins");
    def additionalPluginList = getValueFromEtcd(additionalPluginPath);
    def additionalPlugins = additionalPluginList.split(',');
    for (additionalPlugin in additionalPlugins) {
        println("Add Plugin " + additionalPlugin)
        plugins.add(additionalPlugin)
    }
} else {
    println("No additional plugins configured");
}

def availablePlugins = updateCenter.getAvailables();
println "available plugins: " + availablePlugins.size()
for (def shortName : plugins) {
    def plugin = updateCenter.getPlugin(shortName);
    if (availablePlugins.contains(plugin)) {
        println "Installing missing plugin " + shortName;
        plugin.deploy(true).get();
    } else {
        println "Plugin not available or already installed : " + shortName
    }
}

if (updateCenter.isRestartRequiredForCompletion()) {
    jenkins.restart();
}
```

- You can run this script by adding it to the additional scripts directory in Jenkins or by executing it on the script console
  - Run script via the Jenkins additional scripts directory:
    - Save the groovy script above as `init150NORMinstallAzurePlugins.groovy`
    - Get the Jenkins pod name via `JENKINSPODID=$(kubectl get pods --namespace ecosystem | grep jenkins | awk 'NR==1{print $1}')`
    - Copy script to Jenkins data volume: `kubectl cp --namespace ecosystem init150NORMinstallAzurePlugins.groovy ${JENKINSPODID}:/var/lib/custom.init.groovy.d/init150NORMinstallAzurePlugins.groovy`
    - Restart Jenkins by deleting the Jenkins pod (which will be re-created), so that the script will be executed on startup
  - Run script via the Jenkins script console
    - In the Jenkins GUI go to `Manage Jenkins` -> `Script Console`
    - Paste the groovy script above into the text field and click `Run`

## Configuring Azure Cloud in Jenkins

- Use the following script to configure Azure agents in Jenkins:

```groovy
import com.microsoft.azure.vmagent.builders.*
import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.plugins.credentials.CredentialsScope
import com.microsoft.azure.util.AzureCredentials
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import hudson.util.Secret

String jenkinsAgentAdminCredentialsID = "jenkins-agent-connection-credentials"
String jenkinsAgentAdminCredentialsDescription = "Jenkins Agent User used on Azure VMs"
String jenkinsAgentAdminCredentialsUsername = "jenkinsagent" // This has to be the same as in the Packer template!
String jenkinsAgentAdminCredentialsPassword = "JenkinsAgent123" // Change to something more secure
String azureCredentialsID = "azureCredentials"
String azureCredentialsDescription = "Credentials to interact with Azure Cloud"
String azureCredentialsSubscriptionID = ""
String azureCredentialsClientID = ""
String azureCredentialsClientSecret = ""
String azureCredentialsTenantID = ""
String azureResourceGroup = ""

// Add azure credentials
// See https://nickcharlton.net/posts/setting-jenkins-credentials-with-groovy.html
instance = Jenkins.instance
domain = Domain.global()
systemCredentialStore = instance.getExtensionList(
        "com.cloudbees.plugins.credentials.SystemCredentialsProvider")[0].getStore()
domainCredentialsMap = SystemCredentialsProvider.getInstance().getDomainCredentialsMap();
domainCredentials = domainCredentialsMap.get(Domain.global());

jenkinsAgentAdminCredentials = new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL,
        jenkinsAgentAdminCredentialsID,
        jenkinsAgentAdminCredentialsDescription,
        jenkinsAgentAdminCredentialsUsername,
        jenkinsAgentAdminCredentialsPassword
)

AzureCredentials azureCredentials = new AzureCredentials(
        CredentialsScope.GLOBAL,
        azureCredentialsID,
        azureCredentialsDescription,
        azureCredentialsSubscriptionID,
        azureCredentialsClientID,
        Secret.fromString(azureCredentialsClientSecret));
azureCredentials.setTenant(azureCredentialsTenantID);

systemCredentialStore.addCredentials(domain, jenkinsAgentAdminCredentials)
domainCredentials.add(azureCredentials);
SystemCredentialsProvider.getInstance().setDomainCredentialsMap(domainCredentialsMap);



// Configure Azure Cloud
// See https://plugins.jenkins.io/azure-vm-agents/#plugin-content-configure-vm-template-using-groovy-script
// See https://github.com/jenkinsci/azure-vm-agents-plugin/blob/master/src/main/java/com/microsoft/azure/vmagent/builders/AzureVMCloudBuilder.java
def azureTemplate = new AzureVMTemplateBuilder()
        .withName("jenkins-agent-azure")
        .withLabels("docker node linux ubuntu")
        .withLocation("West Europe")
        .withVirtualMachineSize("Standard_DS3_v2")
        .withExistingStorageAccount("jenkinsagentstor")
        .addNewAdvancedImage()
        .withCustomManagedImage("/subscriptions/${azureCredentialsSubscriptionID}/resourceGroups/${azureResourceGroup}/providers/Microsoft.Compute/images/cesJenkinsDockerAgent")
        .withNetworkSecurityGroupName("Jenkins-Agents_NSG")
        .endAdvancedImage()
        .withAdminCredential(jenkinsAgentAdminCredentialsID)
        .build()
def azureCloud = new AzureVMCloudBuilder()
        .withCloudName("Azure Cloud")
        .withAzureCredentialsId(azureCredentialsID)
        .withExistingResourceGroupName(azureResourceGroup)
        .addToTemplates(azureTemplate)
        .build()
Jenkins.getInstance().clouds.add(azureCloud)

```

- Insert the Azure credentials from the steps before
- Make sure the resource group name is correct
- Run the script via the Jenkins script console or via the additional scripts directory as described above

## Testing with a Jenkins pipeline

You can test if everything worked out correctly by using this test-pipeline:
- Add a Jenkins Pipeline project
- Use the following code for the pipeline:

```groovy
#!groovy
@Library(['github.com/cloudogu/ces-build-lib@1.62.0'])

import com.cloudogu.ces.cesbuildlib.*

node('docker') {
    stage('Clone Repository') {
        checkout scmGit(userRemoteConfigs: [
            [url: 'https://github.com/cloudogu/sonar']
        ])
    }

    def docker = new Docker(this)
    stage('Docker Build') {
        docker.withRegistry("https://my.registry.com", "registrycredentials") {
            docker.build("testing/azuresonar:0.0.0")
            sh 'docker image ls'
        }
    }

    stage('Docker Tag') {
        docker.image("testing/azuresonar:0.0.0").tag("0.0.1")
        sh 'docker image ls'
    }

    stage('Docker Push') {
        docker.withRegistry("https://my.registry.com", "registrycredentials") {
            docker.image("testing/azuresonar:0.0.1").push()
        }
    }
}

```

- Add Jenkins credentials with the ID `registrycredentials` to store your registry credentials
- Adapt the registry URL to your container image registry
- On execution, a new Azure VM should be deployed, connect to Jenkins and run the pipeline