#!/bin/bash
curl -i -H "Content-Type: text/plain" -X POST --data-binary @skos.kwrl  http://localhost:8080/LMF/reasoner/program/skos