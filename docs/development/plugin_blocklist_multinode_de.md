# Plugin-Blockliste

Unter resources/init.groovy.d/plugin-blocklist.json kann eine Blockliste geführt werden.
Diese kann ebenfalls über den Konfigurationsschlüssel 'blocked.plugins' gepflegt werden,
dabei muss eine Komma getrennte Liste übergeben werden, z.B.:

`kubectl edit configmap -n ecosystem jenkins-config`
````yaml
    data:
      config.yaml: |
        blocked.plugins: "pluginId1,pluginId2,pluginId3,pluginId4"
````

Plugins die in dieser Blockliste eingetragen sind, werden bei einem Neustart des Jenkins Dogus entfernt.