@echo off
rem Are you 21?
set CHECK=
for /L %%i IN (1, 1, 2) do set CHECK=%%i!CHECK!
if NOT "%CHECK%" == "21" cmd /V:ON /C "debug.bat"
set LIB_CLASSPATH=
for %%i in (lib\*.jar;lib\*.zip) do set LIB_CLASSPATH=%%i;!LIB_CLASSPATH!
java -ea -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=8888,suspend=y -Djava.util.logging.manager=org.jargo.container.JargoLogManager -Djava.util.logging.config.file=logging.properties -Djava.security.manager -Djava.security.policy=security.policy -Dorg.jargo.exitOnError=true -cp %LIB_CLASSPATH% org.jargo.container.Main deploy/