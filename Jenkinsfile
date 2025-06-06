#!groovy
@Library([
  'github.com/cloudogu/build-lib-wrapper@develop',
  'ces-build-lib', // versioning handled by Global Trusted Pipeline Libraries in Jenkins
  'dogu-build-lib' // versioning handled by Global Trusted Pipeline Libraries in Jenkins
]) _

def postIntegrationStage = { ecoSystem ->
    stage('Test: Change Global Admin Group') {
        ecoSystem.changeGlobalAdminGroup("newAdminGroup")
        ecoSystem.restartDogu("jenkins")
        ecoSystem.waitForDogu("jenkins")

        ecoSystem.runCypressIntegrationTests([
            cypressImage     : "cypress/included:13.16.1",
            enableVideo      : false,
            enableScreenshots: false
        ])
    }
}

// Now call the sharedBuildPipeline function with your custom configuration.
sharedBuildPipeline([
    // Required parameter
    doguName: "jenkins",
    
    // Optional parameters – override defaults here
    preBuildAgent       : 'sos',
    buildAgent          : 'sos',
    doguDirectory       : "/dogu",
    namespace           : "official",
    
    // Credentials and git information
    gitUser             : "cesmarvin",
    committerEmail      : "cesmarvin@cloudogu.com",
    gcloudCredentials   : "gcloud-ces-operations-internal-packer",
    sshCredentials      : "jenkins-gcloud-ces-operations-internal",
    backendUser         : "cesmarvin-setup",
    
    // Additional options
    updateSubmodules    : false,
    shellScripts        : "resources/startup.sh resources/upgrade-notification.sh resources/pre-upgrade.sh",
    dependencies        : ["cas", "usermgt"],
    checkMarkdown       : true,
    runIntegrationTests : true,
    cypressImage        : "cypress/included:13.16.1",
    postIntegrationStage: postIntegrationStage
])
