# TCP Agent Listener Port

Jenkins can expose a TCP port that allows inbound agents to connect to it. It can be enabled, disabled, and configured in Manage Jenkins -> Security -> Agents -> TCP port for inbound agents.

The Jenkins dogu explicitly exposes port 50000 for this purpose to the host system. If you want to activate inbound agent connections to Jenkins, you have to set the configuration to "Fixed" and specify port "50000" in the configuration section mentioned above.

If you enable inbound agent connections, you should also consider activating security measures to prevent unwanted connection attempts. You could, for example, restrict connection attempts to port 50000 to specific Jenkins worker IPs via a firewall.

For further information on this topic see https://www.jenkins.io/doc/book/security/services/
