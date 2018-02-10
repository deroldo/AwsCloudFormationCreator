package br.com.deroldo.aws;

import br.com.deroldo.aws.cloudformation.userdata.InterpreterUserData;

public class Main {

    public static void main(String[] args) {
        String awsYml = new InterpreterUserData("teste").interpretAndGetYml();
        System.out.println(awsYml);
    }

}
