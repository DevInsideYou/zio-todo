import sbt._

object Dependencies {
  case object com {
    case object github {
      case object liancheng {
        val `organize-imports` =
          "com.github.liancheng" %% "organize-imports" % "0.5.0"
      }
    }
  }

  case object dev {
    case object zio {
      val zio =
        "dev.zio" %% "zio" % "1.0.9"

      val `zio-interop-cats` =
        "dev.zio" %% "zio-interop-cats" % "3.1.1.0"
    }
  }

  case object io {
    case object circe {
      val `circe-generic` =
        dependency("generic")

      private def dependency(artifact: String): ModuleID =
        "io.circe" %% s"circe-$artifact" % "0.14.1"
    }

    case object monix {
      val `monix-eval` =
        "io.monix" %% "monix-eval" % "3.3.0"
    }
  }

  case object org {
    case object http4s {
      val `http4s-blaze-server` =
        dependency("blaze-server")

      val `http4s-circe` =
        dependency("circe")

      val `http4s-dsl` =
        dependency("dsl")

      private def dependency(artifact: String): ModuleID =
        "org.http4s" %% s"http4s-$artifact" % "1.0.0-M23"
    }

    case object scalacheck {
      val scalacheck =
        "org.scalacheck" %% "scalacheck" % "1.15.4"
    }

    case object scalatest {
      val scalatest =
        "org.scalatest" %% "scalatest" % "3.2.9"
    }

    case object scalatestplus {
      val `scalacheck-1-15` =
        "org.scalatestplus" %% "scalacheck-1-15" % "3.2.9.0"
    }

    case object slf4j {
      val `slf4j-simple` =
        "org.slf4j" % "slf4j-simple" % "1.7.31"
    }

    case object tpolecat {
      val `skunk-core` =
        "org.tpolecat" %% "skunk-core" % "0.2.0"
    }

    case object typelevel {
      val `cats-core` =
        "org.typelevel" %% "cats-core" % "2.6.1"

      val `cats-effect` =
        "org.typelevel" %% "cats-effect" % "3.1.1"

      val `discipline-scalatest` =
        "org.typelevel" %% "discipline-scalatest" % "2.1.5"
    }
  }
}
