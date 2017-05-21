@ECHO OFF
set ENTEGRATION_TEST_CLIENT_DIR=c:\attest\git\attestagent
set JDKHOME=C:\Program Files (x86)\Java\jdk1.6.0_34
set PATH=%JDKHOME%\bin;%PATH%
set TMPDIR=c:\TEMP
set SELENIUM_CLASS_PATH=c:\seleniumtests
set SELENIUM_SOURCE_PATH=c:\seleniumtests
set QTP_DIR=C:\Program Files\HP\QuickTest Professional
set CLASSPATH=%ENTEGRATION_TEST_CLIENT_DIR%\classes\attestagent-new.jar;%TMPDIR%;%SELENIUM_CLASS_PATH%
set WEBDRIVER_LIBS=C:\selenium-java-2.46.0\selenium-2.46.0

REM set CLASSPATH=%ENTEGRATION_TEST_CLIENT_DIR%\classes;%TMPDIR%;%SELENIUM_CLASS_PATH%
set DRIVENAME=c:
set OATS_VIEWING_BROWSER=c:\Program Files (x86)\Google\Chrome\Application\chrome.exe

echo %CLASSPATH%
 
set IBM_SDP_HOME=C:\Program Files\IBM\SDP
set IBM_RFT_HOME=C:\Program Files\IBM\SDP\FunctionalTester
set RFT_WORKSPACE=C:\Documents and Settings\Sushma\IBM\rationalsdp\workspace
  	
  
for %%i in ("%ENTEGRATION_TEST_CLIENT_DIR%\lib\*.jar") do call "%ENTEGRATION_TEST_CLIENT_DIR%\bin\cappend.bat" %%i
for %%i in ("%ENTEGRATION_TEST_CLIENT_DIR%\lib\*.zip") do call "%ENTEGRATION_TEST_CLIENT_DIR%\bin\cappend.bat" %%i
REM echo "Before Wedrier libs : %WEBDRIVER_LIBS%"
for %%i in ("%WEBDRIVER_LIBS%\libs\*.jar") do call "%ENTEGRATION_TEST_CLIENT_DIR%\bin\cappend.bat" %%i
  

