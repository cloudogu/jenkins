import jenkins.model.*;
import hudson.security.*;
import groovy.json.JsonSlurper;

// based on https://gist.github.com/xbeta/e5edcf239fcdbe3f1672

def getJenkinsAuthenticatedUserPermissions() {
    return [
            "hudson.model.Hudson.Read",
            "hudson.model.Item.Build",
            "hudson.model.Item.Configure",
            "hudson.model.Item.Create",
            "hudson.model.Item.Delete",
            "hudson.model.Item.Discover",
            "hudson.model.Item.Read",
            "hudson.model.Item.Workspace",
            "hudson.model.Run.Delete",
            "hudson.model.Run.Update",
            "hudson.model.View.Configure",
            "hudson.model.View.Create",
            "hudson.model.View.Delete",
            "hudson.model.View.Read",
            "hudson.model.Item.Cancel"
    ]
}

def getJenkinsAdministratorPermissions() {
    return [
            "hudson.model.Hudson.Administer",
            "hudson.model.Hudson.ConfigureUpdateCenter",
            "hudson.model.Hudson.Read",
            "hudson.model.Hudson.RunScripts",
            "hudson.model.Hudson.UploadPlugins",
            "hudson.model.Computer.Build",
            "hudson.model.Computer.Build",
            "hudson.model.Computer.Configure",
            "hudson.model.Computer.Connect",
            "hudson.model.Computer.Create",
            "hudson.model.Computer.Delete",
            "hudson.model.Computer.Disconnect",
            "hudson.model.Run.Delete",
            "hudson.model.Run.Update",
            "hudson.model.View.Configure",
            "hudson.model.View.Create",
            "hudson.model.View.Read",
            "hudson.model.View.Delete",
            "hudson.model.Item.Create",
            "hudson.model.Item.Delete",
            "hudson.model.Item.Configure",
            "hudson.model.Item.Read",
            "hudson.model.Item.Discover",
            "hudson.model.Item.Build",
            "hudson.model.Item.Workspace",
            "hudson.model.Item.Cancel"
    ]
}

def getValueFromEtcd(String key){
    String ip = new File("/etc/ces/node_master").getText("UTF-8").trim();
    URL url = new URL("http://${ip}:4001/v2/keys/${key}");
    def json = new JsonSlurper().parseText(url.text)
    return json.node.value
}

def getJenkinsConfigurationState(String key){
    try {
        return getValueFromEtcd(key).toString()
    } catch (FileNotFoundException ex) {
        return "false"
    }
}
// JVM did not like 'hyphen' in the class name, it will crap out saying it is
// illegal class name.
def buildNewAccessList(userOrGroup, permissions) {
    def newPermissionsMap = [:]
    permissions.each {
        newPermissionsMap.put(Permission.fromId(it), userOrGroup)
    }
    return newPermissionsMap
}

final ETCD_CONFIGURED_KEY = "config/jenkins/configured"
boolean isConfigured = getJenkinsConfigurationState(ETCD_CONFIGURED_KEY).toBoolean()


def instance = Jenkins.getInstance()
if (instance.isUseSecurity()) {
    if (instance.pluginManager.activePlugins.find { it.shortName == "matrix-auth" } != null) {
        authStrategy = instance.getAuthorizationStrategy()
        if (!authStrategy || !isConfigured){
            println("initialize new matrix authorization strategy")
            authStrategy = new hudson.security.ProjectMatrixAuthorizationStrategy()
            //------------------- authenticated ---------------------------------------
            authenticated = buildNewAccessList("authenticated", getJenkinsAuthenticatedUserPermissions())
            authenticated.each { p, u -> authStrategy.add(p, u) }
        }
        // if the user changes the authorization-strategy the admin group will not be setup automatically
        if (authStrategy instanceof GlobalMatrixAuthorizationStrategy){
            //----------------- jenkins admin -----------------------------------------
            String adminGroup = getValueFromEtcd("config/_global/admin_group")
            jenkinsAdmin = buildNewAccessList(adminGroup, getJenkinsAdministratorPermissions())
            jenkinsAdmin.each { p, u -> authStrategy.add(p, u) }
            // now set the strategy globally
            instance.setAuthorizationStrategy(authStrategy)
            instance.save()
        }

    }
}
