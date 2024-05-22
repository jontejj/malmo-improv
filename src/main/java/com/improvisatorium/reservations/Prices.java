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

public class Prices
{
	private static int determinePriceModifier(String discount, Event event)
	{
		switch(discount)
		{
		// case "Folk Universitetet":
		// return folkUniPricePercentage;
		case "MAF-member":
		case "Student":
			return event.getMemberPricePercentage();
		case "Normal":
		default:
			return 100;
		}
	}

	public static long priceToPay(long nrOfSeats, String discount, Event event)
	{
		return event.getTicketPrice() * nrOfSeats * determinePriceModifier(discount, event) / 100;
	}

	public static long priceToPay(Reservation reservation, Event event)
	{
		return priceToPay(reservation.getNrOfSeats(), reservation.getDiscount(), event);
	}
}
