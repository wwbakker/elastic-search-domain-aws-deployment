package nl.wwbakker.deployment.elasticsearch

import java.nio.file.Paths

import nl.wwbakker.deployment.elasticsearch.ActionArgument.Undeploy
import org.scalactic.Bad
import org.scalatest.{FlatSpec, _}

class ArgumentsSpec extends FlatSpec with Matchers {
  "Arguments.parse" should "return usage when no arguments are given" in {
    Arguments.parse(Array.empty).isBad shouldBe true
    Arguments.parse(Array.empty).swap.get.contains("usage") shouldBe true
  }

  it should "complain about subnetIds with no configuration overrides file" in {
    Arguments.parse(Array("deploy")).isBad shouldBe true
    Arguments.parse(Array("deploy")).swap.get.contains("subnetId") shouldBe true
  }

  it should "parse and deploy correctly with a configuration and overrides files" in {
    val (info, arguments) = Arguments.parse(Array("UNdeploy", Paths.get("src", "test", "resources", "test_overrides.conf").toString)).get.run
    info.isEmpty shouldBe true
    arguments.action shouldBe Undeploy
    arguments.configuration.stackName shouldBe "overridden"
  }

  it should "fail when specifying an invalid master count" in {
    Arguments.parse(Array("UNdeploy", Paths.get("src", "test", "resources", "test_invalid_master_count.conf").toString))
      .shouldBe(Bad("Configuration invalid: When dedicated master is enabled, the count must be 3 or 5."))
  }
}
