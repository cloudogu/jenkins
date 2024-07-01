import hudson.model.*;
import jenkins.model.*;
import groovy.json.JsonSlurper;
import hudson.util.VersionNumber
import hudson.PluginWrapper

def jenkins = Jenkins.instance;
def pluginManager = jenkins.pluginManager;
def updateCenter = jenkins.updateCenter;

def keyExists(String key) {
    String ip = new File("/etc/ces/node_master").getText("UTF-8").trim();
    URL url = new URL("http://${ip}:4001/v2/keys/${key}");
    try {
        def json = new JsonSlurper().parseText(url.text)
    } catch (FileNotFoundException) {
        return false
    }
    return true
}

def getValueFromEtcd(String key) {
    String ip = new File("/etc/ces/node_master").getText("UTF-8").trim();
    URL url = new URL("http://${ip}:4001/v2/keys/${key}");
    try {
        def json = new JsonSlurper().parseText(url.text)
        return json.node.value
    } catch (FileNotFoundException) {
        return false
    }
}

boolean isVersionSufficient(PluginWrapper plugin, VersionNumber versionNumber) {
    return plugin.getVersionNumber().isNewerThanOrEqualTo(versionNumber)
}

try {
    pluginManager.doCheckUpdatesServer();
} catch (IOException ex) {
    println "Plugin update server unreachable"
    println ex
}

// configuration
def plugins = [
        'azure-vm-agents',
        'docker-workflow'
];

def additionalPluginPath = "config/jenkins/additional.plugins";

if (keyExists(additionalPluginPath)) {
    println("Install additional plugins");
    def additionalPluginList = getValueFromEtcd(additionalPluginPath);
    def additionalPlugins = additionalPluginList.split(',');
    for (additionalPlugin in additionalPlugins) {
        println("Add Plugin " + additionalPlugin)
        plugins.add(additionalPlugin)
    }
} else {
    println("No additional plugins configured");
}

def availablePlugins = updateCenter.getAvailables();
println "available plugins: " + availablePlugins.size()
for (def shortName : plugins) {
    def plugin = updateCenter.getPlugin(shortName);
    if (availablePlugins.contains(plugin)) {
        println "Installing missing plugin " + shortName;
        plugin.deploy(true).get();
    } else {
        println "Plugin not available or already installed : " + shortName
    }
}

if (updateCenter.isRestartRequiredForCompletion()) {
    jenkins.restart();
}
