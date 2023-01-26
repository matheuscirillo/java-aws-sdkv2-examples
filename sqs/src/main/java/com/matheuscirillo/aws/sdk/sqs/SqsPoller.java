package com.matheuscirillo.aws.sdk.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

public class SqsPoller {

    private static final Logger LOG = LoggerFactory.getLogger(SqsDemo.class);

    protected SqsClient client;
    protected String queueUrl;
    protected String queueName;
    protected SqsMessageHandler handler;

    public SqsPoller(String queueName, SqsClient client, SqsMessageHandler handler) {
        this.client = client;
        this.queueName = queueName;
        this.queueUrl = this.client.getQueueUrl(builder -> builder
                .queueName(queueName)
                .build()).queueUrl();
        this.handler = handler;
    }

    public void pollForMessages() {
        try {
            LOG.info("Queue: {} - Polling for messages", this.queueName);
            ReceiveMessageResponse response = this.client.receiveMessage(builder -> builder.queueUrl(queueUrl)
                    .waitTimeSeconds(15)
                    .maxNumberOfMessages(10));
            LOG.info("Queue: {} - " + response.messages().size() + " messages received", this.queueName);

            for (Message message : response.messages()) {
                String msgBody = message.body();
                LOG.info("Queue: {} - Message body: {}", this.queueName, msgBody);

                LOG.info("Queue: {} - Delegating the message handler", this.queueName);
                try {
                    handler.handle(message);
                    LOG.info("Queue: {} - Deleting message", this.queueName);
                    this.client.deleteMessage(builder -> builder
                            .queueUrl(this.queueUrl)
                            .receiptHandle(message.receiptHandle()));
                    LOG.info("Queue: {} - Message deleted", this.queueName);
                } catch (Exception e) {
                    LOG.error("Queue: {} - Error while reading message {} from queue {}", this.queueName, message.messageId(), this.queueName, e);
                }
            }
        } catch (Exception e) {
            LOG.info("Queue: {} - Unhandled error occured while reading from queue {}", this.queueName, this.queueName);
        }
    }

}
