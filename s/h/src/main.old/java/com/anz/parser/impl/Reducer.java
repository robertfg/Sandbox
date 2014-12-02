package com.anz.parser.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class Reducer implements Runnable{

	public Reducer( List<byte[]> data) {
		super(); 
		byte[] textToSave = data.get(0);
		FileChannel rwChannel;
		try {
			rwChannel = new RandomAccessFile(new File( "c:\\temp\\textfile" + System.currentTimeMillis()+ ".txt"), "rw").getChannel();
			ByteBuffer wrBuf = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, textToSave.length);
 			wrBuf.put(textToSave);

			rwChannel.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	
	
	} 

	@Override
	public void run() {
	
		
	}

}
