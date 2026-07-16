# Configure the time zone

The Jenkins Dogu time zone can be configured using additional Java arguments.

Edit the Jenkins Dogu configuration:

```bash
cesapp edit-config jenkins
```

Set the `additional_java_args` configuration key to the desired JVM time zone parameter, for example:

```
additional_java_args: "-Duser.timezone=Europe/Berlin"
```

After changing the configuration, restart the Jenkins Dogu so that the new Java arguments are applied.

