package com.amex.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.amex.base.TestBase;
import com.amex.utils.Constants;
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
	private String request; 
	private String contentType; 
	private String requestMethod; 
	private String responseKeySet; 
	private String errorName;
	private int currentTestcase;
	private boolean testFailed=false;

	public static void main(String[] args) throws IOException {
		DriverScript driver = new DriverScript();
		driver.load();
		driver.invoke();
	}

	public void invoke() {
		clearResults();
		for (currentTestcase = 2; currentTestcase <= reader.getRowCount(Constants.SHEET_NAME); currentTestcase++) {
			HashMap<String, String> values = reader.getRowData(Constants.SHEET_NAME, currentTestcase);
			String runMode = values.get(Constants.COLUMN_RUN_MODE).trim();
			
			if(runMode.equalsIgnoreCase("YES")){
				initialize(values);
				replaceURLParameters(requestParam);
				Response response = getResponse();
				if(response!=null){
					// To-DO write request and response to a file by creating a folder+timestamp,file+TCID.         
					validateResponse(response);
					
					// Updating the pass result only if there is no failure
					if(!testFailed){
						testPassed();
					}
				}
				
			}else{
				// Update log file - Test skipped
				testSkipped();				
			}			
		}
	}	
	


	private void clearResults() {
		for (currentTestcase = 2; currentTestcase <= reader.getRowCount(Constants.SHEET_NAME); currentTestcase++) {
			reader.setCellData(Constants.SHEET_NAME, Constants.COLUMN_FAILURE_CAUSE, currentTestcase, "");
		}
		testFailed=false;
		
	}

	private void initialize(HashMap<String, String> values) {       
		requestURL           = Constants.URL+ values.get(Constants.COLUMN_API).trim();          
		headerKey            = values.get(Constants.COLUMN_HEADER_KEY).trim();
		headerValue          = values.get(Constants.COLUMN_HEADER_VALUE).trim();
		requestName          = values.get(Constants.COLUMN_REQUEST_NAME).trim();
		requestParam         = values.get(Constants.COLUMN_REQUEST_PARAM).trim();
		expectedResponseCode = values.get(Constants.COLUMN_RESPONSE_CODE).trim();
		requestMethod        = values.get(Constants.COLUMN_REQUEST_METHOD).trim();          
		contentType          = Constants.CONTENT_TYPE_JSON;
		request              = generateValidRequest(requestName, requestParam);
		errorName            = values.get(Constants.COLUMN_ERROR_NAME).trim();
		responseKeySet       = values.get(Constants.COLUMN_RESPONSE_KEY).trim();
	}
	
	private void testSkipped(){
		reader.setCellData(Constants.SHEET_NAME, Constants.COLUMN_TEST_RESULT, currentTestcase, Constants.TEST_SKIP);
	}
	
	private void testPassed(){
		reader.setCellData(Constants.SHEET_NAME, Constants.COLUMN_TEST_RESULT, currentTestcase, Constants.TEST_PASSED);
	}
	
	private void testFailed(String failureCause){
		reader.setCellData(Constants.SHEET_NAME, Constants.COLUMN_TEST_RESULT, currentTestcase, Constants.TEST_FAILED);
		reader.setCellData(Constants.SHEET_NAME, Constants.COLUMN_FAILURE_CAUSE, currentTestcase, failureCause);
		testFailed=true;
	}

	private Response getResponse() {
		Response response = null;
		RestAssured.useRelaxedHTTPSValidation();
		try{
		if(requestMethod.equalsIgnoreCase("POST")){
			response = RestAssured.given().headers(headerKey, headerValue)
					.body(request).contentType(contentType).post(requestURL)
					.andReturn();
		}else if (requestMethod.equalsIgnoreCase("GET")) {
			response = RestAssured.given().headers(headerKey, headerValue)
					.contentType(contentType).get(requestURL)
					.andReturn();
		}
		}catch(Exception e){
			testFailed("Host is not reachable");
						
		}
		return response;

	}

	private void validateResponse(Response response) {
		int actualResponseCode=response.getStatusCode();      
		int expResponseCode=(int) Float.parseFloat(expectedResponseCode);     
		if(actualResponseCode==expResponseCode){
			if(actualResponseCode==200){
				validateValidResponse(response);
			}else if(actualResponseCode==400||actualResponseCode==499){
				validateErrorResponse(response);
			}else{
				// To-Do
			}
		}else{
			// TO-DO : Fail the test and do the logging
			testFailed("Exp response: "
					+ expResponseCode + " Act response: "
					+ actualResponseCode);
		}

	}

	private void validateValidResponse(Response response) {
		// TODO Auto-generated method stub
		System.out.println("Valid response");

		// Fetching the JSON response
		JsonPath json = response.getBody().jsonPath();

		// Read the expected response values to be validated
		String[] responseKeys = responseKeySet.split(",");

		for (int i = 0; i < responseKeys.length; i++) {
			String key = responseKeys[i].trim();
			String actualValue = json.getString(key);
			System.out.println(key + "-----" + actualValue);
		}
		System.out.println(response.asString());
	}



	private void validateErrorResponse(Response response) {     
		// Fetching the JSON response
		JsonPath json = response.getBody().jsonPath();

		// Get the expected error details from Property files
		String[] expValues = errorCodesProp.getProperty(errorName).split(",");

		// Read the expected response values to be validated
		String[] responseKeys = responseKeySet.split(",");

		// Validating the error response details 
		for(int i=0;i<expValues.length;i++){            
			String ActualValue = json.getString((responseKeys[i].trim()));
			if(!expValues[i].equalsIgnoreCase(ActualValue)){
				// TO-DO Logging and updating excel sheet
				System.out.println("Test Failed : Expected : "+expValues[i]+" Actual : "+ActualValue);
			}
		}
		System.out.println(response.asString());
	}   

	private String getRequestSchema(String requestName) {
		return requestProp.getProperty(requestName);
	}

	private String generateValidRequest(String requestName, String requestParam) {

		if(requestMethod.equalsIgnoreCase("POST")){
			String request = getRequestSchema(requestName);
			HashMap<String, String> paramsMap = generateRequestParamsMap(requestParam);
			String finalRequest = replaceRequestParameters(request, paramsMap);
			return finalRequest;
		}
		return null;
	}

	private void replaceURLParameters(String requestParam){
		if (requestMethod.equalsIgnoreCase("GET")){
			HashMap<String, String> paramsMap = generateRequestParamsMap(requestParam);	
			requestURL =replaceRequestParameters(requestURL, paramsMap);
		}
		
	}

	private HashMap<String, String> generateRequestParamsMap(String params) {
		HashMap<String, String> paramsMap = new HashMap<String, String>();
		String[] paramSet = params.split(",");
		for (int i = 0; i < paramSet.length; i++) {
			String[] param = paramSet[i].split(":");
			if(param[1].equalsIgnoreCase("null")){
				param[1]="";
			}
			paramsMap.put(param[0].trim(), param[1].trim());
		}
		return paramsMap;
	}

	private String replaceRequestParameters(String request, HashMap<String, String> params) {
		System.out.println(request);
		for (Map.Entry<String, String> entry : params.entrySet()) {
			request = request.replace(entry.getKey(), entry.getValue());
		}
		return request;
	}
}