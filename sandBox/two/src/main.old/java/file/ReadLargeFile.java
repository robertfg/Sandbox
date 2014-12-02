package file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReadLargeFile {


	public void memoryMappedCopy(String fromFile, String toFile ) throws Exception{
		long timeIn = new Date().getTime();
		// read input file
		RandomAccessFile rafIn = new RandomAccessFile(fromFile, "rw");
		FileChannel fcIn = rafIn.getChannel();
	//	int size = rafIn.readLine().length();
		long size = 200L * 1024 * 1024 * 1024;
		ByteBuffer byteBuffIn = fcIn.map(FileChannel.MapMode.READ_WRITE, 0,(int) size);
		fcIn.read(byteBuffIn);
		byteBuffIn.flip();

		RandomAccessFile rafOut = new RandomAccessFile(toFile, "rw");
		FileChannel fcOut = rafOut.getChannel();

		ByteBuffer writeMap = ByteBuffer.allocateDirect((int) size);
                   

		writeMap.put(byteBuffIn);   

		long timeOut = new Date().getTime();
		System.out.println("Memory mapped copy Time for a file of size :" + (int) size +" is "+(timeOut-timeIn));
		fcOut.close();
		fcIn.close();
	}
	
	public void mapToOSMemory() throws IOException{
		 // 200GB
	     // long len = 200L * 1024 * 1024 * 1024;
		
		 //1.6GB
		  long len = 1L * 1024 * 1024 * 1024;
	      File file = new File("C:\\VARIN.txt");

	      RandomAccessFile raf = new RandomAccessFile(file, "rw");
	      raf.setLength(len);
	      FileChannel chan = raf.getChannel();

	      long t0 = System.currentTimeMillis();
	      
	      List<MappedByteBuffer> maps = new ArrayList<MappedByteBuffer>(); 
	      

	      long off = 0;
	      while (off < len)
	      {
	         long chunk = Math.min(len - off, Integer.MAX_VALUE);
	         ByteBuffer map =  MappedByteBuffer.allocateDirect((int) chunk) ;
	         map = chan.map(MapMode.READ_WRITE, off, chunk);
	         
	         off += map.capacity();
	        // maps.add(map);     
	      }
	      raf.close();

	      long t1 = System.currentTimeMillis();

	      System.out.println("took: " + (t1 - t0) + "ms");
	}

	public static void main(String args[]){		
		String inFileName="C:\\freefallprotection.log";
		File inFile = new File(inFileName);

		if (inFile.exists() != true){			
	//		System.exit(0);
		}

		try{
			//new ReadLargeFile().memoryMappedCopy(inFileName, inFileName+".new" );	 
			new ReadLargeFile().mapToOSMemory();	   
		}catch(FileNotFoundException fne){
			fne.printStackTrace();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}
	}


}
