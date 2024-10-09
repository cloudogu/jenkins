import groovy.json.JsonSlurper
import jenkins.model.*
import hudson.model.*

File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/EcoSystem.groovy")
Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
ecoSystem = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()

def getUpdateSites() {
    def configKey = "updateSiteUrl"
    def updateSites = ecoSystem.getDoguConfig(configKey)
    if (updateSites.length() > 0) {
        def sitePairs = new JsonSlurper().parseText(updateSites)
        return convertJsonToUpdateSites(sitePairs)
    } else {
        println "could not find update site configuration in registry"
        return new ArrayList<hudson.model.UpdateSite>()
    }
}

def convertJsonToUpdateSites(Map<String, String> jsonSites) {
    List<hudson.model.UpdateSite> updateSites = []
    jsonSites.each { key, value ->
        println "found update site: ${key} ${value}"
        updateSites.add(new hudson.model.UpdateSite(key, value))
    }

    return updateSites
}

def instance = Jenkins.getInstance()
List<hudson.model.UpdateSite> updateSites = getUpdateSites()

if (updateSites.size() > 0) {
    println "set signatureCheck=false"
    hudson.model.DownloadService.signatureCheck = false
    updateCenter = instance.getUpdateCenter()
    def sites = updateCenter.getSites()
    sites.clear()
    println "add new update sites"
    for (hudson.model.UpdateSite site : updateSites) {
        sites.add(site)
    }
    println "save instance"
    instance.save()
}
