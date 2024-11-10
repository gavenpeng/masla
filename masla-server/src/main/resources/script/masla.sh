#! /usr/bin/env bash
bin=`dirname "$0"`
bin=`cd "$bin">/dev/null; pwd`

#JAVA_HOME=/usr/local/jdk7
JAVA_HOME=/usr/local/jdk1.8.0_65

MASLA_HOME=$bin

JAVA=$JAVA_HOME/bin/java

#JAVA_HEAP_MAX=-Xmx4g
#JAVA_HEAP_MIN=-Xms4g

_LOG_DIR=/var/log/masla

# override default settings for this command, if applicable
if [ -f "${MASLA_HOME}/masla-env" ]; then
  . "${MASLA_HOME}/malsa-env"
fi

JAVA_HEAP_MAX=2g
JAVA_HEAP_MIN=2g

# check envvars which might override default args
if [ "$MASLA_HEAPSIZE" != "" ]; then
  SUFFIX="m"
  if [ "${MASLA_HEAPSIZE: -1}" == "m" ] || [ "${MASLA_HEAPSIZE: -1}" == "M" ]; then
    SUFFIX=""
  fi
  if [ "${MASLA_HEAPSIZE: -1}" == "g" ] || [ "${MASLA_HEAPSIZE: -1}" == "G" ]; then
    SUFFIX=""
  fi
  #echo "run with heapsize $MASLA_HEAPSIZE"
  JAVA_HEAP_MAX="-Xmx""$MASLA_HEAPSIZE""$SUFFIX"
  JAVA_HEAP_MIN="-Xms""$MASLA_HEAPSIZE""$SUFFIX"
  #echo $JAVA_HEAP_MAX
fi


MASLA_OPTS="$MASLA_OPTS $JAVA_HEAP_MIN $JAVA_HEAP_MAX -XX:PermSize=256M -XX:MaxPermSize=256M -Xmn1500M -Xss256k -XX:SurvivorRatio=2 -XX:MaxTenuringThreshold=6 -XX:+UseCondCardMark"
MASLA_OPTS="$MASLA_OPTS -XX:ParallelCMSThreads=4 -XX:+UseConcMarkSweepGC  -XX:+CMSParallelRemarkEnabled -XX:+CMSClassUnloadingEnabled -XX:CMSFullGCsBeforeCompaction=2 -XX:+CMSScavengeBeforeRemark -XX:+HeapDumpOnOutOfMemoryError"
MASLA_OPTS="$MASLA_OPTS -XX:+UseParNewGC -XX:+ExplicitGCInvokesConcurrent -XX:CMSInitiatingOccupancyFraction=70 -XX:SoftRefLRUPolicyMSPerMB=0"
MASLA_OPTS="$MASLA_OPTS -XX:+UseCMSInitiatingOccupancyOnly -XX:+UseCMSCompactAtFullCollection -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
MASLA_OPTS="$MASLA_OPTS -Xloggc:$MASLA_LOG_DIR/masla-server-gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=1 -XX:GCLogFileSize=512M"

#JMX_PORT=3998

#LOCAL_IP=`ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}'`



#disable jmx
#MASLA_JMX_BASE="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
#MASLA_OPTS="${MASLA_OPTS} $MASLA_JMX_BASE -Dcom.sun.management.jmxremote.port=${JMX_PORT} -XX:+HeapDumpOnOutOfMemoryError"
#MASLA_OPTS="${MASLA_OPTS} -Djava.rmi.server.hostname=$LOCAL_IP -DbrokerIp=$LOCAL_IP"

#MASLA_OPTS="$MASLA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8170"

CLASSPATH=$JAVA_HOME/lib/tools.jar
CLASSPATH=${CLASSPATH}:$MASLA_HOME/conf
CLASSPATH=${CLASSPATH}:$MASLA_HOME/ssl
for f in $MASLA_HOME/libs/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

CLASS='com.msw.masla.MaslaServer'

exec "$JAVA" -XX:OnOutOfMemoryError="kill -9 %p" $MASLA_OPTS -classpath "$CLASSPATH" $CLASS "$@"

