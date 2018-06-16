package nl.wwbakker.deployment.elasticsearch

import java.nio.file.Paths

import ActionArgument.{Deploy, UnDeploy}
import configuration.DeploymentConfiguration
import org.scalatest.{FlatSpec, _}

class ArgumentsSpec extends FlatSpec with Matchers {
  "Arguments.parse" should "return usage when no arguments are given" in {
    Arguments.parse(Array.empty).isBad shouldBe true
    Arguments.parse(Array.empty).swap.get.contains("usage") shouldBe true
  }

  it should "parse deploy correctly with no configuration overrides file" in {
    val (info, arguments) = Arguments.parse(Array("deploy")).get.run
    info.headOption.map(_.contains("No configuration file was specified.")) shouldBe Some(true)
    arguments shouldBe Arguments(Deploy, DeploymentConfiguration.default)
  }

  it should "parse and deploy correctly with a configuration and overrides files" in {
    val (info, arguments) = Arguments.parse(Array("UNdeploy", Paths.get("src", "test", "resources", "test_overrides.conf").toString)).get.run
    info.isEmpty shouldBe true
    arguments.action shouldBe UnDeploy
    arguments.configuration.stackName shouldBe "overridden"
  }
}
