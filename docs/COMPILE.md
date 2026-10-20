# Building FastCursor from Source

## Voraussetzungen

- **JDK 21+ (LTS)** — [Adoptium Temurin](https://adoptium.net/) oder Oracle JDK
- **Maven 3.9+** — [Apache Maven](https://maven.apache.org/download.cgi)
- **Betriebssystem**: Windows 10 (Build 19041+) oder Windows 11 (x64)

> [!NOTE]
> FastCursor nutzt das moderne **Java Foreign Function & Memory (FFM) API** (Project Panama). Es ist **kein** C/C++ Compiler (wie MSVC oder GCC) erforderlich! Es werden zur Laufzeit keine externen C-DLLs kompiliert oder benötigt.

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

## Java 21+ FFM Preview Flags

Beim Kompilieren und Ausführen von Projekten, die native FFM-Aufrufe verwenden, müssen die entsprechenden Preview- und Native-Access-Argumente an die JVM übergeben werden:

```bash
# Beim Starten einer Anwendung:
java --enable-preview --enable-native-access=ALL-UNNAMED -cp target/FastCursor-0.1.0.jar YourApp
```

In Maven übernimmt dies automatisch die Konfiguration des `maven-compiler-plugin` und `maven-surefire-plugin` in der `pom.xml`.
