# Configure log level

Via the configuration of Jenkins, you can control the log level of the application. This configuration is updated each
time Jenkins starts.

## Configuration of the root logger

The log level of the root logger can be configured with the configuration key `logging/root`. Possible values are
`ERROR, WARN, INFO, DEBUG`. If nothing is configured, the log level `WARN` will be used as the default value.

You can adjust the configuration via `kubectl`:

`kubectl edit configmap -n ecosystem jenkins-config`
````yaml
    data:
      config.yaml: |
        logging:
          root: INFO
````


## Configuration of other loggers

Besides the root logger, you can configure other loggers as well. For each logger you need to add an entry under
`logging`.

Format: `logging/<logger-name> <log-level>`
Example: `logging/org.apache.sshd WARN`
