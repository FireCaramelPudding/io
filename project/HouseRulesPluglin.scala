package iobuild

import sbt._
import Keys._

object HouseRulesPlugin extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  override def projectSettings: Seq[Def.Setting[_]] = baseSettings

  lazy val baseSettings: Seq[Def.Setting[_]] = Seq(
    scalacOptions ++= Seq("-encoding", "utf8"),
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
    scalacOptions += "-language:higherKinds",
    scalacOptions += "-language:implicitConversions",
    scalacOptions ++= "-Xfuture".ifScala213OrMinus.value.toList,
    scalacOptions ++= "-Xfatal-warnings"
      .ifScala(v => {
        sys.props.get("sbt.build.fatal") match {
          case Some(_) => java.lang.Boolean.getBoolean("sbt.build.fatal")
          case _       => v == 12
        }
      })
      .value
      .toList,
    scalacOptions ++= "-Yinline-warnings".ifScala211OrMinus.value.toList,
    scalacOptions ++= "-Yno-adapted-args".ifScala212OrMinus.value.toList,
    scalacOptions ++= "-Ywarn-dead-code".ifScala213OrMinus.value.toList,
    scalacOptions ++= "-Ywarn-numeric-widen".ifScala213OrMinus.value.toList,
    scalacOptions ++= "-Ywarn-value-discard".ifScala213OrMinus.value.toList,
  ) ++ Seq(Compile, Test).flatMap(
    c => (c / console / scalacOptions) --= Seq("-Ywarn-unused-import", "-Xlint")
  )

  private def scalaPartV = Def setting (CrossVersion partialVersion scalaVersion.value)

  private implicit final class AnyWithIfScala[A](val __x: A) {
    def ifScala(p: Long => Boolean) =
      Def setting (scalaPartV.value collect { case (2, y) if p(y) => __x })
    def ifScalaLte(v: Long) = ifScala(_ <= v)
    def ifScalaGte(v: Long) =
      Def.setting(
        scalaPartV.value.collect {
          case (2, y) if y >= v => __x
          case (n, _) if n >= 3 => __x
        }
      )
    def ifScala211OrMinus = ifScalaLte(11)
    def ifScala211OrPlus = ifScalaGte(11)
    def ifScala212OrMinus = ifScalaLte(12)
    def ifScala213OrMinus = ifScalaLte(13)
  }
}
