package com.amex.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Prokarma
 *
 */
public class DbReader {

	private Connection connection = null;
	private ResultSet resultSet = null;
	private PreparedStatement preparedStatement = null;
	private ResultSetMetaData resultSetMetaData = null;
	
	private static Logger logger = LoggerFactory.getLogger(DbReader.class);

	/**
	 * This method is used get the database connection 
	 * @param driver
	 * @param url
	 * @param userName
	 * @param password
	 * @param tableName
	 * 
	 */
	public DbReader(String driver, String url, String userName,
			String password, String tableName) {
		try {
			logger.info(" Trying to connect database");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, userName, password);
			connection.setAutoCommit(false);
			preparedStatement = connection.prepareStatement(tableName);
			resultSet = preparedStatement.executeQuery();
			resultSetMetaData = resultSet.getMetaData();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private static volatile DbReader instance = null;

	
	/**
	 * This method create the instance of DbReader
	 * @param driver
	 * @param url
	 * @param userName
	 * @param password
	 * @param tableName
	 * @return DbReader
	 */
	public static DbReader getInstance(String driver, String url,
			String userName, String password, String tableName) {
		logger.info(" Trying to create instance for DB");
		if (instance == null) {
			synchronized (DbReader.class) {
				if (instance == null) {
					instance = new DbReader(driver, url, userName, password,
							tableName);
				}
			}
		}
		return instance;
	}

	/**
	 * This method read the data from database 
	 * @return List of map objects
	 */
	public List<Map<String, String>> read() {
		logger.info(" inside read() method");
		List<Map<String, String>> maplist = null;
		try {

			int colCount = resultSetMetaData.getColumnCount();
			Map<String, String> map = new HashMap<String, String>();
			maplist = new ArrayList<Map<String, String>>();
			while (resultSet.next()) {
				for (int i = 1; i <= colCount; i++) {
					map.put(resultSetMetaData.getColumnName(i).trim(), resultSet.getString(i)
							.trim());
				}
				maplist.add(map);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			try {
				resultSet.close();
				connection.close();
			} catch (SQLException e) {
				if(e.getMessage() != null && e.getMessage().isEmpty()){
					logger.error(e.getMessage());
				}
				else{
					logger.error(" Problem occured while getting data from db");
				}				
			}

		}
		return maplist;
	}
}
