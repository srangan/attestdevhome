REM @ECHO OFF
call %~dp0\setenv
cd %ENTEGRATION_TEST_CLIENT_DIR%
REM java -Dattest.path=%ENTEGRATION_TEST_CLIENT_DIR% -classpath %CLASSPATH% entg.job.JobManager

%ENTEGRATION_TEST_CLIENT_DIR%\bin\EntgJobMgrSvc.exe -install "JobMgrService" "%JDKHOME%\jre\bin\server\jvm.dll" -Dattest.path=%ENTEGRATION_TEST_CLIENT_DIR% -Djava.class.path=%CLASSPATH% -start entg.job.JobManager -out %ENTEGRATION_TEST_CLIENT_DIR%\jobmgr.log -err %ENTEGRATION_TEST_CLIENT_DIR%\jobmgrerr.log -overwrite