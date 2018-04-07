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

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.converter.StringToBigDecimalConverter;
import com.vaadin.data.converter.StringToLongConverter;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

public class EventCreationPage
{
	public static Component form()
	{
		Binder<Event> binder = new BeanValidationBinder<>(Event.class);

		final VerticalLayout container = new VerticalLayout();
		final FormLayout eventForm = new FormLayout();
		/**
		 * @NotBlank
		 * 			// Duration duration;
		 */

		final TextField name = new TextField();
		name.setCaption("Name of Event:");
		binder.forField(name).bind("name");

		final TextField facebookUrl = new TextField();
		facebookUrl.setCaption("Facebook url to event:");
		binder.forField(facebookUrl).bind("facebookUrl");

		// Show uploaded file in this placeholder
		final Image image = new Image("Uploaded Image");

		// Implement both receiver that saves upload in a file and
		// listener for successful upload

		ImageUploader receiver = new ImageUploader(image);

		// Create the upload with a caption and set receiver later
		Upload posterUrl = new Upload("Upload Poster here", receiver);
		posterUrl.addSucceededListener(receiver);

		final TextField organizer = new TextField();
		organizer.setValue("Malmö Improvisatorium");
		organizer.setCaption("Organizer:");
		binder.forField(organizer).bind("organizer");

		final TextField phoneNumber = new TextField();
		phoneNumber.setCaption("Phonenumber to swish to:");
		binder.forField(phoneNumber).bind("phoneNumber");

		final TextField ticketPrice = new TextField();
		ticketPrice.setValue("40");
		ticketPrice.setCaption("Ticket price:");
		binder.forField(ticketPrice).withConverter(new StringToBigDecimalConverter("Invalid ticket price")).bind("ticketPrice");

		final TextField memberPricePercentage = new TextField();
		memberPricePercentage.setCaption("Member price percentage:");
		memberPricePercentage.setValue("80");
		binder.forField(memberPricePercentage).withConverter(new StringToBigDecimalConverter("")).bind("memberPricePercentage");

		final TextField nrOfSeats = new TextField();
		nrOfSeats.setCaption("Nr of seats:");
		nrOfSeats.setValue("31");
		binder.forField(nrOfSeats).withConverter(new StringToLongConverter("")).bind("nrOfSeats");

		final DateTimeField startTime = new DateTimeField();
		startTime.setCaption("Start time:");
		binder.forField(startTime).bind("startTime");

		final TextField addressName = new TextField();
		addressName.setValue("MAF, scen 2");
		addressName.setCaption("Name of Place:");
		binder.forField(addressName).bind("addressName");

		final TextField street = new TextField();
		street.setValue("Norra Skolgatan 12");
		street.setCaption("Street:");
		binder.forField(street).bind("street");

		final TextField city = new TextField();
		city.setValue("Malmö");
		city.setCaption("City:");
		binder.forField(city).bind("city");

		final TextField postalCode = new TextField();
		postalCode.setValue("21152");
		postalCode.setCaption("Postal code:");
		binder.forField(postalCode).bind("postalCode");

		final TextField country = new TextField();
		country.setValue("SE");
		country.setCaption("Country:");
		binder.forField(country).bind("country");

		final TextField region = new TextField();
		region.setValue("SE-M");
		region.setCaption("Region:");
		binder.forField(region).bind("region");

		Button createButton = new Button("Create event");
		createButton.setEnabled(false);
		binder.addValueChangeListener((e) -> {
			createButton.setEnabled(binder.isValid());
		});

		createButton.addClickListener(e -> {
			System.out.println(e);
		});
		eventForm.addComponents(name, facebookUrl, organizer, phoneNumber, ticketPrice, memberPricePercentage, nrOfSeats, startTime, addressName,
								street, city, country, region, posterUrl, createButton);

		container.addComponent(eventForm);
		container.addComponent(image);

		return container;
	}
}
