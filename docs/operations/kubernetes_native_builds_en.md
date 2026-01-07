# Kubernetes-native builds

## Limit builds to certain nodes

Since resource-heavy builds might interfere with other workloads or compromise security,
you might want to limit your Jenkins builds to certain nodes with no other workloads.

Jenkins builds are already limited to the `jenkins-build`-namespace by default, so it's easy to single them out.
Mutating any pods of that namespace to only run on a specific node is usually accomplished with an admission-controller.
Now, we could implement our own but there are already multiple options readily available:

1. [PodNodeSelector][podnodeselector] is a Kubernetes built-in admission controller currently in an alpha stage.
   Enabling it allows to annotate a namespace for running pods on a specific node.
2. [Kyverno][kyverno] is a policy engine and admission controller that you might have already installed.
   A policy like the one we want can easily be implemented, [see this example][kyverno-example].
3. [Gatekeeper][gatekeeper] is an alternative to Kyverno.
   See these examples on how to set [an affinity][gatekeeper-affinity] and [tolerations][gatekeeper-tolerations] for
   your pods in Gatekeeper.

[podnodeselector]: https://kubernetes.io/docs/reference/access-authn-authz/admission-controllers/#podnodeselector
[kyverno]: https://kyverno.io/
[kyverno-example]: https://github.com/cloudogu/jenkins/blob/develop/docs/operations/kyverno.yaml
[gatekeeper]: https://open-policy-agent.github.io/gatekeeper/website/
[gatekeeper-affinity]: https://github.com/cloudogu/jenkins/blob/develop/docs/operations/gatekeeper_affinity.yaml
[gatekeeper-tolerations]: https://github.com/cloudogu/jenkins/blob/develop/docs/operations/gatekeeper_tolerations.yaml