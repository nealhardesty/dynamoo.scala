#!/bin/bash
java -Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=1024M -jar `dirname $0`/sbt-launch.jar "$@"
