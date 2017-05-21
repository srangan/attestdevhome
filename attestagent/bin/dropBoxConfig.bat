@ECHO OFF
call %~dp0\setenv

"%JDKHOME%\bin\java" -Dattest.path=%ENTEGRATION_TEST_CLIENT_DIR% entg.util.DropboxUploader %1 %2