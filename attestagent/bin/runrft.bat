@ECHO OFF
call %~dp0\setenv
set CLASSPATH=%IBM_RFT_HOME%\bin\rational_ft.jar;%RFT_WORKSPACE%
@ECHO ON
"%IBM_SDP_HOME%\jdk\jre\bin\java" -Xmx1024m com.rational.test.ft.rational_ft -datastore "%RFT_WORKSPACE%\%1"  -logfolder "%3" -log "%4" -rt.log_format "html" -rt.bring_up_logviewer false -playback %2
