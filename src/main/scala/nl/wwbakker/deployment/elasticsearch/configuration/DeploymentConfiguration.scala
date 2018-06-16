package nl.wwbakker.deployment.elasticsearch.configuration

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

class DeploymentConfiguration(overridesConfig: Config = ConfigFactory.empty()) {
  val config : Config = overridesConfig.withFallback(ConfigFactory.load())

  private val cloudformation = config.getConfig("cloudformation")
  val stackName : String = cloudformation.getString("stack-name")
  val stackDescription : String = cloudformation.getString("stack-description")

  private val domain = cloudformation.getConfig("elastic-search-domain")
  val elasticSearchDomainName : String = domain.getString("name")
  val elasticSearchVersion : String = domain.getString("version")
  val zoneAwarenessEnabled : Boolean = domain.getBoolean("zone-awareness-enabled")

  val instanceType : String = domain.getString("instance.type")
  val instanceCount : Int = domain.getInt("instance.count")
  val instanceVolumeSize : Int = domain.getInt("instance.volume-size")

  val dedicatedMasterEnabled : Boolean = domain.getBoolean("dedicated-master.enabled")
  val dedicatedMasterType : String = domain.getString("dedicated-master.type")
  val dedicatedMasterCount : Int = domain.getInt("dedicated-master.count")

  val subnetIds : Seq[String] = config.getStringList("vpc.subnetIds").asScala
    .take(if (zoneAwarenessEnabled) 2 else 1)





}
object DeploymentConfiguration {
  lazy val default = new DeploymentConfiguration

//  implicit class NotNullHelper[A](val v : A) extends AnyVal {
//    def required : A = if (v != null) v else throw new IllegalArgumentException("Configuration value cannot be null.")
//  }
}