package com.matheuscirillo.aws.sdk.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final ScheduledExecutorService POC_QUEUE_POLLER_THREAD = Executors.newScheduledThreadPool(
            1,
            r -> new Thread(r, "poc-queue-poller"));

    private static final ScheduledExecutorService DLQ_POLLER_THREAD = Executors.newScheduledThreadPool(
            1,
            r -> new Thread(r, "dlq-queue-poller"));

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOG.info("App started");
        final SqsClient sqsClient = configureClient();
        LOG.info("SQS Client configured");

        SqsPoller javaPocPoller = new SqsPoller("java-poc-queue", sqsClient, message -> {
            String body = message.body();

            // force error in case of even numbers to send to DLQ
            if (Integer.parseInt(body) % 2 == 0) throw new RuntimeException("Error because it's a even number");
        });

        SqsPoller dlqPoller = new SqsPoller("java-poc-dlq", sqsClient, message -> {
            System.out.println("DLQ message body: " + message.body());
        });

        initJavaPocPoller(javaPocPoller::pollForMessages);
        initDlqPoll(dlqPoller::pollForMessages);

        LOG.info("End of main");

    }

    private static void initDlqPoll(Runnable runnable) {
        DLQ_POLLER_THREAD.scheduleWithFixedDelay(runnable, 0, 1, TimeUnit.MINUTES);
    }

    private static void initJavaPocPoller(Runnable runnable) {
        POC_QUEUE_POLLER_THREAD.scheduleWithFixedDelay(runnable, 0, 10, TimeUnit.SECONDS);
    }

    private static SqsClient configureClient() {
        return SqsClient.builder()
                .region(Region.SA_EAST_1)
                .credentialsProvider(credentialsProvider())
                .build();
    }

    private static AwsCredentialsProvider credentialsProvider() {
        return DefaultCredentialsProvider.create();
    }

}
