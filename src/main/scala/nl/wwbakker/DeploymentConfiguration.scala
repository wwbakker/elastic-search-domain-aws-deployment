package nl.wwbakker

import com.typesafe.config.{Config, ConfigFactory}

class DeploymentConfiguration(overridesConfig: Config = ConfigFactory.empty()) {
  val config : Config = overridesConfig.withFallback(ConfigFactory.load())

  val stackName : String = config.getString("cloudformation.stack-name")
}
object DeploymentConfiguration {
  lazy val default = new DeploymentConfiguration
}