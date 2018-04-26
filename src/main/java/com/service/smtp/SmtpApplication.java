package com.service.smtp;

import com.service.smtp.configuration.SMTPConfig;
import com.service.smtp.handler.SMTPSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Properties;

@SpringBootApplication
public class SmtpApplication {

	public static void main(String[] args) {

		SpringApplication.run(SmtpApplication.class, args);

		ClassPathXmlApplicationContext context =
				new ClassPathXmlApplicationContext(new String[] {
				        "messageConsumerContext.xml",
						"integrationContext.xml"
				});

		SMTPConfig smtpConfig = (SMTPConfig) context.getBean("smtpConfig");
        SMTPSessionManager.init(smtpConfig);

	}
}
