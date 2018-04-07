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
package com.malmoimprov;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public final class Event implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id long id;

	@NotBlank String phoneNumber;

	BigDecimal ticketPrice;
	BigDecimal memberPricePercentage;
	String facebookUrl;
	@NotBlank String name;
	@NotBlank String organizer;

	@Min(1) @NotNull Long nrOfSeats;

	LocalDateTime startTime;

	// Duration duration;

	String addressName;
	String street;
	String city;
	String postalCode;
	String country;
	String region;
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

	public BigDecimal getTicketPrice()
	{
		return ticketPrice;
	}

	public void setTicketPrice(BigDecimal ticketPrice)
	{
		this.ticketPrice = ticketPrice;
	}

	public BigDecimal getMemberPricePercentage()
	{
		return memberPricePercentage;
	}

	public void setMemberPricePercentage(BigDecimal memberPricePercentage)
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

	public Long getNrOfSeats()
	{
		return nrOfSeats;
	}

	public void setNrOfSeats(Long nrOfSeats)
	{
		this.nrOfSeats = nrOfSeats;
	}

	public LocalDateTime getStartTime()
	{
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime)
	{
		this.startTime = startTime;
	}

	public String getAddressName()
	{
		return addressName;
	}

	public void setAddressName(String addressName)
	{
		this.addressName = addressName;
	}

	public String getStreet()
	{
		return street;
	}

	public void setStreet(String street)
	{
		this.street = street;
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city = city;
	}

	public String getPostalCode()
	{
		return postalCode;
	}

	public void setPostalCode(String postalCode)
	{
		this.postalCode = postalCode;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getRegion()
	{
		return region;
	}

	public void setRegion(String region)
	{
		this.region = region;
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
		return "Event [id=" + id + ", phoneNumber=" + phoneNumber + ", ticketPrice=" + ticketPrice + ", memberPricePercentage="
				+ memberPricePercentage + ", facebookUrl=" + facebookUrl + ", name=" + name + ", organizer=" + organizer + ", nrOfSeats=" + nrOfSeats
				+ ", startTime=" + startTime + ", addressName=" + addressName + ", street=" + street + ", city=" + city + ", postalCode=" + postalCode
				+ ", country=" + country + ", region=" + region + ", posterUrl=" + posterUrl + "]";
	}
}
