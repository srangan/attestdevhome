@ECHO OFF
call %~dp0\setenv

"%JDKHOME%\bin\java" -Dattest.path=%ENTEGRATION_TEST_CLIENT_DIR% -Djava.library.path=%ENTEGRATION_TEST_CLIENT_DIR% entg.util.TCTestcaseGenerator %1

echo "Please press ^C to close and exit"
copy con>1