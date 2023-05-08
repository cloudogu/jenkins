#!groovy
@Library(['github.com/cloudogu/ces-build-lib@1.64.2', 'github.com/cloudogu/dogu-build-lib@v2.1.0'])
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
                choice(name: 'TrivyScanLevels', choices: [TrivyScanLevel.CRITICAL, TrivyScanLevel.HIGH, TrivyScanLevel.MEDIUM, TrivyScanLevel.ALL], description: 'The levels to scan with trivy'),
                choice(name: 'TrivyStrategy', choices: [TrivyScanStrategy.UNSTABLE, TrivyScanStrategy.FAIL, TrivyScanStrategy.IGNORE], description: 'Define whether the build should be unstable, fail or whether the error should be ignored if any vulnerability was found.'),
            ])
        ])

        EcoSystem ecoSystem = new EcoSystem(this, "gcloud-ces-operations-internal-packer", "jenkins-gcloud-ces-operations-internal")
	Trivy trivy = new Trivy(this, ecoSystem)

        stage('Checkout') {
            checkout scm
        }

        stage('Lint') {
            lintDockerfile()
            shellCheck("resources/startup.sh resources/upgrade-notification.sh resources/pre-upgrade.sh")

            if (env.CHANGE_TARGET) {
                echo 'This is a pull request; checking changelog...'
                String newChanges = changelog.changesForVersion('Unreleased')
                if (!newChanges || newChanges.allWhitespace) {
                    unstable('CHANGELOG.md should contain new change entries in the `[Unreleased]` section but none were found.')
                }
            }
        }

        stage('Check Markdown Links') {
            Markdown markdown = new Markdown(this)
            markdown.check()
        }

        try {
            stage('Bats Tests') {
                Bats bats = new Bats(this, docker)
                bats.checkAndExecuteTests()
            }

            stage('Provision') {
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
                trivy.scanDogu("/dogu", TrivyScanFormat.HTML, params.TrivyScanLevels, params.TrivyStrategy)
                trivy.scanDogu("/dogu", TrivyScanFormat.JSON,  params.TrivyScanLevels, params.TrivyStrategy)
                trivy.scanDogu("/dogu", TrivyScanFormat.PLAIN, params.TrivyScanLevels, params.TrivyStrategy)
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
        cypressImage     : "cypress/included:8.7.0",
        enableVideo      : videoRecording,
        enableScreenshots: screenshotRecording
    ])
}
