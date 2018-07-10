# AWS CloudFormation Creator

[![Build Status](https://travis-ci.org/deroldo/AwsCloudFormationCreator.svg?branch=master)](https://travis-ci.org/deroldo/AwsCloudFormationCreator)
[![Coverage Status](https://coveralls.io/repos/github/deroldo/AwsCloudFormationCreator/badge.svg?branch=master)](https://coveralls.io/github/deroldo/AwsCloudFormationCreator)

Templates to create environments and applications easier with CloudFormation on AWS

Back to <a href='https://github.com/deroldo/AwsCloudFormationCreator'>AWS CloudFormation Creator</a>

## Templates
<ul>
    <li>
        Environment
        <ul>
            <li>
                <a href='/templates/environment/cluster-ecs.yml'>Cluster ECS</a>
            </li>
            <li>
                <a href='/templates/environment/ecr.yml'>ECR - Docker image repository</a>
            </li>
        </ul>
    </li>
    <li>
        Application
        <ul>
            <li>
                <a href='/templates/application/ec2-running-on-cluster-ecs.yml'>EC2 running on cluster ECS</a>
            </li>
            <li>
                <a href='/templates/application/rds.yml'>RDS - Relational database</a>
            </li>
            <li>
                <a href='/templates/application/sns.yml'>SNS - Topic</a>
            </li>
            <li>
                <a href='/templates/application/sqs.yml'>SQS - Queue</a>
            </li>
            <li>
                <a href='/templates/application/sns_and_sqs.yml'>SQS subscribing a SNS</a>
            </li>
        </ul>
    </li>
</ul>

## Template sample

First, create a ECS cluster:
```bash
GlobalParameters:
  Environment: development
  ApplicationName: MyCluster
  Domain: my-domain.com

MyCluster:
  Template: ClusterEcs
```

Now, create an ECR repository:
```bash
Repository:
  Template: Ecr
  Name: my-application-name
```
You will need push your docker image to the ECR repository. See more <a href='https://docs.aws.amazon.com/AmazonECR/latest/userguide/docker-push-ecr-image.html'>here</a>

So, deploy your application on the cluster:
```bash
GlobalParameters:
  Environment: development
  ApplicationName: MyApp
  Domain: Output::STACK_NAME::Domain1

MyApp:
  Template: Ec2RunningOnClusterEcs
  ApiContainerPort: 80
  ApplicationImage: 111111111111.dkr.ecr.my-region.amazonaws.com/my-image:latest
  VpcArn: ResourceId::STACK_NAME::Vpc1
  HttpListenerArn: ResourceId::STACK_NAME::HttpListener1
  ListenerPriority: 1 # That can't be equal the priority number of another container
  ListenerPath: /my-app

MyRds:
  Template: Rds
  VpcArn: ResourceId::STACK_NAME::Vpc1
  DBParameterGroupName: ResourceId::STACK_NAME::MysqlParameterGroup1
  DBMasterUsername: my-username
  DBMasterUserPassword: my-password
  DNSPrivado: ResourceId::STACK_NAME::DNSPrivado1
  DBSubnet: ResourceId::STACK_NAME::DBSubnet1
  DBSecurityGroups: ResourceId::STACK_NAME::SgDefault1
```

## Tip

You can replace your docker image tag `latest` to `${gitHash}`. It will be replace for the provided parameter `GIT_HASH`, like that:

```bash
  ApplicationImage: 111111111111.dkr.ecr.my-region.amazonaws.com/my-image:${gitHash}
```

```bash
docker run \
    -e USER_DATA='/data_dir/data.yml' \
    -e GIT_HASH=$(git rev-parse --short HEAD) \
    -v ~/my_template_data_dir:/data_dir \
    deroldo/awscloudformationcreator
```