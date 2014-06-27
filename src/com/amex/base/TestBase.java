package com.amex.base;

import java.io.FileInputStream; 
import java.io.IOException;
import java.util.Properties; 
import com.amex.utils.Constants; 
import com.amex.utils.ExcelReader; 
import com.amex.utils.FileReader;

public class TestBase { 
	protected ExcelReader reader; 
	protected Properties requestProp;
	protected Properties errorCodesProp;

	public void load() throws IOException{

		// Loading Request property file
		requestProp = new Properties();    
		requestProp.load(new FileInputStream(System.getProperty("user.dir")+Constants.REQUEST_PROP_PATH));    

		// Loading Error Codes property file
		errorCodesProp = new Properties();    
		errorCodesProp.load(new FileInputStream(System.getProperty("user.dir")+Constants.ERROR_CODES_PROP_PATH));
		
		// Loading the Test data excel sheet
		reader = FileReader.getExcelReader(System.getProperty("user.dir")+Constants.TESTDATA_PATH);
	}
}