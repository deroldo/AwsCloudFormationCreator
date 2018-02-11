package br.com.deroldo.aws.cloudformation.userdata;

import br.com.deroldo.aws.cloudformation.replace.AttributeIndexAppender;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.*;

public class InterpreterUserDataTest {

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        resetAtomicInteger();
    }

    @Test
    public void interpret_simple_case() throws IOException {
        String yml = new InterpreterUserData(getInputStream("user_data/ud_ref_simple_case.yml")).interpretAndGetYml();
        String expectedYml = getFileContent("user_data/expected/ud_ref_simple_case_expected.yml");
        assertEquals(expectedYml, yml);
    }

    @Test
    public void interpret_inside_list_case() throws IOException {
        String yml = new InterpreterUserData(getInputStream("user_data/ud_ref_inside_list_case.yml")).interpretAndGetYml();
        String expectedYml = getFileContent("user_data/expected/ud_ref_inside_list_case_expected.yml");
        assertEquals(expectedYml, yml);
    }

    @Test
    public void interpret_inside_father_list_case() throws IOException {
        String yml = new InterpreterUserData(getInputStream("user_data/ud_ref_inside_father_list_case.yml")).interpretAndGetYml();
        String expectedYml = getFileContent("user_data/expected/ud_ref_inside_father_list_case_expected.yml");
        assertEquals(expectedYml, yml);
    }

    @Test
    public void interpret_inside_grandfather_list_case() throws IOException {
        String yml = new InterpreterUserData(getInputStream("user_data/ud_ref_inside_grandfather_list_case.yml")).interpretAndGetYml();
        String expectedYml = getFileContent("user_data/expected/ud_ref_inside_grandfather_list_case_expected.yml");
        assertEquals(expectedYml, yml);
    }

    @Test
    public void interpret_inside_father_and_grandfather_list_case() throws IOException {
        String yml = new InterpreterUserData(getInputStream("user_data/ud_ref_inside_father_and_grandfather_list_case.yml")).interpretAndGetYml();
        String expectedYml = getFileContent("user_data/expected/ud_ref_inside_father_and_grandfather_list_case_expected.yml");
        assertEquals(expectedYml, yml);
    }

    @Test
    public void interpret_default_case() throws IOException {
        String yml = new InterpreterUserData(getInputStream("user_data/ud_ref_default_case.yml")).interpretAndGetYml();
        String expectedYml = getFileContent("user_data/expected/ud_ref_default_case_expected.yml");
        assertEquals(expectedYml, yml);
    }

    @Test
    public void interpret_map_primitive_case() throws IOException {
        String yml = new InterpreterUserData(getInputStream("user_data/ud_ref_map_primitive_case.yml")).interpretAndGetYml();
        String expectedYml = getFileContent("user_data/expected/ud_ref_map_primitive_case_expected.yml");
        assertEquals(expectedYml, yml);
    }

    @Test
    public void interpret_map_object_case() throws IOException {
        String yml = new InterpreterUserData(getInputStream("user_data/ud_ref_map_object_case.yml")).interpretAndGetYml();
        String expectedYml = getFileContent("user_data/expected/ud_ref_map_object_case_expected.yml");
        assertEquals(expectedYml, yml);
    }

    @Test
    public void interpret_map_array_case() throws IOException {
        String yml = new InterpreterUserData(getInputStream("user_data/ud_ref_map_array_case.yml")).interpretAndGetYml();
        String expectedYml = getFileContent("user_data/expected/ud_ref_map_array_case_expected.yml");
        assertEquals(expectedYml, yml);
    }

    @Test
    public void interpret_global_parameter_case() throws IOException {
        String yml = new InterpreterUserData(getInputStream("user_data/ud_ref_global_parameter_case.yml")).interpretAndGetYml();
        String expectedYml = getFileContent("user_data/expected/ud_ref_global_parameter_case_expected.yml");
        assertEquals(expectedYml, yml);
    }

    @Test
    public void interpret_global_parameter_not_priority_case() throws IOException {
        String yml = new InterpreterUserData(getInputStream("user_data/ud_ref_global_parameter_not_priority_case.yml")).interpretAndGetYml();
        String expectedYml = getFileContent("user_data/expected/ud_ref_global_parameter_not_priority_case_expected.yml");
        assertEquals(expectedYml, yml);
    }

    @Test
    public void interpret_aws_ref_case() throws IOException {
        String yml = new InterpreterUserData(getInputStream("user_data/ud_ref_aws_ref_case.yml")).interpretAndGetYml();
        String expectedYml = getFileContent("user_data/expected/ud_ref_aws_ref_case_expected.yml");
        assertEquals(expectedYml, yml);
    }

    @Test
    public void interpret_resource_ref_case() throws IOException {
        String yml = new InterpreterUserData(getInputStream("user_data/ud_ref_resource_ref_case.yml")).interpretAndGetYml();
        String expectedYml = getFileContent("user_data/expected/ud_ref_resource_ref_case_expected.yml");
        assertEquals(expectedYml, yml);
    }

    @Test
    public void interpret_list_inside_list_case() throws IOException {
        String yml = new InterpreterUserData(getInputStream("user_data/ud_ref_list_inside_list_case.yml")).interpretAndGetYml();
        String expectedYml = getFileContent("user_data/expected/ud_ref_list_inside_list_case_expected.yml");
        assertEquals(expectedYml, yml);
    }

    @Test
    public void interpret_simple_number_case() throws IOException {
        String yml = new InterpreterUserData(getInputStream("user_data/ud_ref_simple_number_case.yml")).interpretAndGetYml();
        String expectedYml = getFileContent("user_data/expected/ud_ref_simple_number_case_expected.yml");
        assertEquals(expectedYml, yml);
    }

    @Test
    public void interpret_with_missing_parameter() {
        try {
            new InterpreterUserData(getInputStream("user_data/ud_ref_parameter_is_missing.yml")).interpretAndGetYml();
            fail("Parameter missing exception must be throw");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("[Foo]"));
        }
    }

    private InputStream getInputStream(String fileName) {
        return InterpreterUserData.class.getClassLoader().getResourceAsStream(fileName);
    }

    private String getFileContent(String fileName) throws IOException {
        return Files.readAllLines(Paths.get(requireNonNull(InterpreterUserData.class.getClassLoader().getResource(fileName)).getPath()))
                .stream().reduce((s1, s2) -> s1.concat("\n").concat(s2)).get();
    }

    private void resetAtomicInteger() throws NoSuchFieldException, IllegalAccessException {
        Field atomicInteger = AttributeIndexAppender.class.getDeclaredField("ATOMIC_INTEGER");
        atomicInteger.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(atomicInteger, atomicInteger.getModifiers() & ~Modifier.FINAL);
        atomicInteger.set(null, new AtomicInteger());
    }

}