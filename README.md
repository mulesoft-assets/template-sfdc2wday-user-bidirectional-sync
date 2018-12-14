
# Anypoint Template: Salesforce to Workday User Bidirectional Sync	

<!-- Header (start) -->
Bidirectionally synchronizes user data between Salesforce and Workday. Configure this template  by only modifying the fields to synchronize, how they map, and criteria on when to trigger the synchronization. 

Real time synchronization is achieved via rapid polling of both systems, or can be extended to include outbound notifications. This template uses Mule batching and watermarking capabilities to capture only recent changes, and to efficiently process large numbers of records.

![2bd0044b-52c8-494e-8ed7-b9ee0e0ff053-image.png](https://exchange2-file-upload-service-kprod.s3.us-east-1.amazonaws.com:443/2bd0044b-52c8-494e-8ed7-b9ee0e0ff053-image.png)
<!-- Header (end) -->

# License Agreement
This template is subject to the conditions of the <a href="https://s3.amazonaws.com/templates-examples/AnypointTemplateLicense.pdf">MuleSoft License Agreement</a>. Review the terms of the license before downloading and using this template. You can use this template for free with the Mule Enterprise Edition, CloudHub, or as a trial in Anypoint Studio. 
# Use Case
<!-- Use Case (start) -->
Synchronize users between Salesforce and Workday.
					
To keep Salesforce synchronized with Workday:

1. Ask Salesforce:
> *Which changes have there been since the last time I got in touch with you?*

2. For each of the updates fetched in the previous step 1, ask Workday:
> *Does the update received from A should be applied?*

3. If the answer for the previous question 2 is *Yes*, then *upsert* (create or update depending each particular case) Workday with the belonging change.

4. Repeat previous steps (1 to 3) the other way around (using Workday as source and Salesforce as the target).

 Repeat *ad infinitum*:

5. Ask Salesforce:
> *Which changes have there been since the question I've made in the step 1?*

And so on...
			  
The question for recent changes since a certain moment uses a [scheduler](https://docs.mulesoft.com/mule4-user-guide/v/4.1/scheduler-concept) with a [watermark](http://blogs.mulesoft.org/data-synchronizing-made-easy-with-mule-watermarks/) defined.
<!-- Use Case (end) -->

# Considerations
<!-- Default Considerations (start) -->
<!-- Default Considerations (end) -->
<!-- Considerations (start) -->
Salesforce Customization: Add a custom field ExtId (Text 255) to the Salesforce User. For more information, see [Salesforce - Create Custom Fields](https://help.salesforce.com/HTViewHelpDoc?id=adding_fields.htm).

**Note:** This template illustrates the synchronization use case between Salesforce and a Workday.
To run this template:

1. Users cannot be deleted in Salesforce: The only way to remove users is disabling or deactivating them, but this doesn't make the username available for a new user.

2. Each user needs to be associated to a Profile: Salesforce's profiles define the permissions a user has for manipulating data and other uses. Each Salesforce account has its own profiles. Check out the next section to define a map between Profile IDs from the source account to the ones in the target account and the other way around.

3. Workers cannot be deleted in Workday: They are only set as terminated employees.

4. Required Fields: The following fields are required for synchronization at Salesforce instance: Street, City, State/Province, Zip/Postal Code, Country, Phone.
<!-- Considerations (end) -->

## Salesforce Considerations

- Where can I check that the field configuration for my Salesforce instance is the right one? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US">Salesforce: Checking Field Accessibility for a Particular Field</a>.
- Can I modify the Field Access Settings? How? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US">Salesforce: Modifying Field Access Settings</a>.

### As a Data Source

If a user who configures the template for the source system does not have at least *read only* permissions for the fields that are fetched, then an *InvalidFieldFault* API fault displays.

```
java.lang.RuntimeException: [InvalidFieldFault [ApiQueryFault 
[ApiFault  exceptionCode='INVALID_FIELD'
exceptionMessage='Account.Phone, Account.Rating, Account.RecordTypeId, 
Account.ShippingCity
^
ERROR at Row:1:Column:486
No such column 'RecordTypeId' on entity 'Account'. If you are 
attempting to use a custom field, be sure to append the '__c' 
after the custom field name. Reference your WSDL or the describe 
call for the appropriate names.'
]
row='1'
column='486'
]
]
```

### As a Data Destination

There are no considerations with using Salesforce as a data destination.

## Workday Considerations

### As a Data Source

There are no considerations with using Workday as a data origin.
### As a Data Destination

There are no considerations with using Workday as a data destination.

# Run it!
Simple steps to run this template.
<!-- Run it (start) -->

<!-- Run it (end) -->

## Running On Premises
In this section we help you run this template on your computer.
<!-- Running on premise (start) -->

<!-- Running on premise (end) -->

### Where to Download Anypoint Studio and the Mule Runtime
If you are new to Mule, download this software:

+ [Download Anypoint Studio](https://www.mulesoft.com/platform/studio)
+ [Download Mule runtime](https://www.mulesoft.com/lp/dl/mule-esb-enterprise)

**Note:** Anypoint Studio requires JDK 8.
<!-- Where to download (start) -->

<!-- Where to download (end) -->

### Importing a Template into Studio
In Studio, click the Exchange X icon in the upper left of the taskbar, log in with your Anypoint Platform credentials, search for the template, and click Open.
<!-- Importing into Studio (start) -->

<!-- Importing into Studio (end) -->

### Running on Studio
After you import your template into Anypoint Studio, follow these steps to run it:

1. Locate the properties file `mule.dev.properties`, in src/main/resources.
2. Complete all the properties required per the examples in the "Properties to Configure" section.
3. Right click the template project folder.
4. Hover your mouse over `Run as`.
5. Click `Mule Application (configure)`.
6. Inside the dialog, select Environment and set the variable `mule.env` to the value `dev`.
7. Click `Run`.
<!-- Running on Studio (start) -->

<!-- Running on Studio (end) -->

### Running on Mule Standalone
Update the properties in one of the property files, for example in mule.prod.properties, and run your app with a corresponding environment variable. In this example, use `mule.env=prod`.

## Running on CloudHub
When creating your application in CloudHub, go to Runtime Manager > Manage Application > Properties to set the environment variables listed in "Properties to Configure" as well as the mule.env value.
<!-- Running on Cloudhub (start) -->

<!-- Running on Cloudhub (end) -->

### Deploying a Template in CloudHub
In Studio, right click your project name in Package Explorer and select Anypoint Platform > Deploy on CloudHub.
<!-- Deploying on Cloudhub (start) -->

<!-- Deploying on Cloudhub (end) -->

## Properties to Configure
To use this template, configure properties such as credentials, configurations, etc.) in the properties file or in CloudHub from Runtime Manager > Manage Application > Properties. The sections that follow list example values.
### Application Configuration
<!-- Application Configuration (start) -->
#### Properties to be used across all the environments

- scheduler.frequency `10000`
- scheduler.startDelay `500`

#### Development Environment Properties

#### Workday Connector configuration
- wday.username `user@company`
- wday.tenant `tenant`
- wday.password `secret`
- wday.host=https://impl-cc.workday.com/ccx/service/company/Human_Resources/v23.1
- wday.integration.user.id `72d1073ba8f51050e3c83a48d7a9ead6`
- wday.watermark.default.expression `2019-12-13T03:00:59Z`

- wday.country `USA`
- wday.state `USA-CA`
- wday.organization `SUPERVISORY_ORGANIZATION-1-435`
- wday.jobprofileId `39905`
- wday.postalCode `90001`
- wday.city `San Francisco`
- wday.location `San_Francisco_Site`
- wday.currency `USD`

#### Salesforce Connector
- sfdc.username `user@company.com`
- sfdc.password `secret`
- sfdc.securityToken `h0fcC2Y7dnuH7ELk9BhoW0xu`
- sfdc.integration.user.id `00520000003LtvGAAS`

	**Note:** To find the correct *sfdc.integration.user.id* value, see the example project [Salesforce Data Retrieval](https://www.mulesoft.com/exchange/org.mule.examples/salesforce-data-retrieval/).

- sfdc.watermark.default.expression `2019-12-13T03:00:59Z`
- sfdc.profileId `00e200000015oKFAAY`

- sfdc.localeSidKey `en_US`
- sfdc.languageLocaleKey `en_US`
- sfdc.timeZoneSidKey `America/New_York`
- sfdc.emailEncodingKey `ISO-8859-1`
<!-- Application Configuration (end) -->

# API Calls
<!-- API Calls (start) -->
Salesforce imposes limits on the number of API calls that can be made. Therefore calculating this amount may be an important factor to consider. The template calls to the API can be calculated using the formula:

- ***1 + X + X / 200*** -- Where ***X*** is the number of users to synchronize on each run. 
- Divide by ***200*** because by default, users are gathered in groups of 200 for each upsert API call in the commit step. Also consider that this calls are executed repeatedly every scheduler cycle.	

For instance if 10 records are fetched from origin instance, then 12 API calls are made (1 + 10 + 1).
<!-- API Calls (end) -->

# Customize It!
This brief guide provides a high level understanding of how this template is built and how you can change it according to your needs. As Mule applications are based on XML files, this page describes the XML files used with this template. More files are available such as test classes and Mule application files, but to keep it simple, we focus on these XML files:

* config.xml
* businessLogic.xml
* endpoints.xml
* errorHandling.xml
<!-- Customize it (start) -->
<!-- Customize it (end) -->

## config.xml
<!-- Default Config XML (start) -->
This file provides the configuration for connectors and configuration properties. Only change this file to make core changes to the connector processing logic. Otherwise, all parameters that can be modified should instead be in a properties file, which is the recommended place to make changes.<!-- Default Config XML (end) -->

<!-- Config XML (start) -->
<!-- Config XML (end) -->

## businessLogic.xml
<!-- Default Business Logic XML (start) -->
The business logic XML file creates or updates objects in the destination system for a represented use case. You can customize and extend the logic of this template in this XML file to more meet your needs.
<!-- Default Business Logic XML (end) -->
<!-- Business Logic XML (start) -->
<!-- Business Logic XML (end) -->

## endpoints.xml
<!-- Default Endpoints XML (start) -->
This file contains the endpoints for triggering the template and for retrieving the objects that meet the defined criteria in a query. You can execute a batch job process with the query results.
<!-- Default Endpoints XML (end) -->
<!-- Endpoints XML (start) -->
<!-- Endpoints XML (end) -->

## errorHandling.xml
<!-- Default Error Handling XML (start) -->
This file handles how your integration reacts depending on the different exceptions. This file provides error handling that is referenced by the main flow in the business logic.
<!-- Default Error Handling XML (end) -->
<!-- Error Handling XML (start) -->
<!-- Error Handling XML (end) -->
<!-- Extras (start) -->
<!-- Extras (end) -->
