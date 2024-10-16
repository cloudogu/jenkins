package scripts

import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.matrixauth.*

final String ADMIN_GROUP_GLOBAL_KEY = 'admin_group'
final String ADMIN_GROUP_LAST_KEY = 'admin_group_last'
final String CONFIGURED_KEY = 'configured'

File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
ecoSystem = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()

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

LinkedHashMap buildNewAccessList(String userOrGroup, String[] permissions) {
    def newPermissionsMap = [:]
    permissions.each {
        newPermissionsMap.put(Permission.fromId(it), userOrGroup)
    }
    return newPermissionsMap
}

ProjectMatrixAuthorizationStrategy removeGroupFromAuthStrategy(String adminGroupLast, AuthorizationStrategy authStrategy) {
    Map<Permission, Set<PermissionEntry>> modifiablePermissionList = new HashMap<Permission, Set<PermissionEntry>>(authStrategy.getGrantedPermissionEntries())

    for (permissionWithEntriesPair in authStrategy.getGrantedPermissionEntries()) {
        for (permissionEntry in permissionWithEntriesPair.value) {
            if (permissionEntry.getSid() == adminGroupLast) {
                currentValue = new HashSet<PermissionEntry>(permissionWithEntriesPair.value)
                currentValue.remove(permissionEntry)
                modifiablePermissionList.replace(permissionWithEntriesPair.key, currentValue)
            }
        }
    }

    ProjectMatrixAuthorizationStrategy strategy = new ProjectMatrixAuthorizationStrategy()
    modifiablePermissionList.each { key, value ->
        for (userOrGroup in value) {
            strategy.add(key, userOrGroup)
        }
    }
    return strategy
}

ProjectMatrixAuthorizationStrategy updateOldUserGroupEntries(String groupName, AuthorizationStrategy authStrategy) {
    println('update user/group entries for "' + groupName + '"')
    testEntry = new PermissionEntry(AuthorizationType.EITHER, groupName)
    for (permission in authStrategy.getGrantedPermissionEntries()) {
        if (permission.value.contains(testEntry)) {
            println('change PermissionEntry.Type from "AuthorizationType.EITHER" to "AuthorizationType.GROUP" for group "' + groupName + '"')
            currentValue = permission.value
            currentValue.remove(testEntry)
            currentValue.add(PermissionEntry.group(groupName))
        }
    }
}

Jenkins instance = Jenkins.get()
String isConfigured = ecoSystem.getDoguConfig(CONFIGURED_KEY)
String adminGroup = ecoSystem.getGlobalConfig(ADMIN_GROUP_GLOBAL_KEY)
String adminGroupLast = ecoSystem.getDoguConfig(ADMIN_GROUP_LAST_KEY)
if (adminGroup == '') {
    println 'ERROR: There is no global admin group set in ' + ADMIN_GROUP_GLOBAL_KEY
    throw new IllegalStateException('dogu config key ' + ADMIN_GROUP_GLOBAL_KEY + 'is missing')
}
if (instance.isUseSecurity()) {
    if (instance.pluginManager.activePlugins.find { it.shortName == 'matrix-auth' } != null) {
        AuthorizationStrategy authStrategy = instance.getAuthorizationStrategy()
        if (!authStrategy || isConfigured == '') {
            println('Initializing new matrix authorization strategy')
            authStrategy = new ProjectMatrixAuthorizationStrategy()
            // add permissions for "authenticated" users
            authenticated = buildNewAccessList('authenticated', getJenkinsAuthenticatedUserPermissions())
            authenticated.each { p, u -> authStrategy.add(p, PermissionEntry.group(u)) }
        } else if(authStrategy) {
            updateOldUserGroupEntries(adminGroup, authStrategy)
            updateOldUserGroupEntries('authenticated', authStrategy)
        }
        // if the user changes the authorization-strategy the admin group will not be setup automatically
        if (authStrategy instanceof GlobalMatrixAuthorizationStrategy) {
            if (adminGroupLast == '') {
                println 'Setting initial auth strategy'
                // Adding admin group with admin permissions
                jenkinsAdmin = buildNewAccessList(adminGroup, getJenkinsAdministratorPermissions())
                jenkinsAdmin.each { p, u -> authStrategy.add(p, PermissionEntry.group(u)) }
            } else if (adminGroupLast == adminGroup) {
                println 'The admin group has not changed'
            } else {
                println('The admin group has changed from "' + adminGroupLast + '" to "' + adminGroup + '"')
                println 'Adding admin group "' + adminGroup + '" with admin permissions'
                LinkedHashMap jenkinsAdmin = buildNewAccessList(adminGroup, getJenkinsAdministratorPermissions())
                jenkinsAdmin.each { p, u -> authStrategy.add(p, PermissionEntry.group(u)) }
                //println 'Granting normal user permissions to old admin group "' + adminGroupLast + '"'
                println 'Removing old admin group "' + adminGroupLast + '" from auth strategy'
                ProjectMatrixAuthorizationStrategy newAuthStrategy = removeGroupFromAuthStrategy(adminGroupLast, authStrategy)
                authStrategy = newAuthStrategy
            }
            // Updating last admin group key with current admin group name
            try {
                ecoSystem.setDoguConfig(ADMIN_GROUP_LAST_KEY, adminGroup)
            } catch (IllegalStateException exception) {
                throw new IllegalStateException('Could not write last admin group key to dogu config', exception)
            }
            // now set the strategy globally
            instance.setAuthorizationStrategy(authStrategy)
            instance.save()
        }
    }
}
