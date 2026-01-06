package scripts

import jenkins.model.*
import org.csanchez.jenkins.plugins.kubernetes.*

def jenkins = Jenkins.instance

def getDoguctlWrapper() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    doguctlWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return doguctlWrapper
}

doguctl = getDoguctlWrapper()

if (doguctl.isMultinode()) {
    def kubernetesCloud = new KubernetesCloud(
            "kubernetes"
    )
    // TODO configure kubernetes cloud correctly
    kubernetesCloud.setNamespace("jenkins-build")
    kubernetesCloud.setServerUrl("https://kubernetes.default")

    jenkins.clouds.add(kubernetesCloud)
    jenkins.save()
}
