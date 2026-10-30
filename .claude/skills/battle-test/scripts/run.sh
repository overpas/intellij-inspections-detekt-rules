#!/bin/zsh
set -u
here=${0:A:h}
work=${1:A}
project=$2
log=$3
shift 3

export JAVA_HOME=${BATTLE_JAVA_HOME:-$(find $HOME/.gradle/jdks -maxdepth 4 -type d -path '*-21-*' -name Home | head -1)}
export ANDROID_HOME=${ANDROID_HOME:-$HOME/Library/Android/sdk}
export BATTLE_JARS=$work/jars
export BATTLE_OVERLAY=$work/overlay.yml

mkdir -p $work/logs
[[ -f $BATTLE_OVERLAY ]] || printf 'config:\n  validation: false\n' > $BATTLE_OVERLAY

cd $project && ./gradlew \
  --init-script $here/battle.init.gradle \
  --no-configuration-cache \
  -Dorg.gradle.unsafe.isolated-projects=false \
  --continue "$@" > $work/logs/$log.log 2>&1
echo "exit=$? log=$work/logs/$log.log"
