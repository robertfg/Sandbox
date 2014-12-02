package com.anz.commands;

import java.io.*;

public class DosCmd {
	
	public static void main(String args[]) {
		try {
			Process p = Runtime.getRuntime().exec("cmd /c dirxx ");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = reader.readLine();  
			while (line != null) {
				System.out.println(line);
				line = reader.readLine();
			}

			System.out.println("Done" + line.length());
		} catch (IOException e1) {
		} /*catch (InterruptedException e2) {
		}*/

		
	}
}