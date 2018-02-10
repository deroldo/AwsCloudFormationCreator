package br.com.deroldo.aws;

import br.com.deroldo.aws.cloudformation.userdata.InterpreterUserData;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class Main {

    public static void main(String[] args) {
        String userData = System.getProperty("USER_DATA");
        if (isEmpty(userData)){
            throw new RuntimeException("Required param USER_DATA is missing");
        }
        String awsYml = new InterpreterUserData(userData).interpretAndGetYml();
        System.out.println(awsYml);
    }

}
