Apache Marmotta Installer
=========================

This package contains the binary installation wizard for Apache Marmotta. It
allows you to install a complete instance of Apache Marmotta including an
Apache Tomcat application server, so you are ready-to-run in a few minutes.

1. Installing
-------------

To run the installer, simply execute the following command on the command
line:

java -jar marmotta-installer-${project.version}.jar

Alternatively, on GUI-based systems you can usually double-click on the
jar file to start the installation procedure.

The installer will guide you through the most important steps of the
installation:

1. acknowledge license agreement (see also LICENSE.txt)
2. select modules to install; except Marmotta Core, all modules are optional;
   a brief description is given by the installer
3. select installation directory; Apache Marmotta will not change any files
   outside this directory; please make sure the directory is empty before
   installation, as updating existing installations currently does not work
4. finish installation

2. Running
----------

To run the installed Apache Marmotta system, there are different options
depending on the operating system you are using:
- on all operating systems, you can use the startup.sh and shutdown.sh
  shell scripts in the Tomcat bin directory to start/stop Marmotta
- on Windows, the installer will add entries to the Start menu that
  gives you easier access to starting and stopping
- on MacOS X, the installer creates two applications for starting
  and stopping inside the installation directory

When Apache Marmotta is started for the first time, it will ask you which
network interface and IP address you would like to use as primary network
address. This step is very important, because it determines the base URI
for all Linked Data resources managed by the system.
- for demonstration purposes, you will probably select "localhost"
- for server purposes, you should select once of the publicly available
  network interfaces, or otherwise the resources will not be created
  correctly

Once you selected the network interface, Apache Marmotta will automatically
open a browser window pointing to your Marmotta server.

3. Stopping
-----------

Apache Marmotta will install a systray icon on operating systems that support
this feature (look out for a small marmot). Through the systray menu you have
quick access to important functionality like opening a browser to the Marmotta
server and shutting down the system.

