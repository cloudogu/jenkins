
@Library([
  'pipe-build-lib',
  'ces-build-lib',
  'dogu-build-lib'
]) _

def pipe = new com.cloudogu.sos.pipebuildlib.DoguPipe(this, [
    doguName           : "jenkins",
    shellScripts        : "resources/startup.sh resources/upgrade-notification.sh resources/pre-upgrade.sh",
    dependencies        : ["cas", "usermgt"],
    checkMarkdown       : true,
    runIntegrationTests : true,
    // Default cypress/included:13.17.0 bundles Node 22.13, too old for
    // cosmiconfig@10 (pulled in by @badeball/cypress-cucumber-preprocessor@28,
    // required for cypress@16 compatibility). Override to an image with a
    // newer bundled Node until the shared pipeline lib's own default catches up.
    cypressImage        : "cypress/included:16.1.0",

])

pipe.setBuildProperties()
pipe.addDefaultStages()

pipe.insertStageAfter("Integration tests","Test: Change Global Admin Group") {
    def ctx = pipe.ecoSystem
        ctx.changeGlobalAdminGroup("newAdminGroup")
        ctx.restartDogu("jenkins")
        ctx.waitForDogu("jenkins")

        ctx.runCypressIntegrationTests([
            // Default cypress/included:13.17.0 bundles Node 22.13, too old for
            // cosmiconfig@10 (pulled in by @badeball/cypress-cucumber-preprocessor@28,
            // required for cypress@16 compatibility). Override to an image with a
            // newer bundled Node until the shared pipeline lib's own default catches up.
            cypressImage     : "cypress/included:16.1.0",
            enableVideo      : false,
            enableScreenshots: false
        ])
}
pipe.run()
