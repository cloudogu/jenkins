package scripts

import java.util.logging.Level
import java.util.logging.Logger

File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
ecoSystem = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()

Map<String, Level> getConfiguredLogLevels() {
    try {
        Map<String, Level> loggerLevelMap = new HashMap<>()
        loggingKeys = ecoSystem.sh("doguctl ls logging").split("\n")
        for(key in loggingKeys) {
            logValue = ecoSystem.getDoguConfig(key)
            logLevel = getLogLevel(logValue)
            logKey = key.replace("logging/", "")
            loggerLevelMap.put(logKey, logLevel)
        }

        return loggerLevelMap
    } catch (FileNotFoundException ignored) {
        println "no valid logging configuration found"
    }
    return [:]
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