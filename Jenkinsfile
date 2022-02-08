#!groovy
@Library(['github.com/cloudogu/ces-build-lib@v1.48.0', 'github.com/cloudogu/dogu-build-lib@v1.6.0'])
import com.cloudogu.ces.cesbuildlib.*
import com.cloudogu.ces.dogubuildlib.*

node('vagrant') {

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
            ])
        ])

        EcoSystem ecoSystem = new EcoSystem(this, "gcloud-ces-operations-internal-packer", "jenkins-gcloud-ces-operations-internal")

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

            stage('Verify') {
                ecoSystem.verify("/dogu")
            }

            stage('Integration tests') {
                ecoSystem.runCypressIntegrationTests([
                    cypressImage     : "cypress/included:8.7.0",
                    enableVideo      : params.EnableVideoRecording,
                    enableScreenshots: params.EnableScreenshotRecording
                ])
            }

            if (params.TestDoguUpgrade != null && params.TestDoguUpgrade){
                stage('Upgrade dogu') {
                    // Remove new dogu that has been built and tested above
                    ecoSystem.purgeDogu(doguName)

                    if (params.OldDoguVersionForUpgradeTest != '' && !params.OldDoguVersionForUpgradeTest.contains('v')){
                        println "Installing user defined version of dogu: " + params.OldDoguVersionForUpgradeTest
                        ecoSystem.installDogu("official/" + doguName + " " + params.OldDoguVersionForUpgradeTest)
                    } else {
                        println "Installing latest released version of dogu..."
                        ecoSystem.installDogu("official/" + doguName)
                    }
                    ecoSystem.startDogu(doguName)
                    ecoSystem.waitForDogu(doguName)
                    ecoSystem.upgradeDogu(ecoSystem)

                    // Wait for upgraded dogu to get healthy
                    ecoSystem.waitForDogu(doguName)
                    // TODO: Replace this with "ecosystem.waitUntilAvailable(doguName)" from dogu-build-lib 1.5.0
                    // curl the dogu URL until the "Dogu is starting" page (status code 503) is gone
                    // and the CAS login page is returned (status code 302)
                    String externalIP = ecoSystem.externalIP
                    echo "Waiting for https://$externalIP/$doguName to be reachable..."
                    for (i=0; i < 30; i++) {
                        def response = sh(script: "curl --insecure --silent --head https://${externalIP}/${doguName} | head -n 1", returnStdout: true)
                        if (response.contains("302")){
                            break;
                        }
                        sleep 3
                    }
                }

                stage('Integration Tests - After Upgrade') {
                    // Run integration tests again to verify that the upgrade was successful
                    ecoSystem.runCypressIntegrationTests([
                        cypressImage     : "cypress/included:8.7.0",
                        enableVideo      : params.EnableVideoRecording,
                        enableScreenshots: params.EnableScreenshotRecording
                    ])
                }
            }

            if (gitflow.isReleaseBranch()) {
                String releaseVersion = git.getSimpleBranchName()

                stage('Finish Release') {
                    gitflow.finishRelease(releaseVersion)
                }

                stage('Push Dogu to registry') {
                    ecoSystem.push("/dogu")
                }

                stage ('Add Github-Release'){
                    github.createReleaseWithChangelog(releaseVersion, changelog)
                }
            }

        } finally {
            stage('Clean') {
                ecoSystem.destroy()
            }
        }
    }
}
