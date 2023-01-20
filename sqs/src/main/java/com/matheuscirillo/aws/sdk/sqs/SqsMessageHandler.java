package com.matheuscirillo.aws.sdk.sqs;

import software.amazon.awssdk.services.sqs.model.Message;

@FunctionalInterface
public interface SqsMessageHandler {

    void handle(Message response);

}
