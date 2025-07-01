
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
    def ctx = pipe.script
    new com.cloudogu.ces.cesbuildlib.Docker(ctx)
        .image('golang:1.23')
        .mountJenkinsUser()
        .inside('-e ENVIRONMENT=ci')
    {
            ctx.sh 'make carp-clean'
            ctx.sh 'make build-carp'
            ctx.sh 'make carp-unit-test'
    }

    stage('Test: Change Global Admin Group') {
        ctx.ecoSystem.changeGlobalAdminGroup("newAdminGroup")
        ctx.ecoSystem.restartDogu("jenkins")
        ctx.ecoSystem.waitForDogu("jenkins")

        ctx.ecoSystem.runCypressIntegrationTests([
            cypressImage     : "cypress/included:13.16.1",
            enableVideo      : false,
            enableScreenshots: false
        ])
    }
}
pipe.run()
