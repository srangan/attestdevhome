call %~dp0\setenv
REM echo %CLASSPATH%
echo %~n1
"%JDKHOME%\bin\java" -Dattest.path=%ENTEGRATION_TEST_CLIENT_DIR% -Djava.library.path=%ENTEGRATION_TEST_CLIENT_DIR% entg.test.TCAgentMain %1 %~n1 
REM > %TMPDIR%\%~n1.log

