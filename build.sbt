import com.typesafe.sbt.SbtStartScript

seq(SbtStartScript.startScriptForClassesSettings: _*)


libraryDependencies += "com.typesafe" % "config" % "1.0.2"

libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.5.2"
