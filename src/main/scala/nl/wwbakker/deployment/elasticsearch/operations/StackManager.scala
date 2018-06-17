package nl.wwbakker.deployment.elasticsearch.operations

import java.time.LocalDateTime

import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder
import com.amazonaws.services.cloudformation.model.{CreateStackRequest, DeleteStackRequest, DescribeStackEventsRequest, DescribeStacksRequest}
import com.amazonaws.waiters.{Waiter, WaiterParameters}
import nl.wwbakker.deployment.elasticsearch.configuration.DeploymentConfiguration

import scala.collection.JavaConverters._
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
      case Success(sr) =>
        logInformationalMessage(s"Stack (id: ${sr.getStackId}) is being created.")
        await(cloudFormationClient.waiters().stackCreateComplete())
      case Failure(e) => logInformationalMessage(s"Stack creation failed: ${e.getMessage}")
    }

    logStackInformation
  }

  private def updateStack(implicit configuration: DeploymentConfiguration) : Unit = {
    logInformationalMessage("Stack exists, updating stack.")
    val request = CloudformationRequestBuilder.updateStackRequest(configuration)
    Try(cloudFormationClient.updateStack(request)) match {
      case Success(sr) =>
        logInformationalMessage(s"Stack (id: ${sr.getStackId}) is being updated.")
        await(cloudFormationClient.waiters().stackUpdateComplete())
      case Failure(e) => logInformationalMessage(s"Stack update failed: ${e.getMessage}")
    }

    logStackInformation
  }

  private def await(waiter : Waiter[DescribeStacksRequest])(implicit configuration: DeploymentConfiguration) : Unit =
    Try(waiter.run(new WaiterParameters[DescribeStacksRequest](describeStackRequest))) match {
      case Success(_) => logInformationalMessage("Done...")
      case Failure(e) => logErrorMessage(e.getMessage)
    }

  private def logStackInformation(implicit configuration: DeploymentConfiguration) : Unit = {
    logInformationalMessage("Retrieving stack information:")
    Try(cloudFormationClient.describeStacks(describeStackRequest)).map(_.getStacks.asScala.headOption) match {
      case Success(Some(stack)) =>
        logInformationalMessage(stack.toString)
        logInformationalMessage("Retrieving stack event information:")
        Try(cloudFormationClient.describeStackEvents(new DescribeStackEventsRequest().withStackName(configuration.stackName))).map(_.getStackEvents.asScala) match {
          case Success(stackEvents) =>
            stackEvents.map(_.toString).foreach(logInformationalMessage)
          case Failure(e) =>
            logErrorMessage("Could not retrieve stack events: " + e.getMessage)
        }
      case Success(None) => logErrorMessage("No stack information available.")
      case Failure(e) => logErrorMessage("Could not print stack information: " + e.getMessage)
    }
  }

  def undeploy(implicit configuration: DeploymentConfiguration): Unit = {
    logInformationalMessage("Deleting stack.")
    Try(cloudFormationClient.deleteStack(new DeleteStackRequest().withStackName(configuration.stackName))) match {
      case Success(_) =>
        await(cloudFormationClient.waiters().stackDeleteComplete())
      case Failure(e) =>
        logErrorMessage("Could not delete stack." + e.getMessage)
    }
  }

}
