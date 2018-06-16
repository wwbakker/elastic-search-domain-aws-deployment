## Cloudformation deployment script
# Introduction
Deploy an elastic search domain / cluster to Amazon Web Services automatically based on provided settings.

# Requirements
- AWS credentials and config set up [documentation](https://docs.aws.amazon.com/cli/latest/userguide/cli-config-files.html).
- Your AWS account must have the appropriate permissions
- You must enable a service-linked role to give Amazon ES permissions to access your VPC. [documentation](https://docs.aws.amazon.com/elasticsearch-service/latest/developerguide/slr-es.html)
- SBT

# Actions
- Create a configuration file where you configure the subnets where the elastic search domain should be hosted. For example:
```conf
vpc.subnetIds = ["subnet-f33e8f93", "subnet-8d00def1"]
```
- Run sbt
- Start the application
```sbt
sbt:elastic-search-domain-aws-deployment> run deploy c:\your\conf\file.conf
```