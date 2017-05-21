@ECHO OFF
call %~dp0\setenv
cd %ENTEGRATION_TEST_CLIENT_DIR%
java -Dattest.path=%ENTEGRATION_TEST_CLIENT_DIR% -classpath %CLASSPATH% entg.job.JobManager
