package uk.co.squadlist.web.services.email;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailService {
	
	private final static Logger log = Logger.getLogger(EmailService.class);

	private final String smtpHost;
	private final int smtpPort;

	@Autowired	
	public EmailService(@Value("#{squadlist['smtp.host']}") String smtpHost, @Value("#{squadlist['smtp.port']}") int smtpPort) {
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
	}
	
	public void sendEmail(final String subject, final String from, final String body, final String to) throws EmailException {
		log.info("Sending email to: " + to);
		final Email email = new SimpleEmail();
		email.setHostName(smtpHost);
		email.setSmtpPort(smtpPort);
		email.setSSLOnConnect(false);
		email.setFrom(from);
		email.setSubject(subject);
		email.setMsg(body);
		email.addTo(to);		
		email.send();
	}

}
