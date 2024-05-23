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

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@PermitAll
@Route("new-event")
public class NewEventCreationPage extends VerticalLayout
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(NewEventCreationPage.class);

	public NewEventCreationPage()
	{
		Event newEvent = new Event();
		newEvent.setStage(Stages.STAGE_1);
		newEvent.setOrganizer("Malmö Improvisatorium");
		newEvent.setTicketPrice(80);
		newEvent.setMemberPricePercentage(75);
		Binder<Event> binder = new BeanValidationBinder<>(Event.class);
		AtomicReference<String> posterUrl = new AtomicReference<>();

		final FormLayout eventForm = new FormLayout();

		final TextField name = new TextField("Name of Event");
		binder.forField(name).bind("name");

		final TextField facebookUrl = new TextField("Facebook url to event");
		binder.forField(facebookUrl).bind("facebookUrl");

		MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
		Upload upload = new Upload(buffer);
		UploadExamplesI18N i18n = new UploadExamplesI18N();
		i18n.getAddFiles().setOne("Upload Image...");
		i18n.getDropFiles().setOne("Drop Image here");
		i18n.getError().setIncorrectFileType("The provided file does not have the correct format (Image document).");
		upload.setI18n(i18n);

		upload.addSucceededListener(event -> {
			String fileName = event.getFileName();
			InputStream inputStream = buffer.getInputStream(fileName);
			byte[] image;
			try
			{
				image = ByteStreams.toByteArray(inputStream);
				UploadObjectFromMemory.uploadObjectFromMemory(	"malmo-improv", "malmo-improv.appspot.com", "events/" + fileName, event.getMIMEType(),
																image);
				posterUrl.set("https://storage.googleapis.com/malmo-improv.appspot.com/events/" + fileName);
			}
			catch(IOException e1)
			{
				LOG.error("Failed to upload image file", e1);
			}
		});

		final TextField organizer = new TextField("Organizer");
		binder.forField(organizer).bind("organizer");

		final TextField phoneNumber = new TextField("Phonenumber to swish to");
		binder.forField(phoneNumber).bind("phoneNumber");

		final IntegerField ticketPrice = new IntegerField("Ticket price");
		binder.forField(ticketPrice).bind("ticketPrice");

		final IntegerField memberPricePercentage = new IntegerField("Member price percentage");
		binder.forField(memberPricePercentage).bind("memberPricePercentage");

		DateTimePicker startTime = new DateTimePicker();
		startTime.setLabel("Date and time");
		startTime.setMin(LocalDateTime.now());
		binder.forField(startTime).bind("startTime");

		ComboBox<Stages> stage = new ComboBox<>();
		stage.setItems(Stages.values());
		binder.forField(stage).bind("stage");

		binder.readBean(newEvent);

		Button createButton = new Button("Create event");
		createButton.setEnabled(false);
		binder.addValueChangeListener((e) -> {
			createButton.setEnabled(binder.isValid());
		});

		createButton.addClickListener(e -> {
			try
			{
				binder.writeBean(newEvent);
				newEvent.setPosterUrl(posterUrl.get());
				LOG.info("Creating event: " + newEvent);
				try(Closeable closeable = ObjectifyService.begin())
				{
					ObjectifyService.ofy().transactNew(() -> {
						Objectify ofy = ObjectifyService.ofy();
						Map<Key<Event>, Event> savedData = ofy.save().entities(newEvent).now();
						Event savedEvent = savedData.values().iterator().next();
						SeatsRemaining seatsRemaining = new SeatsRemaining().setEventId("" + savedEvent.getId())
								.setSeatsRemaining(savedEvent.getStage().seatCapacity());
						ofy.save().entities(seatsRemaining).now();
						return savedEvent;
					});
				}
				Notification.show("Saved event: " + newEvent);
				LOG.info("Saved event: " + newEvent);
				UI.getCurrent().navigate(MyUI.class);
			}
			catch(ValidationException e1)
			{
				LOG.error("Failed to validate new event", e1);
			}
		});
		eventForm.add(name, facebookUrl, organizer, phoneNumber, ticketPrice, memberPricePercentage, startTime, stage, upload, createButton);
		add(eventForm);
	}
}
