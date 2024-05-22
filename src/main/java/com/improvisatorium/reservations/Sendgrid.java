/* Copyright 2024 jonatanjonsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.improvisatorium.reservations;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.datastore.DatastoreException;
import com.googlecode.objectify.ObjectifyService;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

public class Sendgrid
{
	private static final Logger LOG = LoggerFactory.getLogger(Sendgrid.class);

	private static final String CONFIG_KEY_SENDGRID = "SENDGRID";

	private final String key;

	public Sendgrid()
	{
		key = ObjectifyService.run(() -> loadConfig(CONFIG_KEY_SENDGRID));
	}

	public static void init()
	{
		String sendgridKey = System.getProperty("config." + CONFIG_KEY_SENDGRID);
		if(sendgridKey != null)
		{
			Config alreadyExists = ofy().load().key(ObjectifyService.key(Config.class, CONFIG_KEY_SENDGRID)).now();
			if(alreadyExists == null)
			{
				Config config = new Config();
				config.setKey(CONFIG_KEY_SENDGRID);
				config.setValue(sendgridKey);
				ofy().save().entities(config).now();
			}
		}
	}

	private static String loadConfig(String key)
	{
		try
		{
			Config configEntry = ObjectifyService.ofy().load().key(ObjectifyService.key(Config.class, key)).now();
			if(configEntry == null)
				throw new IllegalStateException("No config for key: " + key);
			return configEntry.getValue();
		}
		catch(DatastoreException e)
		{
			e.printStackTrace();
			throw e;
		}
	}

	public void sendEmail(Reservation reservation, String subject, String template, String category, Event event)
	{
		Email from = new Email("noreply@improvisatorium.com", "Malmö Improvisatorium Reservations");
		Email replyTo = new Email("a.l.bobrick@gmail.com");
		Email to = new Email(reservation.getEmail(), reservation.getName());
		String emailText = Freemarker.generateTemplateWithData(template, reservation, event);
		Content content = new Content("text/html", emailText);
		Mail mail = new Mail(from, subject, to, content);
		mail.setReplyTo(replyTo);

		SendGrid sg = new SendGrid(key);
		Request request = new Request();
		try
		{
			mail.addCategory(category);
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sg.api(request);
			LOG.info("Returned status code: {}", response.getStatusCode());
			LOG.info("Body: {}", response.getBody());
			LOG.info("Headers: {}", response.getHeaders());
		}
		catch(IOException ex)
		{
			throw new EmailException("Failed to send reservation email to " + reservation.getEmail(), ex);
		}
	}
}
