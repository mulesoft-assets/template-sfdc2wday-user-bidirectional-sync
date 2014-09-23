package org.mule.kicks;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;

import com.workday.hr.EventHistoryDataType;
import com.workday.hr.WorkerEventHistoryType;

public class EventHistoryProcessor implements MessageProcessor{

	@Override
	public MuleEvent process(MuleEvent event) throws MuleException {
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
