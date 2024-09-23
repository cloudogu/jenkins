package scripts

import jenkins.model.*
import groovy.json.JsonSlurper

def blocklistPath = "/var/lib/jenkins/init.groovy.d/plugin-blocklist.json"
def blocklistFile = new File(blocklistPath)

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

blocklist.plugins.each { it ->
    def pluginId = it.trim()
    if (pluginId) {
        def plugin = Jenkins.instance.pluginManager.getPlugin(pluginId)

        if (plugin != null) {
            println("removing plugin: ${pluginId}. The ${pluginId} plugin is blocked and may be incompatible with other plugins")
            plugin.doDoUninstall()
        }
    }
}