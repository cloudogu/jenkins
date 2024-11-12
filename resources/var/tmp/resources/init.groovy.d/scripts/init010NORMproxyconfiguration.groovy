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

def instance = Jenkins.getInstance()
boolean isProxyEnabled = false
@Field boolean enableProxyInJenkins = false
@Field String proxyName, noProxyHost, proxyUser, proxyPassword
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
    noProxyHost = doguctl.getGlobalConfig("fqdn")
}

if (enableProxyInJenkins) {
    def proxyConfiguration = new hudson.ProxyConfiguration(proxyName, proxyPort, proxyUser, proxyPassword, noProxyHost)
    instance.proxy = proxyConfiguration
    instance.save()
}