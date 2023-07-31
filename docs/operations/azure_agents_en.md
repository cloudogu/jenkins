# Set up Jenkins agents in the Azure Cloud

This text describes how to create and configure Jenkins agents to run on Azure Cloud Virtual Machines.
It includes the installation of Docker to run containers inside Jenkins pipelines.

You will need the [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) tool and
[Packer](https://www.packer.io/) installed on your local machine.
You also need access to your Kubernetes cluster via `kubectl`.


## Building an Azure image with Packer

- Create a Packer template for the VM image like [azure-jenkins-agent.pkr.hcl](azure-jenkins-agent.pkr.hcl)
   - Adapt `source.azure-arm.jenkins-agent.managed_image_resource_group_name` to a resource group in your Azure environment
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

- Install the Azure VM Agents plugin (and, optionally, the Docker Pipeline plugin) with [init150NORMinstallAzurePlugins.groovy](init150NORMinstallAzurePlugins.groovy)
- You can run this script by adding it to the additional scripts directory in Jenkins or by executing it on the script console
  - Run script via the Jenkins additional scripts directory:
    - Get the Jenkins pod name via `JENKINSPODID=$(kubectl get pods --namespace ecosystem | grep jenkins | awk 'NR==1{print $1}')`
    - Copy script to Jenkins data volume: `kubectl cp --namespace ecosystem init150NORMinstallAzurePlugins.groovy ${JENKINSPODID}:/var/lib/custom.init.groovy.d/init150NORMinstallAzurePlugins.groovy`
    - Restart Jenkins by deleting the Jenkins pod (which will be re-created), so that the script will be executed on startup
  - Run script via the Jenkins script console:
    - In the Jenkins GUI go to `Manage Jenkins` -> `Script Console`
    - Paste the groovy script above into the text field and click `Run`

## Configuring Azure Cloud in Jenkins

- Use the script [init160NORMconfigureAzureCloud.groovy](init160NORMconfigureAzureCloud.groovy) to configure Azure agents in Jenkins
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
            [url: 'https://github.com/ianmiell/simple-dockerfile/']
        ])
    }

    def docker = new Docker(this)
    stage('Docker Build') {
        docker.build("testing/azuretest:0.0.0")
        sh 'docker image ls'
    }

    stage('Docker Tag') {
        docker.image("testing/azuretest:0.0.0").tag("0.0.1")
        sh 'docker image ls'
    }

    stage('Docker Push') {
        String REGISTRY_URL = ""
        docker.withRegistry(REGISTRY_URL, "registrycredentials") {
            docker.image("testing/azuretest:0.0.1").push()
        }
    }
}

```

- Adapt the registry URL (REGISTRY_URL) to your container image registry
- Add Jenkins credentials with the ID `registrycredentials` to store your container image registry credentials
- Execute the pipeline. A new Azure VM should be deployed, connect to Jenkins and run the pipeline
