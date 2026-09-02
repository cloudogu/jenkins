# Weitere Konfigurationen


## Zusätzliche Java-Args für den Jenkins-Prozess

Über den Konfigurationsschlüssel `additional_java_args` können beliebig viele weitere Java-Args an den Jenkins-Prozess
übergeben werden. Diese müssen dabei im gewohnten Format von Java-Args übergeben werden.
Beispiel: 
`kubectl edit configmap -n ecosystem jenkins-config`
````yaml
    data:
      config.yaml: |
        additional_java_args: "-Dmykey1=test1 -Dmykey2=test2"
````