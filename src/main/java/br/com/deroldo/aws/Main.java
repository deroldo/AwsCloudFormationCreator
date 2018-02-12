package br.com.deroldo.aws;

import br.com.deroldo.aws.cloudformation.publish.CloudFormationPublisher;
import br.com.deroldo.aws.cloudformation.runner.CloudFormationExecutor;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;

public class Main {

    public static void main(String[] args) throws Exception {
        AmazonCloudFormationClientBuilder stackBuilder = AmazonCloudFormationClientBuilder.standard();
        DefaultAWSCredentialsProviderChain credentialsProvider = new DefaultAWSCredentialsProviderChain();
        CloudFormationPublisher publisher = new CloudFormationPublisher(stackBuilder, credentialsProvider);
        CloudFormationExecutor.execute(publisher);
    }

}
