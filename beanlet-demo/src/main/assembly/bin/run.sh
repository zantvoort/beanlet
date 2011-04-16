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

#
# The following command runs the jargo container, which is responsible for 
# implementing the beanlet specification. The 'org.jargo.threads' system 
# property limits the maximum number of deployment threads that may be active 
# concurrently. If  omitted, the container defaults to the number of available 
# processors. The 'org.jargo.exitOnError' system property instructs the 
# container to shutdown if an Error slips through any of its exceptions.
# The custom log manager lets the container continue logging while the Java VM 
# is shutting down.
#

$JAVACMD \
-Djava.util.logging.manager=org.jargo.container.JargoLogManager \
-Djava.util.logging.config.file=logging.properties \
-Dcom.sun.management.config.file=jmx.properties \
-Dorg.jargo.exitOnError=true \
-Dorg.jargo.threads=16 \
-cp $LIB_CLASSPATH org.jargo.container.Main deploy/
