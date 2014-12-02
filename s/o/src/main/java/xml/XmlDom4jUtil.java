package xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public final class  XmlDom4jUtil {
	public static Document document;
	static{
		document = load();
	}
	
	public XmlDom4jUtil(){
	}
	
	public static Document load(){
		SAXReader reader = new SAXReader();
		try {
			return (Document) reader.read(new FileInputStream(	"C:\\devs\\xml\\MarketRiskDimensions.xml" ));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

	public static void parse() {
			try {
				
				//List<Element> elements = document.getRootElement().selectNodes("//dimension[@name='"+"Container"+"']//level");
				List<Element> elements = document.getRootElement().selectNodes("//dimension//level");
				
					for (Element element : elements) {
					    String name = element.attributeValue("name");
					    String property = element.attributeValue("property");
					 System.out.println( "name:" + name + ",property:" +property);
					}

				
			} catch (Exception e) {
				
				e.printStackTrace();
			} 
	        
	}  
}
