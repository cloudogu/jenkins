package scripts
// Script to activate proxy settings

import jenkins.model.*
import groovy.transform.Field

def getDoguctlWrapper() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    doguctlWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return doguctlWrapper
}

doguctl = getDoguctlWrapper()

@Field def instance = Jenkins.getInstance()
boolean isProxyEnabled = false
@Field boolean enableProxyInJenkins = false
@Field String proxyName, noProxyHosts, proxyUser, proxyPassword
@Field int proxyPort

try {
    isProxyEnabled = "true".equals(doguctl.getGlobalConfig("proxy/enabled"))
} catch (FileNotFoundException e) {
    println("proxy configuration does not exist.")
}

if (isProxyEnabled) {
    enableProxy()
    setProxyServerSettings()
    setProxyAuthenticationSettings()
    setProxyExcludes()
}

def enableProxy() {
    enableProxyInJenkins = true
}

def disableProxy() {
    enableProxyInJenkins = false
}

def setProxyServerSettings() {
    try {
        proxyName = doguctl.getGlobalConfig("proxy/server")
        proxyPort = Integer.parseInt(doguctl.getGlobalConfig("proxy/port"))
    } catch (FileNotFoundException e) {
        println("proxy configuration is incomplete (server or port not found).")
        disableProxy()
    }
}

def setProxyAuthenticationSettings() {
    // Authentication credentials are optional
    try {
        proxyPassword = doguctl.getGlobalConfig("proxy/password")
        proxyUser = doguctl.getGlobalConfig("proxy/username")
    } catch (FileNotFoundException e) {
        println("proxy authentication configuration is incomplete or not existent.")
    }
}

def setProxyExcludes() {
    def excludes = getDoguConfiguredExcludes()

    // FQDN should always be excluded
    excludes.add(doguctl.getGlobalConfig("fqdn"))

    boolean excludesExistsInGlobalConfig = doguctl.keyExists("global", "proxy/no_proxy_hosts")

    if (!excludesExistsInGlobalConfig) {
        println("proxy exclude configuration not existent in global config.")
        noProxyHosts = excludes.unique().join(" ")
        return
    }

    def actualGlobalConfigExcludes = new ArrayList<String>(Arrays.asList(doguctl.getGlobalConfig("proxy/no_proxy_hosts").split(",")))
    excludes.addAll(actualGlobalConfigExcludes)
    noProxyHosts = excludes.unique().join(" ")

    println("set no proxy hosts")
    println(noProxyHosts)
}

// getActualConfiguredExcludes returns the current configured no proxy hosts as list from the dogu or an empty list if no proxy is configured.
def getDoguConfiguredExcludes() {
    isProxyInstanceSet = instance.proxy

    if (isProxyInstanceSet) {
        return new ArrayList<String>(Arrays.asList(instance.proxy.noProxyHost.replaceAll(",|;", " ").split("\\n|\\s+")))
    }

    return new ArrayList<String>()
}

// TODO Discuss. Disabling proxy will not revert proxy changes a user manually changed the proxy via dogu ui. Cached change will be used.
if (enableProxyInJenkins) {
    def proxyConfiguration = new hudson.ProxyConfiguration(proxyName, proxyPort, proxyUser, proxyPassword, noProxyHosts)
    instance.proxy = proxyConfiguration
    instance.save()
}