package nl.wwbakker.deployment.elasticsearch

import nl.wwbakker.deployment.elasticsearch.ActionArgument.{Deploy, Undeploy}
import nl.wwbakker.deployment.elasticsearch.operations.StackManager
import org.scalactic.{Bad, Good}

object Application extends App {
  Arguments.parse(args).map(_.run) match {
    case Good((info, Arguments(action, configuration))) =>
      info.foreach(System.out.println)
      action match {
        case Deploy => StackManager.deploy(configuration)
        case Undeploy => StackManager.undeploy(configuration)
      }
    case Bad(errorMessage) =>
      System.err.println(errorMessage)
  }
}
