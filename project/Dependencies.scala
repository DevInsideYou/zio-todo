import sbt._

object Dependencies {
  object com {
    object github {
      object liancheng {
        val `organize-imports` =
          "com.github.liancheng" %% "organize-imports" % "0.6.0"
      }
    }
  }

  object dev {
    object zio {
      val zio =
        "dev.zio" %% "zio" % "1.0.13"

      val `zio-interop-cats` =
        "dev.zio" %% "zio-interop-cats" % "3.2.9.1"
    }
  }

  object io {
    object circe {
      val `circe-generic` =
        dependency("generic")

      private def dependency(artifact: String): ModuleID =
        "io.circe" %% s"circe-$artifact" % "0.14.1"
    }
  }

  object org {
    object http4s {
      val `http4s-blaze-server` =
        dependency("blaze-server")

      val `http4s-circe` =
        dependency("circe")

      val `http4s-dsl` =
        dependency("dsl")

      private def dependency(artifact: String): ModuleID =
        "org.http4s" %% s"http4s-$artifact" % "1.0.0-M32"
    }

    object scalacheck {
      val scalacheck =
        "org.scalacheck" %% "scalacheck" % "1.15.4"
    }

    object scalatest {
      val scalatest =
        "org.scalatest" %% "scalatest" % "3.2.11"
    }

    object scalatestplus {
      val `scalacheck-1-15` =
        "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0"
    }

    object slf4j {
      val `slf4j-simple` =
        "org.slf4j" % "slf4j-simple" % "1.7.36"
    }

    object tpolecat {
      val `skunk-core` =
        "org.tpolecat" %% "skunk-core" % "0.3.1"
    }

    object typelevel {
      val `cats-core` =
        "org.typelevel" %% "cats-core" % "2.7.0"

      val `discipline-scalatest` =
        "org.typelevel" %% "discipline-scalatest" % "2.1.5"
    }
  }
}
