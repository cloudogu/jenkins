# TCP Agent Listener Port

Jenkins kann einen TCP-Port freigeben, über den sich eingehende Agents mit ihm verbinden können. Er kann unter Jenkins verwalten -> Security -> Agents -> TCP port for inbound agents konfiguriert werden.

Das Jenkins-Dogu gibt zu diesem Zweck explizit den Port 50000 für das Hostsystem frei. Wenn Sie eingehende Agentverbindungen zu Jenkins aktivieren möchten, müssen Sie im oben genannten Konfigurationsabschnitt die Konfiguration auf "Statisch" setzen und den Port "50000" angeben.

Wenn Sie eingehende Agentverbindungen aktivieren, sollten Sie auch die Aktivierung von Sicherheitsmaßnahmen in Betracht ziehen, um unerwünschte Verbindungsversuche zu verhindern. Sie könnten zum Beispiel die Verbindungsversuche zu Port 50000 über eine Firewall auf bestimmte Jenkins Worker-IPs beschränken.

Weitere Informationen zu diesem Thema finden Sie unter https://www.jenkins.io/doc/book/security/services/.
