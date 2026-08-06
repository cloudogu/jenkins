# Plugin blocklist

A blocklist can be maintained under `resources/init.groovy.d/plugin-blocklist.json`.
This can also be maintained via the configuration key `blocked.plugins`.
A comma-separated list must be passed, e.g.:

`kubectl edit configmap -n ecosystem jenkins-config`
````yaml
    data:
      config.yaml: |
        blocked.plugins: "pluginId1,pluginId2,pluginId3,pluginId4"
````

Plugins that are entered in this blocklist are removed when Jenkins is restarted.
