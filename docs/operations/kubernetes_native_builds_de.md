# Kubernetes-native Builds

## Builds auf bestimmte Knoten einschränken

Da ressourcenintensive Builds andere Workloads stören oder die Sicherheit beeinträchtigen könnten, möchten Sie Ihre 
Jenkins-Builds möglicherweise auf bestimmte Knoten ohne andere Workloads beschränken.

Jenkins-Builds sind standardmäßig bereits auf den Namespace `jenkins-build` beschränkt, sodass sie leicht isoliert 
werden können. Das Mutieren von Pods dieses Namespaces, um sie nur auf einem bestimmten Knoten auszuführen, wird 
üblicherweise mit einem Admission-Controller erreicht. Wir könnten zwar einen eigenen implementieren, aber es stehen
bereits mehrere Optionen zur Verfügung:

1. [PodNodeSelector][podnodeselector] ist ein integrierter Kubernetes Admission-Controller, der sich derzeit in der
   Alpha-Phase befindet. Seine Aktivierung ermöglicht es, einen Namespace zu annotieren, um Pods auf einem bestimmten 
   Knoten auszuführen.
2. [Kyverno][kyverno] ist eine Policy-Engine und ein Admission-Controller, den Sie möglicherweise bereits installiert 
   haben. Eine Richtlinie, wie wir sie benötigen, kann leicht implementiert werden, 
   [siehe dieses Beispiel][kyverno-example].
3. [Gatekeeper][gatekeeper] ist eine Alternative zu Kyverno. In diesen Beispielen sehen Sie, wie Sie 
   [eine Affinity][gatekeeper-affinity] und [Tolerations][gatekeeper-tolerations] für Ihre Pods in Gatekeeper festlegen.

[podnodeselector]: https://kubernetes.io/docs/reference/access-authn-authz/admission-controllers/#podnodeselector
[kyverno]: https://kyverno.io/
[kyverno-example]: https://github.com/cloudogu/jenkins/blob/develop/docs/operations/kyverno.yaml
[gatekeeper]: https://open-policy-agent.github.io/gatekeeper/website/
[gatekeeper-affinity]: https://github.com/cloudogu/jenkins/blob/develop/docs/operations/gatekeeper_affinity.yaml
[gatekeeper-tolerations]: https://github.com/cloudogu/jenkins/blob/develop/docs/operations/gatekeeper_tolerations.yaml
