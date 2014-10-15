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
import com.workday.hr.EmployeeGetType;
import com.workday.hr.EmployeeReferenceType;
import com.workday.hr.ExternalIntegrationIDReferenceDataType;
import com.workday.hr.GetWorkersResponseType;
import com.workday.hr.IDType;
import com.workday.hr.WorkerType;
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

	private static String WORKDAY_MANAGER_ID;
	private static String WORKDAY_POSITION_ID;
	private static final String INTEGRATION_ID = "Salesforce - Chatter";
	private static final String TERMINATION_REASON_ID = "208082cd6b66443e801d95ffdc77461b";
	private static final String VAR_ID = "Id";
	private static final String VAR_USERNAME = "Username";
	private static final String VAR_LAST_NAME = "LastName";
	private static final String VAR_FIRST_NAME = "FirstName";
	private static final String VAR_EMAIL = "Email";
	private static String SFDC_PROFILE_ID;
	private String EXT_ID, LAST_NAME = "WorkdayWorker";
	private final String EMAIL = "bwillis@gmailtest.com";
	private final String EMAIL1 = "bwillisss@gmailtest.com";
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
	private static String SFDC_ID = null;
	
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
	
		WORKDAY_MANAGER_ID = props.getProperty("wday.manager.id");
		WORKDAY_POSITION_ID = props.getProperty("wday.position.id");
		SFDC_ID = props.getProperty("sfdc.testuser.id");
		SFDC_PROFILE_ID = props.getProperty("sfdc.profileId");
		
		System.setProperty("page.size", "1000");

		System.setProperty("poll.startDelayMillis", "8000");
        System.setProperty("poll.frequencyMillis", "15000");
		
	}

	@Before
	public void setUp() throws Exception {		
		stopAutomaticPollTriggering();
		getAndInitializeFlows();

		batchTestHelper = new BatchTestHelper(muleContext);			
		createTestDataInSalesforceSandbox();
	}

	private void createTestDataInSalesforceSandbox() throws MuleException, Exception{
		Map<String, Object> salesforceUser0 = new HashMap<String, Object>();
		String infixSalesforce = "_0_SFDC_" + ANYPOINT_TEMPLATE_NAME + "_" + System.currentTimeMillis();
		salesforceUser0.put(VAR_ID, SFDC_ID);
		salesforceUser0.put(VAR_USERNAME, "Name" + infixSalesforce + "@example.com");
		salesforceUser0.put(VAR_FIRST_NAME, "fn" + infixSalesforce);
		salesforceUser0.put(VAR_LAST_NAME, "ln" + infixSalesforce);
		salesforceUser0.put(VAR_EMAIL, EMAIL);
		salesforceUser0.put("ProfileId", SFDC_PROFILE_ID);
		salesforceUser0.put("Alias", "al0Sfdc");
		salesforceUser0.put("TimeZoneSidKey", "GMT");
		salesforceUser0.put("LocaleSidKey", "en_US");
		salesforceUser0.put("EmailEncodingKey", "ISO-8859-1");
		salesforceUser0.put("LanguageLocaleKey", "en_US");
		salesforceUser0.put("CommunityNickname", "cn" + infixSalesforce);
		createdUsersInSalesforce.clear();
		createdUsersInSalesforce.add(salesforceUser0);
		logger.info("updating salesforce user: " + salesforceUser0.get(VAR_EMAIL));
		upsertUserInSalesforceFlow.process(getTestEvent(Collections.singletonList(salesforceUser0), MessageExchangePattern.REQUEST_RESPONSE));		
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

		// Flow for updating a user in Workday
		hireWorkdaEmployeeFlow = getSubFlow("hireEmployeeFlow");
		hireWorkdaEmployeeFlow.initialise();

		// Flow for querying the user in Salesforce
		queryUserFromSalesforceFlow = getSubFlow("queryUserFromSalesforceFlow");
		queryUserFromSalesforceFlow.initialise();

		// Flow for querying the user in Workday
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
		employee = new Employee(EXT_ID, LAST_NAME, EMAIL1, "232-2323", "999 Main St", "San Francisco", "CA", "94105", "US", "o7aHYfwG", 
				"2014-04-17-07:00", "2014-04-21-07:00", "QA Engineer", "San_Francisco_site", "Regular", "Full Time", "Salary", "USD", "140000", "Annual", WORKDAY_POSITION_ID, WORKDAY_MANAGER_ID, EXT_ID);
		List<Employee> list = new ArrayList<Employee>();
		list.add(employee);
		createdUsersInWorkday.add(EXT_ID);
		return list;
	}
    
	@Test
	public void testSalesforceDirection() throws MuleException, Exception {		
		// test sfdc -> workday	
		Thread.sleep(1000);
		executeWaitAndAssertBatchJob(SALESFORCE_INBOUND_FLOW_NAME);
		Map<String, Object> user = new HashMap<String, Object>();
		user.put(VAR_EMAIL, EMAIL);
		Map<String, Object> sfdcUser = (Map<String, Object>)queryUser(user , queryUserFromSalesforceFlow);
		
		WorkerType worker = queryWorkdayUser(sfdcUser.get("ExtId__c").toString(), queryUserFromWorkdayFlow);
		
		Assert.assertFalse("Synchronized user should not be null payload", worker == null);
		Assert.assertEquals("The user should have been sync and new name must match", createdUsersInSalesforce.get(0).get(VAR_FIRST_NAME), 
				worker.getWorkerData().getPersonalData().getNameData().getPreferredNameData().getNameDetailData().getFirstName());
		Assert.assertEquals("The user should have been sync and new title must match", createdUsersInSalesforce.get(0).get(VAR_LAST_NAME), 
				worker.getWorkerData().getPersonalData().getNameData().getPreferredNameData().getNameDetailData().getLastName());
			
	}

	@Test
	public void testWorkdayDirection() throws Exception{
		// because of workday delay in processing, test data is created here
		createTestDataInWorkdaySandBox();
		// test workday -> sfdc
		Thread.sleep(15000);
		
		executeWaitAndAssertBatchJob(WORKDAY_INBOUND_FLOW_NAME);

		Map<String, Object> workdayUser = new HashMap<String, Object>();
		workdayUser.put(VAR_EMAIL, EMAIL1);
		workdayUser.put(VAR_FIRST_NAME, EXT_ID);
		workdayUser.put(VAR_LAST_NAME, LAST_NAME);
		Object object =  queryUser(workdayUser , queryUserFromSalesforceFlow);
		
		Assert.assertFalse("Synchronized user should not be null payload", object instanceof NullPayload);
		
		Map<String, Object> payload = (Map<String, Object>) object;
		
		Assert.assertEquals("The user should have been sync and new name must match", workdayUser.get(VAR_FIRST_NAME), payload.get(VAR_FIRST_NAME));
		Assert.assertEquals("The user should have been sync and new title must match", workdayUser.get(VAR_LAST_NAME), payload.get(VAR_LAST_NAME));
		createdUsersInSalesforce.add(payload);
	
	}
	
	private WorkerType queryWorkdayUser(String id,
			InterceptingChainLifecycleWrapper queryUserFromWorkdayFlow2) {
		try {
			MuleEvent response = queryUserFromWorkdayFlow2.process(getTestEvent(id, MessageExchangePattern.REQUEST_RESPONSE));
			GetWorkersResponseType res = (GetWorkersResponseType) response.getMessage().getPayload();
			return res.getResponseData().getWorker().isEmpty() ? null : res.getResponseData().getWorker().get(0);
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
		List<Map<String, Object>> idList = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> c : createdUsersInSalesforce) {
			logger.info("deleting SFDC user: " + c.get(VAR_ID));			
			Map<String, Object> entry = new HashMap<String, Object>();
			entry.put("Id", c.get(VAR_ID));
			entry.put("isActive", false);
			idList.add(entry);
		}
		
		SubflowInterceptingChainLifecycleWrapper deleteUserFromSalesforceFlow = getSubFlow("deleteUserFromSalesforceFlow");
		deleteUserFromSalesforceFlow.initialise();
		deleteUserFromSalesforceFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));		
	}
}
