# Plugin-Blocklist

A blocklist can be maintained under resources/init.groovy.d/plugin-blocklist.json.
This can also be maintained via the etcd-key 'config/jenkins/blocked.plugins'.
A comma-separated list must be passed to the key, e.g:

```shell
etcdctl set config/jenkins/blocked.plugins pluginId1,pluginId2,pluginId3,pluginId4
```

Plugins that are entered in this blocklist are removed when the Jenkins Dogu is restarted.