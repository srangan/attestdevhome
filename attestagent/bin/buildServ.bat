cd c:\attestagentworkspace
REM jar cvf attestagent.jar attestagent\*.properties attestagent\lib attestagent\classes\entg\util attestagent\classes\entg\job\oracm attestagent\classes\entg\job\extract attestagent\classes\entg\job\Job* attestagent\classes\entg\job\Agent* attestagent\bin\*.sh
jar cvf attestagent\attestagent.jar attestagent\attest_serv.properties attestagent\classes\entg\util\*.class attestagent\classes\entg\util\*.java attestagent\classes\entg\job\oracm attestagent\classes\entg\job\extract attestagent\classes\entg\job\Job* attestagent\classes\entg\job\testplan attestagent\classes\entg\job\Agent* attestagent\bin\*.sh