package scripts

import jenkins.model.*
import hudson.model.*

def getDoguctlWrapper() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    doguctlWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return doguctlWrapper
}

doguctl = getDoguctlWrapper()

def getUpdateSites() {
    List<hudson.model.UpdateSite> updateSites = []

    def listResult = doguctl.sh("doguctl ls updateSiteUrl")
    if (listResult.contains("could not print values for key updateSiteUrl")) {
        println("No updateSite urls set, skip step...")
        return updateSites
    }

    def updateSiteKeys = listResult.split("\n")
    for(key in updateSiteKeys) {
        updateSiteValue = doguctl.getDoguConfig(key)
        updateSiteKey = key.replace("updateSiteUrl/", "")
        println "found update site: ${updateSiteKey} ${updateSiteValue}"
        updateSites.add(new hudson.model.UpdateSite(updateSiteKey, updateSiteValue))
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