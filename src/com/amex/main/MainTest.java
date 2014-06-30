package com.amex.main;

import java.io.IOException;

public class MainTest {	

	public static void main(String[] args)  {		
		DriverScript driver = new DriverScript();
		try {
			driver.load();	
			driver.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
}