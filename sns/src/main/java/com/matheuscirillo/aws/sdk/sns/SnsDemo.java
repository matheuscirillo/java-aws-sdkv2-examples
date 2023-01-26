package com.matheuscirillo.aws.sdk.sns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

public class SnsDemo {

    private static final Logger LOG = LoggerFactory.getLogger(SnsDemo.class);

    public static void main(String[] args) {
        LOG.info("App started");
        final String topicArn = "arn:aws:sns:sa-east-1:963523016525:my-test-topic";

        // the try-with-resources is not actually needed. there's no connection persistently opened and no risk of leaks here
        // anyway, compiler was complaining about this, and then I decided to close the client, but is not strictly needed...
        try (SnsClient client = configureSnsClient()) {
            LOG.info("Sending message");
            PublishResponse publishResponse = client.publish(PublishRequest.builder()
                    .topicArn(topicArn)
                    .message("123")
                    .build());
            LOG.info("Message published. Response: {}", publishResponse);
        } catch (Exception e) {
            LOG.error("Exception occurred while sending message to SNS", e);
        }
        LOG.info("App finished");
    }

    private static SnsClient configureSnsClient() {
        return SnsClient.builder()
                .credentialsProvider(credentialsProvider())
                .region(Region.SA_EAST_1)
                .build();
    }

    private static AwsCredentialsProvider credentialsProvider() {
        return DefaultCredentialsProvider.create();
    }

}
