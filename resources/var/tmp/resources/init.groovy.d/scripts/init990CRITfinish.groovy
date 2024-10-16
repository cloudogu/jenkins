package scripts
// finish the jenkins installation by setting state to ready
// the state health check will now mark jenkins as healthy

File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
ecoSystem = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()


ecoSystem.setDoguState('ready')
ecoSystem.setDoguConfig('configured', 'true')