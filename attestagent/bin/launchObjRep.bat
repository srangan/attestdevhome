@ECHO OFF
call %~dp0\setenv

java -classpath %CLASSPATH% entg.util.objrep.ParseObjRep %1
