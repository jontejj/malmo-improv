package com.improvisatorium.reservations;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.Map;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.component.page.Page.ExtendedClientDetailsReceiver;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import jakarta.servlet.annotation.WebFilter;

@Route("")
@AnonymousAllowed
public class MyUI extends VerticalLayout
{
	private static final long serialVersionUID = 1L;

	private final Sendgrid sendgrid;

	private Binder<Reservation> binder;

	public MyUI()
	{
		this.sendgrid = new Sendgrid();
		binder = new BeanValidationBinder<>(Reservation.class);

		SeatsRemaining seatsRemaining = null;
		Event latestEvent = null;
		try(Closeable closeable = ObjectifyService.begin())
		{
			Objectify objectify = ofy();
			latestEvent = Event.latest(objectify);
			seatsRemaining = SeatsRemaining.load(objectify, latestEvent);
		}

		Image banner = new Image(latestEvent.getPosterUrl(), "");
		// banner.setSizeFull();
		banner.setWidth(30, Unit.PERCENTAGE);
		// banner.addStyleName("jonatan");
		// banner.setWidth(800, Unit.PIXELS);
		add(banner);

		if(seatsRemaining.getSeatsRemaining() > 0)
		{
			add(step1(seatsRemaining, latestEvent));
		}
		else
		{
			add(fullyBooked(latestEvent));
		}
	}

	private VerticalLayout fullyBooked(Event event)
	{
		final VerticalLayout fullyBooked = new VerticalLayout();
		Span text = new Span(event.getName() + " sold out. Better luck next time!");
		// text.addStyleName("small");
		text.setWidth(100, Unit.PERCENTAGE);
		fullyBooked.add(text);

		Anchor facebookLink = new Anchor("https://www.facebook.com/improvisatorium/", "Follow us on facebook for future events!");
		facebookLink.setTarget(AnchorTarget.BLANK);
		// facebookLink.setIcon(VaadinIcon.FACEBOOK_SQUARE);
		// facebookLink.addStyleName("small");
		fullyBooked.add(text, facebookLink);

		return fullyBooked;
	}

	private Component step1(SeatsRemaining seatsRemaining, Event event)
	{
		final VerticalLayout step1Container = new VerticalLayout();
		final StyledText instructions = new StyledText(
				"Step 1/2: Reserve your seats for <b>" + event.getName() + "</b> by filling in your details here:");
		// instructions.setWidth(100, Unit.PERCENTAGE);
		int defaultNrOfSeats = 1;
		final StyledText price = new StyledText(priceDescription(defaultNrOfSeats, "Normal", event));
		final FormLayout step1 = new FormLayout();

		StringBuilder caption = new StringBuilder("Nr of seats to reserve (max 5)");
		if(seatsRemaining.getSeatsRemaining() <= 10)
		{
			caption.append(" (" + seatsRemaining.getSeatsRemaining() + " remaining)");
		}
		final TextField nrOfSeats = new TextField(caption.toString(), "" + defaultNrOfSeats, "" + defaultNrOfSeats);

		nrOfSeats.setRequiredIndicatorVisible(true);
		binder.forField(nrOfSeats).withConverter(new StringToIntegerConverter("Invalid nr of seats")).bind("nrOfSeats");

		RadioButtonGroup<String> discounts = new RadioButtonGroup<>("Discounts");
		discounts.setItems("Normal", "MAF-member", "Student");
		discounts.setValue("Normal"); // Default
		binder.forField(discounts).bind("discount");

		Button reserveButton = new Button("Reserve seats");
		reserveButton.setEnabled(false);
		binder.addValueChangeListener((e) -> {
			reserveButton.setEnabled(binder.isValid());
			try
			{
				price.setText("<text>" + priceDescription(Long.parseLong(nrOfSeats.getValue()), discounts.getValue(), event) + "</text>");
				// price.markAsDirty();
			}
			catch(NumberFormatException invalid)
			{

			}
		});
		reserveButton.addClickListener(e -> {
			reserveButtonClicked(step1Container, event);
		});

		final TextField name = new TextField("Name:");
		name.setRequiredIndicatorVisible(true);
		binder.forField(name).bind("name");

		final TextField email = new TextField("Email:");
		email.setRequiredIndicatorVisible(true);
		binder.forField(email).bind("email");

		final TextField phone = new TextField("Phone:");
		phone.setRequiredIndicatorVisible(true);
		binder.forField(phone).bind("phone");

		step1.add(name, email, phone, discounts, nrOfSeats, reserveButton);
		step1Container.add(instructions, step1, price);
		return step1Container;
	}

	private String priceDescription(long nrOfSeats, String defaultDiscountType, Event event)
	{
		return "Total ticket price: " + Prices.priceToPay(nrOfSeats, defaultDiscountType, event) + " " + Config.CURRENCY;
	}

	private void reserveButtonClicked(final Component step1, Event event)
	{
		UI.getCurrent().getPage().retrieveExtendedClientDetails(new ExtendedClientDetailsReceiver(){
			private static final long serialVersionUID = 1L;

			@Override
			public void receiveDetails(ExtendedClientDetails extendedClientDetails)
			{
				Reservation reservation = new Reservation();
				try
				{
					binder.writeBean(reservation);
				}
				catch(ValidationException e1)
				{
					throw new RuntimeException("server side validation failed after client validation passed, forgot to add a UI component?", e1);
				}
				reservation.setCreationTime(extendedClientDetails.getCurrentDate());
				reservation.setEventId(event.getId());

				Reservation savedReservation = ObjectifyService.run(() -> {
					Objectify ofy = ObjectifyService.ofy();
					SeatsRemaining seatsRemainingCheck = SeatsRemaining.load(ofy, event);
					if(seatsRemainingCheck.getSeatsRemaining() < reservation.getNrOfSeats())
						throw new RuntimeException("Got booked while you were entering your data. Only " + seatsRemainingCheck.getSeatsRemaining()
								+ " seats are now remaining.");
					seatsRemainingCheck.setSeatsRemaining(seatsRemainingCheck.getSeatsRemaining() - reservation.getNrOfSeats());

					Map<Key<Object>, Object> savedData = ofy.save().entities(seatsRemainingCheck, reservation).now();
					return (Reservation) savedData.get(ObjectifyService.key(reservation));
				});

				sendStep1ConfirmationEmail(savedReservation, event);

				final VerticalLayout step2 = new VerticalLayout();

				long priceToPay = Prices.priceToPay(savedReservation.getNrOfSeats(), savedReservation.getDiscount(), event);

				StyledText instructions2 = new StyledText("<b>Step 2/2</b>: Swish " + priceToPay + " " + Config.CURRENCY + " to "
						+ event.getPhoneNumber()
						+ " to finalize your reservation. <br/><b>Note:</b>Your tickets are reserved for 3 days. Please remember to buy them via swish to finish the booking.");
				step2.add(new StyledText("Thanks " + savedReservation.getName() + ", your reservation of " + savedReservation.getNrOfSeats()
						+ " seat(s) is noted! Your reservation number is " + savedReservation.id + ".<br/>An email confirmation has been sent to "
						+ savedReservation.getEmail() + ". <br/><br/>"), instructions2);

				Anchor facebookLink = new Anchor(event.getFacebookUrl(), "Remember to also sign up for the event on facebook!");
				// facebookLink.setIcon(VaadinIcons.FACEBOOK_SQUARE);
				facebookLink.setTarget(AnchorTarget.BLANK);
				step2.add(facebookLink);
				remove(step1);
				add(step2);
			}
		});

	}

	private void sendStep1ConfirmationEmail(Reservation reservation, Event event) throws EmailException
	{
		sendgrid.sendEmail(	reservation, reservation.getName() + " your reservation is partially completed",
							"event-reservation-confirmation-email.ftlh", "reservations", event);
	}

	@WebFilter(urlPatterns = "/*", asyncSupported = true)
	public static class MyObjectifyFilter extends ObjectifyService.Filter
	{
	}
}
