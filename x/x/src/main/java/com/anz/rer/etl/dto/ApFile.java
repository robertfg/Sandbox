package com.anz.rer.etl.dto;


public class ApFile {


	private String[] apFileNameDetails;
	private String fileExtension;
	private long fileLength;
	
	//SIGM0#IR_GAMMA#NON-VAR#2704542726906#20121029#2704542726906.APX.gz.tmp_UVR.gz
	
	public ApFile(String apFile, String delimeter) {
		apFileNameDetails = apFile.split(delimeter);
		fileExtension = getExtension(apFile);
	}
	
	public ApFile(String apFile, String delimeter, long fileLength) {
		apFileNameDetails = apFile.split(delimeter);
		fileExtension = getExtension(apFile);
		this.setFileLength(fileLength);
	}
	public String getPsrCode(){
		return apFileNameDetails[0];
	}
	

	public String getContainerName(){
		return apFileNameDetails[1];
	}
	
	public String getCobDate(){
		return apFileNameDetails[4];
	}
	
	public String getFileExtension() {
		return fileExtension;
	}
	
	
	private String getExtension(String fName)
	{
		String ext = null;
		
		int i = fName.lastIndexOf('.');
	
		if (i > 0 && i < fName.length() - 1)
		ext = fName.substring(i+1).toLowerCase();
	
		if(ext == null)
		return "";
	
		return ext;
		}

	public long getFileLength() {
		return fileLength;
	}

	public void setFileLength(long fileLength) {
		this.fileLength = fileLength;
	}
	
	

	
}
