import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/EcoSystem.groovy")
Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
ecoSystem = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()

static Map<String, Level> getConfiguredLogLevels() {
    try {
        def json = ecoSystem.getDoguConfig("logging")
        if (json.node.nodes == null) {
            println "no valid logging configuration found"
            return [:]
        }
        def logLevels = json.node.nodes.stream()
                .filter({ node -> !node.key.isEmpty() && !parseLoggerName(node.key).isEmpty() && !node.value.isEmpty() })
                .collect(Collectors.toMap({ node -> parseLoggerName(node.key) }, { node -> getLogLevel(node.value) }));
        return logLevels;
    } catch (FileNotFoundException ignored) {
        println "no valid logging configuration found"
    }
    return [:]
}

static String parseLoggerName(String registryPath) {
    String[] registryPathParts = registryPath.split("/");
    if (registryPathParts.length > 1) {
        return registryPathParts[registryPathParts.length - 1]
    }
    return "" // return empty string to indicate that this entry can be ignored
}

static Level getLogLevel(String logLevel) {
    switch (logLevel.toUpperCase()) {
        case "ERROR":
            return Level.SEVERE;
        case "INFO":
            return Level.INFO;
        case "DEBUG":
            return Level.FINE;
        default:
            return Level.WARNING;
    }
}

static def setLogLevel(String logger, Level level){
    String loggerName = logger == "root" ? "" : logger;
    Logger.getLogger(loggerName).setLevel(level);
}

Map<String, Level> configuredLogLevels = getConfiguredLogLevels();
configuredLogLevels.forEach { logger, level -> println "set log level '${level}' for logger '${logger}'"; setLogLevel(logger, level); }
