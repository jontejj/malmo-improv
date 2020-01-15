package com.malmoimprov;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.crudui.crud.impl.GridBasedCrudComponent;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.schemaorg.JsonLdSerializer;
import com.google.schemaorg.JsonLdSyntaxException;
import com.google.schemaorg.core.CoreFactory;
import com.google.schemaorg.core.EventReservation;
import com.google.schemaorg.core.ReservationStatusTypeEnum;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFilter;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.util.Closeable;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Viewport;
import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.GAEVaadinServlet;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
@Viewport("initial-scale=1.0, width=device-width")
public class MyUI extends UI
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(MyUI.class);

	private static final String CONFIG_KEY_SENDGRID = "SENDGRID";
	private static final long EVENT_ID = 16;
	private static final String CURRENCY = "SEK";
	private static final String PHONENUMBER_TO_PAY_TO = "0764088570";
	private static final long initialSeatCapacity = 29;
	private static final BigDecimal ticketPrice = new BigDecimal("50");
	private static final BigDecimal memberPricePercentage = new BigDecimal("0.80");
	// private static final BigDecimal folkUniPricePercentage = new BigDecimal("0.60");

	private static final String facebookEventUrl = "https://www.facebook.com/events/633917914045683/";
	private static final String imageLink = "https://storage.googleapis.com/malmo-improv.appspot.com/events/vision-of-2020.jpg";
	private static final String eventName = "Vision of 2020s - Improv Comedy Show";
	private static final com.google.schemaorg.core.Event event = CoreFactory.newTheaterEventBuilder().addUrl(facebookEventUrl).addName(eventName)
			.addOrganizer("Malmö Improvisatorium").addStartDate("2020-01-30T19:00:00+01:00").addDuration("PT1H00M")
			.addLocation(CoreFactory.newPlaceBuilder().addName("MAF, scen 2")
					.addAddress(CoreFactory.newPostalAddressBuilder().addStreetAddress("Spångatan 20").addAddressLocality("Malmö")
							.addAddressRegion("SE-M").addPostalCode("21152").addAddressCountry("SE")))
			.addProperty("phoneNumber", PHONENUMBER_TO_PAY_TO).build();
	private static final Configuration cfg;
	static
	{
		// SLF4JBridgeHandler.install();

		cfg = new Configuration(Configuration.VERSION_2_3_25);
		cfg.setClassForTemplateLoading(MyUI.class, "/email-templates/");
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
	}
	private Binder<Reservation> binder;

	@Override
	protected void init(VaadinRequest vaadinRequest)
	{
		binder = new BeanValidationBinder<>(Reservation.class);

		final VerticalLayout page = new VerticalLayout();

		Image banner = new Image("", new ExternalResource(imageLink));
		// banner.setSizeFull();
		banner.setWidth(30, Unit.PERCENTAGE);
		// banner.addStyleName("jonatan");
		// banner.setWidth(800, Unit.PIXELS);
		page.addComponent(banner);

		SeatsRemaining seatsRemaining = loadSeatsRemaining(ObjectifyService.ofy());

		if(vaadinRequest.getParameter("admin") != null)
		{
			UserService userService = UserServiceFactory.getUserService();
			if(userService.isUserLoggedIn())
			{
				User currentUser = userService.getCurrentUser();
				ImmutableSet<String> admins = ImmutableSet.of("jontejj@gmail.com", "sara.zeidi58@gmail.com", "a.l.bobrick@gmail.com");
				boolean isAdmin = admins.contains(currentUser.getEmail().toLowerCase());
				if(isAdmin)
				{
					loggedInPage(page);
				}
				else
				{
					page.addComponent(new Label("Your user is not authorized to manage reservations"));
				}
				page.addComponent(new Link("Logout", new ExternalResource(userService.createLogoutURL("//"))));
			}
			else
			{
				page.addComponent(new Link("Login", new ExternalResource(userService.createLoginURL("/?admin"))));
			}
		}
		else if(seatsRemaining.getSeatsRemaining() > 0)
		{
			page.addComponent(step1(page, seatsRemaining));
		}
		else
		{
			page.addComponent(fullyBooked());
		}
		Responsive.makeResponsive(page);// TODO doesn't work?
		// setContent(banner);
		setContent(page);
	}

	private void loggedInPage(final VerticalLayout page)
	{
		GridBasedCrudComponent<Reservation> reservations = new GridBasedCrudComponent<>(Reservation.class);
		reservations.getCrudFormFactory().setUseBeanValidation(true);
		reservations.setUpdateOperation(updatedReservation -> {
			Objectify ofy = ObjectifyService.ofy();
			Reservation oldReservation = ofy.load().entity(updatedReservation).now();
			List<Object> entititesToUpdate = Lists.newArrayList(updatedReservation);
			if(!oldReservation.getCancelled() && updatedReservation.getCancelled())
			{
				SeatsRemaining seatsRemainingCheck = loadSeatsRemaining(ofy);
				seatsRemainingCheck.setSeatsRemaining(seatsRemainingCheck.getSeatsRemaining() + updatedReservation.getNrOfSeats());
				entititesToUpdate.add(seatsRemainingCheck);
			}
			ofy.save().entities(entititesToUpdate).now();
			return updatedReservation;
		});
		reservations.setFindAllOperation(() -> ObjectifyService.ofy().load().type(Reservation.class).filter("eventId = ", EVENT_ID).list());
		page.addComponent(reservations);
		page.addComponent(new Button("Send 10 event reminders", (e) -> {
			sendEventReminders(page, 10);
		}));
		page.addComponent(new Button("Send 10 event confirmations to people who paid", (e) -> {
			sendEventConfirmations(page, 10);
		}));
		page.addComponent(new Button("Take attendance", (e) -> {
			attendanceList(page);
		}));
		// page.addComponent(new Button("Migrate reservations", (e) -> {
		// migrateReservations();
		// }));
		// page.addComponent(new Button("Create new event", (e) -> {
		// page.addComponent(EventCreationPage.form());
		// }));
	}

	private void attendanceList(VerticalLayout page)
	{
		List<Reservation> list = ObjectifyService.ofy().load().type(Reservation.class).filter("eventId = ", EVENT_ID).filter("cancelled =", false)
				.list();

		// Have some data
		// Create a grid bound to the list
		Grid<Reservation> grid = new Grid<>();
		grid.setWidth(100, Unit.PERCENTAGE);
		grid.setItems(list);
		grid.addColumn(Reservation::getName).setCaption("Name");
		grid.addColumn(Reservation::getPhone).setCaption("Phone");
		grid.addColumn(Reservation::getNrOfSeats).setCaption("Seats");
		grid.addColumn(MyUI::priceToPay).setCaption("Price");
		grid.addColumn(Reservation::getPaid).setCaption("Paid");
		grid.setSelectionMode(SelectionMode.NONE);
		// grid.addColumn(Reservation::getAttended).setCaption("Attended");

		grid.addColumn(person -> "Attended", new ButtonRenderer<Reservation>(clickEvent -> {
			Reservation attended = clickEvent.getItem();
			attended.setAttended(true);
			ObjectifyService.ofy().save().entities(attended).now();
			list.remove(attended);
			grid.setItems(list);
		}));

		BigDecimal totalPrepaid = list.stream().filter(r -> r.getPaid()).map(MyUI::priceToPay).reduce(BigDecimal.ZERO, BigDecimal::add);

		page.addComponent(grid);

		page.addComponent(new Label("Total prepaid: " + totalPrepaid));
	}

	private void sendEventConfirmations(VerticalLayout page, int nrOfConfirmationsToSend)
	{
		Objectify ofy = ObjectifyService.ofy();
		// List<Reservation> list = ofy.load().type(Reservation.class).list();
		// list.forEach(r -> {
		// // Test email
		// if(r.getEmail().equals("jontejj@gmail.com"))
		// {
		// sendConfirmationEmail(r);
		// Label label = new Label(r.toString());
		// label.setWidth(100, Unit.PERCENTAGE);
		// page.addComponent(label);
		// }
		// });
		// return;

		List<Reservation> list = ofy.load().type(Reservation.class).filter("eventId = ", EVENT_ID).filter("cancelled =", false)
				.filter("sentConfirmationAboutEvent =", false).filter("paid =", true).limit(nrOfConfirmationsToSend).list();
		list.forEach(r -> {
			sendConfirmationEmail(r);
			r.setSentConfirmationAboutEvent(true);
			ofy.save().entities(r).now();
		});
		String reservationsAsStr = list.toString();
		Label label = new Label(reservationsAsStr);
		label.setWidth(100, Unit.PERCENTAGE);
		page.addComponent(label);
	}

	private void migrateReservations()
	{
		Objectify ofy = ObjectifyService.ofy();
		List<Reservation> list = ofy.load().type(Reservation.class).list();
		ofy.save().entities(list).now();
	}

	private void sendEventReminders(final VerticalLayout page, int nrOfRemindersToSend)
	{
		Objectify ofy = ObjectifyService.ofy();
		/*
		 * List<Reservation> list = ofy.load().type(Reservation.class).list();
		 * list.forEach(r ->
		 * {
		 * //Test email
		 * if(r.getEmail().equals("jontejj@gmail.com"))
		 * {
		 * sendReminderEmail(r);
		 * Label label = new Label(r.toString());
		 * label.setWidth(100, Unit.PERCENTAGE);
		 * page.addComponent(label);
		 * }
		 * }
		 */
		List<Reservation> list = ofy.load().type(Reservation.class).filter("eventId = ", EVENT_ID).filter("cancelled =", false)
				.filter("sentReminderAboutEvent =", false).limit(nrOfRemindersToSend).list();
		list.forEach(r -> {
			sendReminderEmail(r);
			r.setSentReminderAboutEvent(true);
			ofy.save().entities(r).now();
		});
		String reservationsAsStr = list.toString();
		Label label = new Label(reservationsAsStr);
		label.setWidth(100, Unit.PERCENTAGE);
		page.addComponent(label);
	}

	private SeatsRemaining loadSeatsRemaining(Objectify ofy)
	{
		return ofy.load().key(Key.create(SeatsRemaining.class, "" + EVENT_ID)).now();
	}

	private static String loadConfig(String key)
	{
		Config configEntry = ObjectifyService.ofy().load().key(Key.create(Config.class, key)).now();
		if(configEntry == null)
			throw new IllegalStateException("No config for key: " + key);
		return configEntry.getValue();
	}

	private VerticalLayout fullyBooked()
	{
		final VerticalLayout fullyBooked = new VerticalLayout();
		Label text = new Label(eventName + " sold out. Better luck next time!");
		text.addStyleName("small");
		text.setWidth(100, Unit.PERCENTAGE);
		fullyBooked.addComponent(text);
		// Image banner = new Image("", new ClassResource("/contagious-soldout.jpg"));
		// banner.addStyleName("jonatan");
		// banner.setWidth(100, Unit.PERCENTAGE);

		Link facebookLink = new Link("Follow us on facebook for future events!", new ExternalResource("https://www.facebook.com/improvisatorium/"));
		facebookLink.setIcon(VaadinIcons.FACEBOOK_SQUARE);
		facebookLink.addStyleName("small");
		facebookLink.setTargetName("_blank");
		// fullyBooked.addComponents(text, banner, facebookLink);
		fullyBooked.addComponents(text, facebookLink);

		return fullyBooked;
	}

	private Component step1(final VerticalLayout page, SeatsRemaining seatsRemaining)
	{
		final VerticalLayout step1Container = new VerticalLayout();
		String defaultDiscountType = "Normal";
		final Label instructions = new Label("Step 1/2: Reserve your seats for <b>" + eventName + "</b> by filling in your details here:",
				ContentMode.HTML);
		instructions.setWidth(100, Unit.PERCENTAGE);
		int defaultNrOfSeats = 1;
		final Label price = new Label(priceDescription(defaultNrOfSeats, defaultDiscountType));
		final FormLayout step1 = new FormLayout();

		final TextField nrOfSeats = new TextField();
		nrOfSeats.setValue("" + defaultNrOfSeats);
		nrOfSeats.setCaptionAsHtml(true);
		StringBuilder caption = new StringBuilder("Nr of seats to reserve <br/>(max 5)");
		if(seatsRemaining.getSeatsRemaining() <= 10)
		{
			caption.append(" (" + seatsRemaining.getSeatsRemaining() + " remaining)");
		}
		nrOfSeats.setCaption(caption.toString());
		nrOfSeats.setRequiredIndicatorVisible(true);
		binder.forField(nrOfSeats).withConverter(new StringToIntegerConverter("Invalid nr of seats")).bind("nrOfSeats");

		RadioButtonGroup<String> discounts = new RadioButtonGroup<>("Discounts");
		// "Folk Universitetet"
		discounts.setItems("Normal", "MAF-member", "Student");

		discounts.setSelectedItem(defaultDiscountType);
		binder.forField(discounts).bind("discount");

		Button reserveButton = new Button("Reserve seats");
		reserveButton.setEnabled(false);
		binder.addValueChangeListener((e) -> {
			reserveButton.setEnabled(binder.isValid());
			try
			{
				price.setValue(priceDescription(Long.parseLong(nrOfSeats.getValue()), discounts.getSelectedItem().get()));
				price.markAsDirty();
			}
			catch(NumberFormatException invalid)
			{

			}

		});
		reserveButton.addClickListener(e -> {
			reserveButtonClicked(page, step1Container);
		});

		final TextField name = new TextField();
		name.setCaption("Name:");
		name.setRequiredIndicatorVisible(true);
		binder.forField(name).bind("name");

		final TextField email = new TextField();
		email.setCaption("Email:");
		email.setRequiredIndicatorVisible(true);
		binder.forField(email).bind("email");

		final TextField phone = new TextField();
		phone.setCaption("Phone:");
		phone.setRequiredIndicatorVisible(true);
		binder.forField(phone).bind("phone");

		step1.addComponents(name, email, phone, discounts, nrOfSeats, reserveButton);
		step1Container.addComponents(instructions, step1, price);
		return step1Container;
	}

	private String priceDescription(long nrOfSeats, String defaultDiscountType)
	{
		return "Total ticket price: " + priceToPay(nrOfSeats, defaultDiscountType) + " " + CURRENCY;
	}

	private void reserveButtonClicked(final ComponentContainer page, final Component step1)
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
		WebBrowser webBrowser = UI.getCurrent().getPage().getWebBrowser();
		reservation.setCreationTime(webBrowser.getCurrentDate());
		reservation.setEventId(EVENT_ID);

		Objectify ofy = ObjectifyService.ofy();
		SeatsRemaining seatsRemainingCheck = loadSeatsRemaining(ofy);
		if(seatsRemainingCheck.getSeatsRemaining() < reservation.getNrOfSeats())
			throw new RuntimeException(
					"Got booked while you were entering your data. Only " + seatsRemainingCheck.getSeatsRemaining() + " seats are now remaining.");
		seatsRemainingCheck.setSeatsRemaining(seatsRemainingCheck.getSeatsRemaining() - reservation.getNrOfSeats());

		Map<Key<Object>, Object> savedData = ofy.save().entities(seatsRemainingCheck, reservation).now();
		reservation = (Reservation) savedData.get(Key.create(reservation));

		sendStep1ConfirmationEmail(reservation);

		final VerticalLayout step2 = new VerticalLayout();

		BigDecimal priceToPay = priceToPay(reservation.getNrOfSeats(), reservation.getDiscount());

		Label instructions2 = new Label("<b>Step 2/2</b>: Swish " + priceToPay.longValue() + " " + CURRENCY + " to " + PHONENUMBER_TO_PAY_TO
				+ " to finalize your reservation. <br/><b>Note:</b>Your tickets are reserved for 3 days. Please remember to buy them via swish to finish the booking.",
				ContentMode.HTML);
		step2.addComponents(new Label(
				"Thanks " + reservation.getName() + ", your reservation of " + reservation.getNrOfSeats()
						+ " seat(s) is noted! Your reservation number is " + reservation.id + ".<br/>An email confirmation has been sent to "
						+ reservation.getEmail() + ". <br/><br/>",
				ContentMode.HTML), instructions2);

		Link facebookLink = new Link("Remember to also sign up for the event on facebook!", new ExternalResource(facebookEventUrl));
		facebookLink.setIcon(VaadinIcons.FACEBOOK_SQUARE);
		facebookLink.setTargetName("_blank");
		step2.addComponent(facebookLink);

		page.removeComponent(step1);
		page.addComponent(step2);
	}

	static Map<String, Object> reservationInformation(Reservation reservation, BigDecimal priceToPay)
	{
		ReservationStatusTypeEnum status = ReservationStatusTypeEnum.RESERVATION_PENDING;
		if(reservation.getPaid())
		{
			status = ReservationStatusTypeEnum.RESERVATION_CONFIRMED;
		}
		else if(reservation.getCancelled())
		{
			status = ReservationStatusTypeEnum.RESERVATION_CANCELLED;
		}
		// TODO: set paid for the template somehow. status is a bit odd to use
		EventReservation eventReservation = CoreFactory.newEventReservationBuilder().addReservationId("" + reservation.id)
				.addReservationStatus(status)
				.addUnderName(CoreFactory.newPersonBuilder().addName(reservation.getName()).addEmail(reservation.getEmail())
						.addTelephone(reservation.getPhone()))
				.addDescription(reservation.getNrOfSeats() + " seats").addReservationFor(event)
				.addTotalPrice(CoreFactory.newPriceSpecificationBuilder().addPriceCurrency(CURRENCY).addPrice(priceToPay.toString())).build();
		String asJsonLd = getAsJson(eventReservation);

		HashMap<String, Object> map = new HashMap<>(new Gson().fromJson(asJsonLd, Map.class));
		map.put("jsonLd", asJsonLd);
		return map;
	}

	private static BigDecimal priceToPay(long nrOfSeats, String discount)
	{
		return ticketPrice.multiply(new BigDecimal(nrOfSeats)).multiply(determinePriceModifier(discount)).setScale(0, RoundingMode.HALF_UP);
	}

	private static BigDecimal priceToPay(Reservation reservation)
	{
		return ticketPrice.multiply(new BigDecimal(reservation.getNrOfSeats())).multiply(determinePriceModifier(reservation.getDiscount()))
				.setScale(0, RoundingMode.HALF_UP);
	}

	private void sendStep1ConfirmationEmail(Reservation reservation) throws EmailException
	{
		sendEmail(	reservation, reservation.getName() + " your reservation is partially completed", "event-reservation-confirmation-email.ftlh",
					"reservations");
	}

	private void sendReminderEmail(Reservation reservation) throws EmailException
	{
		sendEmail(reservation, "See you soon @ " + eventName + "!", "reservation-reminder.ftlh", "reminder");
	}

	private void sendConfirmationEmail(Reservation reservation)
	{
		sendEmail(reservation, "Payment confirmed for " + eventName, "reservation-confirmation.ftlh", "reminder");
	}

	private void sendEmail(Reservation reservation, String subject, String template, String category)
	{
		Email from = new Email("jontejj@gmail.com", "Malmö Improvisatorium Reservations");
		Email to = new Email(reservation.getEmail(), reservation.getName());
		String emailText = generateTemplateWithData(template, reservation);
		Content content = new Content("text/html", emailText);
		Mail mail = new Mail(from, subject, to, content);

		String apiKey = loadConfig(CONFIG_KEY_SENDGRID);
		SendGrid sg = new SendGrid(apiKey);
		Request request = new Request();
		try
		{
			mail.addCategory(category);
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sg.api(request);
			LOG.info("Returned status code: {}", response.getStatusCode());
			LOG.info("Body: {}", response.getBody());
			LOG.info("Headers: {}", response.getHeaders());
		}
		catch(IOException ex)
		{
			throw new EmailException("Failed to send reservation email to " + reservation.getEmail(), ex);
		}
	}

	private static String generateTemplateWithData(String templateName, Reservation reservation) throws EmailException
	{
		BigDecimal priceToPay = priceToPay(reservation.getNrOfSeats(), reservation.getDiscount());
		Map<String, Object> map = reservationInformation(reservation, priceToPay);
		try
		{
			Template temp = cfg.getTemplate(templateName);
			StringWriter out = new StringWriter();
			temp.process(map, out);
			return out.toString();
		}
		catch(IOException | TemplateException ex)
		{
			throw new EmailException("Failed to generate reservation email", ex);
		}
	}

	private static final JsonLdSerializer serializer = new JsonLdSerializer(true /* setPrettyPrinting */);

	public static String getAsJson(EventReservation reservation)
	{
		try
		{
			return serializer.serialize(reservation);
		}
		catch(JsonLdSyntaxException | JsonIOException e)
		{
			throw new RuntimeException("Failed to generate schema.org string", e);
		}
	}

	private static BigDecimal determinePriceModifier(String discount)
	{
		switch(discount)
		{
		// case "Folk Universitetet":
		// return folkUniPricePercentage;
		case "MAF-member":
		case "Student":
			return memberPricePercentage;
		case "Normal":
		default:
			return BigDecimal.ONE;
		}
	}

	@SuppressWarnings("deprecation")
	@WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = MyUI.class, productionMode = true)
	public static class MyUIServlet extends GAEVaadinServlet
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void init(ServletConfig servletConfig) throws ServletException
		{
			super.init(servletConfig);
			// ObjectifyService.init();
			// ObjectifyService.init(new
			// ObjectifyFactory(DatastoreOptions.newBuilder().setCredentials(GoogleCredentials.getApplicationDefault()).build().getService()));
			// ObjectifyService.register(Reservation.class);
			// ObjectifyService.register(SeatsRemaining.class);
			// ObjectifyService.register(Config.class);

		}

		@Override
		protected void service(HttpServletRequest unwrappedRequest, HttpServletResponse unwrappedResponse) throws ServletException, IOException
		{
			// System.out.println("Env:" + System.getenv());
			// System.out.println("Properties:" + System.getProperties());
			super.service(unwrappedRequest, unwrappedResponse);
		}

		@Override
		public void destroy()
		{
			super.destroy();
			// MemcacheServiceFactory.getMemcacheService().clearAll();
		}
	}

	@WebFilter(urlPatterns = "/*", asyncSupported = true)
	public static class MyObjectifyFilter extends ObjectifyFilter
	{
		@Override
		public void init(FilterConfig filterConfig) throws ServletException
		{
			ObjectifyService.register(Reservation.class);
			ObjectifyService.register(SeatsRemaining.class);
			ObjectifyService.register(Config.class);
			try(Closeable closeable = ObjectifyService.begin())
			{
				ObjectifyService.ofy().transactNew(new VoidWork(){
					@Override
					public void vrun()
					{
						LOG.info("Configuring seats");
						Objectify ofy = ObjectifyService.ofy();
						SeatsRemaining now = ofy.load().key(Key.create(SeatsRemaining.class, "" + EVENT_ID)).now();
						if(now == null)
						{
							ofy.save().entities(new SeatsRemaining().setEventId("" + EVENT_ID).setSeatsRemaining(initialSeatCapacity)).now();
						}
						String sendgridKey = System.getProperty("config." + CONFIG_KEY_SENDGRID);
						if(sendgridKey != null)
						{
							Config alreadyExists = ofy.load().key(Key.create(Config.class, CONFIG_KEY_SENDGRID)).now();
							if(alreadyExists == null)
							{
								Config config = new Config();
								config.setKey(CONFIG_KEY_SENDGRID);
								config.setValue(sendgridKey);
								ofy.save().entities(config).now();
							}
						}
					}
				});
			}
		}
	}
}
