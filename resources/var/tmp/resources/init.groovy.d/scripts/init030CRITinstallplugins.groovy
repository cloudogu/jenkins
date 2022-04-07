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

def getValueFromEtcd(String key){
    String ip = new File("/etc/ces/node_master").getText("UTF-8").trim();
    URL url = new URL("http://${ip}:4001/v2/keys/${key}");
    try {
        def json = new JsonSlurper().parseText(url.text)
        return json.node.value
    } catch (FileNotFoundException) {
        return false
    }
}

// Make sure CAS-Plugin version is at least 1.5.0 to work with Jenkins 2.277.3 and following
minimalCasPluginVersion = "1.5.0"
boolean isCasVersionSufficient(String version) {
    List minVer = minimalCasPluginVersion.tokenize('.')
    List testVer = version.tokenize('.')
    def commonIndices = Math.min(minVer.size(), testVer.size())
    for (int i = 0; i < commonIndices; ++i) {
        def numMin = minVer[i].toInteger()
        def numTest = testVer[i].toInteger()
        if (numMin != numTest) {
            return numMin < numTest
        }
    }
      // If we got this far then all the common indices are identical, so whichever version is longer must be more recent
      return testVer.size() >= minVer.size()
}

try {
	pluginManager.doCheckUpdatesServer();
} catch (IOException ex){
	println "Plugin update server unreachable"
	println ex
}

def currentCasPlugin = jenkins.getPluginManager().getPlugin('cas-plugin');
if (currentCasPlugin != null) {
    def currentCasPluginVersion = currentCasPlugin.getVersion();
    if (! isCasVersionSufficient(currentCasPluginVersion)) {
        println "CAS-Plugin version " + currentCasPluginVersion + " is lower than " + minimalCasPluginVersion + "; Upgrading plugin...";
        updateCenter.getPlugin('cas-plugin').deploy(true).get();
    }
}

// configuration
def plugins = [
  'mailer-plugin',
  'cas-plugin',
  'git',
  'mercurial',
  'subversion',
  'scm-manager',
  'workflow-aggregator',
  'matrix-auth',
  'maven-plugin',
  'credentials-binding',
  'ssh-slaves',
  'pipeline-github-lib',
  'authorize-project',
  'pipeline-stage-view'
];

def additionalPluginPath = "config/jenkins/additional.plugins";

if (keyExists(additionalPluginPath)){
    println("Install additional plugins");
    def additionalPluginList = getValueFromEtcd(additionalPluginPath);
    def additionalPlugins = additionalPluginList.split(',');
    for (additionalPlugin in additionalPlugins){
        println("Add Plugin "+ additionalPlugin)
        plugins.add(additionalPlugin)
    }
}else{
    println("No additional plugins configured");
}

// add sonar plugin to Jenkins if SonarQube is installed
if (keyExists("dogu/sonar/current")) {
  plugins.add('sonar');
}

// add Nexus platform plugin to Jenkins if IQ-server is installed
if (keyExists("dogu/iqserver/current")) {
  plugins.add('nexus-jenkins-plugin');
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

currentCasPluginVersion = jenkins.getPluginManager().getPlugin('cas-plugin').getVersion();
if (!isCasVersionSufficient(currentCasPluginVersion)) {
  throw new Exception("Installed cas-plugin version " + currentCasPluginVersion + " is too old. It needs to be at least " + minimalCasPluginVersion);
}
