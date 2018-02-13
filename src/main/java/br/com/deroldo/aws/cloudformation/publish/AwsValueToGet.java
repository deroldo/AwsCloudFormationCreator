package br.com.deroldo.aws.cloudformation.publish;

public class AwsValueToGet {

    private String stackName;
    private String value;

    public AwsValueToGet (String paramValue){
        String[] split = paramValue.split("::");
        this.stackName = split[1];
        this.value = split[2];
    }

    public String getStackName () {
        return stackName;
    }

    public String getValue () {
        return value;
    }

}
