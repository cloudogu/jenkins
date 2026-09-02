# Additional configurations


## Additional Java args for the Jenkins process

The configuration key `additional_java_args` can be used to pass any number of additional Java args to the Jenkins
process. These must be passed in the usual format of Java args.
Example:
`kubectl edit configmap -n ecosystem jenkins-config`
````yaml
    data:
      config.yaml: |
        additional_java_args: "-Dmykey1=test1 -Dmykey2=test2"
````
