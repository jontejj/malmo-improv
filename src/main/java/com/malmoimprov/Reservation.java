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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class Reservation
{
	@Id Long id;

	@NotBlank
	private String name;

	@NotBlank
	private String phone;
	@Email
	private String email;

	@NotBlank
	private String discount;

	@Min(1)
	@Max(5)
	private int nrOfSeats;

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
	public int getNrOfSeats()
	{
		return nrOfSeats;
	}
	public void setNrOfSeats(int nrOfSeats)
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
}