# Einrichten von Jenkins-Agents in der Azure-Cloud

In diesem Text wird beschrieben, wie Sie Jenkins-Agents für die Ausführung auf virtuellen Maschinen in der Azure-Cloud erstellen und konfigurieren.
Er umfasst auch die Installation von Docker, um Container in Jenkins-Pipelines auszuführen.

Sie benötigen die Tools [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) und [Packer](https://www.packer.io/) auf Ihrem lokalen Rechner.
Außerdem benötigen Sie Zugriff auf Ihren Kubernetes-Cluster über `kubectl`.


## Erstellen eines Azure-Images mit Packer

- Erstellen Sie eine Packer-Vorlage für das VM-Abbild wie [azure-jenkins-agent.pkr.hcl](azure-jenkins-agent.pkr.hcl)
  - Passen Sie `source.azure-arm.jenkins-agent.managed_image_resource_group_name` an eine Ressourcengruppe in Ihrer Azure-Umgebung an
- Erstellen Sie die entsprechende Variablendatei, z.B. `azure-jenkins-agent-variables.auto.pkrvars.hcl`:

```hcl
client_id = ""
client_secret = ""
tenant_id = ""
subscription_id = ""
```

- Holen Sie sich die in der Variablendatei benötigten Anmeldedaten über Azure-CLI-Befehle:
  - Verwenden Sie `az login`, um sich mit Ihrer Azure-Umgebung zu verbinden
  - Ermitteln Sie die subscription_id über `az account show --query "{ subscription_id: id }"`
  - Ermitteln Sie die Client-ID, die Tenant-ID und das Client-Secret über `az ad sp create-for-rbac --role Contributor --scopes /subscriptions/SUBSCRIPTION_ID --query "{ client_id: appId, client_secret: password, tenant_id: tenant }"`
    - Ersetzen Sie `SUBSCRIPTION_ID` durch die subscription_id, die Sie im vorherigen Schritt erhalten haben

- Installieren Sie die erforderlichen Plugins über `packer init .`
- Erstellen Sie das VM-Image mit `packer build .`
- Sie erhalten ein VM-Image in Ihrer Azure-Umgebung mit dem Namen `cesJenkinsDockerAgent`.

## Installation des Azure VM Agents-Plugins

- Installieren Sie das Azure VM Agents Plugin (und, optional, das Docker Pipeline Plugin) mit [init150NORMinstallAzurePlugins.groovy](init150NORMinstallAzurePlugins.groovy)
- Sie können dieses Skript ausführen, indem Sie es dem Verzeichnis für zusätzliche Skripte in Jenkins hinzufügen oder es über die Skriptkonsole ausführen
  - Führen Sie das Skript über das Jenkins-Verzeichnis für zusätzliche Skripte aus:
    - Ermitteln Sie den Jenkins-Pod-Namen über `JENKINSPODID=$(kubectl get pods --namespace ecosystem | grep jenkins | awk 'NR==1{print $1}')`
    - Kopieren Sie das Skript auf das Jenkins-Daten-Volume: `kubectl cp --namespace ecosystem init150NORMinstallAzurePlugins.groovy ${JENKINSPODID}:/var/lib/custom.init.groovy.d/init150NORMinstallAzurePlugins.groovy`
    - Starten Sie Jenkins neu, indem Sie den Jenkins-Pod löschen (der dann neu erstellt wird), damit das Skript beim Start ausgeführt wird.
  - Führen Sie das Skript über die Jenkins-Skriptkonsole aus:
    - In der Jenkins-GUI gehen Sie zu "Jenkins verwalten" -> "Skript-Konsole".
    - Fügen Sie das obige Groovy-Skript in das Textfeld ein und klicken Sie auf "Ausführen".

## Konfigurieren der Azure Cloud in Jenkins

- Verwenden Sie das Skript [init160NORMconfigureAzureCloud.groovy](init160NORMconfigureAzureCloud.groovy) zur Konfiguration von Azure-Agents in Jenkins
  - Geben Sie die Azure-Anmeldeinformationen aus den vorherigen Schritten ein
  - Stellen Sie sicher, dass der Name der Ressourcengruppe korrekt ist.
- Führen Sie das Skript über die Jenkins-Skriptkonsole oder über das zusätzliche Skriptverzeichnis wie oben beschrieben aus.

## Testen mit einer Jenkins-Pipeline

Sie können testen, ob alles korrekt funktioniert hat, indem Sie diese Test-Pipeline verwenden:
- Fügen Sie ein Jenkins-Pipeline-Projekt hinzu
- Verwenden Sie den folgenden Code für die Pipeline:

```groovy
#!groovy
@Library(['github.com/cloudogu/ces-build-lib@1.62.0'])

import com.cloudogu.ces.cesbuildlib.*

node('docker') {
    stage('Clone Repository') {
        checkout scmGit(userRemoteConfigs: [
                [url: 'https://github.com/ianmiell/simple-dockerfile/']
        ])
    }

    def docker = new Docker(this)
    stage('Docker Build') {
        docker.build("testing/azuretest:0.0.0")
        sh 'docker image ls'
    }

    stage('Docker Tag') {
        docker.image("testing/azuretest:0.0.0").tag("0.0.1")
        sh 'docker image ls'
    }

    stage('Docker Push') {
        String REGISTRY_URL = ""
        docker.withRegistry(REGISTRY_URL, "registrycredentials") {
            docker.image("testing/azuretest:0.0.1").push()
        }
    }
}

```

- Passen Sie die Registry-URL (REGISTRY_URL) an Ihre Container-Image-Registry an
- Fügen Sie Jenkins-Anmeldeinformationen mit der ID `registrycredentials` hinzu, um Ihre Container-Image-Registry-Anmeldeinformationen zu speichern
- Führen Sie die Pipeline aus. Eine neue Azure-VM sollte bereitgestellt werden, mit Jenkins verbunden werden und die Pipeline ausführen