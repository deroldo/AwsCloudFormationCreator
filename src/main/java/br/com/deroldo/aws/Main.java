package br.com.deroldo.aws;

import br.com.deroldo.aws.cloudformation.publish.CloudFormationPublisher;
import br.com.deroldo.aws.cloudformation.runner.CloudFormationExecutor;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;

public class Main {

    public static void main(String[] args) throws Exception {
        CloudFormationExecutor.execute(new CloudFormationPublisher(AmazonCloudFormationClientBuilder.standard()));
    }

}
