package scripts

import java.util.logging.Level
import java.util.logging.Logger
import groovy.json.JsonSlurper

def getDoguctlWrapper() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    doguctlWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return doguctlWrapper
}

doguctl = getDoguctlWrapper()

Map<String, Level> getConfiguredLogLevels() {

    Map<String, Level> loggerLevelMap = new HashMap<>()

    def jsonTextResult = doguctl.sh("doguctl config logging/logger")
    def jsonSlurper = new JsonSlurper()
    def listResult = jsonSlurper.parseText(jsonTextResult)
    assert listResult instanceof List
    if (listResult.size == 0) {
        println("No loggers are set, skip step...")
        return loggerLevelMap
    }

    for (entry in listResult) {
        logLevel = getLogLevel(entry.loglevel)
        loggerLevelMap.put(entry.logger, logLevel)
    }

    def resultRoot = doguctl.sh("doguctl config logging/root")
    logLevel = getLogLevel(resultRoot)
    loggerLevelMap.put("root", logLevel)

    return loggerLevelMap
}

Level getLogLevel(String logLevel) {
    switch (logLevel.toUpperCase()) {
        case "ERROR":
            return Level.SEVERE
        case "INFO":
            return Level.INFO
        case "DEBUG":
            return Level.FINE
        default:
            return Level.WARNING
    }
}

def setLogLevel(String logger, Level level) {
    String loggerName = logger == "root" ? "" : logger
    Logger.getLogger(loggerName).setLevel(level)
}

Map<String, Level> configuredLogLevels = getConfiguredLogLevels()
configuredLogLevels.forEach { logger, level -> println "set log level '${level}' for logger '${logger}'"; setLogLevel(logger, level) }