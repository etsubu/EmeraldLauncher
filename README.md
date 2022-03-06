# Emerald Launcher
Emerald Launcher is custom written java Minecraft Launcher. It is lightweight and utilizes minimal amount \
of dependencies and uses Java Swing library to provide simple UI for launching the game. Due to the \
small size of the project it is easy to modify and extend to fit for different requirements.

The launcher has been tested to work on Windows and Debian based linux platforms. MacOS is not supported \
at the moment as I don't have development environment available for it, any PR to add support for Mac is \
more than welcome.

Note that currently online-mode servers are not supported as user account password is not required \
This is to be added in future to support playing on online-mode servers.

## How to build
The launcher requires Java 17 JDK to build which is the latest LTS version at the time of writing this.

### Windows
You can get Java 17 for windows from here https://adoptium.net/ \
Select OpenJDK 17 LTS or later version.

### Debian
For debian based OS you can install Java 17 with
```
sudo apt install openjdk-17-jdk-headless -y
```
### Build
To build the launcher
```
git clone git@github.com:etsubu/EmeraldLauncher.git
cd EmeraldLauncher
gradlew shadowJar
```
The built launcher can be found in build/libs

## TODO
* Detect JVM and host OS proxy settings and use those with the HTTP client
* Add Mac OS support
* Add support for user account authentication to support online-mode servers