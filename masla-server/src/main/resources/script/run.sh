#!/usr/bin/env bash
bin=`dirname "$0"`
bin=`cd "$bin">/dev/null; pwd`

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home

MASLA_HOME=$bin

JAVA=$JAVA_HOME/bin/java

MASLA_PID_DIR=$MASLA_HOME/pid
MASLA_LOG_DIR=/var/log/masla
#MASLA_LOG_DIR=/Users/pengrongxin/masla/logs
MASLA_LOG_PREFIX="masla-boot"

logout=$MASLA_LOG_DIR/$MASLA_LOG_PREFIX.out
loglog=$MASLA_LOG_DIR/$MASLA_LOG_PREFIX.log


#if [ "${MASLA_PID_DIR}" = "" ]; then
#  echo 'pid dir is null'
#  MASLA_PID_DIR=/tmp
#fi
if [ "$MASLA_IDENT_STRING" = "" ]; then
  export MASLA_IDENT_STRING="$USER"
fi

pid=$MASLA_PID_DIR/masla-$MASLA_IDENT_STRING.pid

if [ "$MASLA_NICENESS" = "" ]; then
    export MASLA_NICENESS=0
fi

mkdir -p "$MASLA_LOG_DIR"
mkdir -p "$MASLA_PID_DIR"

MASLA_OPTS="$MASLA_OPTS $JAVA_HEAP_MIN $JAVA_HEAP_MAX -XX:PermSize=256M -XX:MaxPermSize=256M -Xmn1500M -Xss256k -XX:SurvivorRatio=2 -XX:MaxTenuringThreshold=6 -XX:+UseCondCardMark"
MASLA_OPTS="$MASLA_OPTS -XX:ParallelCMSThreads=4 -XX:+UseConcMarkSweepGC  -XX:+CMSParallelRemarkEnabled -XX:+CMSClassUnloadingEnabled -XX:CMSFullGCsBeforeCompaction=2 -XX:+CMSScavengeBeforeRemark -XX:+HeapDumpOnOutOfMemoryError"
MASLA_OPTS="$MASLA_OPTS -XX:+UseParNewGC -XX:+ExplicitGCInvokesConcurrent -XX:CMSInitiatingOccupancyFraction=70 -XX:SoftRefLRUPolicyMSPerMB=0"
MASLA_OPTS="$MASLA_OPTS -XX:+UseCMSInitiatingOccupancyOnly -XX:+UseCMSCompactAtFullCollection -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
MASLA_OPTS="$MASLA_OPTS -Xloggc:$MASLA_LOG_DIR/masla-server-gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=1 -XX:GCLogFileSize=512M"


CLASSPATH=$JAVA_HOME/lib/tools.jar
CLASSPATH=${CLASSPATH}:$MASLA_HOME/conf
CLASSPATH=${CLASSPATH}:$MASLA_HOME/ssl


if [ "$1" = "start" ] ; then

  if [ -f $pid ]; then
    if kill -0 `cat $pid` > /dev/null 2>&1; then
      echo masla server running as process `cat $pid`.  Stop it first.
      exit 1
    fi
  fi

  echo Starting masla Server on `hostname`, logging to $logout
  echo "`date` Starting masla Server on `hostname`" >> $loglog
  echo "`ulimit -a`" >> $loglog 2>&1

  CLASS='com.msw.masla.MaslaServer'
  for f in $MASLA_HOME/libs/*.jar; do
    CLASSPATH=${CLASSPATH}:$f;
  done

  exec "$JAVA" -XX:OnOutOfMemoryError="kill -9 %p" $MASLA_OPTS -classpath "$CLASSPATH" $CLASS "$@"

  #nohup nice -n $MASLA_NICENESS sh "$MASLA_HOME"/masla.sh "$@" > "$logout" 2>&1 < /dev/null &
  echo $! > $pid

elif [ "$1" = "stop" ]; then
 if [ -f $pid ]; then
   # kill -0 == see if the PID exists 
   if kill -0 `cat $pid` > /dev/null 2>&1; then
      echo "Stopping masla Server....."
      echo "`date` Terminating $command" >> $loglog
      kill `cat $pid` > /dev/null 2>&1
      while kill -0 `cat $pid` > /dev/null 2>&1; do
        echo  "."
        sleep 1;
      done
      rm $pid
      echo
   else
     retval=$?
     echo no $command to stop because kill -0 of pid `cat $pid` failed with status $retval
   fi
 else
   echo no $command to stop because no pid file $pid
 fi
else
  echo "  start             Start masla in a separate window"
  echo "  stop              Stop masla, waiting up to 5 seconds for the process to end"

fi
