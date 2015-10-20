name := "sirc"

autoScalaLibrary := false

javacOptions in Compile ++= Seq("-target", "1.6", "-source", "1.6")

javacOptions in (Compile,doc) ~= {
  _.foldRight(List.empty[String]) { case (o,r) =>
    if (o != "-target") o :: r else r.drop(1)
  }
}

crossPaths := false

organization := "com.hanhuy"

version := "1.1.6-pfn.1"

// sonatype publishing options follow
publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }

pomExtra :=
  <scm>
    <url>git@github.com:pfn/sIRC.git</url>
    <connection>scm:git:git@github.com:pfn/sIRC.git</connection>
  </scm>
  <developers>
    <developer>
      <id>pfnguyen</id>
      <name>Perry Nguyen</name>
      <url>https://github.com/pfn</url>
    </developer>
  </developers>

licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php"))

homepage := Some(url("https://github.com/pfn/sIRC"))

libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.mockito" % "mockito-core" % "1.8.5" % "test"
)
