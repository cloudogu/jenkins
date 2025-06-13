@Library([
  'github.com/cloudogu/pipe-build-lib@develop',
  'ces-build-lib',
  'dogu-build-lib'
]) _

// Create instance of DoguPipe with configuration parameters
def pipe = new com.pipebuildlib.DoguPipe(this, [
    doguName           : "jenkins",

    // Optional behavior settings
    shellScripts       : "resources/startup.sh resources/upgrade-notification.sh resources/pre-upgrade.sh",
    dependencies       : ["cas", "usermgt"],
    checkMarkdown      : true,
    runIntegrationTests: true,
    cypressImage       : "cypress/included:13.16.1"
])

// Set default or custom build parameters (can also pass a list to override defaults)
pipe.setBuildProperties()
pipe.setupStages()

// Insert a custom post-integration stage directly after the "Integration Tests" stage
pipe.insertStageAfter("Integration Tests", "Post Integration Tests", {
    def eco = pipe.ecoSystem
    stage("Test: Change Global Admin Group") {
        // Change the global admin group and restart jenkins
        eco.changeGlobalAdminGroup("newAdminGroup")
        eco.restartDogu("jenkins")
        eco.waitForDogu("jenkins")

        // Run Cypress tests again without video/screenshot recording
        eco.runCypressIntegrationTests([
            cypressImage     : "cypress/included:13.16.1",
            enableVideo      : false,
            enableScreenshots: false
        ])
    }
})


// Run the pipeline – this will execute all previously added stages
pipe.run()
