package nl.wwbakker.deployment.elasticsearch.operations

import com.amazonaws.services.cloudformation.model.{CreateStackRequest, UpdateStackRequest, Parameter => AwsParameter}
import com.monsanto.arch.cloudformation.model.resource.VolumeType.GP2
import com.monsanto.arch.cloudformation.model.resource._
import com.monsanto.arch.cloudformation.model.{Parameter => MonsantoParameter, _}
import nl.wwbakker.deployment.elasticsearch.configuration.DeploymentConfiguration
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.collection.JavaConverters._

object CloudformationRequestBuilder {
  object Parameters {
    val InstanceCount = NumberParameter(name = "InstanceCount", Description = Some("The number of instances in the elastic search domain."))
    val InstanceType = StringParameter(name = "InstanceType", Description = Some("The instance type of the nodes."))
    val InstanceVolumeSize = NumberParameter(name = "VolumeSize", Description = Some("The size of each elastic search domain node's disk in GB."))

    val DedicatedMasterEnabled = BooleanParameter("DedicatedMasterEnabled", Description = Some("Indicates whether to use a dedicated master node for the Amazon ES domain. A dedicated master node is a cluster node that performs cluster management tasks, but doesn't hold data or respond to data upload requests. Dedicated master nodes offload cluster management tasks to increase the stability of your search clusters."))
    val DedicatedMasterCount = NumberParameter(name = "DedicatedMasterCount", Description = Some("The number of instances to use for the master node."))
    val DedicatedMasterType = StringParameter(name = "DedicatedMasterType", Description = Some("The hardware configuration of the computer that hosts the dedicated master node, such as m3.medium.elasticsearch. "))

    val ZoneAwarenessEnabled = BooleanParameter(name = "ZoneAwarenessEnabled", Description = Some("Indicates whether to enable zone awareness for the Amazon ES domain. When you enable zone awareness, Amazon ES allocates the nodes and replica index shards that belong to a cluster across two Availability Zones (AZs) in the same region to prevent data loss and minimize downtime in the event of node or data center failure."))
    val ElasticsearchVersion = StringParameter(name = "ElasticsearchVersion", Description = Some("The version of Elastic Search."))

    private def parameter(parameter : MonsantoParameter, value : Any) =
      new AwsParameter().withParameterKey(parameter.name).withParameterValue(value.toString).withUsePreviousValue(false)

    def apply(configuration : DeploymentConfiguration) : Seq[AwsParameter] = Seq(
      parameter(InstanceCount, configuration.instanceCount),
      parameter(InstanceType, configuration.instanceType),
      parameter(InstanceVolumeSize, configuration.instanceVolumeSize),
      parameter(DedicatedMasterEnabled, configuration.dedicatedMasterEnabled),
      parameter(DedicatedMasterCount, configuration.dedicatedMasterCount),
      parameter(DedicatedMasterType, configuration.dedicatedMasterType),
      parameter(ZoneAwarenessEnabled, configuration.zoneAwarenessEnabled),
      parameter(ElasticsearchVersion, configuration.elasticSearchVersion),
    )

  }
  import Parameters._


  private def elasticSearchResourceName = "ElasticSearchDomain"

  private def elasticSearchDomain(configuration: DeploymentConfiguration) = `AWS::Elasticsearch::Domain`(
    name = elasticSearchResourceName,
    DomainName = configuration.elasticSearchDomainName,
    AccessPolicies = Some(
      PolicyDocument(
        Version = Some(IAMPolicyVersion.`2012-10-17`),
        Statement = Seq(
          PolicyStatement(
            Effect = "Allow",
            Principal = Some(
              DefinedPrincipal(
                Map(
                  "AWS" -> "*"
                )
              )
            ),
            Action = Seq("es:*"),
            Resource = Some(Seq(
              `Fn::Sub`(
                template = "",
                subs = Some(Map("ElasticSearchDomainName" -> configuration.elasticSearchDomainName))
              )
            )),
          )
        )
      )),
    EBSOptions = Some(EBSOptions(
      EBSEnabled = Some(true),
      Iops = None,
      VolumeType = Some(GP2),
      VolumeSize = Some(ParameterRef(InstanceVolumeSize))
    )),
    AdvancedOptions = Some(Token.fromAny(
      Map[String, String](
        "rest.action.multi.allow_explicit_index" -> "true"
      )
    )),
    ElasticsearchClusterConfig = Some(
      ElasticsearchClusterConfig(
        DedicatedMasterCount = Some(ParameterRef(DedicatedMasterCount)),
        DedicatedMasterEnabled = Some(ParameterRef(DedicatedMasterEnabled)),
        DedicatedMasterType = Some(ParameterRef(DedicatedMasterType)),
        InstanceCount = Some(ParameterRef(InstanceCount)),
        InstanceType = Some(ParameterRef(InstanceType)),
        ZoneAwarenessEnabled = Some(ParameterRef(ZoneAwarenessEnabled))
      )
    ),
    ElasticsearchVersion = Some(ParameterRef(ElasticsearchVersion)),
    SnapshotOptions = Some(SnapshotOptions(
      AutomatedSnapshotStartHour = Some(0)
    )),
    VPCOptions = Some(VPCOptions(
      SubnetIds = configuration.subnetIds.map(Token.fromString)
    ))
  )

  private def outputs : Seq[Output[String]] = Seq(
    Output(
      name = "DomainArn",
      Value = `Fn::GetAtt`(Seq(elasticSearchResourceName, "DomainArn"))
    ),
    Output(
      name = "DomainEndpoint",
      Value = `Fn::GetAtt`(Seq(elasticSearchResourceName, "DomainEndpoint"))
    ),
  )

  private def template(configuration: DeploymentConfiguration): Template = Template(
    Description = Some(configuration.stackDescription),
    Parameters = Some(Seq(InstanceType, InstanceCount)),
    Resources = Seq(
      elasticSearchDomain(configuration)
    ),
    Outputs = Some(outputs)
  )

  def createStackRequest(configuration : DeploymentConfiguration) : CreateStackRequest =
    new CreateStackRequest()
      .withStackName(configuration.stackName)
      .withTemplateBody(template(configuration).toJson.prettyPrint)
      .withParameters(Parameters(configuration).asJavaCollection)

  def updateStackRequest(configuration : DeploymentConfiguration) : UpdateStackRequest =
    new UpdateStackRequest()
      .withStackName(configuration.stackName)
      .withTemplateBody(template(configuration).toJson.prettyPrint)
      .withParameters(Parameters(configuration).asJavaCollection)
}
