package com.improvisatorium.reservations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

/**
 * The entry point of the Spring Boot application.
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */
@SpringBootApplication
@Theme(value = "mytodo")
public class Application extends SpringBootServletInitializer implements AppShellConfigurator
{
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
	{
		return application.sources(Application.class);
	}

	public static void main(String[] args)
	{
		ObjectifyService.init();
		ObjectifyService.register(Reservation.class);
		ObjectifyService.register(SeatsRemaining.class);
		ObjectifyService.register(Config.class);
		try(Closeable closeable = ObjectifyService.begin())
		{
			ObjectifyService.ofy().transactNew(() -> {
				CurrentEvent.init();
				Sendgrid.init();
			});
		}

		SpringApplication.run(Application.class, args);
	}
}
