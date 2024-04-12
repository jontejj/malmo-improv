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

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Based on https://vaadin.com/blog/oauth-2-and-google-sign-in-for-a-vaadin-application
 */
@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout
{
	/**
	 * URL that Spring uses to connect to Google services
	 */
	private static final String OAUTH_URL = "/oauth2/authorization/google";

	public LoginView()
	{
		Anchor loginLink = new Anchor(OAUTH_URL, "Login with Google");
		// Set router-ignore attribute so that Vaadin router doesn't handle the login request
		loginLink.getElement().setAttribute("router-ignore", true);
		add(loginLink);
		getStyle().set("padding", "200px");
		setAlignItems(Alignment.CENTER);
	}
}
