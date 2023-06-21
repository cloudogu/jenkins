# Plugin-Kompatibilität testen

Im Rahmen Upgrades auf JAVA 17 für das Jenkins-Dogu wurden einige Probleme mit Jenkins-Plugins festgestellt.

Das Jenkins-Dogu hat potenziell eine Menge Plugins installiert. Diese Plugins müssen mit der aktuellen Jenkins-Version
und der zugrunde liegenden JAVA-Version (derzeit JAVA 11) kompatibel sein.

Wenn ein Upgrade von Jenkins oder JAVA ansteht, sollten die Plugins mit der neuen Version getestet werden.

## Testen mit der Jenkins-CLI
Die Jenkins-CLI hat die Möglichkeit, alle installierten Plugins aufzulisten und auch mit ihnen über ein Groovy-Skript zu interagieren.

Die folgenden Befehle wurden innerhalb des Jenkins-Dogu-Docker-Containers ausgeführt.
Es ist möglich, sie von einem anderen Host aus auszuführen, aber dann benötigt der CES ein gültiges SSL-Zertifikat. Ansonsten akzeptiert die Jenkins-CLI das Zertifikat nicht.

```shell
# Laden des jenkins-cli JAR
wget http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar

# Groovy-Skript ausführen
java -jar jenkins-cli.jar -auth admin:admin -s http://localhost:8080/jenkins/ groovy = < plugins.groovy
```
Das entsprechende Groovy-Skript sieht wie folgt aus:
```groovy
println "Running plugin enumerator"
println ""
def plugins = jenkins.model.Jenkins.instance.getPluginManager().getPlugins()
plugins.each {
    println "${it.getShortName()} - ${it.getVersion()} - ${it.getPlugin().getTarget().class.name}"
    println "  stopping ${it.getPlugin().class.name}..."
    it.getPlugin().stop()
    println "  ...stopped ${it.getPlugin().class.name}"

    println "  loading ${it.getPlugin().class.name}..."
    it.getPlugin().load()
    println "  ...loaded ${it.getPlugin().class.name}"

    println "  starting ${it.getPlugin().class.name}..."
    it.getPlugin().start()
    println "  ...started ${it.getPlugin().class.name}"
}
println ""
println "Total number of plugins: ${plugins.size()}"
```

> **Warnung:** Diese Methode zeigt nur einige grundlegende Informationen über die installierten Plugins an, erkennt aber keine Laufzeitprobleme.

## Testen mit PCT (Plugin Compatibility Tester)

Jenkins hat einen offiziellen Kompatibilitätstester: (https://github.com/jenkinsci/plugin-compat-tester)[https://github.com/jenkinsci/plugin-compat-tester]
Dieser Tester kann alle in einer Jenkins WAR-Datei enthaltenen Plugins testen. Für jedes Plugin (`*.hpi`-Datei) werden die folgenden Schritte ausgeführt:
* Extrahieren der `pom.xml` aus der `*.hpi`-Datei
* Auschecken des entsprechenden Quellcode-Repositorys
* patchen der Jenkins-Version in der `pom.xml`, um die Jenkins-Version aus der WAR-Datei zu verwenden
* Ausführen der Tests über Maven (`mvn test`)

> Alle Plugins (`*.hpi` Dateien) müssen sich im Verzeichnis `/WEB-INF/plugins` innerhalb der WAR-Datei befinden!

> Dieser Prozess kann sehr viel Zeit in Anspruch nehmen (abhängig von der Anzahl der Plugins)!
> Außerdem ist er fehleranfällig. Wenn ein Test für ein Plugin fehlschlägt, schlägt der gesamte Prozess fehl.

Den Tester starten:
```shell
java -jar pct.jar test-plugins --war "$(pwd)/jenkins-with-plugins.war" --working-dir "$(pwd)/pct-work"
```
> Es müssen absolute Pfade im Befehl verwendet werden!

Das `pct.jar` kann mit `mvn clean package` aus dem pct-Repository erstellt werden.

### Probleme mit PCT
Einige der Plugins sind ziemlich alt und möglicherweise veraltet. Der PCT hat  Probleme beim Testen dieser Plugins.

#### Unauthentifiziertes Git-Protokoll
Die `pom.xml` könnte einen Verweis auf das Quellcode-Repository enthalten, das ein nicht authentifiziertes Git-Protokoll auf Port 9418 wie z.B. `git://github.com/jenkinsci/async-http-client-plugin.git` verwendet.
Um dies zu beheben, muss die `pom.xml` innerhalb der `hpi`-Datei innerhalb der WAR-Datei bearbeitet werden. Das `git://`-Protokoll kann durch `https://` ersetzt werden.

#### Blockierte Maven-Spiegel
Maven blockiert Downloads von Mirrors, die `http` verwenden. Wenn ein Plugin `http`-Mirrors verwendet, ist ein Workaround, den Block in der maven `settings.xml` zu deaktivieren.
Kommentieren Sie den Mirror-Block aus, der wie folgt aussieht:
```xml
<!-- commented out to allow http-mirrors
<mirror>
    <id>maven-default-http-blocker</id>
    <mirrorOf>external:http:*</mirrorOf>
    <name>Pseudo repository to mirror external repositories initially using HTTP.</name>
    <url>http://0.0.0.0/</url>
    <blocked>true</blocked>
</mirror> 
-->
```

## Fazit

Zum Zeitpunkt des Schreibens (20.06.2023) waren beide Testmethoden nicht sehr nützlich / erfolgreich, da einige der Plugins, die derzeit verwendet werden, nicht getestet werden können.
Außerdem konnten beide Methoden bekannte Fehler nicht identifizieren, wenn ein Plugin (async-http-client) Reflection verwendet, um einige Klassen zur Laufzeit zu laden, die in JAVA 17 fehlen.
Diese Art von Problemen kann nur durch das Ausführen der spezifischen Code-Blöcke erkannt werden (entweder durch manuelles Testen oder durch gute Unit-Tests, die von dem Plugin bereitgestellt werden).

Wir haben keine geeignete Methode gefunden, um alle verwendeten Jenkins-Plugins in einem vertretbaren Zeitrahmen zu testen.    

Ein Upgrade auf JAVA 17 ist derzeit nicht ratsam. Auch die offizielle 
[Jenkins-Dokumentation](https://www.jenkins.io/doc/developer/tutorial/prepare/#download-and-install-a-jdk) empfiehlt die Verwendung von JAVA 11. 

## Weiteres Vorgehen
Um potenziell problematische Plugins zu identifizieren, wäre ein erster Schritt, die von Jenkins festgelegten minimalen Plugin-Anforderungen zu überprüfen (siehe [Blog-Post](https://www.jenkins.io/blog/2022/12/14/require-java-11/)).

Dieses Vorgehen könnte wie folgt aussehen:
* Extrahieren der `pom.xml` aus der `hpi.file`
* Überprüfung der Jenkins- und der Java-Version des Plugins

Alternativ kann auch die Jenkins-CLI verwendet werden, die zwar ebenfalls Plugin-Informationen enthält, der aber die JAVA-Version des Plugins fehlt. 

> Zusätzlich bietet die Jenkins-Admin-UI Informationen über den Deprecation-Status und die Abhängigkeiten von Plugins.