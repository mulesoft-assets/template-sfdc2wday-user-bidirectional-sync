/**
 * Mule Anypoint Template
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 */

package org.mule.kicks;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.workday.hr.EffectiveAndUpdatedDateTimeDataType;
import com.workday.hr.EmployeeReferenceType;
import com.workday.hr.ExternalIntegrationIDReferenceDataType;
import com.workday.hr.GetWorkersRequestType;
import com.workday.hr.IDType;
import com.workday.hr.TransactionLogCriteriaType;
import com.workday.hr.WorkerEventHistoryGetType;
import com.workday.hr.WorkerProfileGetType;
import com.workday.hr.WorkerReferenceType;
import com.workday.hr.WorkerRequestCriteriaType;
import com.workday.hr.WorkerResponseGroupType;

public class WorkersRequest {

public static GetWorkersRequestType create(Date startDate, int periodInMillis, int offset) throws ParseException, DatatypeConfigurationException {
		
		EffectiveAndUpdatedDateTimeDataType dateRangeData = new EffectiveAndUpdatedDateTimeDataType();
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.add(Calendar.SECOND, -offset / 1000);
		dateRangeData.setUpdatedThrough(xmlDate(cal.getTime()));
		System.out.println("through: " + cal.getTime());
		cal.setTime(startDate);
		cal.add(Calendar.SECOND, - (periodInMillis + offset) / 1000);
		dateRangeData.setUpdatedFrom(xmlDate(cal.getTime()));
		System.out.println("from: " + cal.getTime());
		TransactionLogCriteriaType transactionLogCriteria = new TransactionLogCriteriaType();
		transactionLogCriteria.setTransactionDateRangeData(dateRangeData);
		
		WorkerRequestCriteriaType workerRequestCriteria = new WorkerRequestCriteriaType();
		workerRequestCriteria.getTransactionLogCriteriaData().add(transactionLogCriteria);
		
		GetWorkersRequestType getWorkersType = new GetWorkersRequestType();
		getWorkersType.setRequestCriteria(workerRequestCriteria);
		
		WorkerResponseGroupType resGroup = new WorkerResponseGroupType();
		resGroup.setIncludeRoles(true);	
		resGroup.setIncludePersonalInformation(true);
		resGroup.setIncludeOrganizations(true);
		resGroup.setIncludeEmploymentInformation(true);
		resGroup.setIncludeReference(true);
		resGroup.setIncludeUserAccount(true);
		resGroup.setIncludeTransactionLogData(true);
		getWorkersType.setResponseGroup(resGroup);
		
		return getWorkersType;
	}

	private static XMLGregorianCalendar xmlDate(Date date) throws DatatypeConfigurationException {
		GregorianCalendar gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
		gregorianCalendar.setTime(date);
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
	}

	public static WorkerProfileGetType getWorker(String id){
		WorkerProfileGetType get = new WorkerProfileGetType();
		
		WorkerReferenceType workerRef = new WorkerReferenceType();
		EmployeeReferenceType empRef = new EmployeeReferenceType();
		ExternalIntegrationIDReferenceDataType extId = new ExternalIntegrationIDReferenceDataType();
		IDType idType = new IDType();
		idType.setSystemID("Salesforce - Chatter");
		idType.setValue(id);
		extId.setID(idType );
		empRef.setIntegrationIDReference(extId);
		workerRef.setEmployeeReference(empRef );
		get.setWorkerReference(workerRef );
		return get;
	}
	
	public static WorkerEventHistoryGetType getWorkerHistory(String id){
		WorkerEventHistoryGetType get = new WorkerEventHistoryGetType();
		WorkerReferenceType wRef = new WorkerReferenceType();
		EmployeeReferenceType empRef = new EmployeeReferenceType();
		ExternalIntegrationIDReferenceDataType extId = new ExternalIntegrationIDReferenceDataType();
		IDType idType = new IDType();
		idType.setSystemID("Salesforce - Chatter");
		idType.setValue(id);
		extId.setID(idType );
		empRef.setIntegrationIDReference(extId);
		wRef.setEmployeeReference(empRef);
		get.setWorkerReference(wRef);
		return get ;
	}
	
}
