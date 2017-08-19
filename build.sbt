name := "sifin"

organization := "manuellimaf"

scalaVersion := "2.11.7"

javacOptions  := Seq("-source", "1.8", "-target", "1.8")

val jacksonVersion = "2.6.1"
val scalatraVersion = "2.4.0"

checksums in update := Nil

libraryDependencies ++= Seq(
  "org.slf4j"                     %   "slf4j-api"                   % "1.7.12",
  "ch.qos.logback"                %   "logback-classic"             % "1.1.3",
  "org.eclipse.jetty"             %   "jetty-webapp"                % "9.3.10.v20160621",
  "org.scalatra"                  %%  "scalatra"                    % scalatraVersion,
  "com.fasterxml.jackson.module"  %%  "jackson-module-scala"        % jacksonVersion,
  "com.fasterxml.jackson.module"  %   "jackson-module-afterburner"  % jacksonVersion,
  "org.apache.httpcomponents"     %   "httpclient"                  % "4.5",
  "org.apache.httpcomponents"     %   "httpmime"                    % "4.5",
  "mysql"                         %   "mysql-connector-java"        % "6.0.6",
  "com.despegar.sbt"	            %%  "madonna-configuration" 	    % "0.0.4",
  "org.apache.commons"            %   "commons-email"               % "1.3.3",
  "org.scalatest"                 %%  "scalatest"                   % "2.2.6"   % "test",
  "org.mockito"                   %   "mockito-all"                 % "1.10.19" % "test"
)

crossPaths := false

resourceGenerators in Compile <+= (resourceManaged, baseDirectory) map { (managedBase, base) =>
	val webappBase = base / "src" / "main" / "webapp"
	for {
		(from, to) <- webappBase ** "*" pair rebase(webappBase, managedBase / "main" / "webapp")
	} yield {
		Sync.copy(from, to)
		to
	}
}
