package com.amex.testsamples;

import java.util.HashMap;

import com.amex.utils.Constants;
import com.amex.utils.ExcelReader;
import com.amex.utils.FileReader;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public class TestSample {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ExcelReader reader = FileReader.getExcelReader(System.getProperty("user.dir")+Constants.TESTDATA_PATH);
		for (int i = 2; i <= reader.getRowCount(Constants.SHEET_NAME); i++) {
			HashMap<String, String> values = reader.getRowData(Constants.SHEET_NAME, i);
		String requestName          = values.get(Constants.COLUMN_REQUEST_NAME).trim();
		System.out.println("REQUEST NAME : "+requestName);
		String paramRequest1         = values.get(Constants.COLUMN_REQUEST_PARAM).trim();
		System.out.println("REQUEST PARAMETERS1 : "+paramRequest1);
		System.out.println("***********************************");
		Response response = RestAssured.given().headers("sdfsd", "dfgdfsd")
				.contentType("application/json").get("http://192.168.1.16:8080/EmployeeData/disputes-web/disputes/v1/response/reasons/123")
				.andReturn();
		
		response.prettyPrint();

	}

}
}
