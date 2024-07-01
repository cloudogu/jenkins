# Weitere Konfigurationen


## Zusätzliche Java-Args für den Jenkins-Prozess

Über den Registry-Key `config/jenkins/additional_java_args` können beliebig viele weitere Java-Args an den Jenkins-Prozess
übergeben werden. Diese müssen dabei im gewohnten Format von Java-Args übergeben werden.
Beispiel: `etcdctl set config/jenkins/additional_java_args "-Dmykey1=test1 -Dmykey2=test2"`.
Nachdem der Registry-Key gesetzt wurde, muss das Dogu neu gestartet werden, damit die Konfiguration angewandt wird.