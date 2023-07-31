# Configure log level

Via the configuration of the Jenkins Dogu you can control the log level of the application. The configuration will be 
applied on each start of the Jenkins Dogu.

## Configuration of the root logger

The log level of the root logger can be configured with the configuration key `/config/jenkins/logging/root`. Possible
values are `ERROR, WARN, INFO, DEBUG`. If nothing is configured the log level `WARN` will be applied as default value.

You can configue the log level with the `edit-config` command of the `cesapp` application.

```shell
cesapp edit-config jenkins
```

Alternative, you can use the application `etcdctl`.

```shell
etcdctl set /config/jenkins/logging/root INFO
```

## Configuration of other logger

Besides the root logger, you can configure other loggers as well. For each logger you need to add an entry to `/config/jenkins/logging` 
containing of the logger name and the desired log level.

Format: `/config/jenkins/logging/<logger-name> <log-level>`
Example: `/config/jenkins/logging/org.apache.sshd WARN`
