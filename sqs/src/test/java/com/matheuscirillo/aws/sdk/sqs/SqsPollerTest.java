package com.matheuscirillo.aws.sdk.sqs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SqsPollerTest {

    @Mock
    SqsClient sqsClient;

    @Mock
    SqsMessageHandler handler;

    @Test
    @DisplayName("Guarantee that message has been deleted")
    public void testIfMessageHasBeenDeleted() {
        when_SqsClient_getQueueUrl_IsCalled();
        when_SqsClient_receiveMessage_IsCalled();
        when_SqsMessageHandler_handle_IsCalled_With_Success();

        SqsPoller sqsPoller = new SqsPoller("", sqsClient, handler);
        sqsPoller.pollForMessages();

        assertThat_SqsClient_deleteMessage_HasBeenCalled();
    }

    @Test
    @DisplayName("Guarantee that message hasn't been deleted")
    public void testIfMessageHasntBeenDeleted() {
        when_SqsClient_getQueueUrl_IsCalled();
        when_SqsClient_receiveMessage_IsCalled();
        when_SqsMessageHandler_handle_IsCalled_With_Exception();

        SqsPoller sqsPoller = new SqsPoller("", sqsClient, handler);
        sqsPoller.pollForMessages();

        assertThat_SqsClient_deleteMessage_HasntBeenCalled();
    }

    // MOCKS
    public void when_SqsMessageHandler_handle_IsCalled_With_Success() {
        doNothing().when(handler).handle(any());
    }

    public void when_SqsMessageHandler_handle_IsCalled_With_Exception() {
        doThrow(new RuntimeException("Error while handling message"))
                .when(handler).handle(any());
    }

    public void when_SqsClient_receiveMessage_IsCalled() {
        doReturn(ReceiveMessageResponse.builder()
                .messages(List.of(Message.builder()
                        .receiptHandle("mocked")
                        .build()))
                .build()).when(sqsClient)
                .receiveMessage(any(Consumer.class));
    }

    public void when_SqsClient_getQueueUrl_IsCalled() {
        doReturn(GetQueueUrlResponse.builder().queueUrl("queue-url").build())
                .when(sqsClient).getQueueUrl(any(Consumer.class));
    }

    public void assertThat_SqsClient_deleteMessage_HasBeenCalled() {
        Mockito.verify(sqsClient, atLeast(1))
                .deleteMessage(any(Consumer.class));
    }

    public void assertThat_SqsClient_deleteMessage_HasntBeenCalled() {
        Mockito.verify(sqsClient, never())
                .deleteMessage(any(Consumer.class));
    }

}
