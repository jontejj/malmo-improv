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

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
@Cache
public class Counter
{
	@Id String name;  // Use a fixed name for the counter
	long value;

	public Counter()
	{
	}

	public Counter(String name, long value)
	{
		this.name = name;
		this.value = value;
	}

	public long getValue()
	{
		return value;
	}

	public void setValue(long value)
	{
		this.value = value;
	}

	private static final String COUNTER_NAME = "globalCounter";

	public static long getNextId()
	{
		return ObjectifyService.run(new Work<Long>(){
			@Override
			public Long run()
			{
				Counter counter = ObjectifyService.ofy().load().type(Counter.class).id(COUNTER_NAME).now();
				if(counter == null)
				{
					counter = new Counter(COUNTER_NAME, 1L);
				}
				else
				{
					counter.setValue(counter.getValue() + 1);
				}
				ObjectifyService.ofy().save().entity(counter).now();
				return counter.getValue();
			}
		});
	}
}
