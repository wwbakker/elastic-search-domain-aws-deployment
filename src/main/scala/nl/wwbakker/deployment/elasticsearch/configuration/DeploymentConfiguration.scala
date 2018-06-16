package nl.wwbakker.deployment.elasticsearch.configuration

import com.typesafe.config.{Config, ConfigFactory}
import nl.wwbakker.deployment.elasticsearch.configuration.DeploymentConfiguration.InvalidConfigurationException
import org.scalactic.Or

import scala.collection.JavaConverters._
import scala.util.Try

class DeploymentConfiguration(overridesConfig: Config = ConfigFactory.empty()) {
  val config : Config = overridesConfig.withFallback(ConfigFactory.defaultApplication())

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

  zoneAwarenessEnabled match {
    case true if instanceCount % 2 != 0  =>
      throw new InvalidConfigurationException("Zone awareness requires the instance count to be an even number.")
    case true if subnetIds.length < 2 =>
      throw new InvalidConfigurationException("When zone awareness is enabled, at least 2 subnetIds are required.")
    case _ =>
  }

  if (!zoneAwarenessEnabled && subnetIds.length < 1)
    throw new InvalidConfigurationException("At least one subnetId is required. Please set property vpc.subnetIds = ['subnet-example'].")

  if (dedicatedMasterEnabled && (dedicatedMasterCount != 3 && dedicatedMasterCount != 5))
    throw new InvalidConfigurationException("When dedicated master is enabled, the count must be 3 or 5.")







}
object DeploymentConfiguration {
  type ErrorMessage = String

  lazy val default: DeploymentConfiguration Or ErrorMessage = parseSafely()

  def parseSafely(overridesConfig: => Config = ConfigFactory.empty()) : DeploymentConfiguration Or ErrorMessage =
    Or.from(Try(new DeploymentConfiguration(overridesConfig))).badMap(_.getMessage)

  class InvalidConfigurationException(message : String) extends Exception("Configuration invalid: " + message)
}