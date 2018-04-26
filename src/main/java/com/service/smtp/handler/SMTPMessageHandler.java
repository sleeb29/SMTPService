package com.service.smtp.handler;

import com.service.smtp.model.MessageQueueServiceData;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Set;

public class SMTPMessageHandler implements MessageHandler {

    private String emailTextType = "text/plain";

    @Override
    public void handleMessage(org.springframework.messaging.Message<?> message) throws MessagingException {

        SMTPSessionManager smtpSessionManager = SMTPSessionManager.getInstance();

        MessageQueueServiceData messageQueueServiceData = (MessageQueueServiceData) message.getPayload();

        Set<String> userIds = messageQueueServiceData.getUserIds();
        Address[] recipients = new Address[userIds.size()];
        int i = 0;
        for(String user : userIds){
            try {
                recipients[i] = new InternetAddress(user);
            } catch (AddressException e) {
                e.printStackTrace();
            }
            i++;
        }

        javax.mail.Message smtpMessage = new MimeMessage(smtpSessionManager.getSession());

        try {

            smtpMessage.setSubject(messageQueueServiceData.getPost().getTitle());
            smtpMessage.setContent(messageQueueServiceData.getPost().getText(),emailTextType);
            smtpMessage.setFrom(new InternetAddress(smtpSessionManager.getSenderEmailAddress()));
            smtpMessage.addRecipients(javax.mail.Message.RecipientType.BCC, recipients);
            smtpSessionManager.sendMessage(smtpMessage, recipients);

        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
