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

doguctl = getDoguctlWrapper()

def getDefaultValues() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/DefaultValues.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    defaultValues = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return defaultValues
}

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
def plugins = defaultValues.getPlugins()

if (doguctl.isMultinode()) {
    def mnPlugins = defaultValues.getMnPlugins()
    plugins = plugins + mnPlugins
}

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

// Minimal versions of default plugins which fix known vulnerabilities.
// Installed plugins are not updated automatically, so existing instances would keep vulnerable versions otherwise.
def MINIMAL_PLUGIN_VERSIONS = [
        'subversion'     : '1349.v15e470a_46048',
        'workflow-cps'   : '4403.4405.va_b_cdb_b_d0b_89f',
        'script-security': '1436.1438.v3dc5ef7d16f2',
]

println "Checking minimal plugin versions ..."
MINIMAL_PLUGIN_VERSIONS.each { shortName, minimalVersion ->
    def installedPlugin = pluginManager.getPlugin(shortName)
    if (installedPlugin == null) {
        println "plugin " + shortName + " is not installed; skipping version check"
        return
    }
    if (isVersionSufficient(installedPlugin, new VersionNumber(minimalVersion))) {
        println "plugin " + shortName + " version " + installedPlugin.getVersion() + " is sufficient"
        return
    }
    def availablePlugin = updateCenter.getPlugin(shortName)
    if (availablePlugin == null) {
        println "plugin " + shortName + " version " + installedPlugin.getVersion() + " is lower than " + minimalVersion + " but no update is available"
        return
    }
    println "plugin " + shortName + " version " + installedPlugin.getVersion() + " is lower than " + minimalVersion + "; upgrading to " + availablePlugin.version + " ..."
    availablePlugin.deploy(true).get()
}

if (updateCenter.isRestartRequiredForCompletion()) {
    jenkins.restart()
}

if (currentCasPlugin != null) {
    if (!isVersionSufficient(currentCasPlugin, MINIMAL_CAS_PLUGIN_VERSION)) {
        throw new Exception("Installed cas-plugin version " + currentCasPlugin.getVersion() + " is too old. It needs to be at least " + MINIMAL_CAS_PLUGIN_VERSION)
    }
}