## Cloudformation deployment script
# Introduction
Deploy an elastic search domain / cluster to Amazon Web Services automatically based on provided settings.

# Requirements
- AWS credentials and config set up [documentation](https://docs.aws.amazon.com/cli/latest/userguide/cli-config-files.html).
- Your AWS account must have the appropriate permissions
- You must enable a service-linked role to give Amazon ES permissions to access your VPC.
-- [official documentation](https://docs.aws.amazon.com/elasticsearch-service/latest/developerguide/slr-es.html)
-- You can also add the role after having aws cli tools installed by running the following command
```
aws iam create-service-linked-role --aws-service-name es.amazonaws.com
```
- SBT
- GIT

# Actions
- Check out the project
```bash
git clone https://github.com/wwbakker/elastic-search-domain-aws-deployment
```
- Create a configuration file where you configure the subnets where the elastic search domain should be hosted. For example:
```conf
vpc.subnetIds = ["subnet-f33e8f93", "subnet-8d00def1"]
```
- Run sbt inside the cloned repository
```bash
cd elastic-search-domain-aws-deployment
sbt
```
- Start the application
```sbt
sbt:elastic-search-domain-aws-deployment> run deploy c:\temp\test.conf
[info] Running nl.wwbakker.deployment.elasticsearch.Application deploy c:\temp\test.conf
2018-06-17T09:10:01.728 - Stack does not yet exist. Creating a new stack.
2018-06-17T09:10:40.458 - Stack (id: arn:aws:cloudformation:eu-west-1:539407351942:stack/example-elastic-search-stack/75a3be10-71fd-11e8-93e5-503ac9eaaa99) is being created.
2018-06-17T09:25:40.458 - Done...
2018-06-17T09:25:40.461 - Retrieving stack information:
2018-06-17T09:25:40.538 - {StackId: arn:aws:cloudformation:eu-west-1:539407351942:stack/example-elastic-search-stack/75a3be10-71fd-11e8-93e5-503ac9eaaa99,StackName: example-elastic-search-stack,Description: An elastic search cluster.,Parameters: [{ParameterKey: InstanceCount,ParameterValue: 1,}, {ParameterKey: ElasticsearchVersion,ParameterValue: 6.2,}, {ParameterKey: DedicatedMasterEnabled,ParameterValue: false,}, {ParameterKey: DedicatedMasterCount,ParameterValue: 3,}, {ParameterKey: InstanceType,ParameterValue: t2.small.elasticsearch,}, {ParameterKey: VolumeSize,ParameterValue: 10,}, {ParameterKey: ZoneAwarenessEnabled,ParameterValue: false,}, {ParameterKey: DedicatedMasterType,ParameterValue: t2.small.elasticsearch,}],CreationTime: Sun Jun 17 09:10:04 CEST 2018,RollbackConfiguration: {RollbackTriggers: [],},StackStatus: CREATE_COMPLETE,DisableRollback: false,NotificationARNs: [],Capabilities: [],Outputs: [{OutputKey: DomainEndpoint,OutputValue: vpc-elasticsearchdomain-d3pp5ps4vedri7t7poohnoqciq.eu-west-1.es.amazonaws.com,}, {OutputKey: DomainArn,OutputValue: arn:aws:es:eu-west-1:539407351942:domain/elasticsearchdomain,}],Tags: [],EnableTerminationProtection: false,}
2018-06-17T09:25:40.556 - Retrieving stack event information:
2018-06-17T09:25:40.653 - {StackId: arn:aws:cloudformation:eu-west-1:539407351942:stack/example-elastic-search-stack/75a3be10-71fd-11e8-93e5-503ac9eaaa99,EventId: 963adf80-71ff-11e8-b7c2-500c4267f861,StackName: example-elastic-search-stack,LogicalResourceId: example-elastic-search-stack,PhysicalResourceId: arn:aws:cloudformation:eu-west-1:539407351942:stack/example-elastic-search-stack/75a3be10-71fd-11e8-93e5-503ac9eaaa99,ResourceType: AWS::CloudFormation::Stack,Timestamp: Sun Jun 17 09:25:18 CEST 2018,ResourceStatus: CREATE_COMPLETE,}
[...]
```

# Undeploying
- Start the application
```sbt
sbt:elastic-search-domain-aws-deployment> run undeploy c:\temp\test.conf
[info] Packaging E:\Dev\Repos\elastic-search-domain-aws-deployment\target\scala-2.12\elastic-search-domain-aws-deployment_2.12-1.0.jar ...
[info] Done packaging.
[info] Running nl.wwbakker.deployment.elasticsearch.Application undeploy c:\temp\test.conf
2018-06-17T09:26:54.690 - Deleting stack.
2018-06-17T09:33:28.426 - Done...
[success] Total time: 401 s, completed 17-jun-2018 9:33:29
```
