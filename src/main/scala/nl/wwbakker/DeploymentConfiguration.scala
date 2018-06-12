package nl.wwbakker

import com.typesafe.config.{Config, ConfigFactory}

class DeploymentConfiguration(config: Config) {
  val stackIdentifier : String = config.getString("cloudformation.stackName")
}
object DeploymentConfiguration {
  def default = new DeploymentConfiguration(ConfigFactory.load())
}