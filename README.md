# AWS CloudFormation Creator

[![Build Status](https://travis-ci.org/deroldo/AwsCloudFormationCreator.svg?branch=master)](https://travis-ci.org/deroldo/AwsCloudFormationCreator)
[![Coverage Status](https://coveralls.io/repos/github/deroldo/AwsCloudFormationCreator/badge.svg?branch=master)](https://coveralls.io/github/deroldo/AwsCloudFormationCreator)

Templates to create environments and applications easier with CloudFormation on AWS

## Usage without Docker

##### Build
```bash
./gradlew clean build 
```

##### Then
> Basic:
```bash
java -jar \
    -DUSER_DATA=/my_full_path/file.yml \
    build/libs/AwsCloudFormationCreator.jar > ~/aws_data.yml
```

> To file:
```bash
java -jar \
    -DUSER_DATA=/my_full_path/file.yml \
    -DAWS_FILE=/destination_full_path/file_name.yml \
    build/libs/AwsCloudFormationCreator.jar 
```

> AWS publish:
```bash
java -jar \
    -DUSER_DATA=/my_full_path/file.yml \
    -DAWS_PUBLISH=/true \
    -DSTACK_NAME=my-stack-name \
    -DAWS_REGION=us-east-1 \
    build/libs/AwsCloudFormationCreator.jar 
```
<small>
PS: To know about AWS authentication <a href='https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html' target='_blank'>click here</a>
</small>

## Usage with Docker

> Basic:
```bash
docker run \
    -e USER_DATA='/data_dir/data.yml' \
    -v ~/my_template_data_dir:/data_dir \
    deroldo/awscloudformationcreator > ~/my_template_data_dir/aws.yml
```

> To file:
```bash
docker run \
    -e USER_DATA='/data_dir/data.yml' \
    -e AWS_FILE='/data_dir/aws.yml' \
    -v ~/my_template_data_dir:/data_dir \
    deroldo/awscloudformationcreator
```

> AWS publish:
```bash
docker run \
    -e USER_DATA='/data_dir/data.yml' \
    -e AWS_ACCESS_KEY_ID='MY_ACCESS_KEY_ID' \
    -e AWS_SECRET_ACCESS_KEY='MY_SECRET_ACCESS_KEY' \
    -e AWS_PUBLISH='true' \
    -e AWS_REGION='us-east-1' \
    -e STACK_NAME='my-stack-name' \
    -v ~/my_template_data_dir:/data_dir \
    deroldo/awscloudformationcreator
```

## Tips

> How to get a resource id from an existing CloudFormation stack:
```bash
aws cloudformation describe-stack-resources \
    --stack-name STACK_NAME \
    --logical-resource-id RESOURCE_ID \
    --query "(StackResources[].PhysicalResourceId)[0]"
```

<small>
PS.: Replace "STACK_NAME" and "RESOURCE_ID" for the correct value
</small>

> How to get a output value from an existing CloudFormation stack:
```bash
aws cloudformation describe-stacks \
    --stack-name STACK_NAME \
    --query 'Stacks[0].Outputs[?OutputKey==`OUTPUTKEY`].OutputValue' \
    --output text
```

<small>
PS.: Replace "STACK_NAME" and "OUTPUTKEY" for the correct value
</small>