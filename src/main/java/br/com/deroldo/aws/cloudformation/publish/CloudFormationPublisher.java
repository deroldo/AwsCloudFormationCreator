package br.com.deroldo.aws.cloudformation.publish;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.*;

import java.util.List;
import java.util.Optional;

/**
 * Copied and adapted from:
 * https://github.com/aws/aws-sdk-java/blob/master/src/samples/AwsCloudFormation/CloudFormationSample.java
 */
public class CloudFormationPublisher {

    public void publish(String awsYml) {
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("AWS credentials (file: ~/.aws/credentials) must be configured or the " +
                    "properties AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY must be provided", e);
        }

        AmazonCloudFormation stackBuilder = AmazonCloudFormationClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(Regions.fromName(System.getProperty("AWS_REGION")))
                .build();

        String stackName = Optional.ofNullable(System.getProperty("STACK_NAME")).orElseThrow(() -> new RuntimeException("The STACK_NAME must be provided"));

        System.out.println("===========================================");
        System.out.println("Getting Started with AWS CloudFormation");
        System.out.println("===========================================\n");

        DescribeStacksRequest describer = new DescribeStacksRequest();
        describer.setStackName(stackName);

        boolean stackAlreadyExists = !stackBuilder.describeStacks(describer).getStacks().isEmpty();

        if (stackAlreadyExists){
            UpdateStackRequest updateRequest = new UpdateStackRequest();
            updateRequest.setStackName(stackName);
            updateRequest.setTemplateBody(awsYml);
            System.out.println("Updating a stack called " + updateRequest.getStackName() + ".");
            stackBuilder.updateStack(updateRequest);
        } else {
            CreateStackRequest createRequest = new CreateStackRequest();
            createRequest.setStackName(stackName);
            createRequest.setTemplateBody(awsYml);
            System.out.println("Creating a stack called " + createRequest.getStackName() + ".");
            stackBuilder.createStack(createRequest);
        }

        try {
            System.out.println("Stack creation completed, the stack " + stackName + " completed with " + waitForCompletion(stackBuilder, stackName));

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it to " +
                    "AWS CloudFormation, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS CloudFormation, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String waitForCompletion(AmazonCloudFormation stackBuilder, String stackName) throws Exception {

        DescribeStacksRequest wait = new DescribeStacksRequest();
        wait.setStackName(stackName);
        Boolean completed = false;
        String stackStatus = "Unknown";
        String stackReason = "";

        System.out.print("Waiting");

        while (!completed) {
            List<Stack> stacks = stackBuilder.describeStacks(wait).getStacks();
            System.out.println(stacks);
            if (stacks.isEmpty()) {
                completed = true;
                stackStatus = "NO_SUCH_STACK";
                stackReason = "Stack has been deleted";
            } else {
                for (Stack stack : stacks) {
                    System.out.println(stack.getStackName() + " - " + stack.getStackStatus());
                    if (stack.getStackStatus().equals(StackStatus.CREATE_COMPLETE.toString()) ||
                            stack.getStackStatus().equals(StackStatus.CREATE_FAILED.toString()) ||
                            stack.getStackStatus().equals(StackStatus.ROLLBACK_FAILED.toString()) ||
                            stack.getStackStatus().equals(StackStatus.ROLLBACK_COMPLETE.toString()) ||
                            stack.getStackStatus().equals(StackStatus.DELETE_FAILED.toString())) {
                        completed = true;
                        stackStatus = stack.getStackStatus();
                        stackReason = stack.getStackStatusReason();
                    }
                }
            }

            // Show we are waiting
            System.out.print(".");

            // Not done yet so sleep for 10 seconds.
            if (!completed) Thread.sleep(10000);
        }

        // Show we are done
        System.out.print("done\n");

        return stackStatus + " (" + stackReason + ")";
    }

}
