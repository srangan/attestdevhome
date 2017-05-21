call %~dp0\setenv
%DRIVENAME%
cd %ENTEGRATION_TEST_CLIENT_DIR%

jar xvf %1

RMDIR /Q /S "%ENTEGRATION_TEST_CLIENT_DIR%\resultsviewer\Report"
move Report "%ENTEGRATION_TEST_CLIENT_DIR%\resultsviewer"
"%QTP_DIR%\bin\QTReport.exe" "%ENTEGRATION_TEST_CLIENT_DIR%\resultsviewer\Report\Results.qtp
rem copy con>1"