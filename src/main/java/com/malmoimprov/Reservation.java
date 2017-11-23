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
package com.malmoimprov;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfDefault;

@Entity
public class Reservation implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	Long id;

	@NotBlank
	private String name;

	@NotBlank
	private String phone;
	@Email
	@NotBlank
	private String email;

	@NotBlank
	private String discount;

	@Min(1)
	@Max(5)
	private Integer nrOfSeats;

	private Boolean paid = false;

	@Index
	private Boolean cancelled = false;

	@Index(IfDefault.class)
	private Boolean sentReminderAboutEvent = false;

	private long eventId;

	private Date creationTime;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public Integer getNrOfSeats()
	{
		return nrOfSeats;
	}

	public void setNrOfSeats(Integer nrOfSeats)
	{
		this.nrOfSeats = nrOfSeats;
	}

	public String getDiscount()
	{
		return discount;
	}

	public void setDiscount(String discount)
	{
		this.discount = discount;
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public Boolean getPaid()
	{
		return paid;
	}

	public void setPaid(Boolean paid)
	{
		this.paid = checkNotNull(paid);
	}

	public Boolean getCancelled()
	{
		return cancelled;
	}

	public void setCancelled(Boolean cancelled)
	{
		this.cancelled = cancelled;
	}

	public Boolean getSentReminderAboutEvent()
	{
		return sentReminderAboutEvent;
	}

	public void setSentReminderAboutEvent(Boolean sentReminderAboutEvent)
	{
		this.sentReminderAboutEvent = sentReminderAboutEvent;
	}

	public long getEventId()
	{
		return eventId;
	}

	public void setEventId(long eventId)
	{
		this.eventId = eventId;
	}

	@Override
	public String toString()
	{
		return "Reservation [eventId=" + eventId + ", id=" + id + ", name=" + name + ", phone=" + phone + ", email=" + email + ", discount="
				+ discount + ", nrOfSeats=" + nrOfSeats + ", paid=" + paid + ", cancelled=" + cancelled + ", sentReminderAboutEvent="
				+ sentReminderAboutEvent + "]";
	}

	public Date getCreationTime()
	{
		return creationTime;
	}

	public void setCreationTime(Date creationTime)
	{
		this.creationTime = creationTime;
	}
}
