package nl.wwbakker

import java.io.File

import org.scalactic._
import cats.data.Writer
import cats.instances.vector._
import cats.syntax.writer._
import cats.syntax.applicative._
import com.typesafe.config.ConfigFactory


trait ActionArgument {
  def name : String = this.getClass.getSimpleName.replace("$", "").toLowerCase
}

object ActionArgument {
  type ErrorMessage = String

  object Deploy extends ActionArgument
  object Undeploy extends ActionArgument

  private def all = Set(Deploy, Undeploy)
  def parse(actionName : String) : ActionArgument Or ErrorMessage =
    all.find(_.name == actionName.toLowerCase).map(Good(_))
      .getOrElse(Bad(s"'$actionName' is not a valid value. Possible values: ${all.mkString(", ")} "))

}


case class Arguments(action : ActionArgument, configuration: DeploymentConfiguration)
object Arguments {
  type ErrorMessage = String
  type WithInfo[A] = Writer[Vector[String], A]

  def parse(args : Array[String]) : WithInfo[Arguments] Or ErrorMessage = args.toList match {
    case Nil =>
      Bad("usage: ./java -jar deploy-elastic-search-domain.jar [action*] [configuration-file-path]\n" +
      "action (required) - possible values: deploy, undeploy\n" +
      "configuration-file-path - path to deployment configuration file or empty for default settings.\n")
    case actionName :: Nil =>
      ActionArgument.parse(actionName).map(actionArgument =>
        for {
          arguments <- Arguments(actionArgument, DeploymentConfiguration.default).pure[WithInfo]
          _         <- Vector("No configuration file was specified. Using default configuration file.").tell
        } yield arguments
      )
    case actionName :: configurationFilePath :: _ =>
      ActionArgument.parse(actionName).map(actionArgument =>
        Arguments(actionArgument, new DeploymentConfiguration(ConfigFactory.parseFile(new File(configurationFilePath)))).pure[WithInfo]
      )
  }
}