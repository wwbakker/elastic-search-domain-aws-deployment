package nl.wwbakker.deployment.elasticsearch.operations

import java.time.LocalDateTime

import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder
import com.amazonaws.services.cloudformation.model.{CreateStackRequest, DeleteStackRequest, DescribeStacksRequest}
import com.amazonaws.waiters.{Waiter, WaiterParameters}
import nl.wwbakker.deployment.elasticsearch.configuration.DeploymentConfiguration

import scala.util.{Failure, Success, Try}

object StackManager {
  private val cloudFormationClient = AmazonCloudFormationClientBuilder.standard().build()

  sealed trait StackExistence
  case object StackExists extends StackExistence
  case object StackDoesNotExist extends StackExistence

  private def logInformationalMessage(message : String) : Unit = System.out.println(s"${LocalDateTime.now.toString} - $message")
  private def logErrorMessage(message : String) : Unit = System.err.println(s"${LocalDateTime.now.toString} - Error: $message")

  class StackManagerException(message : String, cause : Option[Throwable] = None) extends Exception(message, cause.orNull)

  def deploy(implicit configuration: DeploymentConfiguration): Unit =
    stackExistence match {
      case StackDoesNotExist => createStack
      case StackExists => updateStack
    }

  private def stackExistence(implicit configuration: DeploymentConfiguration): StackExistence = {
    Try(cloudFormationClient.describeStacks(describeStackRequest)) match {
      case Success(_) => StackExists
      case Failure(e) if e.getMessage != null && e.getMessage.contains("does not exist") => StackDoesNotExist
      case Failure(e) => throw new StackManagerException("Could not determine whether or not stack already exists.", Some(e))
    }
  }

  private def describeStackRequest(implicit configuration: DeploymentConfiguration) : DescribeStacksRequest =
    new DescribeStacksRequest().withStackName(configuration.stackName)

  private def createStack(implicit configuration: DeploymentConfiguration): Unit = {
    logInformationalMessage("Stack does not yet exist. Creating a new stack.")
    val request : CreateStackRequest = CloudformationRequestBuilder.createStackRequest(configuration)
    Try(cloudFormationClient.createStack(request)) match {
      case Success(sr) => logInformationalMessage(s"Stack (id: ${sr.getStackId}) is being created.")
      case Failure(e) => logInformationalMessage(s"Stack creation failed: ${e.getMessage}")
    }
    await(cloudFormationClient.waiters().stackCreateComplete())
  }

  private def updateStack(implicit configuration: DeploymentConfiguration) : Unit = {
    logInformationalMessage("Stack exists, updating stack.")
    val request = CloudformationRequestBuilder.updateStackRequest(configuration)
    Try(cloudFormationClient.updateStack(request)) match {
      case Success(sr) => logInformationalMessage(s"Stack (id: ${sr.getStackId}) is being updated.")
      case Failure(e) => logInformationalMessage(s"Stack update failed: ${e.getMessage}")
    }
    await(cloudFormationClient.waiters().stackUpdateComplete())
  }

  private def await(waiter : Waiter[DescribeStacksRequest])(implicit configuration: DeploymentConfiguration) : Unit =
    Try(waiter.run(new WaiterParameters[DescribeStacksRequest](describeStackRequest))) match {
      case Success(_) => logInformationalMessage("Done...")
      case Failure(e) => logErrorMessage(e.getMessage)
    }

  def undeploy(implicit configuration: DeploymentConfiguration): Unit = {
    logInformationalMessage("Deleting stack.")
    cloudFormationClient.deleteStack(new DeleteStackRequest().withStackName(configuration.stackName))
    await(cloudFormationClient.waiters().stackDeleteComplete())
  }

}
