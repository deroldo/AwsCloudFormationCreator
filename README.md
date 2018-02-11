# AWS CloudFormation Creator

[![Build Status](https://travis-ci.org/deroldo/AwsCloudFormationCreator.svg)](https://travis-ci.org/deroldo/AwsCloudFormationCreator)
[![Coverage Status](https://coveralls.io/repos/github/deroldo/AwsCloudFormationCreator/badge.svg)](https://coveralls.io/github/deroldo/AwsCloudFormationCreator)

## Templates to create environments and applications with CloudFormation on AWS

> How to get a resource id from an existing CloudFormation stack:
```bash
aws cloudformation describe-stack-resources --stack-name STACK_NAME --logical-resource-id RESOURCE_ID --query "(StackResources[].PhysicalResourceId)[0]"
```

<small>PS.: Replace "STACK_NAME" and "RESOURCE_ID" for the correct value</small>

> How to get a output value from an existing CloudFormation stack:
```bash
aws cloudformation describe-stacks --stack-name STACK_NAME --query 'Stacks[0].Outputs[?OutputKey==`OUTPUTKEY`].OutputValue' --output text
```

<small>PS.: Replace "STACK_NAME" and "OUTPUTKEY" for the correct value</small>