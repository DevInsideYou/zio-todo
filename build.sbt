import Dependencies.{ io, _ }
import Util._

ThisBuild / organization := "dev.insideyou"
ThisBuild / scalaVersion := "3.0.0"

lazy val `todo` =
  project
    .in(file("."))
    .aggregate(
      core,
      `custom-zio-interop-cats`,
      delivery,
      `delivery-http-http4s`,
      persistence,
      `persistence-postgres-skunk`,
      main,
      `main-http-http4s`,
      `main-postgres-skunk`,
      `main-http-http4s-postgres-skunk`,
    )

lazy val core =
  project
    .in(file("01-core"))
    .settings(commonSettings)
    .settings(commonDependencies)
    .settings(
      libraryDependencies ++= Seq(
        dev.zio.zio,
        org.typelevel.`cats-core`,
      )
    )

lazy val `custom-zio-interop-cats` =
  project
    .in(file("01-custom-zio-interop-cats"))
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        dev.zio.`zio-interop-cats`
      )
    )

lazy val delivery =
  project
    .in(file("02-delivery"))
    .dependsOn(core % Cctt)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        org.typelevel.`cats-effect`
      )
    )

lazy val `delivery-http-http4s` =
  project
    .in(file("02-delivery-http-http4s"))
    .dependsOn(core % Cctt)
    .dependsOn(`custom-zio-interop-cats` % Cctt)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        io.circe.`circe-generic`,
        org.http4s.`http4s-blaze-server`,
        org.http4s.`http4s-circe`,
        org.http4s.`http4s-dsl`,
        org.slf4j.`slf4j-simple`,
        org.typelevel.`cats-effect`,
      )
    )

lazy val persistence =
  project
    .in(file("02-persistence"))
    .dependsOn(core % Cctt)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        org.typelevel.`cats-effect`
      )
    )

lazy val `persistence-postgres-skunk` =
  project
    .in(file("02-persistence-postgres-skunk"))
    .dependsOn(core % Cctt)
    .dependsOn(`custom-zio-interop-cats` % Cctt)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        org.tpolecat.`skunk-core`,
        org.typelevel.`cats-effect`,
      )
    )

lazy val main =
  project
    .in(file("03-main"))
    .dependsOn(delivery % Cctt)
    .dependsOn(persistence % Cctt)
    .settings(commonSettings)
    .settings(libraryDependencies ++= effects)

lazy val `main-http-http4s` =
  project
    .in(file("03-main-http-http4s"))
    .dependsOn(`delivery-http-http4s` % Cctt)
    .dependsOn(persistence % Cctt)
    .settings(commonSettings)
    .settings(libraryDependencies ++= effects)

lazy val `main-postgres-skunk` =
  project
    .in(file("03-main-postgres-skunk"))
    .dependsOn(delivery % Cctt)
    .dependsOn(`persistence-postgres-skunk` % Cctt)
    .settings(commonSettings)
    .settings(libraryDependencies ++= effects)

lazy val `main-http-http4s-postgres-skunk` =
  project
    .in(file("03-main-http-http4s-postgres-skunk"))
    .dependsOn(`delivery-http-http4s` % Cctt)
    .dependsOn(`persistence-postgres-skunk` % Cctt)
    .settings(commonSettings)
    .settings(libraryDependencies ++= effects)

lazy val commonSettings = Seq(
  update / evictionWarningOptions := EvictionWarningOptions.empty,
  Test / console / scalacOptions :=
    (Compile / console / scalacOptions).value,
)

lazy val commonDependencies = Seq(
  libraryDependencies ++= Seq(
    org.scalacheck.scalacheck,
    org.scalatest.scalatest,
    org.scalatestplus.`scalacheck-1-15`,
    org.typelevel.`discipline-scalatest`,
  ).map(_ % Test)
)

lazy val effects = Seq(
  dev.zio.`zio-interop-cats`,
  dev.zio.zio,
  // io.monix.`monix-eval`,
  org.typelevel.`cats-effect`,
)
