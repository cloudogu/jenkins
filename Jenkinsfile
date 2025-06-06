#!groovy
@Library([
  'github.com/cloudogu/build-lib-wrapper@develop',
  'ces-build-lib', // versioning handled by Global Trusted Pipeline Libraries in Jenkins
  'dogu-build-lib' // versioning handled by Global Trusted Pipeline Libraries in Jenkins
]) _

def postIntegrationStage = { ctx ->
    stage('Test: Change Global Admin Group') {
        ctx.ecoSystem.changeGlobalAdminGroup("newAdminGroup")
        ctx.ecoSystem.restartDogu("jenkins")
        ctx.ecoSystem.waitForDogu("jenkins")

        ctx.ecoSystem.runCypressIntegrationTests([
            cypressImage     : ctx.cypressImage,
            enableVideo      : ctx.params.EnableVideoRecording,
            enableScreenshots: ctx.params.EnableScreenshotRecording
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
