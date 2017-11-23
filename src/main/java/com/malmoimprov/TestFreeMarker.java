/* Copyright 2017 jonatanjonsson
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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;

public class TestFreeMarker
{
	public static void test(String[] args)
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException
	{
		/* ------------------------------------------------------------------------ */
		/* You should do this ONLY ONCE in the whole application life-cycle: */

		/* Create and adjust the configuration singleton */
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
		cfg.setClassForTemplateLoading(MyUI.class, "/email-templates/");
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);

		/* ------------------------------------------------------------------------ */
		/* You usually do these for MULTIPLE TIMES in the application life-cycle: */

		Map root = new HashMap();
		root.put("name", "Big Joe");

		Template temp = cfg.getTemplate("event-reservation-confirmation-email.ftlh");
		Writer out = new OutputStreamWriter(System.out);
		temp.process(root, out);
	}
}
