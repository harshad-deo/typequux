import sbtcrossproject.{crossProject, CrossType}

def commonSettings(nameStr: String) = Seq(
  name := nameStr,
  organization := "com.simianquant",
  version := Settings.version,
  scalaVersion := Settings.scalaVersion,
  crossScalaVersions := Settings.crossScalaVersions,
  incOptions := incOptions.value.withLogRecompileOnMacro(false),
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value
  ),
  wartremoverErrors ++= Seq(
    Wart.Any2StringAdd,
    Wart.EitherProjectionPartial,
    Wart.Enumeration,
    Wart.ListOps,
    Wart.Option2Iterable,
    Wart.OptionPartial,
    Wart.Product,
    Wart.Return,
    Wart.Serializable,
    Wart.TryPartial
  ),
  scalacOptions in (Compile) ++= Settings.commonScalacOptions,
  scalacOptions in (Compile, doc) ++= Settings.scalacDocOptions
)

lazy val commonJVMSettings = List(
  fork := true,
  scalacOptions ++= Seq(
    "-Ywarn-dead-code",
    "-Ywarn-value-discard"
  ),
  scalacOptions += (if (scalaVersion.value.startsWith("2.11")) "-target:jvm-1.6" else "-target:jvm-1.8")
)

lazy val commonJSSettings = List(
  coverageExcludedPackages := ".*"
)

lazy val commonNativeSettings = List(
  nativeMode := "release",
  coverageExcludedPackages := ".*",
  scalaVersion := "2.11.11",
  crossScalaVersions -= "2.12.2"
)

lazy val typequux = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("typequux"))
  .settings(commonSettings("typequux"))
  .jvmSettings(commonJVMSettings)
  .jvmSettings(
    initialCommands := """|class Witness1[T](val x: T)
                          |object Witness1{
                          |  def apply[T](x: T): Witness1[T] = new Witness1(x)
                          |}
                          |class Witness2[T]
                          |import typequux._
                          |import Typequux._""".stripMargin
  )
  .jsSettings(commonJSSettings)
  .nativeSettings(commonNativeSettings)

lazy val typequuxJVM = typequux.jvm
lazy val typequuxJS = typequux.js
lazy val typequuxNative = typequux.native

// taken from https://stackoverflow.com/questions/40741244/in-sbt-how-to-execute-a-command-in-task
def runCommandAndRemaining(command: String): State => State = { st: State =>
  import sbt.complete.Parser
  @annotation.tailrec
  def runCommand(command: String, state: State): State = {
    val nextState = Parser.parse(command, state.combinedParser) match {
      case Right(cmd) => cmd()
      case Left(msg) => throw sys.error(s"Invalid programmatic input:\n$msg")
    }
    nextState.remainingCommands.toList match {
      case Nil => nextState
      case head :: tail => runCommand(head, nextState.copy(remainingCommands = tail))
    }
  }
  runCommand(command, st.copy(remainingCommands = Nil)).copy(remainingCommands = st.remainingCommands)
}

lazy val crossTest = taskKey[Unit]("Test library against all scala versions")

lazy val jvmJSTestSettings = Seq(
  libraryDependencies += "org.scalatest" %%% "scalatest" % Settings.Version.scalaTest % "test",
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, Settings.scalaTestOptions)
)

lazy val typequuxtests = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("typequuxtests"))
  .settings(commonSettings("typequuxtests"))
  .jvmSettings(commonJVMSettings)
  .jvmSettings(jvmJSTestSettings)
  .jsSettings(commonJSSettings)
  .jsSettings(jvmJSTestSettings)
  .jvmSettings(
    (crossTest in Test) := {
      runCommandAndRemaining("+typequuxtestsJVM/test")(state.value)
    }
  )
  .jsSettings(
    scalaJSStage in Test := FullOptStage,
    coverageExcludedPackages := ".*",
    (crossTest in Test) := {
      runCommandAndRemaining("+typequuxtestsJS/test")(state.value)
    }
  )
  .nativeSettings(commonNativeSettings)
  .nativeSettings(
    libraryDependencies += "com.simianquant" %% "sntb" % Settings.Version.sntb % "test",
    coverageExcludedPackages := ".*"
  )
  .dependsOn(typequux)

lazy val typequuxtestsJVM = typequuxtests.jvm
lazy val typequuxtestsJS = typequuxtests.js
lazy val typequuxtestsNative = typequuxtests.native

lazy val cleanAll = taskKey[Unit]("Cleans everything")
lazy val testAll = taskKey[Unit]("Tests everything")
lazy val buildCoverage = taskKey[Unit]("Generate coverage report")

lazy val Typequux = config("typequuxJVM")

lazy val root = project
  .in(file("."))
  .settings(commonSettings("root"))
  .settings(
    inThisBuild(
      List(
        cleanAll := {
          clean.in(typequuxJVM).value
          clean.in(typequuxtestsJVM).value

          clean.in(typequuxJS).value
          clean.in(typequuxtestsJS).value

          clean.in(typequuxNative).value
          clean.in(typequuxtestsNative).value
        },
        testAll := {
          crossTest.in(typequuxtestsJVM, Test).value
          crossTest.in(typequuxtestsJS, Test).value
          run.in(typequuxtestsNative, Test).toTask("").value
        },
        buildCoverage := Def
          .sequential(
            clean in typequuxJVM,
            clean in typequuxtestsJVM,
            test in (typequuxtestsJVM, Test),
            coverageReport in typequuxJVM
          )
          .value
        
      ))
  )
  .enablePlugins(SiteScaladocPlugin, PamfletPlugin)
  .settings(
    SiteScaladocPlugin.scaladocSettings(Typequux, mappings in (Compile, packageDoc) in typequuxJVM, "api"),
    ghpages.settings,
    git.remoteRepo := "git@github.com:harshad-deo/typequux.git"
  )

onLoad in Global := (Command.process("project root", _: State)) compose (onLoad in Global).value
// lazy val runcoverageCommand = Command.command("runcoverage") { state =>
//   "project typequuxtestsJVM" :: "clean" :: "coverage" :: "test" ::
//     "project typequuxJVM" :: "coverageReport" ::
//     state
// }

// lazy val releaseLocalCommand = Command.command("releaselocal") { state =>
//   "testall" ::
//     "project typequuxNative" :: "publishLocal" ::
//     "project typequuxJVM" :: "+publishLocal" ::
//     "project typequuxJS" :: "+publishLocal" ::
//     state
// }

// lazy val releaseCommand = Command.command("release") { state =>
//   "testall" ::
//     "project typequuxNative" :: "publishSigned" ::
//     "project typequuxJS" :: "+publishSigned" ::
//     "project typequuxJVM" :: "+publishSigned" ::
//     "sonatypeRelease" ::
//     "++2.12.2" :: "ghpagesPushSite" ::
//     state
// }

// lazy val additionalCommands = Seq(testAllCommand, runcoverageCommand, releaseLocalCommand, releaseCommand)

// lazy val commonShared = Seq(
//   organization := "com.simianquant",
//   version := Settings.version,
//   scalaVersion := Settings.scalaVersion,
//   incOptions := incOptions.value.withLogRecompileOnMacro(false),
//   libraryDependencies ++= Seq(
//     "org.scala-lang" % "scala-reflect" % scalaVersion.value
//   ),
//   commands ++= additionalCommands,
//   wartremoverErrors ++= {
//     import Wart._
//     Seq(Any2StringAdd,
//         EitherProjectionPartial,
//         Enumeration,
//         ListOps,
//         Option2Iterable,
//         OptionPartial,
//         Product,
//         Return,
//         Serializable,
//         TryPartial)
//   },
//   scalacOptions in (Compile) ++= Settings.scalacCompileOptions(scalaVersion.value),
//   scalacOptions in (Compile, doc) ++= Settings.scalacDocOptions,
//   addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % Settings.Version.linter)
// )

// lazy val libSettings = commonShared ++ Seq(
//   name := "typequux",
//   previewLaunchBrowser := false,
//   publishMavenStyle := true,
//   publishArtifact in Test := false,
//   publishTo := Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"),
//   pomIncludeRepository := { _ =>
//     false
//   },
//   pomExtra := (<url>https://harshad-deo.github.io/typequux/TypeQuux.html</url>
//       <licenses>
//         <license>
//           <name>Apache-2</name>
//           <url>http://www.apache.org/licenses/LICENSE-2.0</url>
//           <distribution>repo</distribution>
//         </license>
//       </licenses>
//       <scm>
//         <connection>scm:git:git@github.com:harshad-deo/typequux.git</connection>
//         <developerConnection>scm:git:git@github.com:harshad-deo/typequux.git</developerConnection>
//         <url>git@github.com:harshad-deo/typequux.git</url>
//       </scm>
//       <developers>
//         <developer>
//           <id>harshad-deo</id>
//           <name>Harshad Deo</name>
//           <url>https://github.com/harshad-deo</url>
//         </developer>
//       </developers>)
// )

// val typequux = crossProject(JSPlatform, JVMPlatform, NativePlatform)
//   .in(file("."))
//   .settings(libSettings)
//   .settings(
//     inThisBuild(
//       List(
//         testAll := {
//           println("testing testing")
//         }
//       )
//     )
//   )
//   .jvmSettings(
//     crossScalaVersions := Settings.crossScalaVersions,
//     initialCommands := """| class Witness1[T](val x: T)
//                           | object Witness1{
//                           |   def apply[T](x: T): Witness1[T] = new Witness1(x)
//                           | }
//                           | class Witness2[T]
//                           | import typequux._
//                           | import Typequux._
//                           | """.stripMargin,
//     fork := true
//   )
//   .jsSettings(
//     crossScalaVersions := Settings.crossScalaVersions,
//     scalaJSStage in Test := FullOptStage,
//     coverageExcludedPackages := ".*"
//   )
//   .nativeSettings(
//     nativeMode := "release",
//     coverageExcludedPackages := ".*"
//   )

// lazy val typequuxJVM =
//   typequux.jvm
//     .enablePlugins(SiteScaladocPlugin, PamfletPlugin)
//     .settings(
//       siteSubdirName in SiteScaladoc := "api",
//       ghpages.settings,
//       git.remoteRepo := "git@github.com:harshad-deo/typequux.git"
//     )

// lazy val typequuxNative = typequux.native

// lazy val typequuxJS = typequux.js.settings(coverageExcludedPackages := ".*")

// lazy val testSettings = commonShared ++ Seq(
//   name := "typequuxtests",
//   crossScalaVersions := Settings.crossScalaVersions,
//   libraryDependencies ++= Seq(
//     "org.scalatest" %%% "scalatest" % Settings.Version.scalaTest % "test"
//   ),
//   testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, Settings.scalaTestOptions)
// )

// lazy val typequuxtests = crossProject(JSPlatform, JVMPlatform, NativePlatform)
//   .in(file("typequuxtests"))
//   .settings(commonShared)
//   .dependsOn(typequux)
//   .aggregate(typequux)
//   .jvmSettings(testSettings)
//   .jvmSettings(
//     fork := true
//   )
//   .jsSettings(testSettings)
//   .jsSettings(
//     scalaJSStage in Test := FullOptStage,
//     coverageExcludedPackages := ".*"
//   )
//   .nativeSettings(
//     libraryDependencies += "com.simianquant" %% "sntb" % Settings.Version.sntb % "test",
//     coverageExcludedPackages := ".*"
//   )

// lazy val typequuxtestsJVM = typequuxtests.jvm

// lazy val typequuxtestsJS = typequuxtests.js

// lazy val typequuxtestsNative = typequuxtests.native

// commands ++= additionalCommands
