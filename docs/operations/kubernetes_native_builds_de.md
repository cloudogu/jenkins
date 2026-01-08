# Kubernetes-native Builds

Wenn eine Kubernetes-Umgebung erkannt wird, wird das Kubernetes-Plugin automatisch installiert und konfiguriert.

## Verwendung

Eine kleine Beispiel-Pipeline könnte wie folgt aussehen:
```groovy
podTemplate {
    node(POD_LABEL) {
        stage('Run shell') {
            sh 'echo hello world'
        }
    }
}
```

Weitere Informationen zur Verwendung finden Sie in der [offiziellen Dokumentation][jenkins-kubernetes].

## Builds auf bestimmte Knoten einschränken

Da ressourcenintensive Builds andere Workloads stören oder die Sicherheit beeinträchtigen könnten, möchten Sie Ihre 
Jenkins-Builds möglicherweise auf bestimmte Knoten ohne andere Workloads beschränken.

Es ist möglich, zu verhindern, dass Workloads auf einem Knoten ausgeführt werden, indem man einen Taint setzt:
```shell
kubectl taint nodes worker-2 reserved-node=jenkins-build:NoSchedule
```
Das muss ausgeführt werden, bevor Workloads dem Knoten zugewiesen werden. Falls bereits Workloads darauf laufen, 
sollten Sie den Knoten [leer machen (drain)][drain].

Ggf. ist es sinnvoll, unseren Knoten zu labeln, sodass wir unseren Pods später einfacher eine Affinity geben können:
```shell
kubectl label node worker-2 reserved-node=jenkins-build
```

Jenkins-Builds sind standardmäßig bereits mit `cloudogu.com/pod-kind: jenkins-build` markiert, sodass sie leicht
isoliert werden können. Das Mutieren dieser Pods, um sie nur auf einem bestimmten Knoten auszuführen, wird üblicherweise
mit einem Admission-Controller erreicht. Wir könnten zwar einen eigenen implementieren, aber es stehen bereits mehrere
Optionen zur Verfügung:

1. [PodNodeSelector][podnodeselector] ist ein integrierter Kubernetes Admission-Controller, der sich derzeit in der
   Alpha-Phase befindet. Seine Aktivierung ermöglicht es, einen Namespace zu annotieren, um Pods auf einem bestimmten 
   Knoten auszuführen.
2. [Kyverno][kyverno] ist eine Policy-Engine und ein Admission-Controller, den Sie möglicherweise bereits installiert 
   haben. Eine Richtlinie, wie wir sie benötigen, kann leicht implementiert werden, 
   [siehe dieses Beispiel][kyverno-example].
3. [Gatekeeper][gatekeeper] ist eine Alternative zu Kyverno.
   Wie die Richtlinien in Gatekeeper implementiert werden, [sehen Sie in diesem Beispiel][gatekeeper-example].

[jenkins-kubernetes]: https://plugins.jenkins.io/kubernetes/#plugin-content-usage
[drain]: https://kubernetes.io/docs/tasks/administer-cluster/safely-drain-node/
[podnodeselector]: https://kubernetes.io/docs/reference/access-authn-authz/admission-controllers/#podnodeselector
[kyverno]: https://kyverno.io/
[kyverno-example]: https://github.com/cloudogu/jenkins/blob/develop/docs/operations/kyverno.yaml
[gatekeeper]: https://open-policy-agent.github.io/gatekeeper/website/
[gatekeeper-example]: https://github.com/cloudogu/jenkins/blob/develop/docs/operations/gatekeeper.yaml
