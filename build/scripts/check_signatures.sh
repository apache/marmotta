#!/bin/bash -e
#
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
#
##############################################################################
#
# Usage: ./check_signatures.sh <RELEASE_DIR>
#
# Progress printed on STDOUT, result available via exit-code
#
# Exit-Codes:
#    0 - All fine, signatures and digests are valid and correct
#    1 - A Required file (.asc, .md5, .sha1) is missing
#    2 - Invalid pgp/gpg signature found (.asc)
#    3 - Incorrect md5-sum detected (.md5)
#    4 - Incorrect sha1-sum detected (.sha1)
#  255 - Wrong/Missing command parameter
#

# Check for arguments
[ -z $1 ] && { echo "USAGE: $0 <RELEASE_DIR>" >&2; exit 255; }
[ ! -d $1 ] && { echo "release-dir '$1' not found" >&2; exit 255; }

BASE="${1}"
#cd "$BASE"

KR=$(mktemp)
# make sure that the temp-keyring is removed on exit
trap "{ C=$?; rm -f ${KR} ${KR}~ ; exit $C; }" EXIT

gpg="gpg --primary-keyring $KR"
# If there is a KEYS file, import it into the temp keyring
[ -r "$BASE/KEYS" ] && { echo "Import KEYS into temporary keyring"; $gpg --import "$BASE/KEYS"; echo; }

# Look for all archives: *.zip, *.tar.gz, *.tgz
find "$BASE" -maxdepth 1 -type f -name "*.zip" -o -name "*.t*gz" | sort | while read f; do
	echo "Checking archive $(basename $f)..."

        # Check gpg/pgp signature
        if [ -f "${f}.asc" ]; then
           $gpg --verify "${f}.asc" &>/dev/null && echo "  - Signature: OK" || { echo "  - Signature: ERROR"; exit 2; }
        else
           echo "  - Signature: MISSING"; exit 1
        fi

        # Check md5sum
        if [ -f "${f}.md5" ]; then
           echo "$(cat ${f}.md5)  ${f}" | md5sum --check - &>/dev/null && echo "  - MD5: OK" || { echo "  - MD5: ERROR"; exit 3; }
        else
           echo "  - MD5: MISSING"; exit 1
        fi

        # Check sha1
        if [ -f "${f}.sha1" ]; then
           echo "$(cat ${f}.sha1)  ${f}" | sha1sum --check - &>/dev/null && echo "  - SHA1: OK" || { echo "  - SHA1: ERROR"; exit 4; }
        else
           echo "  - SHA1: MISSING"; exit 1
        fi
	echo
done
echo "All archives in $BASE have valid signatures and digests."
echo
