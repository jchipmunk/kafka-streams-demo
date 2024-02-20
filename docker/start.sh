#!/usr/bin/env bash

# Exit immediately if a *pipeline* returns a non-zero status. (Add -x for command tracing)
set -e

DEFAULT_JAVA_OPTS="$DEFAULT_JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$APP_HOME/dumps/"
DEFAULT_JAVA_OPTS="$DEFAULT_JAVA_OPTS -XX:+ExitOnOutOfMemoryError"

if [[ -z "$LOG4J_OPTS" ]]; then
  LOG4J_OPTS="-Dlog4j.configuration=file:$APP_HOME/config/log4j.properties"
fi

if [[ -z "$HEAP_OPTS" ]]; then
  HEAP_OPTS="-Xms256m -Xmx2G"
fi

: ${DEBUG:="false"}
if [[ "$DEBUG" == true ]]; then
  echo "JMX options: $JMX_OPTS"
  echo "Log4j options: $LOG4J_OPTS"
  echo "Heap options: $HEAP_OPTS"
  echo "Java options: $JAVA_OPTS"
fi

exec java ${DEFAULT_JAVA_OPTS} ${JMX_OPTS} ${JAVA_OPTS} -jar "$APP_HOME/kafka-streams-demo.jar" "$@"
