package com.anz.rer.etl.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;

public class FileUtils extends  org.apache.commons.io.FileUtils{

	private final static Logger logger = Logger.getLogger(FileUtils.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("8/02/13".length() );
		String date = "9/02/13";
		System.out.println("0"+date.substring(0,1) + "-"+ date.substring(2,4) + "-20" + date.substring(5));
		
		System.out.println( "20" + date.substring(5) + date.substring(2,4) + "0"+date.substring(0,1)   );
		
		
						 
		//System.out.println( "20" + date.substring(6) + date.substring(3,5) + date.substring(0,2) );
		
		System.exit(1);
		
		//File uvr = new File( "C:\\temp\\ANZ\\data_unified\\SFDG0#FXO_DELGAMMA#NON-VAR#2718486926585644#20130215#2718486926585644.APX.gz.tmp_UVR.gz.tmp" );
	   //System.out.println( uvr.getName() );
		
		
		
		//System.out.println( FileUtils.renameFile( uvr, "SFDG0#FXO_DELGAMMA#NON-VAR#2718486926585644#20130215#2718486926585644.APX.gz.tmp_UVR.gz.tmp") );	
		
		 /*
		    Collection<File> found = FileUtils.listFiles(new File("C:\\temp\\ANZ\\data_unified\\"),
		        TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		    */
		    
		    Collection<File> found = FileUtils.getFiles("C:\\temp\\ANZ\\data_unified\\", "*SFDG0#FXO_DELGAMMA#.*.#20130216#.*.tmp");
		    		
		    for (File f : found) {
		      System.out.println("Found file: " + f);
		    }
		   //SFDG0#FXO_DELGAMMA#NON-VAR#2718486926585644#20130215#2718486926585644.APX.gz.tmp_UVR.gz.tmp 
		    //"*SFDG0#FXO_DELGAMMA#.*.#20130216#.*.tmp"
		    
		   String remFile = "TAPF0#APRA_STRESS_FX_ALG#NON-VAR#3257790884099834#20130222#3257790884099834.remove.1361974804068.EMT";
		   //"*SFDG0#FXO_DELGAMMA#NON-VAR#.*.#20130216#.*._UVR.*.TMP"
		   File w =  FileUtils.getFile("C:\\temp", "*TAPF0#APRA_STRESS_FX_ALG#NON-VAR#.*.#20130222#.*remove.EMT");
		   System.out.println(w.getName());
		   
	}
	
   
	@SuppressWarnings("static-access")
	public	static boolean renameFile(File file, String newFileName){
		return file.renameTo( new File( file.getParent()+ file.separator + newFileName )  );
	} 
	
	public  static boolean moveFile(File file, String destDir){
		return file.renameTo( new File( destDir  )  );
	}	
	
	public static Collection<File> getFiles(String dir, String filter){
		
		return FileUtils.listFiles(
	    		new File(dir), 
	    		  new RegexFileFilter("." + filter),  DirectoryFileFilter.DIRECTORY
	    		);
		
	}
	
	
	public static File getFile(String dir, String fileName){
		//".*VSAL0_A.*.*.gz"
		
		if(new File(dir).exists() ){
			Collection<File> files = FileUtils.listFiles(
		    		new File(dir), 
		    		  new RegexFileFilter("." + fileName, IOCase.INSENSITIVE ),  DirectoryFileFilter.DIRECTORY	);
			
			if(files!=null && files.size()>0){
				return files.iterator().next();
			}
		}
		return null;
		
	}
	
	
 	public  static void archiveFile(String srcFile, String destDir){
		
		   File afile =new File(srcFile);
		   File directory = new File(destDir);
		   String fileName = afile.getName().toString();
         
		    int dot = fileName.indexOf(".");
		    String ext = fileName.substring(  dot + 1);
			  fileName = fileName.substring( 0, dot)  + "#" + System.nanoTime() + "." + ext;
		
			if(afile.exists()){
				if(! directory.exists()){
					if (directory.mkdir()) {
				    	logger.debug("creating directory Success using alternative 1");
				    } else {
				      if (directory.mkdirs()) {
				    	  logger.debug("creating directory Success using alternative 2");
				      } else {
				         logger.debug("Failed using both alternative 1 and alternative 2");
				      }
				    }
				}
				
				if( ( new File( destDir + File.separator  + afile.getName() )).exists()  ) {
					if(afile.renameTo(new File( destDir + File.separator  + fileName))){
						logger.info("File was moved successful!:" + destDir + File.separator  + fileName);
					} else{
					    
						logger.info("File failed to move! -" + srcFile + "- to:"+ destDir  + File.separator + afile.getName());
						try {
							Thread.sleep(30000);
							logger.info("Retrying to move the file..:0");
							archiveFile(srcFile, destDir,0);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} else {
					if(afile.renameTo(new File( destDir + File.separator  + afile.getName()))){
						logger.info("File was moved successful! " +  destDir + File.separator  + afile.getName());
					} else{
						logger.info("File failed to move! -" + srcFile + "- to:"+ destDir  + File.separator + afile.getName());
						try {
							Thread.sleep(30000);
							logger.info("Retrying to move the file..:1");
							archiveFile(srcFile, destDir,0);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}		
			}else {
				logger.info("File was failed to move or File dont exist:" + srcFile);
			}
			
			    afile = null;
			directory = null;

	} 

 	public  static void archiveFile(String srcFile, String destDir, int retry){
		
		   File afile =new File(srcFile);
		   File directory = new File(destDir);
		   String fileName = afile.getName().toString();
      
		    int dot = fileName.indexOf(".");
		    String ext = fileName.substring(  dot + 1);
			  fileName = fileName.substring( 0, dot)  + "#" + System.nanoTime() + "." + ext;
		
			if(afile.exists()){
				if(! directory.exists()){
					if (directory.mkdir()) {
				    	logger.debug("creating directory Success using alternative 1");
				    } else {
				      if (directory.mkdirs()) {
				    	  logger.debug("creating directory Success using alternative 2");
				      } else {
				         logger.debug("Failed using both alternative 1 and alternative 2");
				      }
				    }
				}
				
				if( ( new File( destDir + File.separator  + afile.getName() )).exists()  ) {
					if(afile.renameTo(new File( destDir + File.separator  + fileName))){
						logger.info("File was moved successful!:" + destDir + File.separator  + fileName);
					} else{
					    
						logger.info("File failed to move! -" + srcFile + "- to:"+ destDir  + File.separator + afile.getName());
						
						try {
							if(retry<=3){
								Thread.sleep(30000);
								logger.info("Retrying to move the file...:" + retry);
								archiveFile(srcFile, destDir, retry++);
							} else {
								logger.info("Maximum retry reach:" + retry);		
							}
							
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					
					}
				} else {
					if(afile.renameTo(new File( destDir + File.separator  + afile.getName()))){
						logger.info("File was moved successful! " +  destDir + File.separator  + afile.getName());
					} else{
						logger.info("File failed to move! -" + srcFile + "- to:"+ destDir  + File.separator + afile.getName());
						try {
							if(retry<=3){
								Thread.sleep(30000);
								logger.info("Retrying to move the file...:" + retry);
								archiveFile(srcFile, destDir, retry++);
							} else {
								logger.info("Maximum retry reach:" + retry);		
							}
							
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}		
					
			}else {
				logger.info("File was failed to move or File dont exist:" + srcFile);
			}
			
			    afile = null;
			directory = null;

	} 
	public static String[] readFileSpecificLine(String csvFileName, String delimeter, int lineNumber) throws IOException {
		   
		String line = (String) readLines( new File(csvFileName) ).get(lineNumber);
		return line.split(delimeter,-1);
		
	}
	
	public static String unzip(File srcFile, String destFileName) throws IOException {
		InputStream in = new FileInputStream(srcFile);
	    String destFile = srcFile.getParent() + File.separator + destFileName;
		OutputStream out = new FileOutputStream( new File( destFile ) );
	    
	    try {
	      in = new GZIPInputStream(in);
	      byte[] buffer = new byte[65536];
	      int noRead;
	      while ((noRead = in.read(buffer)) != -1) {
	      
	    	  out.write(buffer, 0, noRead);
	      
	      }
	    } finally {
	      try { 
	    	  out.close(); 
	    	  in.close();  
	      } catch (Exception e) {}
	    }
	    return destFile;
	    
	  }
	
	 public static void createDirectory(String dir) {

		    File directory = new File(dir);
		   
		    if(!directory.exists()) {
		       if (directory.mkdir()) {
			    	logger.info("creating directory Success using alternative 1");
			    } else {
			      //Alternative 2: If ancestors doesn\'t exist, they will be created.
			      if (directory.mkdirs()) {
			    	  logger.info("creating directory Success using alternative 2");
			      } else {
			         logger.info("Failed using both alternative 1 and alternative 2");
			      }
			    }
		   }
	 }
	 
	 public static String calculateChecksum(Serializable obj) {
		    if (obj == null) {
		        throw new IllegalArgumentException("The object cannot be null");
		    }
		    MessageDigest digest = null;
		    try {
		        digest = MessageDigest.getInstance("MD5");
		    } catch (java.security.NoSuchAlgorithmException nsae) {
		        throw new IllegalStateException("Algorithm MD5 is not present", nsae);
		    }
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ObjectOutput out = null;
		    byte[] objBytes = null;
		    try {
		        out = new ObjectOutputStream(bos);
		        out.writeObject(obj);
		        objBytes = bos.toByteArray();
		        out.close();
		    } catch (IOException e) {
		        throw new IllegalStateException(
		                "There was a problem trying to get the byte stream of this object: " + obj.toString());
		    }
		    digest.update(objBytes);
		    byte[] hash = digest.digest();
		    StringBuilder hexString = new StringBuilder();
		    for (int i = 0; i < hash.length; i++) {
		        String hex = Integer.toHexString(0xFF & hash[i]);
		        if (hex.length() == 1) {
		            hexString.append('0');
		        }
		        hexString.append(hex);
		    }
		    return hexString.toString();
		}
}

