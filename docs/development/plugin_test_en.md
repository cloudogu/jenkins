# Test Plugin compatibility

While investigating an upgrade to JAVA 17 for the Jenkins-Dogu, some problems with Jenkins-plugins have been noticed.

The Jenkins-Dogu potentially has a lot of plugins installed. These plugins need to compatible with the current Jenkins-Version
and the underlying JAVA-Version (currently JAVA 11).

When Jenkins- or JAVA needs to be upgrade the plugins should be tested with the new version. 

## Testing using the Jenkins-CLI
The Jenkins-CLI has the ability to list all installed Plugins and also interact with them using a groovy-script.

The following commands were executed inside the Jenkins-Dogu-Docker-Container. 
It is possible to run them from another host, but then the CES needs a proper SSL-certificate. Otherwise, the Jenkins-CLI does not accept the certificate. 

```shell
# get the jenkins-cli JAR
wget http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar

# execute a groovy-script
java -jar jenkins-cli.jar -auth admin:admin -s http://localhost:8080/jenkins/ groovy = < plugins.groovy
```
The corresponding groovy-script looks like this:
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

> **Warning:** This method only shows some basic information about the installed plugins, but does not detect runtime-issues.

## Testing using PCT (Plugin Compatibility Tester)

Jenkins has an official compatibility tester: (https://github.com/jenkinsci/plugin-compat-tester)[https://github.com/jenkinsci/plugin-compat-tester]
This tester can test all plugins contained in a Jenkins WAR-file. For every plugin (`*.hpi` file) the following steps are executed:
* extract the `pom.xml` from the `*.hpi`-file
* checkout the corresponding source-code-repository
* patch the Jenkins-Version in the pom.xml to use the Jenkins-Version from the WAR-file
* run the tests via maven (`mvn test`)

> All plugins (`*.hpi` files) must be located inside the `/WEB-INF/plugins` directory inside the WAR-file!

> This process can take a lot of time (depending on the amount of plugins)!
> Also it is prone to errors. When a test for one plugin fails, the whole process fails. 

To start the tester execute:
```shell
java -jar pct.jar test-plugins --war "$(pwd)/jenkins-with-plugins.war" --working-dir "$(pwd)/pct-work"
```
> Use absolute paths in the command! 

The `pct.jar` can be created using `mvn clean package` from the pct-repository.

### Problems using PCT
Some of the plugins are quite old and maybe deprecated. The PCT has some problems testing these plugins

#### Unauthenticated Git protocol
The `pom.xml` might contain a reference to the source-code-repository using an unauthenticated Git protocol on port 9418 like `git://github.com/jenkinsci/async-http-client-plugin.git`.
To fix this the `pom.xml` inside the `hpi`-file inside the WAR-file must be edited. The `git://`-protocol can be replaced with `https://`.

#### Blocked maven-mirrors
Maven blocks downloads from mirrors using `http`. When a plugin uses `http`-mirrors a workaround is to disable the block in the maven `settings.xml`.
Comment out the mirror block the looks like this:
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

## Conclusion

As time writing (20.06.2023) both test methods were not very useful / successful, because some of the plugins currently in use, can not be tested.
Furthermore, both methods could not identify known errors, because one plugin (async-http-client) uses reflection to load some classes at runtime, which are missing in JAVA 17.
These kinds of problems can only be detected by executing the specific code-blocks (either by manual testing or good unit-tests provided by th plugin).

We did not find an appropriate method to test all used Jenkins-Plugins in a reasonable time frame.    

Currently an upgrade to JAVA 17 is not advisable. Also the official 
[Jenkins documentation](https://www.jenkins.io/doc/developer/tutorial/prepare/#download-and-install-a-jdk) recommends using JAVA 11. 

## Going forward
To identify potentially problematic plugins a first step would be to check the minimal plugin requirements set by Jenkins (see this [blog-post](https://www.jenkins.io/blog/2022/12/14/require-java-11/)).

This procedure could look like this:
* extract the `pom.xml` from the `hpi.file`
* check the Jenkins- and the Java-Version of the plugin

Alternatively the Jenkins-CLI can be used, which also contains plugin-information, but is missing the JAVA-Version of the plugin. 

> Additionally the Jenkins-Admin-UI offers information about deprecation-status and dependencies of plugins.   