# AWS CloudFormation Creator

[![Build Status](https://travis-ci.org/deroldo/AwsCloudFormationCreator.svg?branch=master)](https://travis-ci.org/deroldo/AwsCloudFormationCreator)
[![Coverage Status](https://coveralls.io/repos/github/deroldo/AwsCloudFormationCreator/badge.svg?branch=master)](https://coveralls.io/github/deroldo/AwsCloudFormationCreator)

Templates to create environments and applications easier with CloudFormation on AWS

## Summary
<ul>
    <li>
        <a href='#usage-without-docker'>Usage without Docker</a>
        <ul>
            <li>
                <a href='#build'>Build</a>
            </li>
            <li>
                <a href='#then-use'>Then use</a>
            </li>
        </ul>
    </li>
    <li>
        <a href='#usage-with-docker'>Usage with Docker</a>
    </li>
    <li>
        <a href='#tips'>Tips</a>
        <ul>
            <li>
                <a href='#get-a-resource-id-from-cloudformation-stack'>Get a resource id from CloudFormation stack</a>
            </li>
            <li>
                <a href='#get-a-output-from-cloudformation-stack'>Get a output from CloudFormation stack</a>
            </li>
        </ul>
    </li>
    <li>
        <a href='#templates-and-sample'>Templates and sample</a>
    </li>
</ul>

## Usage without Docker

##### Build
```bash
git clone https://github.com/deroldo/AwsCloudFormationCreator.git \
    && cd AwsCloudFormationCreator \
    && ./gradlew clean build 
```

##### Then use
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

If you haven't `aws cli` installed, do it clicking <a href='https://docs.aws.amazon.com/cli/latest/userguide/installing.html'>here</a>

##### Get a resource id from CloudFormation stack:
```bash
aws cloudformation describe-stack-resources \
    --stack-name STACK_NAME \
    --logical-resource-id RESOURCE_ID \
    --query "(StackResources[].PhysicalResourceId)[0]"
```

Or do that in your template:
```bash
...
   AnyParam: ResourceId::STACK_NAME::RESOURCE_ID
...
```

<small>
PS.: Replace "STACK_NAME" and "RESOURCE_ID" for the correct value;
To use the template way, the AWS credentials must be provided.
</small>

##### Get a output from CloudFormation stack:
```bash
aws cloudformation describe-stacks \
    --stack-name STACK_NAME \
    --query 'Stacks[0].Outputs[?OutputKey==`OUTPUTKEY`].OutputValue' \
    --output text
```

Or do that in your template:
```bash
...
   AnyParam: Output::STACK_NAME::OUTPUTKEY
...
```

<small>
PS.: Replace "STACK_NAME" and "OUTPUTKEY" for the correct value;
To use the template way, the AWS credentials must be provided.
</small>

## Templates and sample

<a href='/templates'>Click here</a> to follow to templates and sample page