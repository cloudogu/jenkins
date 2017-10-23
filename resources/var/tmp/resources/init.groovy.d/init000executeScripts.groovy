import groovy.io.FileType

// Stop Jenkins in case an exception occurs in one of the scripts
try {

	// Get all filenames from scripts directory
	def scripts = []
	def scriptsDirectory = new File(getClass().protectionDomain.codeSource.location.path).parent + "/scripts"
	new File(scriptsDirectory).eachFileRecurse (FileType.FILES) { script ->
		scripts << script
	}

	// Execute scripts
	scripts.each {
		println "Executing post-initialization script ${it}"
		evaluate(new File("${it}"))
	}

// Stop Jenkins in case an exception occurs
} catch (Exception exception){
	println("An exception occured during initialization");
	exception.printStackTrace();
	println("Init script and Jenkins will be stopped now...");
	throw new Exception("initialization exception")
}
