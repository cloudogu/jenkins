import hudson.model.*;
import jenkins.model.*;
import groovy.json.JsonSlurper;

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

// configuration
def plugins = [
  'mailer-plugin',
  'cas-plugin',
  'git',
  'mercurial',
  'subversion',
  'workflow-aggregator',
  'simple-theme-plugin',
  'matrix-auth',
  'maven-plugin',
  'credentials-binding'
];

// add sonar plugin to Jenkins if SonarQube is installed
if (keyExists("dogu/sonar/current")) {
  plugins.add('sonar');
}

// action
def jenkins = Jenkins.instance;
def pluginManager = jenkins.pluginManager;
def updateCenter = jenkins.updateCenter;

pluginManager.doCheckUpdatesServer();

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
