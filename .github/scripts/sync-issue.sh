#!/usr/bin/env bash
set -euo pipefail

report="${1:?Usage: $0 <report file>}"
label="upstream-sync"
title="IntelliJ Kotlin inspections changed upstream"

gh label create "$label" --color 5319e7 --description "Upstream changes of the IntelliJ inspections" --force
issue=$(gh issue list --label "$label" --state open --json number --jq '.[0].number // empty')

if [ ! -s "$report" ]; then
  if [ -n "$issue" ]; then
    gh issue close "$issue" --comment "The catalog matches upstream again."
  fi
  exit 0
fi

if [ -z "$issue" ]; then
  gh issue create --title "$title" --label "$label" --body-file "$report"
  exit 0
fi

previous=$(gh issue view "$issue" --json body --jq .body | tail -n +2)
gh issue edit "$issue" --body-file "$report"
if [ "$previous" != "$(tail -n +2 "$report")" ]; then
  gh issue comment "$issue" --body "The report has new upstream changes."
fi
