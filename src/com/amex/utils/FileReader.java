package com.amex.utils;

public class FileReader {

public static ExcelReader getExcelReader(String excelFilePath) {

    if (excelFilePath == null) {
        return null;
    } else {
        return ExcelReader.getInstance(excelFilePath);
    }
}
}