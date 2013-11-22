Apache Marmotta
===============

This code repository contains the source-code for Apache Marmotta


1. Building the Source Distribution
-----------------------------------

Apache Marmotta uses Maven to build, test, and install the software. A basic
build requires downloading and installing Maven and then running:

mvn clean install

This will compile, package and test all Apache Marmotta modules and install
it in your local Maven repository. In case you want to build your own
projects based on some of the libraries provided by Apache Marmotta, this
usually suffices.

The default loglevel for most unit and integration tests executed during the
build is INFO. To change the loglevel for either more or less output, you
can pass the loglevel as system property:

mvn clean install -Droot-level=TRACE|DEBUG|INFO|WARN|ERROR

Note that some of the integration tests start up parts of the Marmotta
platform during execution. The log level for these tests cannot be
changed, as Marmotta is taking over the log configuration in these
cases.

2. Building, Running and Deploying the Wep Application
------------------------------------------------------

Apache Marmotta also includes a default configuration for building a Java
Web Application that can be deployed in any Java Application Server. To
build the web application, first run

mvn clean install

in the project root. Then change to the launchers/marmotta-webapp directory
and run

mvn package

This will create a marmotta.war file in the target/ directory. You can
deploy this archive to any Java Application Server by copying it into
its deployment directory. More details can be found on:

http://marmotta.apache.org/installation.html


Alternatively, you can directly startup the Apache Marmotta Web Application
from Maven with a default configuration suitable for development. To try this
out, run

mvn tomcat7:run

wait until the system is started up and point your browser to
http://localhost:8080

When developing it is sometimes useful to always start with a clean confi-
guration of the system. Therefore, you can also start up the web application
as follows:

mvn clean tomcat7:run -Pcleanall

This command will remove any existing configuration directory before startup.



3. Building the Standalone Installer
------------------------------------

The build environment also offers to automatically build an installer package
that guides users through the installation with an easy-to-use installation
wizard. The installer is based on izPack and dynamically assembled when
building the package. To build the installer, first run

mvn clean install

in the project root. Then change to the launchers/marmotta-installer directory
and run

mvn package -Pinstaller

The build process will automatically create an appropriate installer confi-
guration from the Maven dependencies through the Apache Marmotta refpack
build plugin.

The installer can then be tried out by running

java -jar target/marmotta-installer-x.x.x.jar


4. Building with a Clean Repository
-----------------------------------

Sometimes it is useful to check if the build runs properly on a clean local
repository, i.e. simulate what happens if a user downloads the source and
runs the build. This can be achieved by running Maven as follows:

mvn clean install -Dmaven.repo.local=/tmp/testrepo

The command changes the local repository location from ~/.m2 to the
directory passed as argument


5. Simulating a Release
-----------------------

To test the release build without actually deploying the software, we have
created a profile that will deploy to the local file system. You can
simulate the release by running

mvn clean deploy -Pdist-local,marmotta-release,installer

Please keep in mind that building a release involves creating digital
signatures, so you will need a GPG key and a proper GPG configuration to run
this task.
