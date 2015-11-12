#!/usr/bin/env bash

# SIGTERM-handler
sigterm_handler() {
  service tomcat7 stop
  service postgresql stop
  exit 143; # 128 + 15 -- SIGTERM
}

# setup handlers on callback
# kill the last background process (tail) and execute the custom handler
trap 'kill ${!}; sigterm_handler' SIGTERM

# run application
service postgresql start
service tomcat7 start

# wait indefinetely
while true
do
  tail -f /var/log/tomcat7/catalina.out & wait ${!}
done

