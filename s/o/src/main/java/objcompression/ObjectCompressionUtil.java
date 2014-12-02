package objcompression;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The Class ObjectCompressionUtil.
 * 
 * @param <T>
 *            the generic type of the serializable object to be compressed
 */
public class ObjectCompressionUtil<T extends Serializable> {

	/**
	 * Compress object.
	 * 
	 * @param objectToCompress
	 *            the object to compress
	 * @param outstream
	 *            the outstream
	 * @return the compressed object
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public T compressObject(final T objectToCompress,
			final OutputStream outstream) throws IOException {

		final GZIPOutputStream gz = new GZIPOutputStream(outstream);
		final ObjectOutputStream oos = new ObjectOutputStream(gz);

		try {
			oos.writeObject(objectToCompress);
			oos.flush();
			return objectToCompress;
		} finally {
			oos.close();
			outstream.close();
		}

	}

	/**
	 * Expand object.
	 * 
	 * @param objectToExpand
	 *            the object to expand
	 * @param instream
	 *            the instream
	 * @return the expanded object
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 */
	public T expandObject(final T objectToExpand, final InputStream instream)
			throws IOException, ClassNotFoundException {
		final GZIPInputStream gs = new GZIPInputStream(instream);
		final ObjectInputStream ois = new ObjectInputStream(gs);

		try {
			@SuppressWarnings("unchecked")
			T expandedObject = (T) ois.readObject();
			return expandedObject;
		} finally {
			gs.close();
			ois.close();
		}
	}
	
	public static void main(String[] args){
	   long start = System.currentTimeMillis();
		ObjectCompressionUtil<ArrayList<Map<String, Object>>> str = new ObjectCompressionUtil<ArrayList<Map<String, Object>>>();
					try {
						
						ArrayList<Map<String, Object>> objectToExpand = null;
						
						
						objectToExpand = str.expandObject(objectToExpand, new FileInputStream("c:\\devs\\cached\\RwhInfo.bin"));
						for (Map<String, Object> map : objectToExpand) {
							System.out.println(map.get("DimPositionMasterID"));
						}
						
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
					System.out.println( "Object reloading time:" + (System.currentTimeMillis() - start)  );
	}

}