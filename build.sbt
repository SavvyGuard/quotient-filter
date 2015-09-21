lazy val root = (project in file(".")).
  settings(
    name := "Quotient Filter",
    organization := "com.videoamp",
    version := "1.1-SNAPSHOT",
    scalaVersion := "2.11.6",
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Apache Repoitory" at "https://repository.apache.org/content/repositories/releases",

    publishTo := {
      val nexus = "https://videoamp.artifactoryonline.com/videoamp/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "snapshot") 
      else
        Some("releases"  at nexus + "release")
    },

    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),

    libraryDependencies ++= {
      val akkaV = "2.3.10"
      val sprayV = "1.3.3"
      val scalatestV = "2.2.5"
      Seq(
        "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
        "org.scalatest"       %%  "scalatest"     % scalatestV % "test",
        "com.typesafe.akka"   %%  "akka-testkit"  % akkaV  % "test",
        "io.spray"            %%  "spray-can"     % sprayV,
        "io.spray"            %%  "spray-routing" % sprayV,
        "io.spray"            %%  "spray-testkit" % sprayV  % "test",
        "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test"
      )
    }
  )
