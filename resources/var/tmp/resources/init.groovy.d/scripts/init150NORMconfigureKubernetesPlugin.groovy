package scripts

import jenkins.model.*
import org.csanchez.jenkins.plugins.kubernetes.*
import groovy.json.JsonSlurper;

def jenkins = Jenkins.instance

def getDoguctlWrapper() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    doguctlWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return doguctlWrapper
}

doguctl = getDoguctlWrapper()

def kubernetesCloud = new KubernetesCloud("kubernetes")
if (doguctl.isMultinode() && doguctl.getDoguConfig("enable_kubernetes_agents") == "true") {

    kubernetesCloud.setServerUrl("https://kubernetes.default.svc.cluster.local")
    def crt = new File("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt").text
    kubernetesCloud.setServerCertificate(crt)

    def agentNamespace = doguctl.getDoguConfig("agent_kubernetes_namespace")
    kubernetesCloud.setNamespace(agentNamespace)

    def podLabelsJson = doguctl.getDoguConfig("agent_kubernetes_pod_labels")
    def jsonSlurper = new JsonSlurper()
    def podLabelsMap = jsonSlurper.parseText(podLabelsJson)
    def podLabels = podLabelsMap.collectEntries{key, value -> [new PodLabel(key, value)]}
    kubernetesCloud.setPodLabels(podLabels)

    def restrictedSecurityContext = doguctl.getDoguConfig("agent_kubernetes_restricted_pss_security_context")
    kubernetesCloud.setRestrictedPssSecurityContext(restrictedSecurityContext == "true")

    def agentImageRegistry = doguctl.getDoguConfig("agent_kubernetes_docker_registry")
    kubernetesCloud.setJnlpregistry(agentImageRegistry)

    def enableGarbageCollection = doguctl.getDoguConfig("agent_kubernetes_enable_garbage_collection")
    if (enableGarbageCollection == "true") {
        def garbageCollection = new GarbageCollection()
        garbageCollection.setTimeout(300)
        kubernetesCloud.setGarbageCollection(garbageCollection)
    }

    def ecosystemNamespace = doguctl.getDoguConfig("ecosystem_kubernetes_namespace")
    kubernetesCloud.setJenkinsUrl("http://jenkins.${ecosystemNamespace}.svc.cluster.local:8080/jenkins")
    kubernetesCloud.setWebSocket(true)

    jenkins.clouds.replace(kubernetesCloud)
} else {
    jenkins.clouds.remove(kubernetesCloud.getDescriptor())
}

jenkins.save()
