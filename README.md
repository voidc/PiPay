πPay (PiPay)
============

# Keystore
Password : `qk2mxz3F4FeH`

# Spezifikation
Vorschläge und Änderungen erwünscht. :D

## Szenen
Folgende Szenen sind für die erste Version geplant:

#### Main (Haupmenü)
Diese Szene bietet Zugriff auf die verschiedenen Grundfunktionen:
  - Geld senden (Bezahlen)
  - Geld empfangen (Geld verlangen) -> *SendInit*
  - Transaktionsprotokoll anzeigen
  - Einstellungen ändern
  - Minigames (Losen)
  - Admin Modus aktivieren

Außerdem wird der aktuelle Kontostand des Nutzers angezeigt.

#### Settings (Einstellungen)
Diese Szene wird angezeit wenn der Nutzer die App das erste Mal verwendet.
Sie ermöglicht den eigenen Namen festzulegen, der im Transaktionsprotokoll anderer angezeigt wird.
Außerdem lässt sich eine PIN konfigurieren, die zum Bezahlen eingegeben werden muss.
Auch die passwortgeschützte Aktivierung des Admin Modus ist möglich.
Alle Einstellungen sollen auch später geändert werden können.

#### SendInit (Bezahlen)
In dieser Szene wird der Nutzer dazu aufgefordert den QR-Code des Empfängers zu scannen.
Ist der Scan erfolgreich werden Betrag und Name des Empfängers angezeigt.
Klickt der Nutzer nun auf den "Bestätigen" Button, erscheint ein Dialog in dem die PIN eingegeben werden muss.
Wenn diese korrekt ist, wird der Betrag abgebucht, die Transaktion im Protokoll gespeichert und die *SendConfirm* Szene angezeigt.
Sollte der Nutzer den geforderten Betrag nicht bezahlen können ist der "Bestätigen" Button deaktiviert und ein Warnhinweis wird angezeigt.

#### SendConfirm (Zahlung Bestätigen)
Zur Bestätigung der erfolgreichen Abbuchung wird ein QR-Code mit folgenden Informationen generiert:
  - Bezahlter Betrag
  - Transaktions ID
  - Name des Senders

Ein "Fertig" Button erlaubt den Nutzer ins Hauptmenü zurückzukehren.
Zur Sicherheit sollte der Nutzer darauf hingewiesen werden, dass zuvor der Bestätigungscode vom Empfänger gescannt werden muss.

#### ReceiveInit (Empfangen)
In dieser Szene kann der anzufordernde Betrag eingestellt werden.
Bei jeder Eingabe wird ein QR-Code generiert, der folgende Informationen enthält:
  - Geforderter Betrag
  - zufallsgenerierte Transaktions ID
  - Name des Empfängers

Ein "Weiter" Button erlaubt den Nutzer zur *ReceiveConfirm* Szene fortzufahren.
Zur Sicherheit sollte der Nutzer darauf hingewiesen werden, dass zuvor der QR-Code vom Sender gescannt werden muss.

#### ReceiveConfirm (Bestätigen)
In dieser Szene wird der Nutzer dazu aufgefordert den Bestätigungscode des Empfängers zu scannen.
Es wird überprüft, ob die Transaktions ID im QR-Code mit der zuvor generierten übereinstimmt und, ob der Betrag korrekt ist.
Stimmt alles, wird der Bertag auf das Konto des Empfängers zugebucht und die Transaktion im Transaktionsprotokoll gespeichert.
Außerdem erscheint eine Erfolgsmeldung und der Nutzer kann ins Hauptmenü zurückkehren.

## Admin Modus
Im Admin Modus wird der Kontostand beim Senden oder Empfangen nicht geändert.
Im Hauptmenü wird statt des Kontostandes ein "∞" angezeigt.