import groovy.json.JsonSlurper;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

static Map<String, String> getValuesFromEtcd(String key) {
    String ip = new File("/etc/ces/node_master").getText("UTF-8").trim();
    URL url = new URL("http://${ip}:4001/v2/keys/${key}");
    try {
        def json = new JsonSlurper().parseText(url.text);
        if (json.node.nodes == null) {
            println "no valid logging configuration found"
            return [:]
        }
        def logLevels = json.node.nodes.stream()
                .filter({ node -> !node.key.isEmpty() && !parseLoggerName(node.key).isEmpty() && !node.value.isEmpty() })
                .collect(Collectors.toMap({ node -> parseLoggerName(node.key) }, { node -> getLogLevel(node.value) }));
        return logLevels;
    } catch (FileNotFoundException) {
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

final String LOGGING_DIRECTORY_KEY = "config/jenkins/logging";

Map<String, Level> configuredLogLevels = getValuesFromEtcd(LOGGING_DIRECTORY_KEY);
configuredLogLevels.forEach { logger, level -> println "set log level '${level}' for logger '${logger}'"; setLogLevel(logger, level); }
