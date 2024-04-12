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

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.google.schemaorg.core.CoreFactory;
import com.googlecode.objectify.ObjectifyService;

public class CurrentEvent
{
	public static final long EVENT_ID = 38;
	public static final String CURRENCY = "SEK";
	public static final String PHONENUMBER_TO_PAY_TO = "0764088570";
	public static final long INITIAL_SEAT_CAPACITY = 31;
	public static final BigDecimal TICKET_PRICE = new BigDecimal("80");
	public static final BigDecimal MEMBER_PRICE_PERCENTAGE = new BigDecimal("0.75");
	// private static final BigDecimal folkUniPricePercentage = new BigDecimal("0.60");

	public static final String EVENT_URL = "https://www.facebook.com/events/336134925708837";
	/**
	 * https://console.cloud.google.com/storage/browser?folder=&organizationId=&project=malmo-improv
	 */
	public static final String IMAGE_LINK = "https://storage.googleapis.com/malmo-improv.appspot.com/events/blue-cheese.jpg";
	public static final String EVENT_NAME = "The Blue Cheese Show - Improv Show";
	public static final com.google.schemaorg.core.Event EVENT = CoreFactory.newTheaterEventBuilder() //
			.addUrl(EVENT_URL) //
			.addName(EVENT_NAME) //
			.addImage(IMAGE_LINK) //
			.addOrganizer("Malmö Improvisatorium") //
			.addStartDate("2024-04-20T18:00:00+02:00") //
			.addDuration("PT1H30M") //
			.addLocation(Stages.STAGE_1) //
			.addProperty("phoneNumber", PHONENUMBER_TO_PAY_TO).build();

	public static void init()
	{
		SeatsRemaining now = ofy().load().key(ObjectifyService.key(SeatsRemaining.class, "" + EVENT_ID)).now();
		if(now == null)
		{
			ofy().save().entities(new SeatsRemaining().setEventId("" + EVENT_ID).setSeatsRemaining(INITIAL_SEAT_CAPACITY)).now();
		}
	}

	private static BigDecimal determinePriceModifier(String discount)
	{
		switch(discount)
		{
		// case "Folk Universitetet":
		// return folkUniPricePercentage;
		case "MAF-member":
		case "Student":
			return CurrentEvent.MEMBER_PRICE_PERCENTAGE;
		case "Normal":
		default:
			return BigDecimal.ONE;
		}
	}

	public static BigDecimal priceToPay(long nrOfSeats, String discount)
	{
		return CurrentEvent.TICKET_PRICE.multiply(new BigDecimal(nrOfSeats)).multiply(determinePriceModifier(discount))
				.setScale(0, RoundingMode.HALF_UP);
	}

	public static BigDecimal priceToPay(Reservation reservation)
	{
		return CurrentEvent.TICKET_PRICE.multiply(new BigDecimal(reservation.getNrOfSeats()))
				.multiply(determinePriceModifier(reservation.getDiscount())).setScale(0, RoundingMode.HALF_UP);
	}
}
