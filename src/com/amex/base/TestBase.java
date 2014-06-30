
package com.amex.base;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amex.utils.Constants;
import com.amex.utils.ExcelReader;
import com.amex.utils.FileReader;

public class TestBase { 
	protected ExcelReader reader; 
	protected Properties requestProp;
	protected Properties errorCodesProp;
	protected String	currentTimeStamp;
	private static Logger logger = LoggerFactory.getLogger(TestBase.class);

	public void load() throws IOException{
		logger.info("Loading all the required files");

		// Loading Request property file		
		requestProp = loadPropertyFile(System.getProperty("user.dir")+Constants.REQUEST_PROP_PATH);    

		// Loading Error Codes property file		
		errorCodesProp = loadPropertyFile(System.getProperty("user.dir")+Constants.ERROR_CODES_PROP_PATH);

		// Loading the Test data excel sheet		
		reader = FileReader.getExcelReader(System.getProperty("user.dir")+Constants.TESTDATA_PATH);

		currentTimeStamp = getCurrentTimeStamp();
	}

	private String getCurrentTimeStamp() {
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy_HH_mm_ss");
		Date now = new Date();
		String strDate = sdfDate.format(now);
		return strDate;
	}

	private Properties loadPropertyFile(String path) {
		logger.info("Loading the property file : "+path);
		Properties prop = new Properties();  
		FileInputStream fis=null;

		try {
			fis = new FileInputStream(path);
			prop.load(fis);
		} catch (FileNotFoundException e)  {
			logger.info(e.getMessage(), e);
		}catch (IOException e) {
			logger.info(e.getMessage(), e);
		}finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return prop;
	}
}
