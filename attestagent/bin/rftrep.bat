call %~dp0\setenv
%DRIVENAME%
cd %ENTEGRATION_TEST_CLIENT_DIR%\resultsviewer

jar xvf %1

"C:\Program Files\Internet Explorer\iexplore.exe" "%ENTEGRATION_TEST_CLIENT_DIR%\resultsviewer\rational_ft_log.html
copy con>1"