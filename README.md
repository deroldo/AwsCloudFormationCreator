# AWS CloudFormation templates

## Templates to create environments and applications with CloudFormation on AWS

> How to get a resource id from an existing CloudFormation stack:
```bash
aws cloudformation describe-stack-resources --stack-name STACKNAME --logical-resource-id RESOURCENAME --query "(StackResources[].PhysicalResourceId)[0]"
```

<small>PS.: Replace "STACKNAME" and "RESOURCENAME" for the correct value</small>

> How to get a output value from an existing CloudFormation stack:
```bash
aws cloudformation describe-stacks --stack-name STACKNAME --query 'Stacks[0].Outputs[?OutputKey==`OUTPUTKEY`].OutputValue' --output text
```

<small>PS.: Replace "STACKNAME" and "OUTPUTKEY" for the correct value</small>