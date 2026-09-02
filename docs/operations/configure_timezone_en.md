# Configure the time zone

The Jenkins Dogu time zone can be configured using additional Java arguments.

Edit the Jenkins Dogu configuration:

`kubectl edit configmap -n ecosystem jenkins-config`
````yaml
    data:
      config.yaml: |
        additional_java_args: "-Duser.timezone=Europe/Berlin"
````
