/**
 * Mule Anypoint Template
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;

import com.workday.integrations.GetEventDetailsResponseType;

public class TransactionLogProcessor implements MessageProcessor{

	@Override
	public MuleEvent process(MuleEvent event) throws MuleException {
		GetEventDetailsResponseType response = (GetEventDetailsResponseType) event.getMessage().getPayload();
		if (response.getResponseData().getEvent().isEmpty())
			throw new IllegalArgumentException("No event detail data to process.");
		if (response.getResponseData().getEvent().get(0).
				getEventDetailData().getInitiatingPersonReference().getID().isEmpty())
			throw new IllegalArgumentException("No initiator of event.");
		
		Map<String, String> payload = new HashMap<String, String>();
		payload.put("LastModifiedById", response.getResponseData().getEvent().get(0).
				getEventDetailData().getInitiatingPersonReference().getID().get(0).getValue());
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'");
		
		String lastModifiedDate = formatter.print(response.getResponseData().getEvent().get(0).getEventDetailData().getCompletedDate().getTimeInMillis());
		
		payload.put("LastModifiedDate", lastModifiedDate);
		
		event.getMessage().setPayload(payload);
		return event;
	}
	
}
