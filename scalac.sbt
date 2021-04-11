ThisBuild / scalacOptions ++=
  Seq(
    "-deprecation",
    "-feature",
    "-language:implicitConversions",
    "-unchecked",
    "-Ykind-projector",
  ) ++ Seq("-rewrite", "-indent") ++ Seq("-source", "future")
