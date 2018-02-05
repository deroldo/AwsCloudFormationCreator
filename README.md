# AWS CloudFormation templates

## Templates to create environments and applications with CloudFormation on AWS

### HOW TO

> How to get a resource id from an existing CloudFormation stack:

<code>aws cloudformation describe-stack-resources --stack-name STACKNAME --logical-resource-id RESOURCENAME --query "(StackResources[].PhysicalResourceId)[0]"</code>

PS.: Replace "STACKNAME" and "RESOURCENAME" for the correct value

> How to get a output value from an existing CloudFormation stack:

<code>aws cloudformation describe-stacks --stack-name STACKNAME --query 'Stacks[0].Outputs[?OutputKey==\`OUTPUTKEY\`].OutputValue' --output text</code>

PS.: Replace "STACKNAME" and "OUTPUTKEY" for the correct value