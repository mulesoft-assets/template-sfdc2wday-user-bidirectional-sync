/**
 * Mule Anypoint Template
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
import com.workday.hr.WorkerObjectIDType;
import com.workday.hr.WorkerObjectType;
import com.workday.hr.WorkerProfileGetType;
import com.workday.hr.WorkerReferenceType;
import com.workday.hr.WorkerRequestCriteriaType;
import com.workday.hr.WorkerRequestReferencesType;
import com.workday.hr.WorkerResponseGroupType;
import com.workday.integrations.GetReferencesRequestCriteriaType;
import com.workday.integrations.GetReferencesRequestType;
import com.workday.integrations.PutReferenceRequestType;
import com.workday.integrations.ReferenceIDDataType;
import com.workday.integrations.ReferenceIndexObjectIDType;
import com.workday.integrations.ReferenceIndexObjectType;
import com.workday.integrations.ReferencesRequestReferencesType;

public class WorkersRequest {

	public static GetWorkersRequestType create(Date startDate, int periodInMillis, int offset) throws ParseException, DatatypeConfigurationException {
		
		EffectiveAndUpdatedDateTimeDataType dateRangeData = new EffectiveAndUpdatedDateTimeDataType();
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
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

	public static GetWorkersRequestType getWorker(String id) throws ParseException, DatatypeConfigurationException {
		
		WorkerRequestReferencesType reqRefs = new WorkerRequestReferencesType();
		List<WorkerObjectType> workerReferences = new ArrayList<WorkerObjectType>();
		WorkerObjectType wot = new WorkerObjectType();
		List<WorkerObjectIDType> woids = new ArrayList<WorkerObjectIDType>();
		WorkerObjectIDType woidd = new WorkerObjectIDType();
		woidd.setType("Employee_ID");
		woidd.setValue(id);
		woids.add(woidd );
		wot.setID(woids );
		workerReferences.add(wot );
		reqRefs.setWorkerReference(workerReferences );
		GetWorkersRequestType getWorkersType = new GetWorkersRequestType();

		getWorkersType.setRequestReferences(reqRefs);
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

public static GetWorkersRequestType getWorkerEvents(String id, int periodInMillis, int offset) throws ParseException, DatatypeConfigurationException {
		
		GetWorkersRequestType get = getWorker(id);
		EffectiveAndUpdatedDateTimeDataType dateRangeData = new EffectiveAndUpdatedDateTimeDataType();
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.SECOND, -offset / 1000);
		dateRangeData.setUpdatedThrough(xmlDate(cal.getTime()));
		System.out.println("through: " + cal.getTime());
		cal.add(Calendar.SECOND, - (periodInMillis + offset) / 1000);
		dateRangeData.setUpdatedFrom(xmlDate(cal.getTime()));
		System.out.println("from: " + cal.getTime());
		
		TransactionLogCriteriaType transactionLogCriteria = new TransactionLogCriteriaType();
		transactionLogCriteria.setTransactionDateRangeData(dateRangeData);
		
		WorkerRequestCriteriaType workerRequestCriteria = new WorkerRequestCriteriaType();
		workerRequestCriteria.getTransactionLogCriteriaData().add(transactionLogCriteria);
		get.setRequestCriteria(workerRequestCriteria);
		return get ;
	}
	private static XMLGregorianCalendar xmlDate(Date date) throws DatatypeConfigurationException {
		GregorianCalendar gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
		gregorianCalendar.setTime(date);
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
	}

	public static WorkerProfileGetType getWorkers(String id){
		WorkerProfileGetType get = new WorkerProfileGetType();
		
		WorkerReferenceType workerRef = new WorkerReferenceType();		
		EmployeeReferenceType empRef = new EmployeeReferenceType();
		ExternalIntegrationIDReferenceDataType extId = new ExternalIntegrationIDReferenceDataType();
		IDType idType = new IDType();
		idType.setSystemID("WID");
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
		idType.setSystemID("Employee_ID");
		idType.setValue(id);
		extId.setID(idType );
		empRef.setIntegrationIDReference(extId);
		wRef.setEmployeeReference(empRef);
		get.setWorkerReference(wRef);
		return get ;
	}
	
	
	public static PutReferenceRequestType updateId(String id, String sfdcId){
		PutReferenceRequestType get = new PutReferenceRequestType();
		ReferenceIndexObjectType indexO = new ReferenceIndexObjectType();
		List<ReferenceIndexObjectIDType> ids = new ArrayList<ReferenceIndexObjectIDType>();
		ReferenceIndexObjectIDType e = new ReferenceIndexObjectIDType();
		e.setParentType(null);
		e.setParentId(null);
		e.setType("WID");
		e.setValue(id);
		ids.add(e );
		indexO.setID(ids );		
		get.setReferenceIDReference(indexO );
		
		ReferenceIDDataType idData = new ReferenceIDDataType();
		idData.setReferenceIDType("Integration_Identifier_ID");
		idData.setID(sfdcId);
		
		get.setReferenceIDData(idData );
		
		return get;
	}
	
	public static GetReferencesRequestType getReferences(){
		GetReferencesRequestType get = new GetReferencesRequestType();
		GetReferencesRequestCriteriaType crit = new GetReferencesRequestCriteriaType();
		crit.setReferenceIDType("Integration_Identifier_ID");
		
		ReferencesRequestReferencesType reqRef = new ReferencesRequestReferencesType();
		List<ReferenceIndexObjectType> referenceIDReference = new ArrayList<ReferenceIndexObjectType>();
		ReferenceIndexObjectType arg0 = new ReferenceIndexObjectType();
		List<ReferenceIndexObjectIDType> id = new ArrayList<ReferenceIndexObjectIDType>();
		ReferenceIndexObjectIDType e = new ReferenceIndexObjectIDType();
		e.setParentId(null);
		e.setParentType(null);
		e.setType("WID");
		e.setValue("6f02815386371053595fe1d9c9a1d008");
		
//		value 6f02815386371053595fe025d041d002
		
//		pid "72d1073ba8f510514c94a7d746091bb7"
//		pt "WID"
//		t "Integration_Identifier_ID"
//		v "Bruce_1410519854827"
		id.add(e);
		arg0.setID(id);
		referenceIDReference.add(arg0);
		reqRef.setReferenceIDReference(referenceIDReference );
//		get.setRequestReferences(reqRef );
		get.setRequestCriteria(crit );
		return get ;
	}
}
