package com.service.smtp;

import com.service.smtp.handler.SMTPMessageHandler;
import com.service.smtp.handler.SMTPSessionManager;
import com.service.smtp.model.MessageQueueServiceData;
import com.service.smtp.model.Post;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.MessagingException;
import java.util.HashSet;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SMTPMessageHandlerTests {

    @Test
    public void testWithPracticePayload() throws MessagingException {

        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext(new String[] {"messageConsumerContext.xml",
                        "integrationContext.xml",
                        "smtpContext.xml"});

        DirectChannel smtpApplicationChannel = (DirectChannel) context.getBean("smtpApplicationChannel");
        MessageHandler messageHandler = new SMTPMessageHandler();
        smtpApplicationChannel.subscribe(messageHandler);

        int i = 0;
        while(i < 50) {

            System.out.println("at: " + i);

            Runnable runnableThread = new Runnable() {
                @Override
                public void run() {

                    MessageQueueServiceData messageQueueServiceData = new MessageQueueServiceData();

                    messageQueueServiceData.setTopic("test");
                    messageQueueServiceData.setService("SMTP");
                    Set<String> userIds = new HashSet<>();
                    userIds.add("testerleeb@umich.edu");
                    userIds.add("testerleeb@umich.edu");
                    messageQueueServiceData.setUserIds(userIds);
                    Message<MessageQueueServiceData> message = new GenericMessage<>(messageQueueServiceData);

                    Post post = new Post();
                    post.setTitle("this is title:" + Thread.currentThread());
                    post.setText("this is text:" + Thread.currentThread());

                    messageQueueServiceData.setPost(post);
                    smtpApplicationChannel.send(message);

                }
            };

            runnableThread.run();
            i++;

        }

        SMTPSessionManager smtpSessionManager = SMTPSessionManager.getInstance();
        smtpSessionManager.cleanup();

    }

    public static void assertConcurrent(final String message, final List<? extends Runnable> runnables, final int maxTimeoutSeconds) throws InterruptedException {
        final int numThreads = runnables.size();
        final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
        final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        try {
            final CountDownLatch allExecutorThreadsReady = new CountDownLatch(numThreads);
            final CountDownLatch afterInitBlocker = new CountDownLatch(1);
            final CountDownLatch allDone = new CountDownLatch(numThreads);
            for (final Runnable submittedTestRunnable : runnables) {
                threadPool.submit(new Runnable() {
                    public void run() {
                        allExecutorThreadsReady.countDown();
                        try {
                            afterInitBlocker.await();
                            submittedTestRunnable.run();
                        } catch (final Throwable e) {
                            exceptions.add(e);
                        } finally {
                            allDone.countDown();
                        }
                    }
                });
            }
            // wait until all threads are ready
            assertTrue("Timeout initializing threads! Perform long lasting initializations before passing runnables to assertConcurrent", allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS));
            // start all test runners
            afterInitBlocker.countDown();
            assertTrue(message +" timeout! More than" + maxTimeoutSeconds + "seconds", allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
        } finally {
            threadPool.shutdownNow();
        }
        assertTrue(message + "failed with exception(s)" + exceptions, exceptions.isEmpty());
    }

}
