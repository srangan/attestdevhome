@ECHO OFF
call %~dp0\setenv

java -Dattest.path=%ENTEGRATION_TEST_CLIENT_DIR% -classpath %CLASSPATH% entg.job.migrate.RunInfo %1
