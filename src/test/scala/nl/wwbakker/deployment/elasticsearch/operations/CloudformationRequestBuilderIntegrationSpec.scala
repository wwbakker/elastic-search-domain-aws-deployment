package nl.wwbakker.deployment.elasticsearch.operations

import java.io.File
import java.nio.file.Paths

import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder
import com.amazonaws.services.cloudformation.model.ValidateTemplateRequest
import com.typesafe.config.ConfigFactory
import nl.wwbakker.deployment.elasticsearch.configuration.DeploymentConfiguration
import org.scalatest.{FlatSpec, _}

import scala.util.Try

class CloudformationRequestBuilderIntegrationSpec extends FlatSpec with Matchers {
  "The cloudformation template" should "validate according to AWS" in {
    val cloudFormationClient = AmazonCloudFormationClientBuilder.standard().build()
    // default deployment configuration
    val configuration: DeploymentConfiguration =
      new DeploymentConfiguration(ConfigFactory.parseFile(
        new File(Paths.get("src", "test", "resources", "test_overrides.conf").toString)))
    val validationResult = Try(cloudFormationClient.validateTemplate(
      new ValidateTemplateRequest().withTemplateBody(CloudformationRequestBuilder.cloudFormationTemplate(configuration))))

    // this matcher will print the error message when the validate service responds with an error
    validationResult.failed.toOption.map(_.getMessage) shouldBe None
    validationResult.isSuccess shouldBe true
  }
}
