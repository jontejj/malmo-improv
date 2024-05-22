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

import java.time.LocalDateTime;

import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.ValueType;
import com.googlecode.objectify.impl.Path;
import com.googlecode.objectify.impl.translate.CreateContext;
import com.googlecode.objectify.impl.translate.LoadContext;
import com.googlecode.objectify.impl.translate.SaveContext;
import com.googlecode.objectify.impl.translate.SkipException;
import com.googlecode.objectify.impl.translate.TypeKey;
import com.googlecode.objectify.impl.translate.ValueTranslator;
import com.googlecode.objectify.impl.translate.ValueTranslatorFactory;

public class LocalDateTimeTranslatorFactory extends ValueTranslatorFactory<LocalDateTime, String>
{
	public LocalDateTimeTranslatorFactory()
	{
		super(LocalDateTime.class);
	}

	protected ValueTranslator<LocalDateTime, String> createValueTranslator(TypeKey<LocalDateTime> typeKey, CreateContext createContext, Path path)
	{
		return new ValueTranslator<LocalDateTime, String>(ValueType.STRING){

			@Override
			protected LocalDateTime loadValue(Value<String> value, LoadContext ctx, Path path) throws SkipException
			{
				return LocalDateTime.parse(value.get());
			}

			@Override
			protected Value<String> saveValue(LocalDateTime value, SaveContext ctx, Path path) throws SkipException
			{
				return StringValue.of(value.toString());
			}
		};
	}
}
