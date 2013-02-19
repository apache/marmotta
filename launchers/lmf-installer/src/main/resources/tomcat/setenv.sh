#!/bin/sh
export LMF_HOME="%{INSTALL_PATH}/lmf-home"
export JAVA_OPTS="$JAVA_OPTS -splash:%{INSTALL_PATH}/apache-tomcat-%{TOMCAT_VERSION}/icons/splashscreen.png -Xmx1024m -Dehcache.disk.store.dir=$LMF_HOME/cache -Dfile.encoding=UTF-8 -XX:MaxPermSize=256m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled"
export JAVA_HOME="%{JAVA_HOME}"
