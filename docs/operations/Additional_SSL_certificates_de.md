# Zusätzliche SSL-Zertifikate

## Einführung

SSL-Zertifikate helfen bei der Absicherung von verschlüsselter Kommunikation zwischen unterschiedlichen Diensten und Servern. Wenn ein Zertifikat nicht durch die Zertifikate der _Root Certificate Authority (CA)_ überprüft werden kann, dann kann dies ein Hinweis auf einen Angriff bedeuten. Eine zweite Möglichkeit für eine gescheiterte Zertifikatsüberprüfung könnte ein selbst-signiertes Zertifikat darstellen (etwa das einer zweiten CES-Instanz).

In solchen Fällen verweigern Werkzeuge wie `git` oder `curl` den Dienst, da eine sichere, verschlüsselte Kommunikation zu den gewünschten Diensten nicht möglich ist. Für dieses Problem bestehen häufig mehrere Lösungsansätze.

Einer dieser Lösungsansätze besteht darin, die Überprüfung von SSL-Zertifikaten gänzlich auszuschalten. Dieser Ansatz hat den starken Nachteil, dass häufig gar keine Zertifikate mehr überprüft werden. Böswillige Angriffe wäre so nicht mehr erkennbar.

Ein zweiter Lösungsansatz besteht darin, die betroffenen Zertifikate als sicher anzuerkennen. Die Dienste und Werkzeuge können dann ihre reguläre Kommunikation absichern, sodass Angriffe von außen sinnvoll wahrgenommen werden können.

Dieses Dokument beschreibt den zweiten Ansatz.

## Import zusätzlicher Zertifikate

Innerhalb des Dogus besteht für die reguläre Ausführung von Jenkins die Möglichkeit, zusätzliche Zertifikate aus dem `etcd` hinzuzufügen.

Es gibt einige Bedingungen, um erfolgreich die Zertifikate zu finden und im Dogu anzuwenden.

1. Die Zertifikate müssen im PEM-Format vorliegen.
2. Die Zertifikate müssen im `etcd` unterhalb von `/config/_global/certificate/additional/` vorliegen
   - Der Schlüsselname (auch _Alias_ genannt) dient der Adressierung und dogu-internen Ablage und sollte keine Leerzeichen enthalten.
   - Sinnvoll wäre hier die FQDN des Dienstes (etwa: `dienst.example.com`), damit später ein Zertifikat leichter wieder entfernt werden kann
   - Ein Schlüssel kann mehr als ein Zertifikat zu einem Dienst besitzen. Zertifikate im PEM-Format haben textuelle Markierungen, anhand dessen die Zertifikate wieder auseinander getrennt werden können.  
3. Der Schlüsselname, unter dem das Zertifikat abgelegt wurde, muss im `etcd` unter `/config/_global/certificate/additional/toc` abgelegt werden.
   - Zertifikate unterschiedlicher Dienste müssen mit einem einzelnen Leerzeichen getrennt werden


Beispielkonfiguration im `etcd`:

```
config/
└─ _global/
   └─ certificate/
      └─ additional/
         ├─ toc          -> "example.com localserver2 server3"
         ├─ example.com  -> "-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----"
         ├─ localserver2 -> "-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----"
         └─ server3      -> "-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----"
```

## Ablage zusätzlicher Zertifikate im Jenkins-Dogu

Nach einem erfolgreichen Import liegen die Zertifikate in diesen Orten vor:

Name | Dateipfad | abgelegte Zertifikate 
-----|-----------|----------------------
CA-Zertifikatssammlung | `/var/lib/jenkins/ca-certificates.crt` | Sowohl die Standard-CA-Zertifikate des Betriebssystems aus `/etc/ssl/certs/` als auch die zusätzlichen Zertifikate
Java Truststore | `/var/lib/jenkins/truststore.jks` | Sowohl die Standard-CA-Zertifikate des Betriebssystems aus `/etc/ssl/certs/` als auch die zusätzlichen Zertifikate
Subversion-Einzelzertifikate | `/var/lib/jenkins/.subversion/cert-$alias-00` | zusätzliche Zertifikate; bei mehreren Zertifikaten je Alias existieren mehrere Dateien, die sich im Counter-Suffix unterscheiden
