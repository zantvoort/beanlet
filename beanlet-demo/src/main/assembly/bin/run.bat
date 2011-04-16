@echo off

rem This script assumes that a java binary is available to the system's path.

rem Are you 21?
set CHECK=
for /L %%i IN (1, 1, 2) do set CHECK=%%i!CHECK!
if NOT "%CHECK%" == "21" cmd /V:ON /C "run.bat"
set LIB_CLASSPATH=
for %%i in (lib\*.jar;lib\*.zip) do set LIB_CLASSPATH=%%i;!LIB_CLASSPATH!

rem The following command runs the jargo container, which is responsible for 
rem implementing the beanlet specification. The 'org.jargo.threads' system 
rem property limits the maximum number of deployment threads that may be active 
rem concurrently. If  omitted, the container defaults to the number of available 
rem processors. The 'org.jargo.exitOnError' system property instructs the 
rem container to shutdown if an Error slips through any of its exceptions.
rem The custom log manager lets the container continue logging while the Java VM 
rem is shutting down.
 
java -Djava.util.logging.manager=org.jargo.container.JargoLogManager -Djava.util.logging.config.file=logging.properties -Dorg.jargo.exitOnError=true -cp %LIB_CLASSPATH% org.jargo.container.Main deploy/