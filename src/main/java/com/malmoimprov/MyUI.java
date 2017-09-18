package com.malmoimprov;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.annotation.WebServlet;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.schemaorg.JsonLdSerializer;
import com.google.schemaorg.JsonLdSyntaxException;
import com.google.schemaorg.core.CoreFactory;
import com.google.schemaorg.core.EventReservation;
import com.google.schemaorg.core.ReservationStatusTypeEnum;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.server.GAEVaadinServlet;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

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
public class MyUI extends UI {

	//private static final Logger log = Logger.getLogger(MyUI.class.getName());

	//TODO(joj): save in the database
	private static final AtomicLong reservationIds = new AtomicLong(1);
	private static final com.google.schemaorg.core.Event event =
			CoreFactory.newTheaterEventBuilder()
			.addUrl("https://www.facebook.com/events/126158174682437")
			.addName("Malmö Improvisatorium Performance")
			.addOrganizer("Malmö Improvisatorium")
			.addStartDate("2017-10-14T18:30:00+02:00")
			.addDuration("PT1H30M")
			.addLocation(CoreFactory.newPlaceBuilder().addName("MAF, scen 2")
			             .addAddress(CoreFactory.newPostalAddressBuilder().addStreetAddress("Norra Skolgatan 12")
			                         .addAddressLocality("Malmö")
			                         .addAddressRegion("SE-M")
			                         .addPostalCode("21152")
			                         .addAddressCountry("SE")))
			//.addProperty("customPropertyName", "customPropertyValue")
			.build();
	private static final Configuration cfg;
	static
	{
		cfg = new Configuration(Configuration.VERSION_2_3_25);
		cfg.setClassForTemplateLoading(MyUI.class, "/email-templates/");
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
	}
	private Binder<Reservation> binder;

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		binder = new BeanValidationBinder<>(Reservation.class);

		final VerticalLayout layout = new VerticalLayout();

		final TextField name = new TextField();
		name.setCaption("Name:");
		name.setRequiredIndicatorVisible(true);
		binder.forField(name).bind("name");

		final TextField email = new TextField();
		email.setCaption("Email:");
		email.setRequiredIndicatorVisible(true);
		binder.forField(email).bind("email");

		final TextField nrOfSeats = new TextField();
		nrOfSeats.setValue("1");
		nrOfSeats.setCaption("Nr of seats to reserve:");
		nrOfSeats.setRequiredIndicatorVisible(true);
		binder.forField(nrOfSeats).withConverter(new StringToIntegerConverter("Invalid nr of seats")).bind("nrOfSeats");

		Button button = new Button("Reserve seats");
		button.addClickListener( e -> {
			Reservation reservation = new Reservation();
			try
			{
				binder.writeBean(reservation);
			}
			catch(ValidationException e1)
			{
				//server side validation fails after client validation has passed?
			}
			EventReservation eventReservation = CoreFactory.newEventReservationBuilder()
					.addReservationId("" + reservationIds.getAndIncrement())
					.addReservationStatus(ReservationStatusTypeEnum.RESERVATION_CONFIRMED)
					.addUnderName(CoreFactory.newPersonBuilder()
					              .addName(reservation.getName())
					              .addEmail(reservation.getEmail()))
					.addDescription(reservation.getNrOfSeats() + " seats")
					.addReservationFor(event).build();
			String asJsonLd = getAsJson(eventReservation);
			HashMap<String, Object> map = new HashMap<>(new Gson().fromJson(asJsonLd,Map.class));
			map.put("jsonLd", asJsonLd);
			sendConfirmationEmail(reservation, map);
			layout.addComponent(new Label("Thanks " + reservation.getName()
			+ ", your reservation of " + reservation.getNrOfSeats() + " seat(s) is complete! An email confirmation has been sent to " + reservation.getEmail()));
		});

		layout.addComponents(name, email, nrOfSeats, button);

		setContent(layout);
	}

	private void sendConfirmationEmail(Reservation reservation, Map<String, Object> registrationJson) throws EmailException
	{
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		try {
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("jontejj@gmail.com", "Malmö Improvisatorium Reservations"));
			msg.addRecipient(Message.RecipientType.TO,
			                 new InternetAddress(reservation.getEmail(), reservation.getName()));
			msg.setSubject(reservation.getName() + " your reservation is complete");

			String emailText = generateReservationConfirmationEmail(registrationJson);
			System.out.println(emailText);
			msg.setText(emailText, Charsets.UTF_8.toString(), "html");
			Transport.send(msg);
		}catch (MessagingException | UnsupportedEncodingException ex) {
			throw new EmailException("Failed to send reservation email to " + reservation.getEmail(), ex);
		}
	}

	private String generateReservationConfirmationEmail(Map<String, Object> registrationJson) throws EmailException
	{
		try {
			Template temp = cfg.getTemplate("event-reservation-confirmation-email.ftlh");
			StringWriter out = new StringWriter();
			temp.process(registrationJson, out);
			return out.toString();
		}
		catch(IOException | TemplateException ex)
		{
			throw new EmailException("Failed to generate reservation email", ex);
		}
	}

	private static final JsonLdSerializer serializer = new JsonLdSerializer(true /* setPrettyPrinting */);

	public String getAsJson(EventReservation reservation)
	{
		try {
			return serializer.serialize(reservation);
		} catch (JsonLdSyntaxException | JsonIOException e) {
			throw new RuntimeException("Failed to generate schema.org string", e);
		}
	}

	@SuppressWarnings("deprecation")
	@WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = MyUI.class, productionMode = true)
	public static class MyUIServlet extends GAEVaadinServlet {
	}
}
