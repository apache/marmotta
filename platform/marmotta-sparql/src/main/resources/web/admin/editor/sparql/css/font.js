/*
 * Copyright (c) 2011 TSO Ltd
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function waitForStyles() {
  for (var i = 0; i < document.styleSheets.length; i++)
    if (/googleapis/.test(document.styleSheets[i].href))
      return document.body.className += " droid";
  setTimeout(waitForStyles, 100);
}
setTimeout(function() {
  if (/AppleWebKit/.test(navigator.userAgent) && /iP[oa]d|iPhone/.test(navigator.userAgent)) return;
  var link = document.createElement("LINK");
  link.type = "text/css";
  link.rel = "stylesheet";
  link.href = "http://fonts.googleapis.com/css?family=Droid+Sans|Droid+Sans:bold";
  document.documentElement.getElementsByTagName("HEAD")[0].appendChild(link);
  waitForStyles();
}, 10);
