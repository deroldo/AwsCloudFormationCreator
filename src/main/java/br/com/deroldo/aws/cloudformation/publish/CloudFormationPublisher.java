package br.com.deroldo.aws.cloudformation.publish;

import static com.amazonaws.services.cloudformation.model.StackStatus.CREATE_COMPLETE;
import static com.amazonaws.services.cloudformation.model.StackStatus.CREATE_FAILED;
import static com.amazonaws.services.cloudformation.model.StackStatus.DELETE_FAILED;
import static com.amazonaws.services.cloudformation.model.StackStatus.ROLLBACK_COMPLETE;
import static com.amazonaws.services.cloudformation.model.StackStatus.ROLLBACK_FAILED;
import static com.amazonaws.services.cloudformation.model.StackStatus.UPDATE_COMPLETE;
import static java.lang.String.format;
import static java.lang.System.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import br.com.deroldo.aws.cloudformation.exception.AwsStatusFailException;
import br.com.deroldo.aws.cloudformation.userdata.YmlData;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourceRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.UpdateStackRequest;

/**
 * Copied and adapted from:
 * https://github.com/aws/aws-sdk-java/blob/master/src/samples/AwsCloudFormation/CloudFormationSample.java
 */
public class CloudFormationPublisher {

    private static int DELAY = 10000;

    private AmazonCloudFormationClientBuilder stackBuilder;
    private AWSCredentialsProvider credentialsProvider;

    public CloudFormationPublisher(AmazonCloudFormationClientBuilder stackBuilder, AWSCredentialsProvider credentialsProvider){
        this.stackBuilder = stackBuilder;
        this.credentialsProvider = credentialsProvider;
    }

    public String getOutput (AwsValueToGet awsValue, boolean toPublish) {
        AmazonCloudFormation cloudFormation = getCloudFormation(toPublish);

        DescribeStacksRequest describer = new DescribeStacksRequest();
        describer.setStackName(awsValue.getStackName());

        return cloudFormation.describeStacks(describer).getStacks().stream()
                .map(Stack::getOutputs)
                .flatMap(Collection::stream)
                .filter(op -> op.getOutputKey().equals(awsValue.getValue()))
                .findFirst()
                .map(Output::getOutputValue)
                .orElseThrow(() -> new RuntimeException(format("Output %s from stack %s not found", awsValue.getValue(), awsValue.getStackName())));
    }

    public String getResourceId(AwsValueToGet awsValue, boolean toPublish) {
        AmazonCloudFormation cloudFormation = getCloudFormation(toPublish);

        DescribeStackResourceRequest describer = new DescribeStackResourceRequest();
        describer.setStackName(awsValue.getStackName());
        describer.setLogicalResourceId(awsValue.getValue());

        return cloudFormation.describeStackResource(describer)
                .getStackResourceDetail().getPhysicalResourceId();
    }

    public void publish(YmlData ymlData) throws Exception {
        AmazonCloudFormation cloudFormation = getCloudFormation(true);

        String stackName = Optional.ofNullable(System.getProperty("STACK_NAME")).orElseThrow(() -> new RuntimeException("The STACK_NAME must be provided"));

        out.println("===========================================");
        out.println("Starting publish your template");
        out.println("===========================================\n");

        DescribeStacksRequest describer = new DescribeStacksRequest();
        describer.setStackName(stackName);

        List<String> capabilitiesNames = ymlData.getCapabilities().stream()
                .map(Capability::name)
                .collect(Collectors.toList());
        uploadAwsYml(ymlData.getAwsYml(), cloudFormation, stackName, describer, capabilitiesNames);

        try {
            out.println("Stack creation completed, the stack " + stackName + " completed with " + waitForCompletion(cloudFormation, describer));

        } catch (AmazonServiceException ase) {
            out.println("Caught an AmazonServiceException, which means your request made it to " +
                    "AWS CloudFormation, but was rejected with an error response for some reason.");
            out.println("Error Message:    " + ase.getMessage());
            out.println("HTTP Status Code: " + ase.getStatusCode());
            out.println("AWS Error Code:   " + ase.getErrorCode());
            out.println("Error Type:       " + ase.getErrorType());
            out.println("Request ID:       " + ase.getRequestId());
            throw ase;
        } catch (AmazonClientException ace) {
            out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS CloudFormation, "
                    + "such as not being able to access the network.");
            out.println("Error Message: " + ace.getMessage());
            throw ace;
        }
    }

    private AmazonCloudFormation getCloudFormation (boolean toPublish) {
        Regions region = Optional.ofNullable(System.getProperty("AWS_REGION"))
                .map(awsRegion -> "".equals(awsRegion) ? null : awsRegion)
                .map(Regions::fromName)
                .orElseThrow(() -> new RuntimeException(toPublish ? "To publish, the AWS_REGION must be provided" : "The AWS_REGION must be provided to complete this template"));

        return this.stackBuilder.withCredentials(getCredentials())
                    .withRegion(region)
                    .build();
    }

    private void uploadAwsYml(String awsYml, AmazonCloudFormation cloudFormation, String stackName,
                              DescribeStacksRequest describer, List<String> capabilities) {
        try {
            boolean stackAlreadyExists = !cloudFormation.describeStacks(describer).getStacks().isEmpty();
            if (stackAlreadyExists){
                updateStack(awsYml, cloudFormation, stackName, capabilities);
            } else {
                createStack(awsYml, cloudFormation, stackName, capabilities);
            }
        } catch (AmazonCloudFormationException e){
            createStack(awsYml, cloudFormation, stackName, capabilities);
        }
    }

    private void createStack(String awsYml, AmazonCloudFormation cloudFormation, String stackName, List<String> capabilities) {
        CreateStackRequest createRequest = new CreateStackRequest();
        createRequest.setStackName(stackName);
        createRequest.setTemplateBody(awsYml);
        if (!capabilities.isEmpty()){
            createRequest.withCapabilities(capabilities);
        }
        out.println("Creating a stack called " + createRequest.getStackName() + ".");
        cloudFormation.createStack(createRequest);
    }

    private void updateStack (String awsYml, AmazonCloudFormation cloudFormation, String stackName, List<String> capabilities) {
        UpdateStackRequest updateRequest = new UpdateStackRequest();
        updateRequest.setStackName(stackName);
        updateRequest.setTemplateBody(awsYml);
        if (!capabilities.isEmpty()){
            updateRequest.withCapabilities(capabilities);
        }
        out.println("Updating a stack called " + updateRequest.getStackName() + ".");
        cloudFormation.updateStack(updateRequest);
    }

    private String waitForCompletion(AmazonCloudFormation cloudFormation, DescribeStacksRequest describer) throws Exception {
        Boolean completed = false;
        String stackStatus = "Unknown";
        String stackReason = "";

        out.print("Waiting");

        while (!completed) {
            List<Stack> stacks = cloudFormation.describeStacks(describer).getStacks();
            if (stacks.isEmpty()) {
                completed = true;
                stackStatus = "NO_SUCH_STACK";
                stackReason = "Stack has been deleted";
            } else {
                for (Stack stack : stacks) {
                    if (isFinalStatus(stack)) {
                        completed = true;
                        stackStatus = stack.getStackStatus();
                        stackReason = stack.getStackStatusReason();
                    }
                }
            }

            out.print(".");

            if (!completed) Thread.sleep(DELAY);
        }

        out.println(" done!");

        return stackStatus + " (" + stackReason + ")";
    }

    private boolean isFinalStatus(Stack stack){
        String stackStatus = stack.getStackStatus();
        if (isError(stackStatus)){
            throw new AwsStatusFailException(stack);
        }
        return isSuccess(stackStatus);
    }

    private boolean isError(String stackStatus) {
        return CREATE_FAILED.name().equals(stackStatus) ||
                ROLLBACK_FAILED.name().equals(stackStatus) ||
                ROLLBACK_COMPLETE.name().equals(stackStatus) ||
                DELETE_FAILED.name().equals(stackStatus);
    }

    private boolean isSuccess(String stackStatus) {
        return CREATE_COMPLETE.name().equals(stackStatus) || UPDATE_COMPLETE.name().equals(stackStatus);
    }

    private AWSCredentialsProvider getCredentials () {
        try {
            this.credentialsProvider.getCredentials();
            return this.credentialsProvider;
        } catch (Exception e) {
            throw new AmazonClientException("AWS credentials not found. See more: https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html", e);
        }
    }
}
