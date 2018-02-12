package br.com.deroldo.aws.cloudformation.runner;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import br.com.deroldo.aws.MainTest;
import br.com.deroldo.aws.cloudformation.publish.CloudFormationPublisher;
import org.junit.Before;
import org.junit.Test;

public class CloudFormationExecutorTest {

    @Before
    public void setUp() {
        System.clearProperty("USER_DATA");
        System.clearProperty("AWS_FILE");
        System.clearProperty("AWS_PUBLISH");
    }

    @Test
    public void just_to_coverage(){
        new CloudFormationExecutor();
    }

    @Test
    public void main_should_execute() throws Exception {
        String userData = requireNonNull(MainTest.class.getClassLoader().getResource("user_data/ud_ref_simple_case.yml")).getPath();
        System.setProperty("USER_DATA", userData);
        CloudFormationExecutor.execute(null);
    }

    @Test
    public void main_should_execute_and_write_file() throws Exception {
        String userData = requireNonNull(MainTest.class.getClassLoader().getResource("user_data/ud_ref_simple_case.yml")).getPath();
        System.setProperty("USER_DATA", userData);
        String destineFile = System.getProperty("user.dir").concat("/build/aws/" + UUID.randomUUID().toString() + ".yml");
        assertFalse(Files.exists(Paths.get(destineFile)));
        System.setProperty("AWS_FILE", destineFile);
        CloudFormationExecutor.execute(null);
        assertTrue(Files.exists(Paths.get(destineFile)));
    }

    @Test
    public void main_should_execute_and_override_write_file() throws Exception {
        String userData = requireNonNull(MainTest.class.getClassLoader().getResource("user_data/ud_ref_simple_case.yml")).getPath();
        System.setProperty("USER_DATA", userData);
        String destineFile = System.getProperty("user.dir").concat("/build/aws/" + UUID.randomUUID().toString() + ".yml");
        Path destinePath = Paths.get(destineFile);
        Files.createDirectories(destinePath.getParent());
        String oldContent = "any content";
        Files.write(destinePath, singleton(oldContent));
        assertTrue(Files.exists(destinePath));
        System.setProperty("AWS_FILE", destineFile);
        CloudFormationExecutor.execute(null);
        assertTrue(Files.exists(destinePath));
        String fileContent = Files.readAllLines(destinePath).stream().reduce(String::concat).get();
        assertNotEquals(fileContent, oldContent);
    }

    @Test
    public void main_should_execute_and_publish() throws Exception {
        String userData = requireNonNull(MainTest.class.getClassLoader().getResource("user_data/ud_ref_simple_case.yml")).getPath();
        System.setProperty("USER_DATA", userData);
        System.setProperty("AWS_PUBLISH", "foo");

        CloudFormationPublisher publisher = mock(CloudFormationPublisher.class);
        CloudFormationExecutor.execute(publisher);

        verify(publisher, never()).publish(any());
    }

    @Test
    public void main_should_execute_and_publish_is_invalid() throws Exception {
        String userData = requireNonNull(MainTest.class.getClassLoader().getResource("user_data/ud_ref_simple_case.yml")).getPath();
        System.setProperty("USER_DATA", userData);
        System.setProperty("AWS_PUBLISH", "true");

        CloudFormationPublisher publisher = mock(CloudFormationPublisher.class);
        CloudFormationExecutor.execute(publisher);

        verify(publisher).publish(any());
    }

    @Test(expected = RuntimeException.class)
    public void main_should_throw_an_error_when_USER_DATA_para_is_null() throws Exception {
        CloudFormationExecutor.execute(null);
    }

    @Test(expected = RuntimeException.class)
    public void main_should_throw_an_error_when_USER_DATA_para_is_empty() throws Exception {
        System.setProperty("USER_DATA", "");
        CloudFormationExecutor.execute(null);
    }

    @Test
    public void main_should_throw_an_error_if_file_not_exists(){
        System.setProperty("USER_DATA", "foo");
        try {
            CloudFormationExecutor.execute(null);
        } catch (Exception e){
            assertTrue(e.getCause() instanceof FileNotFoundException);
        }
    }

}