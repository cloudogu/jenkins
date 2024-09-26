# Plugin-Blockliste

Unter resources/init.groovy.d/plugin-blocklist.json kann eine Blockliste geführt werden.
Diese kann ebenfalls über den etcd-key 'config/jenkins/blocked.plugins' gepflegt werden,
dabei muss eine Komma getrennte Liste übergeben werden, z.B.:

```shell
etcdctl set config/jenkins/blocked.plugins pluginId1,pluginId2,pluginId3,pluginId4
```
Plugins die in dieser Blockliste eingetragen sind, werden bei einem Neustart des Jenkins Dogus entfernt.