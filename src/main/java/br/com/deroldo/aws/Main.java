package br.com.deroldo.aws;

import br.com.deroldo.aws.cloudformation.publish.CloudFormationPublisher;
import br.com.deroldo.aws.cloudformation.runner.CloudFormationExecutor;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        CloudFormationExecutor.execute(new CloudFormationPublisher());
    }

}
