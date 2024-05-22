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

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.googlecode.objectify.ObjectifyService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinServletRequest;

import jakarta.annotation.security.PermitAll;

@PermitAll
@Route("admin")
public class AdminPage extends VerticalLayout
{
	private static final Logger LOG = LoggerFactory.getLogger(AdminPage.class);

	private static final String LOGOUT_SUCCESS_URL = "/";

	private final Sendgrid sendgrid;

	private VerticalLayout attendanceView;

	public AdminPage()
	{
		this.sendgrid = new Sendgrid();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();

		String givenName = principal.getAttribute("given_name");
		String familyName = principal.getAttribute("family_name");
		String email = principal.getAttribute("email");
		String picture = principal.getAttribute("picture");
		H2 header = new H2("Hello " + givenName + " " + familyName + " (" + email + ")");
		Image image = new Image(picture, "User Image");
		Button logoutButton = new Button("Logout", click -> {
			UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
			SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
			logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
		});
		RouterLink routerLink = new RouterLink("New event", NewEventCreationPage.class);
		add(header, image, routerLink, logoutButton);

		ImmutableSet<String> admins = ImmutableSet.of("jontejj@gmail.com", "sara.zeidi58@gmail.com", "a.l.bobrick@gmail.com");
		boolean isAdmin = admins.contains(email.toLowerCase());
		if(isAdmin)
		{
			ComboBox<Event> eventComboBox = new ComboBox<>("Select Event");
			List<Event> events = getEvents();
			eventComboBox.setItems(events);
			Optional<Event> latestEvent = events.stream().max(Ordering.natural().onResultOf(Event::getId));
			AtomicReference<Event> selectedEvent = new AtomicReference<>(latestEvent.orElse(null));
			eventComboBox.addValueChangeListener(event -> {
				selectedEvent.set(event.getValue());
				attendanceList(selectedEvent.get());
			});
			add(eventComboBox);
			add(new Button("Send 10 event reminders", (e) -> {
				sendEventReminders(10, selectedEvent.get());
			}));
			latestEvent.ifPresent(this::attendanceList);
		}
	}

	private void attendanceList(Event event)
	{
		if(attendanceView != null)
		{
			remove(attendanceView);
		}
		attendanceView = new VerticalLayout();

		List<Reservation> list = getActiveReservations(event);
		// Create a grid bound to the list
		Grid<Reservation> grid = new Grid<>();
		grid.setWidth(100, Unit.PERCENTAGE);
		grid.setItems(list);
		grid.addColumn(Reservation::getName).setHeader("Name");
		grid.addColumn(Reservation::getPhone).setHeader("Phone");
		grid.addColumn(Reservation::getNrOfSeats).setHeader("Seats");
		grid.addColumn(res -> Prices.priceToPay(res, event)).setHeader("Price");
		grid.addColumn(Reservation::getPaid).setHeader("Paid");
		grid.setSelectionMode(SelectionMode.NONE);
		NativeButtonRenderer<Reservation> confirmPaymentButton = new NativeButtonRenderer<Reservation>(
				reservation -> reservation.getSentConfirmationAboutEvent() ? "Already paid" : "Confirm Payment");
		confirmPaymentButton.addItemClickListener(reservationToConfirm -> {
			if(!reservationToConfirm.getSentConfirmationAboutEvent())
			{
				reservationToConfirm.setPaid(true);
				sendConfirmationEmail(reservationToConfirm, event);
				reservationToConfirm.setSentConfirmationAboutEvent(true);
				ObjectifyService.run(() -> ObjectifyService.ofy().save().entities(reservationToConfirm).now());
				grid.setItems(getActiveReservations(event));
			}
		});
		grid.addColumn(confirmPaymentButton);

		NativeButtonRenderer<Reservation> cancelButton = new NativeButtonRenderer<Reservation>("Cancel");
		cancelButton.addItemClickListener(reservationToCancel -> {
			if(!reservationToCancel.getCancelled())
			{
				reservationToCancel.setCancelled(true);
				ObjectifyService.run(() -> {
					SeatsRemaining seatsRemaining = SeatsRemaining.load(ofy(), event);
					seatsRemaining.setSeatsRemaining(seatsRemaining.getSeatsRemaining() + reservationToCancel.getNrOfSeats());
					ofy().save().entities(reservationToCancel, seatsRemaining).now();
				});
				grid.setItems(getActiveReservations(event));
			}
		});
		grid.addColumn(cancelButton);

		NativeButtonRenderer<Reservation> attendedButton = new NativeButtonRenderer<Reservation>("Attended");
		attendedButton.addItemClickListener(reservationThatAttended -> {
			reservationThatAttended.setAttended(true);
			ObjectifyService.run(() -> ObjectifyService.ofy().save().entities(reservationThatAttended).now());
			list.remove(reservationThatAttended);
			grid.setItems(list);
		});
		grid.addColumn(attendedButton);

		long totalPrepaid = list.stream().filter(r -> r.getPaid()).map(res -> Prices.priceToPay(res, event)).reduce(0L, Long::sum);

		attendanceView.add(grid);
		attendanceView.add(new StyledText("Total prepaid: " + totalPrepaid));
		attendanceView.add(new StyledText(
				"Tickets booked: " + (event.getStage().seatCapacity() - seatsRemaining(event)) + " of " + event.getStage().seatCapacity()));
		add(attendanceView);
	}

	private List<Reservation> getActiveReservations(Event event)
	{
		return ObjectifyService.run(() -> ObjectifyService.ofy().load().type(Reservation.class).filter("eventId = ", event.getId())
				.filter("cancelled =", false).list());
	}

	private List<Event> getEvents()
	{
		return ObjectifyService.run(() -> ObjectifyService.ofy().load().type(Event.class).list());
	}

	private long seatsRemaining(Event event)
	{

		long result = ObjectifyService.run(() -> {
			SeatsRemaining seatsRemaining = SeatsRemaining.load(ofy(), event);
			return seatsRemaining.getSeatsRemaining();
		});
		return result;
	}

	private void sendEventReminders(int nrOfRemindersToSend, Event event)
	{
		List<Reservation> list = ObjectifyService.run(() -> ObjectifyService.ofy().load().type(Reservation.class).filter("eventId = ", event.getId())
				.filter("cancelled =", false).filter("sentReminderAboutEvent =", false).limit(nrOfRemindersToSend).list());
		list.forEach(r -> {
			sendReminderEmail(r, event);
			r.setSentReminderAboutEvent(true);
			ObjectifyService.run(() -> ObjectifyService.ofy().save().entities(r).now());
		});
		String reservationsAsStr = list.toString();
		StyledText label = new StyledText(reservationsAsStr);
		// label.setWidth(100, Unit.PERCENTAGE);
		add(label);
	}

	private void sendReminderEmail(Reservation reservation, Event event) throws EmailException
	{
		sendgrid.sendEmail(reservation, "See you soon @ " + event.getName() + "!", "reservation-reminder.ftlh", "reminder", event);
	}

	private void sendConfirmationEmail(Reservation reservation, Event event)
	{
		sendgrid.sendEmail(reservation, "Payment confirmed for " + event.getName(), "reservation-confirmation.ftlh", "reminder", event);
	}
}
