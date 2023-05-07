/* Copyright 2023 jonatanjonsson
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

import com.google.schemaorg.core.CoreFactory;
import com.google.schemaorg.core.Place;

public final class Stages
{
	public static final Place STAGE_1 = CoreFactory.newPlaceBuilder() //
			.addName("MAF, scen 1") // 60 seats
			.addAddress(CoreFactory.newPostalAddressBuilder() //
					.addStreetAddress("Norra Skolgatan 10 A") //
					.addAddressLocality("Malmö") //
					.addAddressRegion("SE-M") //
					.addPostalCode("21152") //
					.addAddressCountry("SE"))
			.build();

	public static final Place STAGE_2 = CoreFactory.newPlaceBuilder() //
			.addName("MAF, scen 2") // 31 seats
			.addAddress(CoreFactory.newPostalAddressBuilder() //
					.addStreetAddress("Norra Skolgatan 12") //
					.addAddressLocality("Malmö") //
					.addAddressRegion("SE-M") //
					.addPostalCode("21152") //
					.addAddressCountry("SE"))
			.build();

	public static final Place STAGE_3 = CoreFactory.newPlaceBuilder() //
			.addName("MAF, scen 3") //
			.addAddress(CoreFactory.newPostalAddressBuilder() //
					.addStreetAddress("Spångatan 20") //
					.addAddressLocality("Malmö") //
					.addAddressRegion("SE-M") //
					.addPostalCode("21153") //
					.addAddressCountry("SE"))
			.build();

}
