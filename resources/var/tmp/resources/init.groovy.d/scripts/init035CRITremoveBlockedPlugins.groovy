package scripts

import jenkins.model.*
import hudson.security.*
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import org.jenkinsci.plugins.matrixauth.*

final String CONFIGURED_KEY = 'configured'
final String ADMIN_GROUP_LAST_KEY = 'admin_group_last'

def blockedPluginKey = "blocked.plugins"
def blocklistPath = "/var/lib/jenkins/init.groovy.d/plugin-blocklist.json"
def blocklistFile = new File(blocklistPath)

def getDoguctlWrapper() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    doguctlWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return doguctlWrapper
}

doguctl = getDoguctlWrapper()


if (doguctl.keyExists("dogu", blockedPluginKey)) {
    def blockListPlugins = doguctl.getDoguConfig(blockedPluginKey)
    def blockedJsonString = [plugins: "$blockListPlugins".split(",")]
    def blockedJson = JsonOutput.toJson(blockedJsonString)
    def blockedJsonObject = JsonOutput.prettyPrint(blockedJson)
    blocklistFile.write(blockedJsonObject)
    println("Overwritten standard blocklist with custom plugin blocklist")
}

def jenkins = Jenkins.instance

if (!blocklistFile.exists()) {
    println("Block-list not found: ${blocklistPath}")
    return
}

def jsonSlurper = new JsonSlurper()
def blocklist = jsonSlurper.parse(blocklistFile)

if (blocklist.plugins == null || blocklist.plugins.isEmpty()) {
    println("Block-list contains no plugins")
    return
}

def restartJenkins = false

blocklist.plugins.each { it ->
    def pluginId = it.toString().trim()
    if (pluginId) {
        def plugin = jenkins.pluginManager.getPlugin(pluginId)

        if (plugin != null) {
            println("removing plugin: ${pluginId}. The ${pluginId} plugin is blocked and may be incompatible with other plugins")
            plugin.disable()
            plugin.doDoUninstall()
            restartJenkins = true

            if(pluginId == "role-strategy") {
                AuthorizationStrategy authStrategy = new ProjectMatrixAuthorizationStrategy()
                Jenkins.get().setAuthorizationStrategy(authStrategy)
                doguctl.setDoguConfig(CONFIGURED_KEY, "")
                doguctl.setDoguConfig(ADMIN_GROUP_LAST_KEY, "")
            }
        }
    }
}

if (restartJenkins) {
    jenkins.doSafeRestart(null, "Restarting because blocked plugins have been removed.")
}