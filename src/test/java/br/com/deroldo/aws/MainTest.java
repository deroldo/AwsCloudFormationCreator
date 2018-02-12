package br.com.deroldo.aws;

import org.junit.Test;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class MainTest {

    @Test
    public void just_to_coverage(){
        new Main();
    }

    @Test
    public void main_should_execute() throws IOException {
        String userData = requireNonNull(MainTest.class.getClassLoader().getResource("user_data/ud_ref_simple_case.yml")).getPath();
        System.setProperty("USER_DATA", userData);
        Main.main(new String[0]);
    }

}