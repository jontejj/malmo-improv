/* Copyright 2017 jonatan.jonsson
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

import java.io.IOException;

import com.improvisatorium.reservations.CurrentEvent;
import com.improvisatorium.reservations.Freemarker;
import com.improvisatorium.reservations.Reservation;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

public class TestSendgrid
{
	public static void main(String[] args) throws IOException
	{

		Reservation reservation = new Reservation();
		reservation.setName("Jonatan Test");
		reservation.setEmail("jontejj@gmail.com");
		reservation.setPaid(false);
		reservation.setPhone("+46705475383");
		reservation.setNrOfSeats(1);
		reservation.setDiscount("MAF-member");
		reservation.setPaid(true);
		reservation.setCancelled(false);
		reservation.setEventId(CurrentEvent.EVENT_ID);
		reservation.id = 1L;

		Email from = new Email("jontejj@gmail.com");
		String subject = "Sending with SendGrid is Fun Yes";
		Email to = new Email(reservation.getEmail());
		String htmlContent = Freemarker.generateTemplateWithData("event-reservation-confirmation-email.ftlh", reservation);
		Content content = new Content("text/html", htmlContent);
		Mail mail = new Mail(from, subject, to, content);

		SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));
		Request request = new Request();
		try
		{
			mail.addCategory("reservations");
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sg.api(request);
			System.out.println(response.getStatusCode());
			System.out.println(response.getBody());
			System.out.println(response.getHeaders());
		}
		catch(IOException ex)
		{
			throw ex;
		}
	}
}
