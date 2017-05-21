@ECHO OFF

call %~dp0\setenv
cd %ENTEGRATION_TEST_CLIENT_DIR%
echo "CLASSPATH --  %CLASSPATH%"
java -Dattest.path=%ENTEGRATION_TEST_CLIENT_DIR% entg.test.SFDCTestMain
