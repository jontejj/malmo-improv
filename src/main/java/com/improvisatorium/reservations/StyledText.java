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

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Span;

/**
 * A component to show HTML text.
 *
 * @author Syam (https://vaadin.com/forum/thread/17072019/inject-an-html-into-a-flow-compoment)
 */
public class StyledText extends Composite<Span> implements HasText
{
	private static final long serialVersionUID = 1L;

	private Span content = new Span();
	private String text;

	public StyledText(String htmlText)
	{
		setText(htmlText);
	}

	@Override
	protected Span initContent()
	{
		return content;
	}

	@Override
	public void setText(String htmlText)
	{
		if(htmlText == null)
		{
			htmlText = "";
		}
		if(htmlText.equals(text))
			return;
		text = htmlText;
		content.removeAll();
		content.add(new Html("<span>" + htmlText + "</span>"));
	}

	@Override
	public String getText()
	{
		return text;
	}
}
