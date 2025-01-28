package scripts

import hudson.model.*
import jenkins.model.*
import hudson.util.VersionNumber
import hudson.PluginWrapper

def jenkins = Jenkins.instance
def pluginManager = jenkins.pluginManager
def updateCenter = jenkins.updateCenter

def getDoguctlWrapper() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    doguctlWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return doguctlWrapper
}

def getDefaultValues() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/DefaultValues.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    defaultWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return defaultWrapper
}

doguctl = getDoguctlWrapper()

defaultValues = getDefaultValues()

// Make sure CAS-Plugin version is at least 1.7.0 to work with Jenkins 2.479.3 and following
def MINIMAL_CAS_PLUGIN_VERSION = new VersionNumber("1.7.0")
// Make sure Matrix-Auth Plugin version is at least 3.0 to work with Jenkins 2.332.1 and following
def MINIMAL_MATRIX_AUTH_PLUGIN_VERSION = new VersionNumber("3.0")

boolean isVersionSufficient(PluginWrapper plugin, VersionNumber versionNumber) {
    return plugin.getVersionNumber().isNewerThanOrEqualTo(versionNumber)
}

try {
    pluginManager.doCheckUpdatesServer()
} catch (IOException ex) {
    println "Plugin update server unreachable"
    println ex
}

println "Checking CAS-Plugin version ..."
def currentCasPlugin = jenkins.getPluginManager().getPlugin('cas-plugin')
if (currentCasPlugin != null) {
    if (!isVersionSufficient(currentCasPlugin, MINIMAL_CAS_PLUGIN_VERSION)) {
        println "CAS-Plugin version " + currentCasPlugin.getVersion() + " is lower than " + MINIMAL_CAS_PLUGIN_VERSION + "; Upgrading plugin..."
        updateCenter.getPlugin('cas-plugin').deploy(true).get()
    }
}

println "Checking Matrix-Auth-Plugin version ..."
def currentMatrixAuthPlugin = jenkins.getPluginManager().getPlugin('matrix-auth')
if (currentMatrixAuthPlugin != null) {
    println "currentMatrixAuthPlugin version is: " +currentMatrixAuthPlugin.getVersion()
    if (!isVersionSufficient(currentMatrixAuthPlugin, MINIMAL_MATRIX_AUTH_PLUGIN_VERSION)) {
        println "Matrix-Auth-Plugin version " + currentMatrixAuthPlugin.getVersion() + " is lower than " + MINIMAL_MATRIX_AUTH_PLUGIN_VERSION + "; Upgrading plugin..."
        updateCenter.getPlugin('matrix-auth').deploy(true).get()
        println "restarting jenkins after plugin upgrade ..."
        jenkins.restart()
        // needed as jenkins performs the restart in 5 seconds. Otherwise the other scripts will get called before the restart
        sleep(5000) }
    else{
        println "Matrix-Auth-Plugin version is sufficent"
    }
}
else{
    println "matrix-auth plugin is not installed! Make sure this is intended."
}


// configuration
def plugins = getDefaultValues().getPlugins()

def additionalPluginPath = "additional.plugins"

if (doguctl.keyExists("dogu", additionalPluginPath)) {
    println("Install additional plugins")
    def additionalPluginList = doguctl.getDoguConfig(additionalPluginPath)
    def additionalPlugins = additionalPluginList.split(',')
    for (additionalPlugin in additionalPlugins) {
        println("Add Plugin " + additionalPlugin)
        plugins.add(additionalPlugin)
    }
} else {
    println("No additional plugins configured")
}

def availablePlugins = updateCenter.getAvailables()
println "available plugins: " + availablePlugins.size()
for (def shortName : plugins) {
    def plugin = updateCenter.getPlugin(shortName)
    if (availablePlugins.contains(plugin)) {
        println "install missing plugin " + shortName
        plugin.deploy(true).get()
    } else {
        println "plugin not available or already installed : " + shortName
    }
}

if (updateCenter.isRestartRequiredForCompletion()) {
    jenkins.restart()
}

if (currentCasPlugin != null) {
    if (!isVersionSufficient(currentCasPlugin, MINIMAL_CAS_PLUGIN_VERSION)) {
        throw new Exception("Installed cas-plugin version " + currentCasPlugin.getVersion() + " is too old. It needs to be at least " + MINIMAL_CAS_PLUGIN_VERSION)
    }
}