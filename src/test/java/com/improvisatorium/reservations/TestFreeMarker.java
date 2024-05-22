/* Copyright 2017 jonatanjonsson
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

import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class TestFreeMarker
{
	private static Configuration cfg;

	@BeforeAll
	public static void before()
	{
		cfg = new Configuration(Configuration.VERSION_2_3_25);
		cfg.setClassForTemplateLoading(MyUI.class, "/email-templates/");
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
	}

	@Test
	public void testThatTemplatesCanBeRendered()
	{
		/* ------------------------------------------------------------------------ */
		/* You should do this ONLY ONCE in the whole application life-cycle: */

		/* Create and adjust the configuration singleton */

		/* ------------------------------------------------------------------------ */
		/* You usually do these for MULTIPLE TIMES in the application life-cycle: */

		// TODO: set paid for the template somehow. status is a bit odd to use
		Reservation reservation = new Reservation();
		reservation.setName("Bob");
		reservation.setEmail("bob@example.com");
		reservation.setPaid(false);
		reservation.setPhone("1234");

		Event event = new Event();

		Map<String, Object> root = Freemarker.reservationInformation(reservation, 10L, event);

		// EventReservation eventReservation = CoreFactory.newEventReservationBuilder().addReservationId("" + reservation.id)
		// .addReservationStatus(ReservationStatusTypeEnum.RESERVATION_PENDING)
		// .addUnderName(CoreFactory.newPersonBuilder().addName(reservation.getName()).addEmail(reservation.getEmail())
		// .addTelephone(reservation.getPhone()))
		// .addDescription(reservation.getNrOfSeats() + " seats").addReservationFor(event)
		// .addTotalPrice(CoreFactory.newPriceSpecificationBuilder().addPriceCurrency(CURRENCY).addPrice(priceToPay.toString())).build();
		// String asJsonLd = getAsJson(eventReservation);
		//
		// HashMap<String, Object> map = new HashMap<>(new Gson().fromJson(asJsonLd, Map.class));
		// map.put("jsonLd", asJsonLd);
		// Map root = new HashMap();
		// root.put("name", "Big Joe");
		render("event-reservation-confirmation-email.ftlh", root);
		render("reservation-reminder.ftlh", root);
		render("reservation-confirmation.ftlh", root);
	}

	private void render(String template, Map<String, Object> root)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Writer out = new OutputStreamWriter(baos);
		Template reservationConfirmation;
		try
		{
			reservationConfirmation = cfg.getTemplate(template);
			reservationConfirmation.process(root, out);
		}
		catch(Exception e)
		{
			fail("Failed to render " + template + ", body so far: " + baos.toString(), e);
		}
	}
}
