package com.amex.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.amex.utils.Constants;
import com.amex.utils.ExcelReader;
import com.amex.utils.FileReader;

public class TestBase { 
	protected ExcelReader reader; 
	protected Properties requestProp;
	protected Properties errorCodesProp;
	protected String	currentTimeStamp;

	public void load() throws IOException{

		// Loading Request property file
		requestProp = loadPropertyFile(System.getProperty("user.dir")+Constants.REQUEST_PROP_PATH);    

		// Loading Error Codes property file
		errorCodesProp = loadPropertyFile(System.getProperty("user.dir")+Constants.ERROR_CODES_PROP_PATH);
		
		// Loading the Test data excel sheet
		reader = FileReader.getExcelReader(System.getProperty("user.dir")+Constants.TESTDATA_PATH);
		
		currentTimeStamp	 = getCurrentTimeStamp();
	}
	
	private String getCurrentTimeStamp() {
	    SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy_HH_mm_ss");
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    return strDate;
	}
	
	private Properties loadPropertyFile(String path) throws IOException{
		Properties prop = new Properties();    
		prop.load(new FileInputStream(path));
		return prop;
	}
}