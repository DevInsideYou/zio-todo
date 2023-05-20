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
        "dev.zio" %% "zio" % "2.0.14"

      val `zio-interop-cats` =
        "dev.zio" %% "zio-interop-cats" % "23.0.0.6"
    }
  }

  object io {
    object circe {
      val `circe-generic` =
        dependency("generic")

      private def dependency(artifact: String): ModuleID =
        "io.circe" %% s"circe-$artifact" % "0.14.5"
    }
  }

  object org {
    object http4s {
      val `http4s-ember-server` =
        dependency("ember-server")

      val `http4s-circe` =
        dependency("circe")

      val `http4s-dsl` =
        dependency("dsl")

      private def dependency(artifact: String): ModuleID =
        "org.http4s" %% s"http4s-$artifact" % "1.0.0-M39"
    }

    object scalacheck {
      val scalacheck =
        "org.scalacheck" %% "scalacheck" % "1.17.0"
    }

    object scalatest {
      val scalatest =
        "org.scalatest" %% "scalatest" % "3.2.16"
    }

    object scalatestplus {
      val `scalacheck-1-15` =
        "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0"
    }

    object slf4j {
      val `slf4j-simple` =
        "org.slf4j" % "slf4j-simple" % "2.0.7"
    }

    object tpolecat {
      val `skunk-core` =
        "org.tpolecat" %% "skunk-core" % "0.6.0"
    }

    object typelevel {
      val `cats-core` =
        "org.typelevel" %% "cats-core" % "2.9.0"

      val `discipline-scalatest` =
        "org.typelevel" %% "discipline-scalatest" % "2.2.0"
    }
  }
}
