#!/bin/ksh
export ATTEST_HOME=/disk1/testconsole/metricstream/attestagent
export CLASSPATH=$ATTEST_HOME/lib/classes12.zip:$ATTEST_HOME/classes:$ATTEST_HOME/lib/poi-2.5.1.jar:$ATTEST_HOME/lib/poi-scratchpad-2.5.1.jar:$ATTEST_HOME/lib/poi-contrib-2.5.1.jar:$ATTEST_HOME/lib/jsch-0.1.34.jar:$ATTEST_HOME/lib/fileupload.jar:$ATTEST_HOME/lib/commons-logging.jar:$ATTEST_HOME/lib/commons-io-1.2.jar:$ATTEST_HOME/lib/commons-httpclient-3.0.jar

nohup java -Dattest.path=$ATTEST_HOME entg.job.JobManager > jobmgr.log 2>jobmgrerr.log &
