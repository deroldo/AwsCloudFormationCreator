package br.com.deroldo.aws;

import br.com.deroldo.aws.cloudformation.userdata.InterpreterUserData;

import java.io.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class Main {

    public static void main(String[] args) throws IOException {
        String userData = System.getProperty("USER_DATA");
        if (isEmpty(userData)){
            throw new RuntimeException("Required param USER_DATA is missing");
        }
        String awsYml = new InterpreterUserData(getInputStream(userData)).interpretAndGetYml();
        System.out.println(awsYml);
    }

    private static InputStream getInputStream(String fileName) {
        try {
            return new FileInputStream(new File(fileName));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
