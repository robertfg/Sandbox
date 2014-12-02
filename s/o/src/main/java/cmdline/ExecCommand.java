package cmdline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExecCommand {

	

	public static int executeRuntimeCommand(String cmdLine){
		int retValue = 0;
	
		try {
			Process p = Runtime.getRuntime().exec(cmdLine);
			p.waitFor();
			
			retValue = p.exitValue();
			p.destroy();
	
		} catch (IOException e1) {
				e1.printStackTrace();
		} catch (InterruptedException e2) {
				e2.printStackTrace();
		}
		return retValue; 
	
	}
	
	
	public int executeSqlCommand(String cmdLine){
		try {
			
			Process p = Runtime.getRuntime().exec(cmdLine);
			
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String line = reader.readLine();
			
			while (line != null) {
				System.out.println(line);
				line = reader.readLine();
			}
			
			return Integer.valueOf(line);
	
		} catch (IOException e1) {
				e1.printStackTrace();
		} catch (InterruptedException e2) {
				e2.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace(); 
		}

		return 0;
		
	}
	   
  }

