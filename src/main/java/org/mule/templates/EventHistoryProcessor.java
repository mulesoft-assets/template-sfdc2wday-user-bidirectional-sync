/**
 * Mule Anypoint Template
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;

import com.workday.hr.GetWorkersResponseType;
import com.workday.hr.TransactionLogEntryType;

public class EventHistoryProcessor implements MessageProcessor{

	@Override
	public MuleEvent process(MuleEvent event) throws MuleException {
		GetWorkersResponseType workerResponse = (GetWorkersResponseType) event.getMessage().getPayload();
		Calendar lastModifiedDate = null;
		if (workerResponse.getResponseData() != null){
			for (TransactionLogEntryType entry : workerResponse.getResponseData().getWorker().get(0).getWorkerData().getTransactionLogEntryData().getTransactionLogEntry()){
				if (lastModifiedDate == null || (lastModifiedDate != null && entry.getTransactionLogData().getTransactionEffectiveMoment() != null 
						&& entry.getTransactionLogData().getTransactionEffectiveMoment().compareTo(lastModifiedDate) > 0))
					lastModifiedDate = entry.getTransactionLogData().getTransactionEffectiveMoment();
			}
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");		
		event.getMessage().setPayload(lastModifiedDate == null ? null : sdf.format(lastModifiedDate.getTime()));		
		return event;
	}
	
}
