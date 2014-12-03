#!/bin/bash
java -Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -jar `dirname $0`/sbt-launch.jar "$@"
