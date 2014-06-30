package com.amex.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amex.base.TestBase;
import com.amex.utils.Constants;
import com.amex.utils.FileUtil;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class DriverScript extends TestBase {
    private String requestURL;
    private String headerKey;
    private String headerValue;
    private String requestName;
    private String requestParam;
    private String expectedResponseCode;
    private String requestBody;
    private String contentType;
    private String requestMethod;
    private String responseKeySet;
    private String errorName;
    private String tcid;
    private String fileoutpath;
    
    private int currentTestcase;
    private boolean testFailed = false;
    private static Logger logger = LoggerFactory.getLogger(DriverScript.class);


    /**
     * This method is the main method that starts the execution.
     * 
     * @throws IOException
     */
    public void run() throws IOException {

	clearResults();
	logger.info("Test Execution started.");

	for (currentTestcase = 2; currentTestcase <= reader.getRowCount(Constants.SHEET_NAME); currentTestcase++) {
	    HashMap<String, String> rowData = reader.getRowData(Constants.SHEET_NAME, currentTestcase);
	    String runMode = rowData.get(Constants.COLUMN_RUN_MODE).trim();
	    tcid = rowData.get(Constants.COLUMN_TCID).trim();

	    if (runMode.equalsIgnoreCase("YES")) {
		// Initialize all the values from test data sheet
		initialize(rowData);
		replaceURLParameters(requestParam);
		FileUtil.createFile(fileoutpath, tcid + "_Request.txt", requestBody);
		Response response = getResponse();

		if (response != null) {
		    FileUtil.createFile(fileoutpath, tcid + "_Response.txt", response.asString());
		    validateResponse(response);

		    // Updating the pass result only if there is no failure
		    if (!testFailed) {
			testPassed();
		    }
		}
	    } else {
		logger.info("Test Skipped : " + tcid);
		testSkipped();
	    }
	}
    }	


    /**
     * The method clears all the previous test results from the test data sheet.
     */
    private void clearResults() {
	logger.info("Clearing all the Test results from Excel sheet");
	for (currentTestcase = 2; currentTestcase <= reader.getRowCount(Constants.SHEET_NAME); currentTestcase++) {
	    reader.setCellData(Constants.SHEET_NAME, Constants.COLUMN_FAILURE_CAUSE, currentTestcase, "");
	}
	testFailed = false;
    }


    /**
     * Initialize all the required parameters for a web service call.
     * 
     * @param values
     */
    private void initialize(HashMap<String, String> values) {
	requestURL           = Constants.URL + values.get(Constants.COLUMN_API).trim();
	headerKey            = values.get(Constants.COLUMN_HEADER_KEY).trim();
	headerValue          = values.get(Constants.COLUMN_HEADER_VALUE).trim();
	requestName          = values.get(Constants.COLUMN_REQUEST_NAME).trim();
	requestParam         = values.get(Constants.COLUMN_REQUEST_PARAM).trim();
	expectedResponseCode = values.get(Constants.COLUMN_RESPONSE_CODE).trim();
	requestMethod        = values.get(Constants.COLUMN_REQUEST_METHOD).trim();
	contentType          = Constants.CONTENT_TYPE_JSON;
	requestBody          = generateValidRequestBody(requestName, requestParam);
	errorName            = values.get(Constants.COLUMN_ERROR_NAME).trim();
	responseKeySet       = values.get(Constants.COLUMN_RESPONSE_KEY).trim();
	fileoutpath	     = getFileOutPath(currentTimeStamp);
    }

	
    /**
     * Call the web service and get the response.
     * 
     * @return
     */
    private Response getResponse() {
	Response response = null;
	RestAssured.useRelaxedHTTPSValidation();
	try {

	    if (requestMethod.equalsIgnoreCase("POST")) {
		// Call POST service
		response = RestAssured.given().headers(headerKey, headerValue)
			.body(requestBody).contentType(contentType)
			.post(requestURL).andReturn();
	    } else if (requestMethod.equalsIgnoreCase("GET")) {
		// Call GET service
		response = RestAssured.given().headers(headerKey, headerValue)
			.contentType(contentType).get(requestURL).andReturn();
	    }
	} catch (Exception e) {
	    testFailed(e.getLocalizedMessage());
	    logger.info(e.getMessage(), e);

	}
	return response;
    }

    
    /**
     * Check the HTTP response status code and validate the reponse. If 200 OK,
     * assert the response with the values from DB If 400, 499, assert the error
     * details in the response body with values in the ErrorCodes.properties
     * file.
     * 
     * @param response
     */
    private void validateResponse(Response response) {

	int actualResponseCode = response.getStatusCode();
	int expResponseCode = (int) Float.parseFloat(expectedResponseCode);

	if (actualResponseCode == expResponseCode) {
	    if (actualResponseCode == 200) {
		validateValidResponse(response);
	    } else if (actualResponseCode == 400 || actualResponseCode == 499) {
		validateErrorResponse(response);
	    } else {
		logger.info("The response code does not fall in 200/400/499, " + actualResponseCode);
	    }
	} else {
	    // TODO Fail the test and do the logging
	    testFailed("Exp response: " + expResponseCode + " Act response: " + actualResponseCode);
	}
    }

	
    /**
     * When HTTP Response Code 200, use this method to validate the response
     * obtained against DB values.
     * 
     * @param response
     */
    private void validateValidResponse(Response response) {

	// Fetching the JSON response
	JsonPath json = response.getBody().jsonPath();

	// Read the expected response values to be validated
	String[] responseKeys = responseKeySet.split(",");

	// TODO Validate against DB
	for (int i = 0; i < responseKeys.length; i++) {
	    String key = responseKeys[i].trim();
	    String actualValue = json.getString(key);
	    System.out.println(key + "-----" + actualValue);
	}
	System.out.println(response.asString());
    }

	
    /**
     * When HTTP Response Code 400/499, use this method to validate the error
     * response obtained against the values in properties file.
     * 
     * @param response
     */
    private void validateErrorResponse(Response response) {
	// Fetching the JSON response
	JsonPath json = response.getBody().jsonPath();

	// Get the expected error details from Property files
	String[] expValues = errorCodesProp.getProperty(errorName).split(",");

	// Read the expected response values to be validated
	String[] responseKeys = responseKeySet.split(",");

	// Validating the error response details
	for (int i = 0; i < expValues.length; i++) {
	    String ActualValue = json.getString((responseKeys[i].trim()));
	    if (!expValues[i].equalsIgnoreCase(ActualValue)) {
		// TO-DO Logging and updating excel sheet
		System.out.println("Test Failed : Expected : " + expValues[i] + " Actual : " + ActualValue);
	    }
	}
	System.out.println(response.asString());
    }

	
    /**
     * Get the json request defined in the Request.properties file
     * 
     * @param requestName
     * @return
     */
    private String getRequestSchema(String requestName) {
	return requestProp.getProperty(requestName);
    }

	
    /**
     * Generates a valid json request from the json obatained from the
     * properties file. Uses the request parameters provided in the Excel sheet
     * to form a valid Json Request body.
     * 
     * @param requestName
     * @param requestParam
     * @return
     */
    private String generateValidRequestBody(String requestName,
	    String requestParam) {
	if (requestMethod.equalsIgnoreCase("POST")) {
	    String request = getRequestSchema(requestName);
	    HashMap<String, String> paramsMap = generateRequestParamsMap(requestParam);
	    String finalRequest = replaceRequestParameters(request, paramsMap);
	    return finalRequest;
	}
	return null;
    }

	
    /**
     * Replace the placeholder parameters in the URL with valid parameter values
     * obtained from excel sheet.
     * 
     * @param requestParam
     */
    private void replaceURLParameters(String requestParam) {
	if (requestMethod.equalsIgnoreCase("GET")) {
	    HashMap<String, String> paramsMap = generateRequestParamsMap(requestParam);
	    requestURL = replaceRequestParameters(requestURL, paramsMap);
	}
    }


    /**
     * Generates a map of key value parameters from a string in the format:
     * "key:value,key1:value1..." -->
     * 
     * @param params
     * @return
     */
    private HashMap<String, String> generateRequestParamsMap(String params) {
	HashMap<String, String> paramsMap = new HashMap<String, String>();
	String[] paramSet = params.split(",");
	for (int i = 0; i < paramSet.length; i++) {
	    String[] param = paramSet[i].split(":");
	    if (param[1].equalsIgnoreCase("null")) {
		param[1] = "";
	    }
	    paramsMap.put(param[0].trim(), param[1].trim());
	}
	return paramsMap;
    }
	
    
    /**
     * Replace the placeholder request parameters in the json request body with
     * the values in a HashMap.
     * 
     * @param requestBody
     * @param params
     * @return
     */
    private String replaceRequestParameters(String requestBody, HashMap<String, String> params) {
	System.out.println(requestBody);
	for (Map.Entry<String, String> entry : params.entrySet()) {
	    requestBody = requestBody.replace(entry.getKey(), entry.getValue());
	}
	return requestBody;
    }

    
    /**
     * Get the output filepath with timstamp as the last folder
     * in the folder structure. 
     * 
     * @return
     */
    private String getFileOutPath(String timestamp) {
	return System.getProperty("user.dir") + Constants.TEST_OUTPUT_PATH + "\\" + timestamp + "\\ ";
    }

    /**
     * Set the Test_Result column in excel sheet as Skipped.
     * 
     */
    private void testSkipped() {
	reader.setCellData(Constants.SHEET_NAME, Constants.COLUMN_TEST_RESULT, currentTestcase, Constants.TEST_SKIP);
    }

    
    /**
     * Set the Test_Result column in excel sheet as Passed.
     * 
     */
    private void testPassed() {
	reader.setCellData(Constants.SHEET_NAME, Constants.COLUMN_TEST_RESULT, currentTestcase, Constants.TEST_PASSED);
    }

	
    /**
     * Set the Test_Result column in excel sheet as Failed. And also sets the
     * failure cause in Failure_Cause column.
     * 
     */
    private void testFailed(String failureCause) {
	logger.info("Test Failed : " + failureCause);
	reader.setCellData(Constants.SHEET_NAME, Constants.COLUMN_TEST_RESULT, currentTestcase, Constants.TEST_FAILED);
	reader.setCellData(Constants.SHEET_NAME, Constants.COLUMN_FAILURE_CAUSE, currentTestcase, failureCause);
	testFailed = true;
    }
}