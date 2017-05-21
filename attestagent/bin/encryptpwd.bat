@ECHO OFF
call %~dp0\setenv

"%JDKHOME%\bin\java" entg.util.PasswordEncryption %1