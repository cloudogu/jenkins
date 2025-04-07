package scripts

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

import java.util.logging.Level
import java.util.logging.Logger

// Helper for evaluating valid json strings
String.metaClass.isJson << { ->
    def normalize = { it.replaceAll("\\s", "").replaceAll("\'", "\"") }

    try {
        normalize(delegate) == normalize(JsonOutput.toJson(new JsonSlurper().parseText(normalize(delegate) as String)))
    } catch (e) {
        false
    }
}

def getDoguctlWrapper() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    doguctlWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return doguctlWrapper
}

doguctl = getDoguctlWrapper()

Map<String, Level> getConfiguredLogLevels() {

    Map<String, Level> loggerLevelMap = new HashMap<>()

    def listResult = doguctl.sh("doguctl ls logging")
    if (listResult.contains("could not print values for key logging")) {
        println("No loggers are set, skip step...")
        return loggerLevelMap
    }

    loggingKeys = listResult.split("\n")
    for (key in loggingKeys) {
        logValue = doguctl.getDoguConfig(key)
        logLevel = getLogLevel(logValue)
        logKey = key.replace("logging/", "")
        loggerLevelMap.put(logKey, logLevel)
    }

    // get additional loggers
    String additionalLoggerJSON = doguctl.getDoguConfig("logging/additional_loggers")
    if (additionalLoggerJSON != null && "DEFAULT_VALUE" != additionalLoggerJSON && additionalLoggerJSON.isJson()) {
        // sanitize poissible quoting issues
        additionalLoggerJSON = additionalLoggerJSON.replaceAll("\'", "\"")

        // iterate over json entries and add them to the logger list
        Map parsedResponse = new JsonSlurper().parseText(additionalLoggerJSON) as Map
        for (entry in parsedResponse.entrySet()) {
            loggerLevelMap.put(entry.getKey().toString(), getLogLevel(entry.value.toString()))
        }
    }

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