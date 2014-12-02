package shared;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
 
   
public class CopyToShared {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String sharedDrive = "\\\\svrsg001rps01.asia.corp.anz.com\\anaboc$\\My Documents\\Downloads\\temp\\VSAL0_A.CM_TR_OILO_CRD.20131008.COM.20131009070600.txt.gz_V12.gz";
		       sharedDrive = "\\\\10.52.16.1\\ApCube-1\\VSAL0_A.CM_TR_OILO_CRD.20131008.COM.20131009070600.txt.gz_V12.gz";
		
  	    String originalLocation = "c:\\temp\\VSAL0_A.CM_TR_OILO_CRD.20131008.COM.20131009070600.txt.gz_V12.gz";
  	    	try {
				Files.copy( Paths.get(originalLocation), Paths.get(sharedDrive) );
	        } catch (IOException e) { 
				e.printStackTrace();
			} 
 	
	}
	
  
}



