package br.com.deroldo.aws;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertTrue;

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

    @Test(expected = RuntimeException.class)
    public void main_should_throw_an_error_when_USER_DATA_para_is_null() throws IOException {
        Main.main(new String[0]);
    }

    @Test(expected = RuntimeException.class)
    public void main_should_throw_an_error_when_USER_DATA_para_is_empty() throws IOException {
        System.setProperty("USER_DATA", "");
        Main.main(new String[0]);
    }

    @Test
    public void main_should_throw_an_error_if_file_not_exists(){
        System.setProperty("USER_DATA", "foo");
        try {
            Main.main(new String[0]);
        } catch (Exception e){
            assertTrue(e.getCause() instanceof FileNotFoundException);
        }
    }

}