package com.service.smtp.handler;

import com.service.smtp.configuration.SMTPConfig;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public final class SMTPSessionManager {

    static private int MAX_TRANSPORTS = 12;
    static private int MAX_RETRIES = 5;

    private Session session;
    private SMTPTransportPool smtpTransportPool;

    private String senderEmailAddress;

    private static class SMTPSessionManagerInstanceHolder {

        public static SMTPConfig smtpConfig;
        private static SMTPSessionManager instance = null;

        static {
            try {
                instance = new SMTPSessionManager(smtpConfig);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

    }

    private class SMTPTransportPool {

        private volatile HashMap<TransportWrapper, Date> transportPool;

        private class TransportWrapper {

            Transport transport;
            ReentrantLock lock;

            private TransportWrapper(Session session) throws MessagingException {
                this.lock = new ReentrantLock();
                this.transport = session.getTransport();
                this.transport.connect("smtpleeb@gmail.com", "LYaSDKa3wPc!");
            }

        }

        private SMTPTransportPool(Session session) throws MessagingException {

            transportPool = new HashMap<>();
            while(transportPool.size() < MAX_TRANSPORTS){
                TransportWrapper transportWrapper = new TransportWrapper(session);
                transportPool.put(transportWrapper, null);
            }

        }

        private TransportWrapper getTransport() throws Exception {
            return getTransport(MAX_RETRIES);
        }

        private TransportWrapper getTransport(int attemptsLeft) throws Exception {

            TransportWrapper transportWrapper = null;
            try {
                if (attemptsLeft == 0) {
                    throw new Exception("UNABLE TO CONNECT. Connection taking too long...");
                }

                for (Map.Entry<TransportWrapper, Date> entry : transportPool.entrySet()) {

                    Boolean successfulLock = entry.getKey().lock.tryLock();
                    if (successfulLock) {
                        return entry.getKey();
                    }

                }

            } finally {
                if(transportWrapper != null){
                    transportWrapper.transport.close();
                }
            }

            return getTransport(attemptsLeft--);

        }

        private void returnTransportToPool(TransportWrapper transportWrapper) {
            transportWrapper.lock.unlock();
        }

        private void destroyConnections() throws MessagingException {
            for(Map.Entry<TransportWrapper, Date> entry : transportPool.entrySet()){
                entry.getKey().transport.close();
                if(entry.getKey().lock.isLocked()){
                    entry.getKey().lock.unlock();
                }
            }
        }

    }

    private SMTPSessionManager(SMTPConfig smtpConfig) throws MessagingException {

        this.senderEmailAddress = smtpConfig.getSenderEmailAddress();

        Properties mailSessionProperties = new Properties();
        mailSessionProperties.setProperty("mail.transport.protocol", "smtp");
        mailSessionProperties.setProperty("mail.smtp.host", smtpConfig.getEmailHost());
        mailSessionProperties.setProperty("mail.smtp.auth", "true");
        mailSessionProperties.setProperty("mail.smtp.starttls.enable", "true");
        mailSessionProperties.setProperty("mail.smtp.socketFactory.port", smtpConfig.getEmailPort());
        mailSessionProperties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        mailSessionProperties.setProperty("mail.smtp.port", smtpConfig.getEmailPort());

        this.session = Session.getInstance(mailSessionProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmailAddress,
                        smtpConfig.getSenderPassword());
            }
        });

        this.smtpTransportPool = new SMTPTransportPool(this.session);

    }

    public static SMTPSessionManager init(SMTPConfig smtpConfig){
        smtpConfig = smtpConfig;
        return SMTPSessionManagerInstanceHolder.instance;
    }

    public static SMTPSessionManager getInstance(){
        return SMTPSessionManagerInstanceHolder.instance;
    }

    public void sendMessage(Message message, Address[] addresses) throws Exception {

        SMTPTransportPool.TransportWrapper transportWrapper = this.smtpTransportPool.getTransport();
        transportWrapper.transport.sendMessage(message, addresses);
        SMTPSessionManagerInstanceHolder.instance.smtpTransportPool.returnTransportToPool(transportWrapper);
    }

    public void cleanup() throws MessagingException {
        this.smtpTransportPool.destroyConnections();
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getSenderEmailAddress() {
        return senderEmailAddress;
    }

    public void setSenderEmailAddress(String senderEmailAddress) {
        this.senderEmailAddress = senderEmailAddress;
    }
}
