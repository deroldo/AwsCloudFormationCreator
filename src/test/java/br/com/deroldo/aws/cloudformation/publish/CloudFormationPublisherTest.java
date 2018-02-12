package br.com.deroldo.aws.cloudformation.publish;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import br.com.deroldo.aws.cloudformation.exception.AwsStatusFailException;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackStatus;
import org.junit.Before;
import org.junit.Test;

public class CloudFormationPublisherTest {

    private static final String AWS_YML = "content";

    private AmazonCloudFormation cloudFormation;
    private AmazonCloudFormationClientBuilder stackBuilder;
    private CloudFormationPublisher publisher;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        System.setProperty("aws.accessKeyId", "myTestKeyId");
        System.setProperty("aws.secretKey", "myTestAccessKey");
        System.setProperty("AWS_REGION", "us-east-1");
        System.setProperty("STACK_NAME", "my-test-stack");

        changeDelay();

        this.stackBuilder = mock(AmazonCloudFormationClientBuilder.class);
        this.publisher = new CloudFormationPublisher(this.stackBuilder);

        this.cloudFormation = mock(AmazonCloudFormation.class);

        doReturn(this.stackBuilder).when(this.stackBuilder).withCredentials(any());
        doReturn(this.stackBuilder).when(this.stackBuilder).withRegion(any(Regions.class));
        doReturn(this.cloudFormation).when(this.stackBuilder).build();
    }

    @Test
    public void publish_should_throw_AmazonClientException_when_accessKeyId_is_not_provided() throws Exception {
        System.clearProperty("aws.accessKeyId");
        try {
            this.publisher.publish(AWS_YML);
            fail("Must throw AmazonClientException when autentication is not provided");
        } catch (AmazonClientException e) {
            assertTrue(e.getMessage().contains("AWS credentials not found"));
        }
    }

    @Test
    public void publish_should_throw_AmazonClientException_when_secretKey_is_not_provided() throws Exception {
        System.clearProperty("aws.secretKey");
        try {
            this.publisher.publish(AWS_YML);
            fail("Must throw AmazonClientException when autentication is not provided");
        } catch (AmazonClientException e) {
            assertTrue(e.getMessage().contains("AWS credentials not found"));
        }
    }

    @Test
    public void publish_should_throw_IllegalArgumentException_when_region_is_invalid() throws Exception {
        System.setProperty("AWS_REGION", "fooRegion");
        try {
            this.publisher.publish(AWS_YML);
            fail("Must throw IllegalArgumentException when an invalid AWS_REGION is provided");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("fooRegion"));
        }
    }

    @Test
    public void publish_should_throw_RuntimeException_when_region_is_null() throws Exception {
        System.clearProperty("AWS_REGION");
        try {
            this.publisher.publish(AWS_YML);
            fail("Must throw RuntimeException when AWS_REGION is not provided");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("The AWS_REGION must be provided"));
        }
    }

    @Test
    public void publish_should_throw_RuntimeException_when_stack_name_is_null() throws Exception {
        System.clearProperty("STACK_NAME");
        try {
            this.publisher.publish(AWS_YML);
            fail("Must throw RuntimeException when STACK_NAME is not provided");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("The STACK_NAME must be provided"));
        }
    }

    @Test
    public void publish_create_stack() throws Exception {
        Stack stack = mock(Stack.class);
        when(stack.getStackStatus())
                .thenReturn(StackStatus.CREATE_IN_PROGRESS.name())
                .thenReturn(StackStatus.CREATE_COMPLETE.name());

        DescribeStacksResult describeResult = mock(DescribeStacksResult.class);
        when(describeResult.getStacks())
                .thenReturn(new ArrayList<>())
                .thenReturn(singletonList(stack));

        doReturn(describeResult).when(this.cloudFormation).describeStacks(any());

        this.publisher.publish(AWS_YML);

        verify(stack, times(3)).getStackStatus();
        verify(this.cloudFormation).createStack(any());
        verify(this.cloudFormation, never()).updateStack(any());
    }

    @Test
    public void publish_update_stack() throws Exception {
        Stack stack = mock(Stack.class);
        when(stack.getStackStatus()).thenReturn(StackStatus.UPDATE_COMPLETE.name());

        DescribeStacksResult describeResult = mock(DescribeStacksResult.class);
        when(describeResult.getStacks()).thenReturn(singletonList(stack));

        doReturn(describeResult).when(this.cloudFormation).describeStacks(any());

        this.publisher.publish(AWS_YML);

        verify(stack, times(2)).getStackStatus();
        verify(this.cloudFormation).updateStack(any());
        verify(this.cloudFormation, never()).createStack(any());
    }

    @Test
    public void publish_when_stack_was_deleted() throws Exception {
        Stack stack = mock(Stack.class);
        when(stack.getStackStatus()).thenReturn(StackStatus.CREATE_COMPLETE.name());

        DescribeStacksResult describeResult = mock(DescribeStacksResult.class);
        when(describeResult.getStacks())
                .thenReturn(singletonList(stack))
                .thenReturn(new ArrayList<>());

        doReturn(describeResult).when(this.cloudFormation).describeStacks(any());

        this.publisher.publish(AWS_YML);

        verify(stack, never()).getStackStatus();
        verify(this.cloudFormation).updateStack(any());
        verify(this.cloudFormation, never()).createStack(any());
    }

    @Test(expected = AmazonClientException.class)
    public void publish_when_aws_throws_client_exception() throws Exception {
        Stack stack = mock(Stack.class);
        doThrow(AmazonClientException.class).when(stack).getStackStatus();

        DescribeStacksResult describeResult = mock(DescribeStacksResult.class);
        when(describeResult.getStacks()).thenReturn(singletonList(stack));

        doReturn(describeResult).when(this.cloudFormation).describeStacks(any());

        this.publisher.publish(AWS_YML);
    }

    @Test(expected = AmazonServiceException.class)
    public void publish_when_aws_throws_service_exception() throws Exception {
        Stack stack = mock(Stack.class);
        doThrow(AmazonServiceException.class).when(stack).getStackStatus();

        DescribeStacksResult describeResult = mock(DescribeStacksResult.class);
        when(describeResult.getStacks()).thenReturn(singletonList(stack));

        doReturn(describeResult).when(this.cloudFormation).describeStacks(any());

        this.publisher.publish(AWS_YML);
    }

    @Test(expected = AwsStatusFailException.class)
    public void publish_when_aws_throws_status_fail_exception_when_CREATE_FAILED() throws Exception {
        Stack stack = mock(Stack.class);
        when(stack.getStackStatus()).thenReturn(StackStatus.CREATE_FAILED.name());

        DescribeStacksResult describeResult = mock(DescribeStacksResult.class);
        when(describeResult.getStacks()).thenReturn(singletonList(stack));

        doReturn(describeResult).when(this.cloudFormation).describeStacks(any());

        this.publisher.publish(AWS_YML);
    }

    @Test(expected = AwsStatusFailException.class)
    public void publish_when_aws_throws_status_fail_exception_when_ROLLBACK_FAILED() throws Exception {
        Stack stack = mock(Stack.class);
        when(stack.getStackStatus()).thenReturn(StackStatus.ROLLBACK_FAILED.name());

        DescribeStacksResult describeResult = mock(DescribeStacksResult.class);
        when(describeResult.getStacks()).thenReturn(singletonList(stack));

        doReturn(describeResult).when(this.cloudFormation).describeStacks(any());

        this.publisher.publish(AWS_YML);
    }

    @Test(expected = AwsStatusFailException.class)
    public void publish_when_aws_throws_status_fail_exception_when_ROLLBACK_COMPLETE() throws Exception {
        Stack stack = mock(Stack.class);
        when(stack.getStackStatus()).thenReturn(StackStatus.ROLLBACK_COMPLETE.name());

        DescribeStacksResult describeResult = mock(DescribeStacksResult.class);
        when(describeResult.getStacks()).thenReturn(singletonList(stack));

        doReturn(describeResult).when(this.cloudFormation).describeStacks(any());

        this.publisher.publish(AWS_YML);
    }

    @Test(expected = AwsStatusFailException.class)
    public void publish_when_aws_throws_status_fail_exception_when_DELETE_FAILED() throws Exception {
        Stack stack = mock(Stack.class);
        when(stack.getStackStatus()).thenReturn(StackStatus.DELETE_FAILED.name());

        DescribeStacksResult describeResult = mock(DescribeStacksResult.class);
        when(describeResult.getStacks()).thenReturn(singletonList(stack));

        doReturn(describeResult).when(this.cloudFormation).describeStacks(any());

        this.publisher.publish(AWS_YML);
    }

    private void changeDelay() throws NoSuchFieldException, IllegalAccessException {
        Field delay = CloudFormationPublisher.class.getDeclaredField("DELAY");
        delay.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(delay, delay.getModifiers() & ~Modifier.FINAL);
        delay.set(null, 1);
    }

}