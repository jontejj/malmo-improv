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

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.schemaorg.JsonLdSerializer;
import com.google.schemaorg.JsonLdSyntaxException;
import com.google.schemaorg.core.CoreFactory;
import com.google.schemaorg.core.EventReservation;
import com.google.schemaorg.core.ReservationStatusTypeEnum;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class Freemarker
{
	private static final Configuration cfg;
	static
	{
		// SLF4JBridgeHandler.install();
		cfg = new Configuration(Configuration.VERSION_2_3_25);
		cfg.setClassForTemplateLoading(MyUI.class, "/email-templates/");
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
	}

	public static String generateTemplateWithData(String templateName, Reservation reservation, Event event) throws EmailException
	{
		long priceToPay = Prices.priceToPay(reservation.getNrOfSeats(), reservation.getDiscount(), event);
		Map<String, Object> map = reservationInformation(reservation, priceToPay, event);
		try
		{
			Template temp = cfg.getTemplate(templateName);
			StringWriter out = new StringWriter();
			temp.process(map, out);
			return out.toString();
		}
		catch(IOException | TemplateException ex)
		{
			throw new EmailException("Failed to generate reservation email", ex);
		}
	}

	static Map<String, Object> reservationInformation(Reservation reservation, long priceToPay, Event event)
	{
		ReservationStatusTypeEnum status = ReservationStatusTypeEnum.RESERVATION_PENDING;
		if(reservation.getPaid())
		{
			status = ReservationStatusTypeEnum.RESERVATION_CONFIRMED;
		}
		else if(reservation.getCancelled())
		{
			status = ReservationStatusTypeEnum.RESERVATION_CANCELLED;
		}
		/*
		 * TODO:
		 * "ticketToken": "qrCode:AB34",
		 * "ticketNumber": "abc123",
		 * "numSeats": "1"
		 */
		// TODO: set paid for the template somehow. status is a bit odd to use
		EventReservation eventReservation = CoreFactory.newEventReservationBuilder()
				// .addReservationId("" + reservation.id) //
				.addProperty("reservationNumber", "" + reservation.id) //
				.addReservationStatus(status) //
				.addUnderName(CoreFactory.newPersonBuilder() //
						.addName(reservation.getName()) //
						.addEmail(reservation.getEmail()) //
						.addTelephone(reservation.getPhone())) //
				.addProperty("numSeats", "" + reservation.getNrOfSeats()) //
				.addDescription(reservation.getNrOfSeats() + " seats") //
				.addReservationFor(schemaForEvent(event)) //
				.addTotalPrice(CoreFactory.newPriceSpecificationBuilder() //
						.addPriceCurrency(Config.CURRENCY) //
						.addPrice("" + priceToPay))
				.build();
		String asJsonLd = getAsJson(eventReservation);

		HashMap<String, Object> map = new HashMap<>(new Gson().fromJson(asJsonLd, Map.class));
		map.put("jsonLd", asJsonLd);
		return map;
	}

	private static final JsonLdSerializer serializer = new JsonLdSerializer(true /* setPrettyPrinting */);

	public static String getAsJson(EventReservation reservation)
	{
		try
		{
			return serializer.serialize(reservation);
		}
		catch(JsonLdSyntaxException | JsonIOException e)
		{
			throw new RuntimeException("Failed to generate schema.org string", e);
		}
	}

	public static com.google.schemaorg.core.Event schemaForEvent(Event event)
	{
		return CoreFactory.newTheaterEventBuilder() //
				.addUrl(event.getFacebookUrl()) //
				.addName(event.getName()) //
				.addImage(event.getPosterUrl()) //
				.addOrganizer(event.getOrganizer()) //
				.addStartDate(event.getStartTime().toString()) //
				.addDuration("PT1H30M") //
				.addLocation(event.getStage().place()) //
				.addProperty("phoneNumber", event.getPhoneNumber()).build();
	}
}
