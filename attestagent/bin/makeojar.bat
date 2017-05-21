@ECHO OFF
call %~dp0\setenv
%DRIVENAME%

cd %1
"%JDKHOME%\bin\jar" cvf %2 *