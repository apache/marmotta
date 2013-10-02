#!/bin/bash
# simple shell script to get the git commit hash of the commit with the tag 
# given as argument; used when generating the vote email for Apache Marmotta
git show-ref -d -s $1 | grep '{}' | cut -f 1 -d ' '
