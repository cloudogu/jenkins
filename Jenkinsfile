#!groovy
@Library(['github.com/cloudogu/ces-build-lib@76fcbaf', 'github.com/cloudogu/dogu-build-lib@f8cca7c9b101ed0bcdde8df556c13711d4cfd5a5', 'github.com/cloudogu/zalenium-build-lib@30923630ced3089ae0861bef25b60903429841aa'])
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

    timestamps{
        properties([
                // Keep only the last x builds to preserve space
                buildDiscarder(logRotator(numToKeepStr: '10')),
                // Don't run concurrent builds for a branch, because they use the same workspace directory
                disableConcurrentBuilds()
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

        } finally {
            stage('Clean') {
                ecoSystem.destroy()
            }
        }
    }
}
