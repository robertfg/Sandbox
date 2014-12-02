package com.anz.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RunBatchBCP {
	
	
	public static void runCommand(){
		
		String command = "dir /w";
		
		Runtime runTime = Runtime.getRuntime();
		Process p = null;
		try {
			p = runTime.exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	public static void main(String args[]) throws IOException {
		
	
	}
	
	
	public void run() throws IOException{
	int batchsize = 1000;
		
		String bcpServerAlies = "serverAlies";
		String bcpUserName = "username";
		String bcpUserPassword = "psw";
		String bcpErrorFile = "c:\\dirA\\";
		String lineSeprator = "\\n";

		String command = "........\\Microsoft SQL Server\\100\\Tools\\Binn\\bcp";
		String dataFile, errorFile;
		
		for (int i = 0; i < 140; i++) {
		
			dataFile = "c:\\dir\\" + i + ".out";
			errorFile = "c:\\\\bcpError\\" + i + ".out";
			String[] cmdArr = { command, "tableName", "in", dataFile, "-c",
					"-b", batchsize + "", "-t", "|", "-r", lineSeprator, "-S",
					bcpServerAlies, "-U", bcpUserName, "-P", bcpUserPassword,
					"-e", errorFile };

			System.out.println("cammand array is:" + cmdArr.toString());
			System.out.println("BCP EXECUTION STARTED:");
			
			Runtime runTime = Runtime.getRuntime();
			Process p = null;
			try {
				p = runTime.exec(cmdArr);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			InputStream inputStream = p.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			
			String line;
		
			while ((line = bufferedReader.readLine()) != null) {
				System.out.println(line);
				System.out.println(line);
			}

			inputStream = p.getErrorStream();
			inputStreamReader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(inputStreamReader);
			line = "";
			
			while ((line = bufferedReader.readLine()) != null) {
				System.out.println(line);
				System.out.println(line);
			}
			System.out.println("bcp executed succefully for file" + dataFile);
			
			
		}// end of for
	}
}