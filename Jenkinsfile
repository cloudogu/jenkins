#!groovy
@Library(['github.com/cloudogu/ces-build-lib@1.56.0', 'github.com/cloudogu/dogu-build-lib@4d77e0241256b96f3fd403e24baaa48a09aec6a5'])
import com.cloudogu.ces.cesbuildlib.*
import com.cloudogu.ces.dogubuildlib.*
import groovy.json.JsonSlurper

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

        stage('Check Markdown Links') {
            Markdown markdown = new Markdown(this)
            markdown.check()
        }

        stage('Shell tests') {
            executeShellTests()
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

            stage('Test global admin group change') {
                ecoSystem.prepareGlobalAdminGroupChangeTest(doguName)
                echo "Running integrationtests with new CES global admin group..."
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
                    echo "Waiting for $doguName to be reachable..."
                    ecoSystem.waitUntilAvailable(doguName, 90)
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

def executeShellTests() {
    def bats_base_image = "bats/bats"
    def bats_custom_image = "cloudogu/bats"
    def bats_tag = "1.2.1"

    def batsImage = docker.build("${bats_custom_image}:${bats_tag}", "--build-arg=BATS_BASE_IMAGE=${bats_base_image} --build-arg=BATS_TAG=${bats_tag} ./build/make/bats")
    try {
        sh "mkdir -p target"

        batsContainer = batsImage.inside("--entrypoint='' -v ${WORKSPACE}:/workspace") {
            sh "make unit-test-shell-ci"
        }
    } finally {
        junit allowEmptyResults: true, testResults: 'target/shell_test_reports/*.xml'
    }
}
