#!/bin/bash
LIB_CLASSPATH=`echo \`find lib -name "*[\.jar|\.zip]"\` |sed 's/ /:/g'`

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=java
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

if [ -z "$JAVA_HOME" ] ; then
  echo "Warning: JAVA_HOME environment variable is not set."
fi

$JAVACMD -ea -Xdebug -Xnoagent -Djava.compiler=NONE \
-Xrunjdwp:transport=dt_socket,server=y,address=8888,suspend=y \
-Djava.util.logging.manager=org.jargo.container.JargoLogManager \
-Djava.util.logging.config.file=logging.properties \
-Djava.security.manager \
-Djava.security.policy=security.policy \
-Dcom.sun.management.config.file=jmx.properties \
-Dorg.jargo.exitOnError=true \
-Dorg.jargo.threads=16 \
-cp $LIB_CLASSPATH org.jargo.container.Main deploy/
