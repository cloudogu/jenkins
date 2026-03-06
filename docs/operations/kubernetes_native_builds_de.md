# Kubernetes-native Builds

## Erste Schritte

Um das Kubernetes-Plugin in Jenkins zu aktivieren, setzen Sie einfach `enable_kubernetes_agents` in der dogu-Konfiguration auf `"true"`.
Eine sinnvolle Standardkonfiguration wird automatisch bereitgestellt, [kann aber an Ihre Bedürfnisse angepasst werden](#plugin-konfiguration).

Um einen Namespace und eine Zugriffskontrolle für die Agenten einzurichten, kann die Komponente [k8s-jenkins-agent-integration][integration-component] installiert werden.
Deren Standardkonfiguration sollte auch für die gängigsten Szenarien funktionieren, hängt jedoch von Kyverno ab, um Agenten nur auf bestimmten Knoten zu planen.
Wenn Sie Kyverno nicht installiert haben, möchten Sie diese Integration möglicherweise deaktivieren, siehe [Konfigurationsoptionen der Komponente](#komponenten-konfiguration).

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

## Plugin-Konfiguration

Die folgenden Konfigurationsoptionen stehen zur Verfügung.
Sie decken nicht alles ab, was mit dem Jenkins Kubernetes-Plugin möglich ist, aber die gängigsten Anwendungsfälle sollten möglich sein. Andere könnten möglich sein, wenn Sie [Ihre eigene Cloud-Konfiguration erstellen](#erstellen-sie-ihre-eigene-cloud-konfiguration).

### `enable_kubernetes_agents`

Standard: `"false"`

Gibt an, ob die Kubernetes-Agenten-Integration aktiviert werden soll. Irrelevant, wenn Jenkins nicht auf Kubernetes läuft.

### `ecosystem_kubernetes_namespace`

Standard: `"ecosystem"`

Kubernetes-Namespace, in dem diese Jenkins-Instanz bereitgestellt wird.

### `agent_kubernetes_namespace`

Standard: `"jenkins-ci"`

Kubernetes-Namespace, in dem die Jenkins-Agenten bereitgestellt werden.

### `agent_kubernetes_pod_labels`

Standard: `"{\"cloudogu.com/pod-kind\": \"jenkins-ci\"}"`

Labels, die den Agenten-Pods hinzugefügt werden sollen.

### `agent_kubernetes_restricted_pss_security_context`

Standard: `"false"`

Gibt an, ob den Agenten-Pods ein Security Context hinzugefügt werden soll, der für die Verwendung des 'restricted' Pod Security Standards geeignet ist.

### `agent_kubernetes_docker_image`

Standard: `""`

Container-Image, das für die Agent-Pods verwendet werden soll, falls keins angegeben ist.
Wenn kein Image angegeben wird, wird das Standard-Image des Kubernetes-Plugins verwendet.

### `agent_kubernetes_docker_registry`

Standard: `""`

Docker-Registry, die für das Agenten-Image verwendet werden soll, falls keine angegeben ist.

### `agent_kubernetes_enable_garbage_collection`

Standard: `"false"`

Wenn aktiviert, prüft Jenkins regelmäßig auf verwaiste Pods, die seit 300 Sekunden nicht mehr berührt wurden, und löscht diese.

### Erstellen Sie Ihre eigene Cloud-Konfiguration

Wenn die bereitgestellten Einstellungen nicht Ihren Anforderungen entsprechen oder wenn Sie weitere Clouds hinzufügen möchten, ist es möglich, eigene hinzuzufügen.
Nennen Sie diese nur nicht `kubernetes`, da dies der Name ist, den unsere Integration verwendet.

Wenn Ihre Cloud Zugriff auf den lokalen Kubernetes-Cluster benötigt, wird ein `jenkins` ServiceAccount bereitgestellt und eingebunden.
Diesen können Sie verwenden, um die benötigten Rollen zu binden.
Rollen und RoleBindings für die enthaltene `kubernetes` Cloud werden bereits durch die oben genannte [Komponente][integration-component] bereitgestellt.

## Komponenten-Konfiguration

### NetworkPolicies

#### `global.networkPolicies.enabled`

Standard: `true`

Ob NetworkPolicies überhaupt verwendet werden sollen.
Normalerweise verwendet das Cloudogu EcoSystem NetworkPolicies, daher ist dies notwendig.
Wenn Sie aus irgendeinem Grund keine NetworkPolicies wünschen oder benötigen, können diese hier deaktiviert werden.

#### `global.networkPolicies.doguSelector`

Standard:
```yaml
matchLabels:
  dogu.name: jenkins
```

Der Selektor, der für NetworkPolicies verwendet werden soll, um den Pod des Dogus auszuwählen.
Normalerweise muss dies nicht angepasst werden.

#### `global.networkPolicies.agentSelector`

Standard:
```yaml
matchLabels:
  cloudogu.com/pod-kind: jenkins-ci
```

Der Selektor, der für NetworkPolicies verwendet werden soll, um die Jenkins-Agenten-Pods auszuwählen.
Dieser muss mit den für die Agenten-Pods gesetzten Labels [in der Konfiguration des Dogus](#agent_kubernetes_pod_labels) übereinstimmen.

### Namespaces

#### `namespaces.create`

Standard: `true`

Gibt an, ob die in [`namespaces.names`](#namespacesnames) angegebenen Namespaces erstellt werden sollen.

#### `namespaces.names`

Standard: `[jenkins-ci]`

Liste der Namespaces, für die Ressourcen (NetworkPolicies, RBAC, ...) erstellt werden sollen.

### RBAC

#### `serviceAccount.name`

Standard: `jenkins`

Der ServiceAccount, an den Rollen gebunden werden sollen.
Da der ServiceAccount vom Jenkins-Dogu bereitgestellt wird, muss dies normalerweise nicht angepasst werden.

### Builds auf bestimmte Knoten einschränken

Da ressourcenintensive Builds andere Workloads stören oder die Sicherheit beeinträchtigen könnten,
möchten Sie Ihre Jenkins-Builds möglicherweise auf bestimmte Knoten ohne andere Workloads beschränken.

Es ist möglich, zu verhindern, dass andere Workloads auf einem Knoten geplant werden, indem man einen Taint setzt:
```shell
kubectl taint nodes <node-name> reserved-node=jenkins-ci:NoSchedule
```
Dies muss geschehen, bevor Workloads geplant werden.
Wenn bereits Workloads darauf geplant sind, sollten Sie den Knoten [leeren (drain)][drain].

Ggf. ist es sinnvoll, unseren Knoten zu labeln, um es einfacher zu machen, unseren Pods später eine Affinity dafür zu geben:
```shell
kubectl label node <node-name> reserved-node=jenkins-ci
```

Jenkins-Builds sind standardmäßig bereits mit `cloudogu.com/pod-kind: jenkins-ci` markiert, sodass sie leicht isoliert werden können.
Das Mutieren dieser Pods, um sie nur auf einem bestimmten Knoten auszuführen, wird üblicherweise mit einem Admission-Controller erreicht.
Wir könnten zwar einen eigenen implementieren, aber es stehen bereits mehrere Optionen zur Verfügung:

1. [PodNodeSelector][podnodeselector] ist ein integrierter Kubernetes Admission-Controller, der sich derzeit in der Alpha-Phase befindet.
   Seine Aktivierung ermöglicht es, einen Namespace zu annotieren, um Pods auf einem bestimmten Knoten auszuführen.
2. [Kyverno][kyverno] ist eine Policy-Engine und ein Admission-Controller, den Sie möglicherweise bereits installiert haben.
3. [Gatekeeper][gatekeeper] ist eine Alternative zu Kyverno.

Kyverno- und Gatekeeper-Policies sind bereits Teil der [k8s-jenkins-agent-integration Komponente][integration-component] und können einzeln aktiviert oder deaktiviert werden.

#### `policies.kyverno.enabled`

Standard: `true`

Ob die Kyverno ClusterPolicy angewendet werden soll, um Agenten-Pods bestimmten Knoten zuzuweisen.

#### `policies.gatekeeper.enabled`

Standard: `false`

Ob die Gatekeeper Assign-Policies angewendet werden sollen, um Agenten-Pods bestimmten Knoten zuzuweisen.

#### Pod-Matching

##### `policies.matchPods.namespaces.enabled`

Standard: `true`

Gibt an, ob Pods aus den in [`namespaces.names`](#namespacesnames) angegebenen Namespaces abgeglichen werden sollen.

##### `policies.matchPods.labelSelector.enabled`

Standard: `false`

Gibt an, ob Pods über ihre Labels abgeglichen werden sollen.

##### `policies.matchPods.labelSelector.matchLabels`

Standard:
```yaml
cloudogu.com/pod-kind: jenkins-ci
```

Labels, mit denen die Pods abgeglichen werden sollen.

##### `policies.matchPods.namespaceSelector.enabled`

Standard: `false`

Gibt an, ob Namespaces über ihre Labels abgeglichen werden sollen.

##### `policies.matchPods.namespaceSelector.matchLabels`

Standard:
```yaml
kubernetes.io/metadata.name: jenkins-ci
```

Labels, mit denen die Namespaces abgeglichen werden sollen.

##### `policies.affinity`

Standard:
```yaml
nodeAffinity:
  requiredDuringSchedulingIgnoredDuringExecution:
    nodeSelectorTerms:
      - matchExpressions:
          - key: reserved-node
            operator: In
            values:
              - jenkins-ci
```

Affinity, die in den Agenten-Pods gesetzt werden soll.

##### `policies.tolerations`

Standard:
```yaml
- key: reserved-node
  operator: Equal
  value: jenkins-ci
  effect: NoSchedule
```

Tolerations, die in den Agenten-Pods gesetzt werden soll.


[integration-component]: https://github.com/cloudogu/k8s-jenkins-agent-integration/
[jenkins-kubernetes]: https://plugins.jenkins.io/kubernetes/#plugin-content-usage
[drain]: https://kubernetes.io/docs/tasks/administer-cluster/safely-drain-node/
[podnodeselector]: https://kubernetes.io/docs/reference/access-authn-authz/admission-controllers/#podnodeselector
[kyverno]: https://kyverno.io/
[gatekeeper]: https://open-policy-agent.github.io/gatekeeper/website/
