#!/bin/bash

ME="$(readlink -f "$0")"
ROOT="$(readlink -f "$(dirname "$ME")/..")"

find "$ROOT" -name pom.xml -not -path '*/target/*' | while read pom; do
	rel="$(xpath -q -e '//plugin/configuration/relativePath/text()' "$pom")"
	[ -z "$rel" ] && continue;
	abs="$(readlink -f "$(dirname "$pom")/$rel")"
	[ "$ROOT" = "$abs" ] || echo "Invalid relativePath for jrebel in $pom: $rel resolves to $abs (should be $ROOT)"
done
