
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
    cypressImage        : "cypress/included:13.16.1",

])

pipe.setBuildProperties()
pipe.addDefaultStages()

pipe.insertStageAfter("Integration tests","Test: Change Global Admin Group") {
    def ctx = pipe.ecoSystem
        ctx.changeGlobalAdminGroup("newAdminGroup")
        ctx.restartDogu("jenkins")
        ctx.waitForDogu("jenkins")

        ctx.runCypressIntegrationTests([
            cypressImage     : "cypress/included:13.16.1",
            enableVideo      : false,
            enableScreenshots: false
        ])
}
pipe.run()
