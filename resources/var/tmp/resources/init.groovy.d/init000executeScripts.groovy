import groovy.transform.Field
import groovy.io.FileType
import jenkins.model.*

/**
 * javadoc on the used classes can be found here:
 * https://javadoc.jenkins.io
 */

// Stop Jenkins in case an exception occurs in one of the scripts

// Mark critical scripts, for example: init010CRITinitScript.groovy
// If an error occurs in a critical script, Jenkins will not be started.
@Field String CRITICAL_SCRIPT_INDICATOR = "CRIT"


try {
	// Get all filenames from scripts directory
	def scripts = []
	def scriptsDirectory = new File(getClass().protectionDomain.codeSource.location.path).parent + "/scripts"
	new File(scriptsDirectory).eachFile (FileType.FILES) { script ->
		scripts << script
	}
	scripts.sort()

	// Execute scripts
	scripts.each {
		scriptPath = it
		println "Executing post-initialization script ${scriptPath}"
		evaluate(scriptPath)
	}

// Stop Jenkins in case an exception occurs
} catch (Exception exception){
	println "An exception occured during initialization"
	exception.printStackTrace()
	filename = scriptPath.name
	if (isCriticalScript(filename)){
		println "Critical init script ${filename} returned an error. Shutting down jenkins..."
		Jenkins.instance.doExit(null, null);
	}else{
		println "Non-critical init script ${filename} returned an error. Continuing jenkins startup..."
	} 
}

def isCriticalScript(String name){
	if (name.contains(CRITICAL_SCRIPT_INDICATOR)){
		return true
	}else{
		return false
	}
}
