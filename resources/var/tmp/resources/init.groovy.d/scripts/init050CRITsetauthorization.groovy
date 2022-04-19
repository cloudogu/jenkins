import jenkins.model.*
import hudson.security.*
import hudson.util.VersionNumber
import groovy.json.JsonSlurper
import org.jenkinsci.plugins.matrixauth.*

final String ADMINGROUPKEY = 'config/_global/admin_group'
final String ADMINGROUPLASTKEY = 'config/jenkins/admin_group_last'
final String ETCD_CONFIGURED_KEY = 'config/jenkins/configured'

String[] getJenkinsAuthenticatedUserPermissions() {
    return [
            'hudson.model.Hudson.Read',
            'hudson.model.Item.Build',
            'hudson.model.Item.Configure',
            'hudson.model.Item.Create',
            'hudson.model.Item.Delete',
            'hudson.model.Item.Discover',
            'hudson.model.Item.Read',
            'hudson.model.Item.Workspace',
            'hudson.model.Run.Delete',
            'hudson.model.Run.Update',
            'hudson.model.View.Configure',
            'hudson.model.View.Create',
            'hudson.model.View.Delete',
            'hudson.model.View.Read',
            'hudson.model.Item.Cancel'
    ]
}

String[] getJenkinsAdministratorPermissions() {
    return [
            'hudson.model.Hudson.Administer',
            'hudson.model.Hudson.ConfigureUpdateCenter',
            'hudson.model.Hudson.Read',
            'hudson.model.Hudson.RunScripts',
            'hudson.model.Hudson.UploadPlugins',
            'hudson.model.Computer.Build',
            'hudson.model.Computer.Build',
            'hudson.model.Computer.Configure',
            'hudson.model.Computer.Connect',
            'hudson.model.Computer.Create',
            'hudson.model.Computer.Delete',
            'hudson.model.Computer.Disconnect',
            'hudson.model.Run.Delete',
            'hudson.model.Run.Update',
            'hudson.model.View.Configure',
            'hudson.model.View.Create',
            'hudson.model.View.Read',
            'hudson.model.View.Delete',
            'hudson.model.Item.Create',
            'hudson.model.Item.Delete',
            'hudson.model.Item.Configure',
            'hudson.model.Item.Read',
            'hudson.model.Item.Discover',
            'hudson.model.Item.Build',
            'hudson.model.Item.Workspace',
            'hudson.model.Item.Cancel'
    ]
}

String getValueFromEtcd(String key) {
    String ip = new File('/etc/ces/node_master').getText('UTF-8').trim()
    try {
        URL url = new URL("http://${ip}:4001/v2/keys/${key}")
        def json = new JsonSlurper().parseText(url.text)
        return json.node.value
    } catch (FileNotFoundException exception) {
        println('Key ' + key + ' does not exist')
        return ''
    }
}

void writeValueToEtcd(String key, String value) {
    String ip = new File('/etc/ces/node_master').getText('UTF-8').trim()
    URL url = new URL("http://${ip}:4001/v2/keys/${key}")

    def conn = url.openConnection()
    conn.setRequestMethod('PUT')
    conn.setDoOutput(true)
    conn.setRequestProperty('Content-Type', 'application/x-www-form-urlencoded')
    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())
    writer.write("value=${value}")
    writer.flush()
    writer.close()

    def responseCode = conn.getResponseCode()
    if (responseCode != 200 && responseCode != 201) {
        throw new IllegalStateException('etcd returned invalid response code ' + responseCode)
    }
}

LinkedHashMap buildNewAccessList(String userOrGroup, String[] permissions) {
    def newPermissionsMap = [:]
    permissions.each {
        newPermissionsMap.put(Permission.fromId(it), userOrGroup)
    }
    return newPermissionsMap
}

ProjectMatrixAuthorizationStrategy removeGroupFromAuthStrategy(String adminGroupLast, AuthorizationStrategy authStrategy) {
    Map<Permission, Set<String>> permissionWithoutAdminGroupLast = new HashMap<Permission, Set<String>>(authStrategy.getGrantedPermissions())
    for (permission in authStrategy.getGrantedPermissions()) {
        if (permission.value.contains(adminGroupLast)) {
            currentValue = permission.value
            currentValue.remove(adminGroupLast)
            permissionWithoutAdminGroupLast.replace(permission.key, currentValue)
        }
    }
    ProjectMatrixAuthorizationStrategy strategy = new ProjectMatrixAuthorizationStrategy()
    permissionWithoutAdminGroupLast.each { key, value ->
        for (userOrGroup in value) {
            strategy.add(key, userOrGroup)
        }
    }
    return strategy
}

// The error with the old user group entries only appears in versions of the matrix-auth plugin > 3.0. Prior to that
// no check is needed
boolean isNewMatrixAuthPluginVersion() {
    final MATRIXAUTHPLUGIN_BREAKINGVERSION = new VersionNumber("3.0")
    matrixAuthPluign = Jenkins.get().pluginManager.activePlugins.find { it.shortName == 'matrix-auth' }
    return matrixAuthPluign.getVersionNumber().isNewerThanOrEqualTo(MATRIXAUTHPLUGIN_BREAKINGVERSION)
}

ProjectMatrixAuthorizationStrategy updateOldUserGroupEntries(String groupName, AuthorizationStrategy authStrategy) {
    if (isNewMatrixAuthPluginVersion()) {
        println('update user/group entries for "' + groupName + '"')
        /**
         * It is fundamentally not a good idea to pull library code from a plugin. Especially if we have no control over
         * what version of the plugin is installed. However, here it was done anyway.
         * So the classes PermissionEntry and AuthorizationType are only available in version 3.0+ of the matrix-auth
         * plugin. Therefore they now need to be loaded via reflection API. If they are referenced directly this ensures
         * in instances with an old plugin that this script is no longer compilable.
         */
        testEntry = Class.forName("org.jenkinsci.plugins.matrixauth.PermissionEntry")
                .getDeclaredConstructors(Class.forName("org.jenkinsci.plugins.matrixauth.AuthorizationType"), String.class)
                .newInstance(Class.forName("org.jenkinsci.plugins.matrixauth.AuthorizationType").EITHER, groupName)
        // testEntry = new PermissionEntry(AuthorizationType.EITHER, groupName)
        for (permission in authStrategy.getGrantedPermissionEntries()) {
            if (permission.value.contains(testEntry)) {
                println('change PermissionEntry.Type from "AuthorizationType.EITHER" to "AuthorizationType.GROUP" for group "' + groupName + '"')
                currentValue = permission.value
                currentValue.remove(testEntry)
                currentValue.add(PermissionEntry.group(groupName))
            }
        }
    }
}

Jenkins instance = Jenkins.get()
String isConfigured = getValueFromEtcd(ETCD_CONFIGURED_KEY)
String adminGroup = getValueFromEtcd(ADMINGROUPKEY)
String adminGroupLast = getValueFromEtcd(ADMINGROUPLASTKEY)
if (adminGroup == '') {
    println 'ERROR: There is no global admin group set in ' + ADMINGROUPKEY
    throw new IllegalStateException('etcd key ' + ADMINGROUPKEY + ' missing')
}
if (instance.isUseSecurity()) {
    if (instance.pluginManager.activePlugins.find { it.shortName == 'matrix-auth' } != null) {
        AuthorizationStrategy authStrategy = instance.getAuthorizationStrategy()
        if (!authStrategy || isConfigured == '') {
            println('Initializing new matrix authorization strategy')
            authStrategy = new ProjectMatrixAuthorizationStrategy()
            // add permissions for "authenticated" users
            authenticated = buildNewAccessList('authenticated', getJenkinsAuthenticatedUserPermissions())
            if (isNewMatrixAuthPluginVersion()) {
                authenticated.each { p, u -> authStrategy.add(p, PermissionEntry.group(u)) }
            } else {
                authenticated.each { p, u -> authStrategy.add(p, u) }
            }

        } else if (authStrategy) {
            updateOldUserGroupEntries(adminGroup, authStrategy)
            updateOldUserGroupEntries('authenticated', authStrategy)
        }
        // if the user changes the authorization-strategy the admin group will not be setup automatically
        if (authStrategy instanceof GlobalMatrixAuthorizationStrategy) {
            if (adminGroupLast == '') {
                println 'Setting initial auth strategy'
                // Adding admin group with admin permissions
                jenkinsAdmin = buildNewAccessList(adminGroup, getJenkinsAdministratorPermissions())

                if (isNewMatrixAuthPluginVersion()) {
                    jenkinsAdmin.each { p, u -> authStrategy.add(p, PermissionEntry.group(u)) }
                } else {
                    jenkinsAdmin.each { p, u -> authStrategy.add(p, u) }
                }
            } else if (adminGroupLast == adminGroup) {
                println 'The admin group has not changed'
            } else {
                println('The admin group has changed from "' + adminGroupLast + '" to "' + adminGroup + '"')
                println 'Adding admin group "' + adminGroup + '" with admin permissions'
                if (isNewMatrixAuthPluginVersion()) {
                    LinkedHashMap jenkinsAdmin = buildNewAccessList(adminGroup, getJenkinsAdministratorPermissions())
                    jenkinsAdmin.each { p, u -> authStrategy.add(p, PermissionEntry.group(u)) }
                } else {
                    jenkinsAdmin = buildNewAccessList(adminGroup, getJenkinsAdministratorPermissions())
                    jenkinsAdmin.each { p, u -> authStrategy.add(p, u) }
                }
                //println 'Granting normal user permissions to old admin group "' + adminGroupLast + '"'
                println 'Removing old admin group "' + adminGroupLast + '" from auth strategy'
                ProjectMatrixAuthorizationStrategy newAuthStrategy = removeGroupFromAuthStrategy(adminGroupLast, authStrategy)
                authStrategy = newAuthStrategy
            }
            // Updating last admin group key with current admin group name
            try {
                writeValueToEtcd(ADMINGROUPLASTKEY, adminGroup)
            } catch (IllegalStateException exception) {
                throw new IllegalStateException('Could not write last admin group key to etcd', exception)
            }
            // now set the strategy globally
            instance.setAuthorizationStrategy(authStrategy)
            instance.save()
        }
    }
}
