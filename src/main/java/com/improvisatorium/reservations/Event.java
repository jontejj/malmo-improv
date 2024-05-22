/*
 * Copyright 2018 jonatan.jonsson
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.improvisatorium.reservations;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public final class Event implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id Long id;

	@NotBlank String phoneNumber;

	@NotNull Integer ticketPrice;
	@NotNull Integer memberPricePercentage;
	@NotNull String facebookUrl;
	@NotBlank String name;
	@NotBlank String organizer;
	@NotNull LocalDateTime startTime;
	@NotNull Stages stage;
	String posterUrl;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getPhoneNumber()
	{
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
	}

	public Integer getTicketPrice()
	{
		return ticketPrice;
	}

	public void setTicketPrice(Integer ticketPrice)
	{
		this.ticketPrice = ticketPrice;
	}

	public Integer getMemberPricePercentage()
	{
		return memberPricePercentage;
	}

	public void setMemberPricePercentage(Integer memberPricePercentage)
	{
		this.memberPricePercentage = memberPricePercentage;
	}

	public String getFacebookUrl()
	{
		return facebookUrl;
	}

	public void setFacebookUrl(String facebookUrl)
	{
		this.facebookUrl = facebookUrl;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getOrganizer()
	{
		return organizer;
	}

	public void setOrganizer(String organizer)
	{
		this.organizer = organizer;
	}

	public LocalDateTime getStartTime()
	{
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime)
	{
		this.startTime = startTime;
	}

	public Stages getStage()
	{
		return stage;
	}

	public void setStage(Stages stage)
	{
		this.stage = stage;
	}

	public String getPosterUrl()
	{
		return posterUrl;
	}

	public void setPosterUrl(String posterUrl)
	{
		this.posterUrl = posterUrl;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(!(obj instanceof Event))
			return false;
		Event other = (Event) obj;
		if(id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public static Event latest(Objectify ofy)
	{
		Event eventWithMaxId = ofy.load().type(Event.class).order("-__key__").first().now();
		return eventWithMaxId;
	}
}
