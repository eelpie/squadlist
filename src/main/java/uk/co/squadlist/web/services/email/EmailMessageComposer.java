package uk.co.squadlist.web.services.email;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineFactory;

import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.TextHelper;

@Component
public class EmailMessageComposer {

	private final static Logger log = Logger.getLogger(EmailMessageComposer.class);

	private final UrlBuilder urlBuilder;
	private final VelocityEngine velocityEngine;
	private final TextHelper textHelper;

	@Autowired
	public EmailMessageComposer(VelocityEngineFactory velocityEngineFactory, UrlBuilder urlBuilder, TextHelper textHelper) throws VelocityException, IOException {
		this.urlBuilder = urlBuilder;
		this.textHelper = textHelper;
		velocityEngine = velocityEngineFactory.createVelocityEngine();
	}

	public String composeNewMemberInviteMessage(Instance instance, Member member, String initialPassword) {
		try {
			Template template = velocityEngine.getTemplate("email/invite.vm");

			final VelocityContext context = new VelocityContext();
			context.put("instance", instance);
			context.put("instanceUrl", urlBuilder.getBaseUrl());
			context.put("member", member);
			context.put("initialPassword", initialPassword);
			context.put("text", textHelper);

			final StringWriter output = new StringWriter();
			template.merge(context, output);

			return output.toString();

		} catch (VelocityException e) {
			log.error(e);
			throw new RuntimeException();
		}
	}

}
