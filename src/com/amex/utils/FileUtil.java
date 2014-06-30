package com.amex.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {
    public static void createFile(String filepath, String fileName,
	    String content) throws IOException {
	try {

	    // if directory is created
	    if (createDirectory(filepath)) {
		File file = new File(filepath + fileName);
		// if file doesnt exists, then create it
		file.createNewFile();

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content);
		bw.close();

		System.out.println("Done");
		System.out.println("Directory/File is created!");

	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private static boolean createDirectory(String path) {
	File file = new File(path);
	if (!file.exists()) {
	    if (file.mkdirs()) {
		return true;
	    }
	} else {
	    return true;
	}
	return false;
    }
    
}
