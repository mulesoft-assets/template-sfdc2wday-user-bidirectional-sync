/**
 * Mule Anypoint Template
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;

import com.workday.hr.EventHistoryDataType;
import com.workday.hr.GetWorkersResponseType;
import com.workday.hr.TransactionLogEntryType;
import com.workday.hr.WorkerEventHistoryType;

public class EventHistoryProcessor implements MessageProcessor{

	@Override
	public MuleEvent process(MuleEvent event) throws MuleException {
		GetWorkersResponseType workerResponse = (GetWorkersResponseType) event.getMessage().getPayload();
		XMLGregorianCalendar lastModifiedDate = null;
		if (workerResponse.getResponseData() != null){
			for (TransactionLogEntryType entry : workerResponse.getResponseData().getWorker().get(0).getWorkerData().getTransactionLogEntryData().getTransactionLogEntry()){
				if (lastModifiedDate == null || (lastModifiedDate != null && entry.getTransactionLogData().getTransactionEffectiveMoment() != null 
						&& entry.getTransactionLogData().getTransactionEffectiveMoment().compare(lastModifiedDate) > 0))
					lastModifiedDate = entry.getTransactionLogData().getTransactionEffectiveMoment();
			}
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");		
		event.getMessage().setPayload(lastModifiedDate == null ? null : sdf.format(lastModifiedDate.toGregorianCalendar().getTime()));		
		return event;
	}

	private MuleEvent getLastModifiedDate(MuleEvent event) {
		WorkerEventHistoryType worker = (WorkerEventHistoryType) event.getMessage().getPayload();
		List<EventHistoryDataType> log = worker.getWorkerEventHistoryData().getEventData();
		XMLGregorianCalendar lastModifiedDate = null;
		if (log != null){
			for (EventHistoryDataType entry : log){		
				if (lastModifiedDate == null || (lastModifiedDate != null && entry.getCompletedDateTime()!= null && entry.getCompletedDateTime().compare(lastModifiedDate) > 0))
					lastModifiedDate = entry.getCompletedDateTime();						
			}
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");		
		event.getMessage().setPayload(lastModifiedDate == null ? null : sdf.format(lastModifiedDate.toGregorianCalendar().getTime()));
		return event;
	}
	
}
