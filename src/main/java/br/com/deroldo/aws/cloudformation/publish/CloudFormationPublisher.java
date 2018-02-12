package br.com.deroldo.aws.cloudformation.publish;

import static com.amazonaws.services.cloudformation.model.StackStatus.CREATE_COMPLETE;
import static com.amazonaws.services.cloudformation.model.StackStatus.CREATE_FAILED;
import static com.amazonaws.services.cloudformation.model.StackStatus.DELETE_FAILED;
import static com.amazonaws.services.cloudformation.model.StackStatus.ROLLBACK_COMPLETE;
import static com.amazonaws.services.cloudformation.model.StackStatus.ROLLBACK_FAILED;
import static com.amazonaws.services.cloudformation.model.StackStatus.UPDATE_COMPLETE;
import static java.lang.String.format;
import static java.lang.System.out;

import java.util.List;
import java.util.Optional;

import br.com.deroldo.aws.cloudformation.exception.AwsStatusFailException;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.UpdateStackRequest;

/**
 * Copied and adapted from:
 * https://github.com/aws/aws-sdk-java/blob/master/src/samples/AwsCloudFormation/CloudFormationSample.java
 */
public class CloudFormationPublisher {

    private static final int DELAY = 10000;

    private AmazonCloudFormationClientBuilder stackBuilder;

    public CloudFormationPublisher(AmazonCloudFormationClientBuilder stackBuilder){
        this.stackBuilder = stackBuilder;
    }

    public void publish(String awsYml) throws Exception {
        DefaultAWSCredentialsProviderChain credentialsProvider = new DefaultAWSCredentialsProviderChain();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("AWS credentials not found. See more: com.amazonaws.auth.DefaultAWSCredentialsProviderChain", e);
        }

        Regions region = Optional.ofNullable(System.getProperty("AWS_REGION"))
                .map(Regions::fromName)
                .orElseThrow(() -> new RuntimeException("The AWS_REGION must be provided"));

        AmazonCloudFormation cloudFormation = this.stackBuilder.withCredentials(credentialsProvider)
                .withRegion(region)
                .build();

        String stackName = Optional.ofNullable(System.getProperty("STACK_NAME")).orElseThrow(() -> new RuntimeException("The STACK_NAME must be provided"));

        out.println("===========================================");
        out.println("Getting Started with AWS CloudFormation");
        out.println("===========================================\n");

        DescribeStacksRequest describer = new DescribeStacksRequest();
        describer.setStackName(stackName);

        uploadAwsYml(awsYml, cloudFormation, stackName, describer);

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

    private void uploadAwsYml (final String awsYml, final AmazonCloudFormation cloudFormation, final String stackName,
            final DescribeStacksRequest describer) {
        boolean stackAlreadyExists = !cloudFormation.describeStacks(describer).getStacks().isEmpty();
        if (stackAlreadyExists){
            updateStack(awsYml, cloudFormation, stackName);
        } else {
            createStack(awsYml, cloudFormation, stackName);
        }
    }

    private void createStack (final String awsYml, final AmazonCloudFormation cloudFormation, final String stackName) {
        CreateStackRequest createRequest = new CreateStackRequest();
        createRequest.setStackName(stackName);
        createRequest.setTemplateBody(awsYml);
        out.println("Creating a stack called " + createRequest.getStackName() + ".");
        cloudFormation.createStack(createRequest);
    }

    private void updateStack (final String awsYml, final AmazonCloudFormation cloudFormation, final String stackName) {
        UpdateStackRequest updateRequest = new UpdateStackRequest();
        updateRequest.setStackName(stackName);
        updateRequest.setTemplateBody(awsYml);
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

}
