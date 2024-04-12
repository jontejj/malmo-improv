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

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
@Cache
public class SeatsRemaining
{
	@Id private String eventId;

	private long seatsRemaining;

	public long getSeatsRemaining()
	{
		return seatsRemaining;
	}

	public SeatsRemaining setSeatsRemaining(long seatsRemaining)
	{
		this.seatsRemaining = seatsRemaining;
		return this;
	}

	public String getEventId()
	{
		return eventId;
	}

	public SeatsRemaining setEventId(String eventId)
	{
		this.eventId = eventId;
		return this;
	}

	public static SeatsRemaining load(Objectify ofy)
	{
		return ofy.load().key(ObjectifyService.key(SeatsRemaining.class, "" + CurrentEvent.EVENT_ID)).now();
	}
}
