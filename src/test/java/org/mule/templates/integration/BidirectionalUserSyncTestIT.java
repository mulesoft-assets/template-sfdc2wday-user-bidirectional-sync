/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.kicks.Employee;
import org.mule.processor.chain.InterceptingChainLifecycleWrapper;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.transport.NullPayload;

import com.mulesoft.module.batch.BatchTestHelper;
import com.sforce.soap.partner.UpsertResult;
import com.workday.hr.EmployeeGetType;
import com.workday.hr.EmployeeReferenceType;
import com.workday.hr.EmployeeType;
import com.workday.hr.ExternalIntegrationIDReferenceDataType;
import com.workday.hr.IDType;
import com.workday.staffing.EventClassificationSubcategoryObjectIDType;
import com.workday.staffing.EventClassificationSubcategoryObjectType;
import com.workday.staffing.TerminateEmployeeDataType;
import com.workday.staffing.TerminateEmployeeRequestType;
import com.workday.staffing.TerminateEventDataType;

/**
 * The objective of this class is validating the correct behavior of the flows
 * for this Mule Anypoint Template
 * 
 */
@SuppressWarnings("unchecked")
public class BidirectionalUserSyncTestIT extends AbstractTemplateTestCase {

	private static final String INTEGRATION_ID = "Salesforce - Chatter";
	private static final String TERMINATION_REASON_ID = "208082cd6b66443e801d95ffdc77461b";
	private static final String VAR_ID = "Id";
	private static final String VAR_USERNAME = "Username";
	private static final String VAR_LAST_NAME = "LastName";
	private static final String VAR_FIRST_NAME = "FirstName";
	private static final String VAR_EMAIL = "Email";
	private static String SFDC_PROFILE_ID;
	private String EXT_ID, EMAIL = "bwillis@gmailtest.com", FIRST_NAME, lAST_NAME;
	private Employee employee;
	private static final String ANYPOINT_TEMPLATE_NAME = "userBiSync";
	private static final String SALESFORCE_INBOUND_FLOW_NAME = "triggerSyncFromSalesforceFlow";
	private static final String WORKDAY_INBOUND_FLOW_NAME = "triggerSyncFromWorkdayFlow";
	private static final int TIMEOUT_MILLIS = 60;

	private SubflowInterceptingChainLifecycleWrapper upsertUserInSalesforceFlow;
	private SubflowInterceptingChainLifecycleWrapper hireWorkdaEmployeeFlow;
	private InterceptingChainLifecycleWrapper queryUserFromSalesforceFlow;
	private InterceptingChainLifecycleWrapper queryUserFromWorkdayFlow;
	private BatchTestHelper batchTestHelper;
	
	private static final String PATH_TO_TEST_PROPERTIES = "./src/test/resources/mule.test.properties";
	
	private List<Map<String, Object>> createdUsersInSalesforce = new ArrayList<Map<String, Object>>();
	private List<String> createdUsersInWorkday = new ArrayList<String>();

	@BeforeClass
	public static void beforeTestClass() {
		
		final Properties props = new Properties();
		try {
			props.load(new FileInputStream(PATH_TO_TEST_PROPERTIES));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		SFDC_PROFILE_ID = props.getProperty("sfdc.profileId");
		
		System.setProperty("page.size", "1000");

		// Set polling frequency to 10 seconds
		System.setProperty("poll.startDelayMillis", "8000");
        System.setProperty("poll.frequencyMillis", "30000");

		// Set default water-mark expression to current time
		System.clearProperty("watermark.default.expression");
		DateTime now = new DateTime(DateTimeZone.UTC);
		DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//		System.setProperty("watermark.default.expression", now.toDate());
		
	}

	@Before
	public void setUp() throws Exception {
		
		stopAutomaticPollTriggering();
		getAndInitializeFlows();

		batchTestHelper = new BatchTestHelper(muleContext);
		
		createTestDataInWorkdaySandBox();
		createTestDataInSalesforceSandbox();
	}

	private void createTestDataInSalesforceSandbox() throws MuleException, Exception{
		Map<String, Object> salesforceUser0 = new HashMap<String, Object>();
		String infixSalesforce = "_0_SFDC_" + ANYPOINT_TEMPLATE_NAME + "_" + System.currentTimeMillis();
		salesforceUser0.put(VAR_USERNAME, "Name" + infixSalesforce + "@example.com");
		salesforceUser0.put(VAR_FIRST_NAME, "fn" + infixSalesforce);
		salesforceUser0.put(VAR_LAST_NAME, "ln" + infixSalesforce);
		salesforceUser0.put(VAR_EMAIL, "email" + infixSalesforce + "@example.com");
		salesforceUser0.put("ProfileId", SFDC_PROFILE_ID);
		salesforceUser0.put("Alias", "al0Sfdc");
		salesforceUser0.put("TimeZoneSidKey", "GMT");
		salesforceUser0.put("LocaleSidKey", "en_US");
		salesforceUser0.put("EmailEncodingKey", "ISO-8859-1");
		salesforceUser0.put("LanguageLocaleKey", "en_US");
		salesforceUser0.put("CommunityNickname", "cn" + infixSalesforce);
		createdUsersInSalesforce.add(salesforceUser0);

		MuleEvent event = upsertUserInSalesforceFlow.process(getTestEvent(Collections.singletonList(salesforceUser0), MessageExchangePattern.REQUEST_RESPONSE));
		salesforceUser0.put(VAR_ID, (((UpsertResult) ((List<?>) event.getMessage().getPayload()).get(0))).getId());
		createdUsersInWorkday.add(salesforceUser0.get(VAR_ID).toString());
	}
	
	private void stopAutomaticPollTriggering() throws MuleException {
		stopFlowSchedulers(SALESFORCE_INBOUND_FLOW_NAME);
		stopFlowSchedulers(WORKDAY_INBOUND_FLOW_NAME);
	}

	private void getAndInitializeFlows() throws InitialisationException {
		// Flow for updating a user in Salesforce
		upsertUserInSalesforceFlow = getSubFlow("upsertUserInSalesforceFlow");
		upsertUserInSalesforceFlow.initialise();

		// Flow for updating a user in Database
		hireWorkdaEmployeeFlow = getSubFlow("hireEmployeeFlow");
		hireWorkdaEmployeeFlow.initialise();

		// Flow for querying the user in Salesforce
		queryUserFromSalesforceFlow = getSubFlow("queryUserFromSalesforceFlow");
		queryUserFromSalesforceFlow.initialise();

		// Flow for querying the user in Database
		queryUserFromWorkdayFlow = getSubFlow("queryWorkdayEmployeeFlow");
		queryUserFromWorkdayFlow.initialise();
	}

	@SuppressWarnings("unchecked")
	private void createTestDataInWorkdaySandBox() throws MuleException, Exception {
		logger.info("creating a workday employee...");
		try {
			hireWorkdaEmployeeFlow.process(getTestEvent(prepareNewHire(), MessageExchangePattern.REQUEST_RESPONSE));						
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    private List<Employee> prepareNewHire(){
		EXT_ID = "SFDC2Workday_" + System.currentTimeMillis();
		logger.info("employee name: " + EXT_ID);
		employee = new Employee(EXT_ID, "Willis1", EMAIL, "650-232-2323", "999 Main St", "San Francisco", "CA", "94105", "US", "o7aHYfwG", 
				"2014-04-17-07:00", "2014-04-21-07:00", "QA Engineer", "San_Francisco_site", "Regular", "Full Time", "Salary", "USD", "140000", "Annual", "39905", "21440", EXT_ID);
		List<Employee> list = new ArrayList<Employee>();
		list.add(employee);
		createdUsersInWorkday.add(EXT_ID);
		return list;
	}
    
	@Test
	public void testBothDirections() throws MuleException, Exception {
		// test workday -> sfdc
		Thread.sleep(20000);
		
		// Execution
		executeWaitAndAssertBatchJob(WORKDAY_INBOUND_FLOW_NAME);

		// Assertions		
		Map<String, Object> workdayUser = new HashMap<String, Object>();
		workdayUser.put(VAR_EMAIL, EMAIL);
		workdayUser.put(VAR_FIRST_NAME, FIRST_NAME);
		workdayUser.put(VAR_LAST_NAME, lAST_NAME);
		Object object =  queryUser(workdayUser , queryUserFromSalesforceFlow);
		Assert.assertFalse("Synchronized user should not be null payload", object instanceof NullPayload);
		Map<String, Object> payload = (Map<String, Object>) object;
		Assert.assertEquals("The user should have been sync and new name must match", workdayUser.get(VAR_FIRST_NAME), payload.get(VAR_FIRST_NAME));
		Assert.assertEquals("The user should have been sync and new title must match", workdayUser.get(VAR_LAST_NAME), payload.get(VAR_LAST_NAME));
		
		// test sfdc -> workday	
//		Thread.sleep(1001);

		// Execution
//		executeWaitAndAssertBatchJob(SALESFORCE_INBOUND_FLOW_NAME);

		// FIXME above call does not wait for batch to complete
//		Thread.sleep(10000);
		
		// Assertions
//		EmployeeType worker = queryWorkdayUser(createdUsersInSalesforce.get(0).get(VAR_ID).toString(), queryUserFromWorkdayFlow);
//		Assert.assertFalse("Synchronized user should not be null payload", worker == null);
//		Assert.assertEquals("The user should have been sync and new name must match", createdUsersInSalesforce.get(0).get(VAR_FIRST_NAME), 
//				worker.getEmployeeData().get(0).getPersonalInfoData().get(0).getPersonData().getNameData().get(0).getFirstName());
//		Assert.assertEquals("The user should have been sync and new title must match", createdUsersInSalesforce.get(0).get(VAR_LAST_NAME), 
//				worker.getEmployeeData().get(0).getPersonalInfoData().get(0).getPersonData().getNameData().get(0).getLastName().get(0).getValue());
		
	}

	private EmployeeType queryWorkdayUser(String id,
			InterceptingChainLifecycleWrapper queryUserFromWorkdayFlow2) {
		try {
			MuleEvent response = queryUserFromWorkdayFlow2.process(getTestEvent(getEmployee(id), MessageExchangePattern.REQUEST_RESPONSE));
			return (EmployeeType) response.getMessage().getPayload();
		} catch (InitialisationException e) {
			e.printStackTrace();
		} catch (MuleException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Object queryUser(Map<String, Object> user, InterceptingChainLifecycleWrapper queryUserFlow) throws MuleException, Exception {
		return queryUserFlow.process(getTestEvent(user, MessageExchangePattern.REQUEST_RESPONSE)).getMessage().getPayload();
	}

	private void executeWaitAndAssertBatchJob(String flowConstructName) throws Exception {
		// Execute synchronization
		runSchedulersOnce(flowConstructName);

		// Wait for the batch job execution to finish
		batchTestHelper.awaitJobTermination(TIMEOUT_MILLIS * 1000, 500);
//		batchTestHelper.assertJobWasSuccessful();
	}
	
	@After
	public void tearDown() throws InitialisationException, MuleException, Exception{
		deleteTestUsersFromSalesforce(); 
		deleteTestDataFromWorkdaySandBox();
	}
	
	private void deleteTestDataFromWorkdaySandBox() throws MuleException, Exception {
		// Delete the created users in Workday
		for (String id : createdUsersInWorkday){
			try {
				SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("getWorkdaytoTerminateFlow");
				flow.initialise();
				logger.info("deleting wday worker: " + id);
				MuleEvent response = flow.process(getTestEvent(getEmployee(id), MessageExchangePattern.REQUEST_RESPONSE));			
				flow = getSubFlow("terminateWorkdayEmployeeFlow");
				flow.initialise();
				response = flow.process(getTestEvent(prepareTerminate(response), MessageExchangePattern.REQUEST_RESPONSE));										
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private TerminateEmployeeRequestType prepareTerminate(MuleEvent response) throws DatatypeConfigurationException{
		TerminateEmployeeRequestType req = (TerminateEmployeeRequestType) response.getMessage().getPayload();
		TerminateEmployeeDataType eeData = req.getTerminateEmployeeData();		
		TerminateEventDataType event = new TerminateEventDataType();
		eeData.setTerminationDate(xmlDate(new Date()));
		EventClassificationSubcategoryObjectType prim = new EventClassificationSubcategoryObjectType();
		List<EventClassificationSubcategoryObjectIDType> list = new ArrayList<EventClassificationSubcategoryObjectIDType>();
		EventClassificationSubcategoryObjectIDType id = new EventClassificationSubcategoryObjectIDType();
		id.setType("WID");
		id.setValue(TERMINATION_REASON_ID);
		list.add(id);
		prim.setID(list);
		event.setPrimaryReasonReference(prim);
		eeData.setTerminateEventData(event );
		return req;		
	}
	
	private static XMLGregorianCalendar xmlDate(Date date) throws DatatypeConfigurationException {
		GregorianCalendar gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
		gregorianCalendar.setTime(date);
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
	}
	
	private EmployeeGetType getEmployee(String id){
		EmployeeGetType get = new EmployeeGetType();
		EmployeeReferenceType empRef = new EmployeeReferenceType();					
		ExternalIntegrationIDReferenceDataType value = new ExternalIntegrationIDReferenceDataType();
		IDType idType = new IDType();
		value.setID(idType);
		idType.setSystemID(INTEGRATION_ID);
		idType.setValue(id);			
		empRef.setIntegrationIDReference(value);
		get.setEmployeeReference(empRef);		
		return get;
	}

	private void deleteTestUsersFromSalesforce() throws InitialisationException, MuleException, Exception {
		List<String> idList = new ArrayList<String>();
		for (Map<String, Object> c : createdUsersInSalesforce) {
			idList.add(c.get(VAR_ID).toString());
		}
		SubflowInterceptingChainLifecycleWrapper deleteUserFromSalesforceFlow = getSubFlow("deleteUserFromSalesforceFlow");
		deleteUserFromSalesforceFlow.initialise();
		deleteUserFromSalesforceFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
	}

}
