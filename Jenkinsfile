#!groovy
@Library(['github.com/cloudogu/ces-build-lib@feature/check_release_notes', 'github.com/cloudogu/dogu-build-lib@v3.0.0'])
import com.cloudogu.ces.cesbuildlib.*
import com.cloudogu.ces.dogubuildlib.*

node('vagrant') {

    String productionReleaseBranch = "main"

    String doguName = "jenkins"
    Git git = new Git(this, "cesmarvin")
    git.committerName = 'cesmarvin'
    git.committerEmail = 'cesmarvin@cloudogu.com'
    GitFlow gitflow = new GitFlow(this, git)
    GitHub github = new GitHub(this, git)
    Changelog changelog = new Changelog(this)
    ReleaseNotes releaseNotes = new ReleaseNotes(this)

    timestamps {
        properties([
            // Keep only the last x builds to preserve space
            buildDiscarder(logRotator(numToKeepStr: '10')),
            // Don't run concurrent builds for a branch, because they use the same workspace directory
            disableConcurrentBuilds(),
            // Parameter to activate dogu upgrade test on demand
            parameters([
                booleanParam(defaultValue: false, description: 'Test dogu upgrade from latest release or optionally from defined version below', name: 'TestDoguUpgrade'),
                string(defaultValue: '', description: 'Old Dogu version for the upgrade test (optional; e.g. 2.222.1-1)', name: 'OldDoguVersionForUpgradeTest'),
                booleanParam(defaultValue: false, description: 'Enables the video recording during the test execution', name: 'EnableVideoRecording'),
                booleanParam(defaultValue: false, description: 'Enables the screenshot recording during the test execution', name: 'EnableScreenshotRecording'),
                choice(name: 'TrivySeverityLevels', choices: [TrivySeverityLevel.CRITICAL, TrivySeverityLevel.HIGH_AND_ABOVE, TrivySeverityLevel.MEDIUM_AND_ABOVE, TrivySeverityLevel.ALL], description: 'The levels to scan with trivy'),
                choice(name: 'TrivyStrategy', choices: [TrivyScanStrategy.UNSTABLE, TrivyScanStrategy.FAIL, TrivyScanStrategy.IGNORE], description: 'Define whether the build should be unstable, fail or whether the error should be ignored if any vulnerability was found.'),
            ])
        ])

        EcoSystem ecoSystem = new EcoSystem(this, "gcloud-ces-operations-internal-packer", "jenkins-gcloud-ces-operations-internal")

        stage('Checkout') {
            checkout scm
        }

        stage('Lint') {
            lintDockerfile()
            shellCheck("resources/startup.sh resources/upgrade-notification.sh resources/pre-upgrade.sh")
        }

        stage('Check changelog') {
            checkChangelog(changelog)
        }

        stage('Check release notes') {
            checkReleaseNotes(releaseNotes)
        }

        stage('Check Markdown Links') {
	    error("DEBUG: REICHT FÜR HEUTE")
            Markdown markdown = new Markdown(this)
            markdown.check()
        }

        try {
            stage('Bats Tests') {
                Bats bats = new Bats(this, docker)
                bats.checkAndExecuteTests()
            }

            stage('Provision') {
                // change namespace to prerelease_namespace if in develop-branch
                if (gitflow.isPreReleaseBranch()) {
                    sh "make prerelease_namespace"
                }
                ecoSystem.provision("/dogu")
            }

            stage('Setup') {
                ecoSystem.loginBackend('cesmarvin-setup')
                ecoSystem.setup()
            }

            stage('Wait for dependencies') {
                timeout(15) {
                    ecoSystem.waitForDogu("cas")
                    ecoSystem.waitForDogu("usermgt")
                }
            }

            stage('Build') {
                ecoSystem.build("/dogu")
            }

            stage('Trivy scan') {
                ecoSystem.copyDoguImageToJenkinsWorker("/dogu")
                Trivy trivy = new Trivy(this)
                trivy.scanDogu(".", params.TrivySeverityLevels, params.TrivyStrategy)
                trivy.saveFormattedTrivyReport(TrivyScanFormat.TABLE)
                trivy.saveFormattedTrivyReport(TrivyScanFormat.JSON)
                trivy.saveFormattedTrivyReport(TrivyScanFormat.HTML)
            }

            stage('Verify') {
                ecoSystem.verify("/dogu")
            }

            stage('Integration tests') {
                runIntegrationTests(ecoSystem, params.EnableVideoRecording, params.EnableScreenshotRecording)
            }

            stage('Test: Change Global Admin Group') {
                ecoSystem.changeGlobalAdminGroup("newAdminGroup")
                // this waits until the dogu is up and running
                ecoSystem.restartDogu("jenkins")
                ecoSystem.waitForDogu("jenkins")
                runIntegrationTests(ecoSystem, params.EnableVideoRecording, params.EnableScreenshotRecording)
            }

            if (params.TestDoguUpgrade != null && params.TestDoguUpgrade){
                stage('Upgrade dogu'){
                    ecoSystem.upgradeFromPreviousRelease(params.OldDoguVersionForUpgradeTest, doguName)
                }
                stage('Integration Tests - After Upgrade'){
                    // Run integration tests again to verify that the upgrade was successful
                    runIntegrationTests(ecoSystem, params.EnableVideoRecording, params.EnableScreenshotRecording)
                }
            }

            if (gitflow.isReleaseBranch()) {
                String releaseVersion = git.getSimpleBranchName()

                stage('Finish Release') {
                    gitflow.finishRelease(releaseVersion, productionReleaseBranch)
                }

                stage('Push Dogu to registry') {
                    ecoSystem.push("/dogu")
                }

                stage ('Add Github-Release'){
                    github.createReleaseWithChangelog(releaseVersion, changelog, productionReleaseBranch)
                }
            } else if (gitflow.isPreReleaseBranch()) {
                // push to registry in prerelease_namespace
                stage('Push Prerelease Dogu to registry') {
                     ecoSystem.pushPreRelease("/dogu")
                }
            }

        } finally {
            stage('Clean') {
                ecoSystem.destroy()
            }
        }
    }
}

def runIntegrationTests(EcoSystem ecoSystem, boolean videoRecording, boolean screenshotRecording) {
    ecoSystem.runCypressIntegrationTests([
        cypressImage     : "cypress/included:13.16.1",
        enableVideo      : videoRecording,
        enableScreenshots: screenshotRecording
    ])
}
