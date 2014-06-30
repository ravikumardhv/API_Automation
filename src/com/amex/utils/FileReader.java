package com.amex.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileReader {
	private static Logger logger = LoggerFactory.getLogger(FileReader.class);
	
	public static ExcelReader getExcelReader(String excelFilePath) {
		
		if (excelFilePath == null) {
			return null;
		} else {
			logger.info("Loading the Excel file : "+excelFilePath);
			return ExcelReader.getInstance(excelFilePath);
		}
	}
}
