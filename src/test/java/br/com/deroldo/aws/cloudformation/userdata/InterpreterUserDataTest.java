package br.com.deroldo.aws.cloudformation.userdata;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import br.com.deroldo.aws.cloudformation.publish.CloudFormationPublisher;
import com.amazonaws.services.cloudformation.model.Capability;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.Test;

public class InterpreterUserDataTest {

    @Test
    public void interpret_simple_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_simple_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_simple_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_inside_list_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_inside_list_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_inside_list_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_inside_father_list_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_inside_father_list_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_inside_father_list_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_inside_grandfather_list_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_inside_grandfather_list_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_inside_grandfather_list_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_inside_father_and_grandfather_list_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_inside_father_and_grandfather_list_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_inside_father_and_grandfather_list_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_default_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_default_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_default_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_map_primitive_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_map_primitive_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_map_primitive_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_map_object_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_map_object_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_map_object_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_map_array_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_map_array_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_map_array_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_global_parameter_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_global_parameter_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_global_parameter_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_global_parameter_not_priority_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_global_parameter_not_priority_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_global_parameter_not_priority_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_aws_ref_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_aws_ref_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_aws_ref_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_resource_ref_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_resource_ref_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_resource_ref_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_list_inside_list_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_list_inside_list_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_list_inside_list_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_simple_number_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_simple_number_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_simple_number_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_sub_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_sub_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_sub_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_sub_resource_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_sub_resource_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_sub_resource_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_comma_delimited_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_comma_delimited_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_comma_delimited_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_inside_ref_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_inside_ref_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_inside_ref_expected_case.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_get_resource_id_case() throws IOException {
        CloudFormationPublisher publisher = mock(CloudFormationPublisher.class);
        doReturn("bar").when(publisher).getResourceId(any(), anyBoolean());
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_get_resource_id_case.yml")).interpretAndGetYmlData(publisher);
        String expectedYml = getFileContent("user_data/expected/ud_ref_simple_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_get_output_case() throws IOException {
        CloudFormationPublisher publisher = mock(CloudFormationPublisher.class);
        doReturn("bar").when(publisher).getOutput(any(), anyBoolean());
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_get_output_case.yml")).interpretAndGetYmlData(publisher);
        String expectedYml = getFileContent("user_data/expected/ud_ref_simple_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
    }

    @Test
    public void interpret_capability_delimited_case() throws IOException {
        YmlData ymlData = new InterpreterUserData(getInputStream("user_data/ud_ref_capability_case.yml")).interpretAndGetYmlData(null);
        String expectedYml = getFileContent("user_data/expected/ud_ref_capability_case_expected.yml");
        assertEquals(expectedYml, ymlData.getAwsYml());
        assertEquals(1, ymlData.getCapabilities().size());
        assertEquals(Capability.CAPABILITY_NAMED_IAM, ymlData.getCapabilities().get(0));
    }

    @Test
    public void interpret_non_unique_resource_name() throws IOException {
        try {
            new InterpreterUserData(getInputStream("user_data/ud_ref_non_unique_resource.yml")).interpretAndGetYmlData(null);
            fail("RuntimeException must be throw when duplicated resource name is present");
        } catch (MismatchedInputException e){
            assertTrue(e.getMessage().startsWith("Duplicate field 'MyData'"));
        }
    }

    @Test
    public void interpret_inside_ref_case_when_is_non_unique_resource_on_template() throws IOException {
        try {
            new InterpreterUserData(getInputStream("user_data/ud_ref_inside_ref_case_when_is_non_unique_resource_on_template_resource.yml")).interpretAndGetYmlData(null);
            fail("RuntimeException must be throw when resource is not unique on template");
        } catch (RuntimeException e){
            assertTrue(e.getMessage().equals("Is not possible to reference a resource from non unique resource template"));
        }
    }

    @Test
    public void interpret_with_missing_parameter() {
        try {
            new InterpreterUserData(getInputStream("user_data/ud_ref_parameter_is_missing.yml")).interpretAndGetYmlData(null);
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

}