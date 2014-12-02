package file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class Tail {
   
	
	public static void main(String[] args) throws IOException {
		
		
		String tailFile ="c:\\GovLog_GOVsrv.exe.txt"; 
		
        Reader fileReader = new FileReader(tailFile);
        BufferedReader input = new BufferedReader(fileReader);
        String line = null;
        while (true) {
            if ((line = input.readLine()) != null) {
                System.out.println(line);
                continue;
            }
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException x) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        input.close();
    }
}
