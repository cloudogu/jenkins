#!groovy
@Library(['github.com/cloudogu/ces-build-lib@1.43.0', 'github.com/cloudogu/dogu-build-lib@a14afd9d', 'github.com/cloudogu/zalenium-build-lib@30923630ced3089ae0861bef25b60903429841aa'])
import com.cloudogu.ces.cesbuildlib.*
import com.cloudogu.ces.dogubuildlib.*
import com.cloudogu.ces.zaleniumbuildlib.*

node('docker'){
        stage('Checkout') {
            checkout scm
        }

        stage('Lint') {
            lintDockerfile()
            shellCheck("resources/startup.sh resources/upgrade-notification.sh")
        }
}

node('vagrant') {

    Git git = new Git(this, "cesmarvin")
    git.committerName = 'cesmarvin'
    git.committerEmail = 'cesmarvin@cloudogu.com'
    GitFlow gitflow = new GitFlow(this, git)
    GitHub github = new GitHub(this, git)
    Changelog changelog = new Changelog(this)

    timestamps{
        properties([
            // Keep only the last x builds to preserve space
            buildDiscarder(logRotator(numToKeepStr: '10')),
            // Don't run concurrent builds for a branch, because they use the same workspace directory
            disableConcurrentBuilds(),
            // Parameter to activate dogu upgrade test on demand
            parameters([
                booleanParam(defaultValue: false, description: 'Test dogu upgrade from latest release', name: 'TestDoguUpgradeFromLatestRelease')
            ])
        ])

        EcoSystem ecoSystem = new EcoSystem(this, "gcloud-ces-operations-internal-packer", "jenkins-gcloud-ces-operations-internal")

        try {

            stage('Provision') {
                ecoSystem.provision("/dogu");
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

            stage('Integration Tests') {

                String externalIP = ecoSystem.externalIP

                if (fileExists('integrationTests/it-results.xml')) {
                    sh 'rm -f integrationTests/it-results.xml'
                }

                timeout(time: 15, unit: 'MINUTES') {

                    try {

                        withZalenium { zaleniumIp ->

                            dir('integrationTests') {

                                docker.image('node:8.14.0-stretch').inside("-e WEBDRIVER=remote -e CES_FQDN=${externalIP} -e SELENIUM_BROWSER=chrome -e SELENIUM_REMOTE_URL=http://${zaleniumIp}:4444/wd/hub") {
                                    sh 'yarn install'
                                    sh 'yarn run ci-test'
                                }

                            }

                        }
                    } finally {
                        // archive test results
                        junit allowEmptyResults: true, testResults: 'integrationTests/it-results.xml'
                    }
                }
            }

            if (params.TestDoguUpgradeFromLatestRelease != null && params.TestDoguUpgradeFromLatestRelease){
                stage('Upgrade test') {
                    // Remove new dogu that has been built and tested above
                    ecoSystem.purge("jenkins")

                    // Install latest released version of dogu
                    ecoSystem.install("official/jenkins")

                    // Start dogu and wait until it is up
                    ecoSystem.start("jenkins")
                    ecoSystem.waitForDogu("jenkins")

                    // Upgrade dogu by building again
                    String currentDoguVersionString = sh(returnStdout: true, script: 'grep .Version dogu.json').trim()
                    print "current dogu version: ${currentDoguVersionString}"
                    String second = currentDoguVersionString.split('-')[1]
                    print "second half: ${second}"
                    String withoutcrap = second - "\","
                    int number = withoutcrap.toInteger()
                    int newnumber = number + 1
                    print "new numer: ${newnumber}"
                    String[] currentDoguVersionSplitted = currentDoguVersionString.split("\"")
                    print "splitted currentDoguVersionString"
                    String currentDoguVersion = currentDoguVersionSplitted[3]
                    print "current dogu version: ${currentDoguVersion}"
                    String newNumber = currentDoguVersion.split("-")[0] + "-" + newnumber
                    print "new number = ${newNumber}"
                    ecoSystem.setVersion(newNumber)
                    ecoSystem.vagrant.sync()
                    ecoSystem.build("/dogu")
                    ecoSystem.waitForDogu("jenkins")

                    // Run integration tests again to verify that the upgrade was successful
                    // see above
                }

            }

            if (gitflow.isReleaseBranch()) {
                String releaseVersion = git.getSimpleBranchName();

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
