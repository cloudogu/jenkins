# Additional configurations

## Additional Java-Args for the Jenkins process

The registry key `config/jenkins/additional_java_args` can be used to pass any number of additional Java args to the Jenkins process.
process. These must be passed in the usual format of Java-Args.
Example: `etcdctl set config/jenkins/additional_java_args "-Dmykey1=test1 -Dmykey2=test2"`.
After the registry key is set, the dogu must be restarted for the configuration to be applied.