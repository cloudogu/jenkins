package scripts
// finish the jenkins installation by setting state to ready
// the state health check will now mark jenkins as healthy

def getDoguctlWrapper() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    doguctlWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return doguctlWrapper
}

doguctl = getDoguctlWrapper()


doguctl.setDoguState('ready')
doguctl.setDoguConfig('configured', 'true')