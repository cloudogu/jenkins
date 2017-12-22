import jenkins.model.*;

// deprecated agent protocols
def deprecated = [ "JNLP-connect", "JNLP2-connect", "JNLP3-connect"];

def jenkins = Jenkins.instance;

Set<String> agentProtocols = new HashSet<>();
def changed = false;
for (String protocol : jenkins.getAgentProtocols()) {
  if (!deprecated.contains(protocol)) {
    agentProtocols.add(protocol);
  } else {
    changed = true;
  }
}

if (changed) {
  println("change agent configuration");
  
  jenkins.setAgentProtocols(agentProtocols);
  jenkins.save();
}