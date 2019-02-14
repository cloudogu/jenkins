import hudson.model.*;
import jenkins.model.*;
import groovy.json.JsonSlurper;

def jenkins = Jenkins.instance;
def pluginManager = jenkins.pluginManager;
def updateCenter = jenkins.updateCenter;

def keyExists(String key){
	String ip = new File("/etc/ces/node_master").getText("UTF-8").trim();
	URL url = new URL("http://${ip}:4001/v2/keys/${key}");
	try {
		def json = new JsonSlurper().parseText(url.text)
	} catch (FileNotFoundException) {
		return false
	}
	return true
}

String mostRecentVersion(List versions) {
    def sorted = versions.sort(false) { a, b ->
        List verA = a.tokenize('.')
        List verB = b.tokenize('.')
        def commonIndices = Math.min(verA.size(), verB.size())
        for (int i = 0; i < commonIndices; ++i) {
            def numA = verA[i].toInteger()
            def numB = verB[i].toInteger()
            if (numA != numB) {
                return numA <=> numB
            }
        }
        // If we got this far then all the common indices are identical, so whichever version is longer must be more recent
        verA.size() <=> verB.size()
    }
    sorted[-1]
}

// Make sure CAS-Plugin version is at least 1.4.3 to work with Jenkins 2.150.2 and following
def minimalCasPluginVersion = "1.4.3"
def currentCasPlugin = jenkins.getPluginManager().getPlugin('cas-plugin');
if (currentCasPlugin != null) {
    def currentCasPluginVersion = currentCasPlugin.getVersion();
    def pluginVersions = [minimalCasPluginVersion, currentCasPluginVersion]
    if (currentCasPluginVersion != minimalCasPluginVersion && mostRecentVersion(pluginVersions) == minimalCasPluginVersion) {
        println "CAS-Plugin version is lower than " + minimalCasPluginVersion + "; Upgrading plugin..."
        updateCenter.getPlugin('cas-plugin').deploy().get();
    }
}

// configuration
def plugins = [
  'mailer-plugin',
  'cas-plugin',
  'git',
  'mercurial',
  'subversion',
  'workflow-aggregator',
  'matrix-auth',
  'maven-plugin',
  'credentials-binding',
  'ssh-slaves'
];

// add sonar plugin to Jenkins if SonarQube is installed
if (keyExists("dogu/sonar/current")) {
  plugins.add('sonar');
}

// action
try {
	pluginManager.doCheckUpdatesServer();
} catch (IOException ex){
	println "Plugin update server unreachable"
	println ex
}

def availablePlugins = updateCenter.getAvailables();
println "available plugins: " + availablePlugins.size()
for (def shortName : plugins){
  def plugin = updateCenter.getPlugin(shortName);
  if (availablePlugins.contains(plugin)) {
      println "install missing plugin " + shortName;
      plugin.deploy(true).get();
  } else {
    println "plugin not available or already installed : " + shortName
  }
}

if (updateCenter.isRestartRequiredForCompletion()) {
  jenkins.restart();
}
