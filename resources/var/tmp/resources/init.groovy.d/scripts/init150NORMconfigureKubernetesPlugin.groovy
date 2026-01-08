package scripts

import jenkins.model.*
import org.csanchez.jenkins.plugins.kubernetes.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*

def jenkins = Jenkins.instance

def getDoguctlWrapper() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    doguctlWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return doguctlWrapper
}

doguctl = getDoguctlWrapper()

if (doguctl.isMultinode()) {

    def token = new File("/var/run/secrets/kubernetes.io/serviceaccount")

    def creds = new StringCredentialsImpl(
            CredentialScope.GLOBAL,
            "k8s-sa-token",
            "Kubernetes SA Token",
            Secret.fromString(token)
    )

    SystemCredentialsProvider.getInstance().getStore().addCredentials(Domain.global(), creds)

    def kubernetesCloud = new KubernetesCloud(
            "kubernetes"
    )

    kubernetesCloud.setNamespace("jenkins-build")
    kubernetesCloud.setServerUrl("kubernetes.default.svc.cluster.local")
    kubernetesCloud.setCredentialsId("k8s-sa-token")

    jenkins.clouds.add(kubernetesCloud)
    jenkins.save()
}
