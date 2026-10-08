package scripts

import jenkins.model.*
import org.csanchez.jenkins.plugins.kubernetes.*
import org.csanchez.jenkins.plugins.kubernetes.model.KeyValueEnvVar
import groovy.json.JsonSlurper

def jenkins = Jenkins.instance

def getDoguctlWrapper() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    doguctlWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return doguctlWrapper
}

doguctl = getDoguctlWrapper()

def kubernetesCloud = jenkins.clouds.getByName("kubernetes")

if (kubernetesCloud == null) {
    kubernetesCloud = new KubernetesCloud("kubernetes")
    jenkins.clouds.add(kubernetesCloud)
}
if (doguctl.isMultinode() && doguctl.getDoguConfigWithDefaultFromDescriptor("enable_kubernetes_agents") == "true") {

    kubernetesCloud.setServerUrl("https://kubernetes.default.svc.cluster.local")
    def crt = new File("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt").text
    kubernetesCloud.setServerCertificate(crt)

    def agentNamespace = doguctl.getDoguConfigWithDefaultFromDescriptor("agent_kubernetes_namespace")
    kubernetesCloud.setNamespace(agentNamespace)

    def podLabelsJson = doguctl.getDoguConfigWithDefaultFromDescriptor("agent_kubernetes_pod_labels")
    def jsonSlurper = new JsonSlurper()
    def podLabelsMap = jsonSlurper.parseText(podLabelsJson)
    def podLabels = podLabelsMap.collect{entry -> new PodLabel(entry.key, entry.value)}
    kubernetesCloud.setPodLabels(podLabels)

    def restrictedSecurityContext = doguctl.getDoguConfigWithDefaultFromDescriptor("agent_kubernetes_restricted_pss_security_context")
    kubernetesCloud.setRestrictedPssSecurityContext(restrictedSecurityContext == "true")

    def agentImageRegistry = doguctl.getDoguConfig("agent_kubernetes_docker_registry")
    kubernetesCloud.setJnlpregistry(agentImageRegistry)

    // ------------------------------------------------------------
    // Proxy configuration for Kubernetes build pods
    // ------------------------------------------------------------
    def proxyTemplateName = "default-template"
    def proxyEnabled = doguctl.getGlobalConfig("proxy/enabled") == "true"

    def templates = new ArrayList<PodTemplate>(
        kubernetesCloud.getTemplates()
    )

    // Make the modification idempotent
    // Remove the template created by a previous execution of this init script.
    templates.removeAll {
        it.getName() == proxyTemplateName
    }

    if (proxyEnabled) {
        def proxyServer = doguctl.getGlobalConfig("proxy/server")
        def proxyPort = doguctl.getGlobalConfig("proxy/port")
        def noProxy = doguctl.getGlobalConfig("proxy/no_proxy")

        if (!proxyServer || !proxyPort) {
            throw new IllegalStateException(
                "CES proxy is enabled, but proxy/server or proxy/port is missing"
            )
        }

        def proxyUrl = "http://${proxyServer}:${proxyPort}"

        def proxyEnvVars = [
            new KeyValueEnvVar("HTTP_PROXY", proxyUrl),
            new KeyValueEnvVar("HTTPS_PROXY", proxyUrl)
        ]

        if (noProxy) {
            proxyEnvVars.add(
                new KeyValueEnvVar("NO_PROXY", noProxy)
            )
        }

        def proxyTemplate = new PodTemplate()
        proxyTemplate.setName(proxyTemplateName)
        proxyTemplate.setEnvVars(proxyEnvVars)

        templates.add(proxyTemplate)

        kubernetesCloud.setTemplates(templates)

        // Every pod template of this Kubernetes cloud inherits these values.
        kubernetesCloud.setDefaultsProviderTemplate(proxyTemplateName)
    } else {
        kubernetesCloud.setTemplates(templates)

        if (kubernetesCloud.getDefaultsProviderTemplate()
                == proxyTemplateName) {
            kubernetesCloud.setDefaultsProviderTemplate(null)
        }
    }
    // ------------------------------------------------------------

    def enableGarbageCollection = doguctl.getDoguConfigWithDefaultFromDescriptor("agent_kubernetes_enable_garbage_collection")
    if (enableGarbageCollection == "true") {
        def garbageCollection = new GarbageCollection()
        garbageCollection.setTimeout(300)
        kubernetesCloud.setGarbageCollection(garbageCollection)
    }

    def ecosystemNamespace = doguctl.getDoguConfigWithDefaultFromDescriptor("ecosystem_kubernetes_namespace")
    kubernetesCloud.setJenkinsUrl("http://jenkins.${ecosystemNamespace}.svc.cluster.local:8080/jenkins")
    kubernetesCloud.setWebSocket(true)

    jenkins.clouds.replace(kubernetesCloud)
} else {
    jenkins.clouds.remove(kubernetesCloud.getDescriptor())
}

jenkins.save()
