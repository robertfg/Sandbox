package json;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

	public static void main(String[] args) {
	
		//byte x = 111;
		
	}  

	public static void jsonDeserial(){
		ObjectMapper mapper = new ObjectMapper();
		try {
			Object user = mapper.readValue(new File("c:\\devs\\data.json"), Object.class);
			System.out.println("test");
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
