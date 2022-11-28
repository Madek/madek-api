#!/usr/bin/env bash
set -eux
cd ${API_DIR}
export JAVA_HOME=${OPENJDK11_HOME}
export PATH=${JAVA_HOME}/bin:${PATH}
rm -rf target

DIGEST=$(git ls-tree HEAD --\
  cider-ci.yml cider-ci Gemfile.lock src project.clj\
  | openssl dgst -sha1 | cut -d ' ' -f 2)

LEIN_UBERJAR_FILE="/tmp/madek_api_${DIGEST}.jar"
if [ -f "${LEIN_UBERJAR_FILE}" ];then
  echo "${LEIN_UBERJAR_FILE} exists"
else
  lein uberjar
  mv "target/api.jar" "${LEIN_UBERJAR_FILE}"
fi
mkdir -p target
ln -s "$LEIN_UBERJAR_FILE" "target/api.jar"
