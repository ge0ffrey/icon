#!/bin/sh

# Change directory to the directory of the script
cd `dirname $0`

# TODO if the script overhead is too high, feel free to lower this to less than 5 minutes
secondsSpentLimit=300
# the machine has 24 threads, but let's be conservative and only take half
processCount=12

# Stay off
jvmOptions="-Xms1024m -Xmx1536m -server"
mainJar=icon-core-0.9-SNAPSHOT-jar-with-dependencies.jar

echo "Usage: ./solveByDelirium.sh inputDir"
echo "Notes:"
echo "- OpenJDK 7 must be installed. Get OpenJDK 7 (not just the JRE!)."
echo "- For JDK, the environment variable JAVA_HOME should be set to the JDK installation directory"
echo "  For example (linux): export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-i386"
echo "- The working dir should probably be the directory of this script."
echo "- The results are written in the inputDir."
echo

if [ -f $JAVA_HOME/bin/java ]; then
    echo "Starting examples app with JDK from environment variable JAVA_HOME ($JAVA_HOME)..."
    for processIndex in $(seq 1 $processCount);
    do
        echo "Starting process $processIndex (out of $processCount)"
        $JAVA_HOME/bin/java ${jvmOptions} -jar ${mainJar} $1 $processIndex $secondsSpentLimit &
    done
    # Wait for all processes to finish
    wait
    echo "All $processCount processes stopped"
else
    echo "ERROR: Check if Java is installed and environment variable JAVA_HOME ($JAVA_HOME) is correct."
fi

if [ $? != 0 ] ; then
    echo
    echo "ERROR: Check if Java is installed and environment variable JAVA_HOME ($JAVA_HOME) is correct."
    # Prevent the terminal window to disappear before the user has seen the error message
    read -p "Press [Enter] key to close this window."
fi
