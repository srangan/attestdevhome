call %~dp0\setenv

javac -d %TMPDIR% %1\%2.java
java junit.textui.TestRunner %2
