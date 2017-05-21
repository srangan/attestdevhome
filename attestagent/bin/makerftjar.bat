@ECHO OFF
call %~dp0\setenv
%DRIVENAME%
@echo ON
cd %1
"%JDKHOME%\bin\jar" cvf %2 *