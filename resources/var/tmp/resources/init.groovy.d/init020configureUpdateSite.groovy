import jenkins.model.*;
import hudson.model.*
import groovy.json.JsonSlurper;

def getJsonFromEtcd(String key) {
  String ip = new File("/etc/ces/node_master").getText("UTF-8").trim();
  URL url = new URL("http://${ip}:4001/v2/keys/${key}");
  return new JsonSlurper().parseText(url.text);
}

def getUpdateSitesFromEtcd(String key) {
	try {
    def json = getJsonFromEtcd(key);
    return convertNodesToUpdateSites(json.node.nodes, json.node.key.length());
	} catch (FileNotFoundException ex) {
    println "could not find update site configuration in registry"
    return new ArrayList<hudson.model.UpdateSite>();
  }
}

def convertNodesToUpdateSites(Object nodes, int parentKeyOffset) {
	List<hudson.model.UpdateSite> updateSites = [];
	nodes.each{ node ->
		// trim the directory from the nodes key
		def name = node.key.substring(parentKeyOffset+1);
		def url = node.value;

		println "found update site: ${name} ${url}";
		updateSites.add(new hudson.model.UpdateSite(name, url));
	}
	return updateSites;
}

// Try block to stop Jenkins in case an exception occurs in the script
try {

def instance = Jenkins.getInstance();
List<hudson.model.UpdateSite> updateSites = getUpdateSitesFromEtcd("config/jenkins/updateSiteUrl");

// TODO: check if already done?
if(updateSites.size() > 0) {
	println "set signatureCheck=false";
	hudson.model.DownloadService.signatureCheck = false;
	updateCenter = instance.getUpdateCenter();
	def sites = updateCenter.getSites();
	sites.clear();
	println "add new update sites";
	for(hudson.model.UpdateSite site : updateSites) {
		sites.add(site);
	}
	println "save instance";
	instance.save();
}

// Stop Jenkins in case an exception occurs
} catch (Exception exception){
  println("An exception occured during initialization");
  exception.printStackTrace();
  println("Init script and Jenkins will be stopped now...");
  throw new Exception("initialization exception")
}
