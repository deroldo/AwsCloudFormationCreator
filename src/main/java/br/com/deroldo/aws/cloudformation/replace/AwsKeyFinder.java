package br.com.deroldo.aws.cloudformation.replace;

import br.com.deroldo.aws.cloudformation.publish.AwsValueToGet;
import br.com.deroldo.aws.cloudformation.publish.CloudFormationPublisher;

public class AwsKeyFinder {

    public static String resourceId (String paramValue, CloudFormationPublisher publisher) {
        AwsValueToGet awsValue = new AwsValueToGet(paramValue);
        return publisher.getResourceId(awsValue, false);
    }

    public static String output (String paramValue, CloudFormationPublisher publisher) {
        AwsValueToGet awsValue = new AwsValueToGet(paramValue);
        return publisher.getOutput(awsValue, false);
    }
}
