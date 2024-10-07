import jenkins.model.*;
import hudson.model.*
import groovy.json.JsonSlurper;

File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/EcoSystem.groovy")
Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
ecoSystem = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()

def getUpdateSites() {
    def key = "updateSiteUrl"
	try {
        def json = new JsonSlurper().parse(ecoSystem.getDoguConfig(key))
		return convertNodesToUpdateSites(json.node.nodes, json.node.key.length());
	} catch (FileNotFoundException ex) {
		println "could not find update site configuration in registry"
		return new ArrayList<hudson.model.UpdateSite>()
	}
}

def convertNodesToUpdateSites(Object nodes, int parentKeyOffset) {
	List<hudson.model.UpdateSite> updateSites = []
	nodes.each{ node ->
		// trim the directory from the nodes key
		def name = node.key.substring(parentKeyOffset+1)
		def url = node.value

		println "found update site: ${name} ${url}"
		updateSites.add(new hudson.model.UpdateSite(name, url))
	}
	return updateSites
}

def instance = Jenkins.getInstance();
List<hudson.model.UpdateSite> updateSites = getUpdateSites()

if(updateSites.size() > 0) {
	println "set signatureCheck=false"
	hudson.model.DownloadService.signatureCheck = false
	updateCenter = instance.getUpdateCenter()
	def sites = updateCenter.getSites()
	sites.clear()
	println "add new update sites"
	for(hudson.model.UpdateSite site : updateSites) {
		sites.add(site);
	}
	println "save instance"
	instance.save()
}
