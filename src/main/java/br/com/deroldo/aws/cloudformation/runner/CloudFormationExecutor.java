package br.com.deroldo.aws.cloudformation.runner;

import static java.lang.Boolean.valueOf;
import static java.util.Collections.singleton;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import br.com.deroldo.aws.cloudformation.publish.CloudFormationPublisher;
import br.com.deroldo.aws.cloudformation.userdata.InterpreterUserData;
import org.apache.commons.lang3.StringUtils;

public class CloudFormationExecutor {

    public static void execute(CloudFormationPublisher publisher) throws Exception {
        String userData = System.getProperty("USER_DATA");
        if (isEmpty(userData)){
            throw new RuntimeException("Required param USER_DATA is missing");
        }
        Optional<String> awsPublish = Optional.ofNullable(System.getProperty("AWS_PUBLISH"));
        Optional<String> awsFile = Optional.ofNullable(System.getProperty("AWS_FILE"))
                .map(awsFilePath -> isEmpty(awsFilePath) ? null : awsFilePath);
        String awsYml = new InterpreterUserData(getInputStream(userData)).interpretAndGetYml();

        if (awsPublish.isPresent() && valueOf(awsPublish.get())){
            publisher.publish(awsYml);
        } else if (awsFile.isPresent()){
            writeAwsUserFile(awsFile, awsYml);
        } else {
            System.out.println(awsYml);
        }
    }

    private static void writeAwsUserFile(Optional<String> awsFile, String awsYml) throws IOException {
        Path file = Paths.get(awsFile.get());
        Files.createDirectories(file.getParent());
        if (!Files.exists(file)){
            Files.createFile(file);
        }
        Files.write(file, singleton(awsYml), Charset.forName("UTF-8"));
    }

    private static InputStream getInputStream(String fileName) {
        try {
            return new FileInputStream(new File(fileName));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
