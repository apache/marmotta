#!/usr/bin/env bash

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License

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

