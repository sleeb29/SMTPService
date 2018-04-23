package com.service.smtp;

import com.service.smtp.handler.SMTPMessageHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageHandler;

@SpringBootApplication
@ComponentScan({"com.service.smtp.configuration"})
public class SmtpApplication {

	public static void main(String[] args) {

		SpringApplication.run(SmtpApplication.class, args);

		ClassPathXmlApplicationContext context =
				new ClassPathXmlApplicationContext(new String[] {"messageConsumerContext.xml",
						"integrationContext.xml",
				        "smtpContext.xml"});

		DirectChannel smtpApplicationChannel = (DirectChannel) context.getBean("smtpApplicationChannel");
		MessageHandler messageHandler = new SMTPMessageHandler();
		smtpApplicationChannel.subscribe(messageHandler);

	}
}
