package scripts
// Script to activate proxy settings

import jenkins.model.*
import groovy.transform.Field

File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
ecoSystem = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()

def instance = Jenkins.getInstance()
boolean isProxyEnabled = false
@Field boolean enableProxyInJenkins = false
@Field String proxyName, noProxyHost, proxyUser, proxyPassword
@Field int proxyPort

try {
    isProxyEnabled = "true".equals(ecoSystem.getGlobalConfig("proxy/enabled"))
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
        proxyName = ecoSystem.getGlobalConfig("proxy/server")
        proxyPort = Integer.parseInt(ecoSystem.getGlobalConfig("proxy/port"))
    } catch (FileNotFoundException e) {
        println("proxy configuration is incomplete (server or port not found).")
        disableProxy()
    }
}

def setProxyAuthenticationSettings() {
    // Authentication credentials are optional
    try {
        proxyPassword = ecoSystem.getGlobalConfig("proxy/password")
        proxyUser = ecoSystem.getGlobalConfig("proxy/username")
    } catch (FileNotFoundException e) {
        println("proxy authentication configuration is incomplete or not existent.")
    }
}

def setProxyExcludes() {
    noProxyHost = ecoSystem.getGlobalConfig("fqdn")
}

if (enableProxyInJenkins) {
    def proxyConfiguration = new hudson.ProxyConfiguration(proxyName, proxyPort, proxyUser, proxyPassword, noProxyHost)
    instance.proxy = proxyConfiguration
    instance.save()
}