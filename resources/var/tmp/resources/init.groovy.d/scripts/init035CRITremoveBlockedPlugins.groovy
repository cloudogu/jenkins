package scripts

import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.matrixauth.*

final String CONFIGURED_KEY = 'configured'
final String ADMIN_GROUP_LAST_KEY = 'admin_group_last'

def blockedPluginKey = "blocked.plugins"

def getDoguctlWrapper() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    doguctlWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return doguctlWrapper
}

doguctl = getDoguctlWrapper()

def blockedPluginsString = doguctl.getDoguConfig(blockedPluginKey)
def blockedPluginList = blockedPluginsString ? blockedPluginsString.split(",") : []

def jenkins = Jenkins.instance

if (blockedPluginList.size() == 0) {
    println("Block-list contains no plugins")
    return
}

def restartJenkins = false

blockedPluginList.each { it ->
    def pluginId = it.toString().trim()
    if (pluginId) {
        def plugin = jenkins.pluginManager.getPlugin(pluginId)

        println("check plugin: ${pluginId}")

        if (plugin != null) {
            println("removing plugin: ${pluginId}. The ${pluginId} plugin is blocked and may be incompatible with other plugins")
            plugin.disable()
            plugin.doDoUninstall()
            restartJenkins = true

            if(pluginId == "role-strategy") {
                AuthorizationStrategy authStrategy = new ProjectMatrixAuthorizationStrategy()
                Jenkins.get().setAuthorizationStrategy(authStrategy)
                doguctl.removeDoguConfig(CONFIGURED_KEY)
                doguctl.removeDoguConfig(ADMIN_GROUP_LAST_KEY)
            }
        }
    }
}

if (restartJenkins) {
    jenkins.doSafeRestart(null, "Restarting because blocked plugins have been removed.")
}