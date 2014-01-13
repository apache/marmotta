Apache Marmotta Web Application
===============================

This package contains a binary web application of the Apache Marmotta Platform
that can be deployed in any Java Application Server (with Servlet API 2.5 or
higher). Apache Marmotta is tested on the following servers:
- Apache Tomcat 6.x and 7.x
- Jetty 6.x

However, most other servers should also work without problems.

The following sections give a short introduction how to deploy the Marmotta
Web Application in your server. More detailed instructions are available at:

http://marmotta.apache.org/installation.html


1. Requirements
---------------

The following minimum requirements need to be satisfied to run the Apache
Marmotta Web Application:

Hardware:
- Dual-Core CPU
- 1GB main memory
- 100MB hard disk

Software:
- Java JDK 6 or higher
- Java Application Server (Apache Tomcat 6.x/7.x or Jetty 6.x)
- Database (PostgreSQL or MySQL)

If no database is available, Apache Marmotta will use an embedded H2 database
for storing its data. This should only be used for testing and demonstration
purposes and is strongly discouraged for production deployments.


2. Deployment
-------------

To deploy the Apache Marmotta Web Application in your application server,
follow the following steps:

1.  download and install the application server and the database you intend
    to use (PostgreSQL or MySQL)
2a. set the environment variable MARMOTTA_HOME to the directory where
    Apache Marmotta should store its persistent runtime data; this can also
    be done permanently in the startup scripts of the application server
2b. alternatively, configure a context definition for your web application
    that sets the init parameter "marmotta.home" to the directory where
    Apache Marmotta should store its persistent runtime data (see below)
3.  configure the application server with sufficient main memory
    (1GB heap space, 256MB permgen space)
4.  copy marmotta.war to the deployment root of your application server
    (Tomcat and Jetty: the webapps/ subdirectory)
5.  start up your application server using the startup script or similar
    provided by the distribution

Examples:

Environment variables for home directory and application server memory:

> export MARMOTTA_HOME=<PATH-TO-HOME>
> export JAVA_OPTS="-Xmx1024m -XX:PermSize=128m -XX:MaxPermSize=256m"

Tomcat context definition for handing over the home directory init parameter
(in conf/Catalina/localhost/marmotta.xml):

<Context docBase="/path/to/marmotta.war" unpackWAR="false" useNaming="true">
  <Parameter name="marmotta.home" value="/data/marmotta" override="false"/>
</Context>


3. Configuration
----------------

3.1 Access Admin Interface

You can now access the Apache Marmotta Web Application through your browser,
e.g. by accessing the URL:

  http://localhost:8080/marmotta

Note that the host name and port you are using for the first access of your
installation decide on how Linked Data resources will be created later. If
you plan a production deployment, you should therefore directly deploy on
the server you are going to use and access it via the host name it will
have in the future.


3.2 Change Database Configuration

In case you are not going to use the embedded H2 database, the first step you
should do is to configure a different database. The database can be changed
in the "configuration" section of the Marmotta Core module (scroll down to
the bottom of the configuration page).

Apache Marmotta can only connect to an existing database with an existing
user and password. It will create the necessary database tables the first
time it accesses the database, in case they do not exist yet. On first start
the database should therefore be empty, or otherwise you will have old data
in your installation.

Special note for MySQL: for legal reasons, we cannot distribute the MySQL
Java drivers that are needed to access a MySQL database. In case you aim
to use MySQL, please download the MySQL JDBC connector manually and place
it either in the application server lib/ directory or in the WEB-INF/lib
directory of Apache Marmotta.
