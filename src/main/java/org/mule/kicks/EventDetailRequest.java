package org.mule.kicks;

import java.util.ArrayList;
import java.util.List;

import com.workday.hr.WorkerType;
import com.workday.integrations.ActionEventObjectIDType;
import com.workday.integrations.ActionEventObjectType;
import com.workday.integrations.EventRequestReferencesType;
import com.workday.integrations.GetEventDetailsRequestType;

public class EventDetailRequest {

	public static GetEventDetailsRequestType create(WorkerType data){
		if (data.getWorkerData().getTransactionLogEntryData().getTransactionLogEntry().isEmpty())
			throw new IllegalArgumentException("No transaction logs to process.");
		
		GetEventDetailsRequestType get = new GetEventDetailsRequestType();
		EventRequestReferencesType req = new EventRequestReferencesType();
		List<ActionEventObjectType> eventReference = new ArrayList<ActionEventObjectType>();
		ActionEventObjectType ot = new ActionEventObjectType();
		List<ActionEventObjectIDType> list = new ArrayList<ActionEventObjectIDType>();
		ActionEventObjectIDType e = new ActionEventObjectIDType();
		e.setType(data.getWorkerData().getTransactionLogEntryData().getTransactionLogEntry().get(0).getTransactionLogReference().getID().get(0).getType());
		e.setValue(data.getWorkerData().getTransactionLogEntryData().getTransactionLogEntry().get(0).getTransactionLogReference().getID().get(0).getValue());
		list.add(e );
		ot.setID(list );
		eventReference.add(ot );
		req.setEventReference(eventReference );
		get.setRequestReferences(req);
		return get ;
	}
}
