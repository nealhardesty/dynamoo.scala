#!/bin/bash
java -Xms128M -Xmx256M -Xss1M -XX:+CMSClassUnloadingEnabled -jar `dirname $0`/sbt-launch.jar "$@"
