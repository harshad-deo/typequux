val typequux = crossProject
  .crossType(CrossType.Pure)
  .enablePlugins(SiteScaladocPlugin, PamfletPlugin)
  .settings(
    name := "typequux",
    organization := "com.simianquant",
    version := "0.3.0",
    scalaVersion := "2.11.8",
    crossScalaVersions := Seq("2.11.8", "2.12.0"),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
    ),
    incOptions := incOptions.value.withLogRecompileOnMacro(false),
    wartremoverErrors ++= {
      import Wart._
      Seq(Any2StringAdd,
          EitherProjectionPartial,
          Enumeration,
          IsInstanceOf,
          ListOps,
          Option2Iterable,
          OptionPartial,
          Product,
          Return,
          Serializable,
          TryPartial)
    },
    scalacOptions in (Compile) ++= Seq(
      "-deprecation",
      "-unchecked",
      "-explaintypes",
      //"-Ywarn-unused-import",
      "-encoding",
      "UTF-8",
      "-feature",
      "-Xlog-reflective-calls",
      "-Ywarn-dead-code",
      "-Ywarn-inaccessible",
      "-Ywarn-infer-any",
      "-Ywarn-unused",
      "-Ywarn-value-discard",
      "-Xlint",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-Xfuture",
      "-P:linter:disable:UnusedParameter"
    ),
    scalacOptions in (Compile, doc) ++= Seq(
      "-author",
      "-groups",
      "-implicits"
    ),
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
    initialCommands := """| class Witness[T](val x: T)
      | object Witness{
      |   def apply[T](x: T): Witness[T] = new Witness(x)
      | }
      | import typequux._
      | import typequux._""".stripMargin,
    addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.16"),
    siteSubdirName in SiteScaladoc := "api",
    previewLaunchBrowser := false,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    publishTo := Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"),
    pomIncludeRepository := { _ =>
      false
    },
    pomExtra := (<url>https://harshad-deo.github.io/typequux/TypeQuux.html</url>
      <licenses>
        <license>
          <name>Apache-2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <connection>scm:git:git@github.com:harshad-deo/typequux.git</connection>
        <developerConnection>scm:git:git@github.com:harshad-deo/typequux.git</developerConnection>
        <url>git@github.com:harshad-deo/typequux.git</url>
      </scm>
      <developers>
        <developer>
          <id>harshad-deo</id>
          <name>Harshad Deo</name>
          <url>https://github.com/harshad-deo</url>
        </developer>
      </developers>)
  )
  .jvmSettings(
    fork := true
  )
  .jsSettings(
    scalaJSStage in Test := FullOptStage,
    coverageExcludedPackages := ".*"
  )

lazy val typequuxJS = typequux.js.settings(name := "typequuxJS")
lazy val typequuxJVM = typequux.jvm.settings(name := "typequuxJVM").aggregate(typequuxJS)

ghpages.settings

git.remoteRepo := "git@github.com:harshad-deo/typequux.git"

onLoad in Global := (Command.process("project typequuxJVM", _: State)) compose (onLoad in Global).value
