resolvers ++= Seq(
  "Nexus Public Repository" at "http://nexus.despegar.it:8080/nexus/content/groups/public",
  "Nexus Proxies Repository" at "http://nexus.despegar.it:8080/nexus/content/groups/proxies",
  "miami" at "http://nexus:8080/nexus/content/groups/public/"
)

logLevel := Level.Warn

addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.0")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")

addSbtPlugin("com.despegar.sbt" %% "madonna" % "0.1.2")

