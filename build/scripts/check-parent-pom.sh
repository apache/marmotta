#!/bin/bash
ME="$(readlink -f "$0")"
ROOT="$(readlink -f "$(dirname "$ME")/../..")"

find "$ROOT" -name pom.xml -not -path '*/target/*' | while read pom; do
	rel="$(xpath -q -e '/project/parent/relativePath/text()' "$pom")"
	[ -z "$rel" ] && continue;
	abs="$(readlink -f "$(dirname "$pom")/${rel%/pom.xml}")"
	# check if we can find the parent pom
	[ -d "$abs" ] && [ -r "$abs/pom.xml" ] \
		|| { echo "Could not resolve relativePath to parent-pom in $pom: $rel resolves to $abs"; continue; }
	# check if it's the right parent
        p_aid="$(xpath -q -e '/project/parent/artifactId/text()' "$pom")"
        r_aid="$(xpath -q -e '/project/artifactId/text()' "$abs/pom.xml")"
        [ "$p_aid" = "$r_aid" ] \
		|| { echo "Wrong parent-pom referenced in $pom: expected $p_aid but found $r_aid"; continue; }

	#echo "$pom: OK"
done
