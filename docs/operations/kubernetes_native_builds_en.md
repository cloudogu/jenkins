# Kubernetes-native builds

## Getting Started

To enable the Kubernetes plugin in Jenkins, simply set `enable_kubernetes_agents` to `"true"` in the dogu config.
A sensible default configuration is automatically deployed but [may be adjusted to your needs](#plugin-configuration).

To set up a namespace and access control for the agents, the [k8s-jenkins-agent-integration][integration-component] component can be installed.
Its default config should also work for the most common scenarios, but depends on Kyverno to schedule agents on specific
nodes only. If you do not have Kyverno installed, you might want to disable this integration, see
[the component's configuration options](#component-configuration).

## Usage

A small example pipeline may look like this:
```groovy
podTemplate {
    node(POD_LABEL) {
        stage('Run shell') {
            sh 'echo hello world'
        }
    }
}
```

For more usage, see [the official documentation][jenkins-kubernetes].

## Plugin configuration

The following configuration options are provided.
They do not cover everything that is possible with the Jenkins Kubernetes plugin,
but the most common use-cases should be possible and others might be if you
[create your own cloud configuration](#create-your-own-cloud-configuration).

### `enable_kubernetes_agents`

Default: `"false"`

Whether to enable kubernetes agent integration. Irrelevant if not on kubernetes.

### `ecosystem_kubernetes_namespace`

Default: `"ecosystem"`

Kubernetes namespace where this Jenkins instance is deployed.

### `agent_kubernetes_namespace`

Default: `"jenkins-ci"`

Kubernetes namespace where the Jenkins agents are deployed.

### `agent_kubernetes_pod_labels`

Default: `"{\"cloudogu.com/pod-kind\": \"jenkins-ci\"}"`

Labels to be added to agent pods.

### `agent_kubernetes_restricted_pss_security_context`

Default: `"false"`

Whether to add a Security Context to agent pods that is suitable for the use of the 'restricted' Pod Security Standard.

### `agent_kubernetes_docker_registry`

Default: `""`

Docker registry to use for the agent image if none is specified.

### `agent_kubernetes_enable_garbage_collection`

Default: `"false"`

When enabled, Jenkins will periodically check for orphan pods that have not been touched for 300 seconds and delete them.

### Create your own cloud configuration

If the provided settings do not fit your needs or if you want to add other clouds, it is possible to add your own.
Just do not name it `kubernetes` as that is the name our integration uses.

If your cloud needs to access the local kubernetes cluster, a `jenkins` ServiceAccount is provided and mounted.
You can use that to bind the roles you need.
Roles and RoleBindings for the included `kubernetes` cloud are readily provided by the aforementioned
[component][integration-component].

## Component configuration

### NetworkPolicies

#### `global.networkPolicies.enabled`

Default: `true`

If NetworkPolicies should be used at all.
Normally the Cloudogu EcoSystem uses NetworkPolicies, so this is necessary.
If for some reason, you do not want or need NetworkPolicies, they can be disabled here.

#### `global.networkPolicies.doguSelector`

Default:
```yaml
matchLabels:
  dogu.name: jenkins
```

The selector that should be used for NetworkPolicies to select the Dogu's Pod.
Normally, this does not have to be adjusted.

#### `global.networkPolicies.agentSelector`

Default:
```yaml
matchLabels:
  cloudogu.com/pod-kind: jenkins-ci
```

The selector that should be used for NetworkPolicies to select Jenkins agent Pods.
This has to match labels set for the agent Pods [in the Dogu's configuration](#agent_kubernetes_pod_labels).

### Namespaces

#### `namespaces.create`

Default: `true`

Whether to create the namespaces specified in [`namespaces.names`](#namespacesnames).

#### `namespaces.names`

Default: `[jenkins-ci]`

List of the namespaces to create resources (NetworkPolicies, RBAC, ...) for.

### RBAC

#### `serviceAccount.name`

Default: `jenkins`

The ServiceAccount to bind Roles to.
Since the ServiceAccount is provided by the Jenkins dogu, this usually does not have to be adjusted.

### Limit builds to certain nodes

Since resource-heavy builds might interfere with other workloads or compromise security,
you might want to limit your Jenkins builds to certain nodes with no other workloads.

It is possible to prevent other workloads from being scheduled on a node by tainting it:
```shell
kubectl taint nodes <node-name> reserved-node=jenkins-ci:NoSchedule
```
This has to be done before any workloads are scheduled.
If workloads are already scheduled on it, you might want to [drain it][drain].

We might also want to label our node to make it easier to give our pods an affinity for it later:
```shell
kubectl label node <node-name> reserved-node=jenkins-ci
```

Jenkins builds are already labeled with `cloudogu.com/pod-kind: jenkins-ci` by default, so it's easy to single them out.
Mutating these to only run on a specific node is usually accomplished with an admission-controller.
Now, we could implement our own but there are already multiple options readily available:

1. [PodNodeSelector][podnodeselector] is a Kubernetes built-in admission controller currently in an alpha stage.
   Enabling it allows to annotate a namespace for running pods on a specific node.
2. [Kyverno][kyverno] is a policy engine and admission controller that you might have already installed.
3. [Gatekeeper][gatekeeper] is an alternative to Kyverno.

Kyverno- and Gatekeeper-Policies are already part of the
[k8s-jenkins-agent-integration component][integration-component] and may be enabled or disabled individually.

#### `policies.kyverno.enabled`

Default: `true`

Whether the Kyverno ClusterPolicy to assign agent pods to specific nodes should be applied.

#### `policies.gatekeeper.enabled`

Default: `false`

Whether the Gatekeeper Assign policies to assign agent pods to specific nodes should be applied.

#### Pod Matching

##### `policies.matchPods.namespaces.enabled`

Default: `true`

Whether to match pods from the namespaces specified in [`namespaces.names`](#namespacesnames).

##### `policies.matchPods.labelSelector.enabled`

Default: `false`

Whether to match pods via their labels.

##### `policies.matchPods.labelSelector.matchLabels`

Default:
```yaml
cloudogu.com/pod-kind: jenkins-ci
```

List of labels of pods to match with the policy.

##### `policies.matchPods.namespaceSelector.enabled`

Default: `false`

Whether to match namespaces via their labels.

##### `policies.matchPods.namespaceSelector.matchLabels`

Default:
```yaml
kubernetes.io/metadata.name: jenkins-ci
```

Labels to match the namespaces with.

##### `policies.affinity`

Default:
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

Affinity to set in the agent pods.

##### `policies.tolerations`

Default:
```yaml
- key: reserved-node
  operator: Equal
  value: jenkins-ci
  effect: NoSchedule
```

Tolerations to set in the agent pods.


[integration-component]: https://github.com/cloudogu/k8s-jenkins-agent-integration/
[jenkins-kubernetes]: https://plugins.jenkins.io/kubernetes/#plugin-content-usage
[drain]: https://kubernetes.io/docs/tasks/administer-cluster/safely-drain-node/
[podnodeselector]: https://kubernetes.io/docs/reference/access-authn-authz/admission-controllers/#podnodeselector
[kyverno]: https://kyverno.io/
[gatekeeper]: https://open-policy-agent.github.io/gatekeeper/website/