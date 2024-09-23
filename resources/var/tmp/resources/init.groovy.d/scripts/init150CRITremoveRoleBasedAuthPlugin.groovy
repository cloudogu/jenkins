package scripts

import jenkins.model.*

def pluginName = 'role-strategy'
def plugin = Jenkins.instance.pluginManager.getPlugin(pluginName)

if (plugin != null) {
    println("removing plugin: " + pluginName)
    plugin.doDoUninstall()
}
