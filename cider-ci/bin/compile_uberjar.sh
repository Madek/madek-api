#!/usr/bin/env bash
set -eux
export JAVA_HOME=${OPENJDK8_HOME}
export PATH=${JAVA_HOME}/bin:${PATH}
rm -rf target
DIGEST=$(git log -n 1 HEAD --pretty=%T)
LEIN_UBERJAR_FILE="/tmp/madek_api_${DIGEST}.jar"
if [ -f "${LEIN_UBERJAR_FILE}" ];then
  echo "${LEIN_UBERJAR_FILE} exists"
else
  lein uberjar
  mv "target/madek_api.jar" "${LEIN_UBERJAR_FILE}"
fi
mkdir -p target
ln -s "$LEIN_UBERJAR_FILE" "target/madek_api.jar"
