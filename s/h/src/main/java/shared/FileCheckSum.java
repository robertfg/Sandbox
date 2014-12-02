package shared;


import java.io.FileInputStream;
import java.security.MessageDigest;

public class FileCheckSum {

	public static String getCheckSum(String filePath) throws Exception {

 
		MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
		
		FileInputStream fileInput = new FileInputStream(filePath);
		byte[] dataBytes = new byte[1024];

		int bytesRead = 0;

		while ((bytesRead = fileInput.read(dataBytes)) != -1) {
			messageDigest.update(dataBytes, 0, bytesRead);
		}
		

		byte[] digestBytes = messageDigest.digest();

		StringBuffer sb = new StringBuffer("");
		
		for (int i = 0; i < digestBytes.length; i++) {
			sb.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		System.out.println("Checksum for the File: " + sb.toString());
		
		fileInput.close();
		return sb.toString();

	}
}