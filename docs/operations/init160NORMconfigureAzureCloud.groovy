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
