#!groovy
@Library(['github.com/cloudogu/ces-build-lib@2.4.0', 'github.com/cloudogu/dogu-build-lib@v2.4.0'])
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

        try {
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
                trivy.scanDogu("/dogu", TrivyScanFormat.PLAIN, params.TrivyScanLevels, params.TrivyStrategy)
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
        cypressImage     : "cypress/included:12.16.0",
        enableVideo      : videoRecording,
        enableScreenshots: screenshotRecording
    ])
}
