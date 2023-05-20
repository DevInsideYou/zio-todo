ThisBuild / scalacOptions ++=
  Seq(
    "-deprecation",
    "-feature",
    "-language:implicitConversions",
    "-Wunused:all",
    "-Wvalue-discard",
    "-unchecked",
    "-Xfatal-warnings",
    "-Ykind-projector",
    "-Ysafe-init",
  ) ++ Seq("-rewrite", "-indent") ++ Seq("-source", "future-migration")
