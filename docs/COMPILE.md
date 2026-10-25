# Building FastCursor from Source

## Voraussetzungen

- **JDK 21+ (LTS)** — [Adoptium Temurin](https://adoptium.net/) oder Oracle JDK
- **Maven 3.9+** — [Apache Maven](https://maven.apache.org/download.cgi)
- **Betriebssystem**: Windows 10 (Build 19041+) oder Windows 11 (x64)

> [!NOTE]
> FastCore extrahiert und bindet fastcursor.dll automatisch ein.

---

## Quick Build

```bash
# Projekt bereinigen, Tests ausführen und JAR bauen
mvn clean package
```

Die fertige JAR-Datei liegt anschließend unter `target/FastCursor-0.1.0.jar`.

---

## Build Befehle im Überblick

| Befehl | Zweck |
|:---|:---|
| `compile.bat` | Schneller Direkt-Build über den Windows Launcher |
| `mvn test` | Führt die Unit-Tests (`FastCursorTest`) aus |
| `mvn clean package` | Baut das distributionsfertige JAR-Paket |
| `run-demo.bat` | Startet die interaktive 3-Sekunden Cursor-Cycle Demonstration |

---

## Java 21+ Standard Runtime

FastCursor benötigt **keine** --enable-preview Flags und kann direkt auf jeder Standard-JVM ab Java 21 ausgeführt werden:

```bash
# Beim Starten einer Anwendung:
java -cp target/FastCursor-0.1.2.jar YourApp
```

In Maven übernimmt dies automatisch die Konfiguration des `maven-compiler-plugin` und `maven-surefire-plugin` in der `pom.xml`.
