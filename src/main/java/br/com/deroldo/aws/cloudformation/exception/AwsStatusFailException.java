package br.com.deroldo.aws.cloudformation.exception;

import static java.lang.String.format;

import com.amazonaws.services.cloudformation.model.Stack;

public class AwsStatusFailException extends RuntimeException {

    public AwsStatusFailException(Stack stack){
        super(format("Fail done with status %s and reason %s", stack.getStackStatus(), stack.getStackStatusReason()));
    }

}
