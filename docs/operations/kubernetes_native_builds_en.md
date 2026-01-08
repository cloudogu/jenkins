# Kubernetes-native builds

## Prerequisites
When a kubernetes environment is detected, the kubernetes plugin is automatically installed and configured.
However, to use agents, the Jenkins agent port has to be set to a static value of 50000.
This can easily be done by setting the dogu config key `tcp_inbound_agent_port` to `static`.

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

## Limit builds to certain nodes

Since resource-heavy builds might interfere with other workloads or compromise security,
you might want to limit your Jenkins builds to certain nodes with no other workloads.

It is possible to prevent other workloads from being scheduled on a node by tainting it:
```shell
kubectl taint nodes worker-2 reserved-node=jenkins-build:NoSchedule
```
This has to be done before any workloads are scheduled.
If workloads are already scheduled on it, you might want to [drain it][drain].

We might also want to label our node to make it easier to give our pods an affinity for it later:
```shell
kubectl label node worker-2 reserved-node=jenkins-build
```

Jenkins builds are already labeled with `cloudogu.com/pod-kind: jenkins-build` by default, so it's easy to single them out.
Mutating these to only run on a specific node is usually accomplished with an admission-controller.
Now, we could implement our own but there are already multiple options readily available:

1. [PodNodeSelector][podnodeselector] is a Kubernetes built-in admission controller currently in an alpha stage.
   Enabling it allows to annotate a namespace for running pods on a specific node.
2. [Kyverno][kyverno] is a policy engine and admission controller that you might have already installed.
   A policy like the one we want can easily be implemented, [see this example][kyverno-example].
3. [Gatekeeper][gatekeeper] is an alternative to Kyverno.
   For how to implement the policies in gatekeeper, [see this example][gatekeeper-example].

[jenkins-kubernetes]: https://plugins.jenkins.io/kubernetes/#plugin-content-usage
[drain]: https://kubernetes.io/docs/tasks/administer-cluster/safely-drain-node/
[podnodeselector]: https://kubernetes.io/docs/reference/access-authn-authz/admission-controllers/#podnodeselector
[kyverno]: https://kyverno.io/
[kyverno-example]: https://github.com/cloudogu/jenkins/blob/develop/docs/operations/kyverno.yaml
[gatekeeper]: https://open-policy-agent.github.io/gatekeeper/website/
[gatekeeper-example]: https://github.com/cloudogu/jenkins/blob/develop/docs/operations/gatekeeper.yaml