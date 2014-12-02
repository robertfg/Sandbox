package mdx;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.metadata.Catalog;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Schema;

public class Olap4jTutorial {
 
    public static void main(String[] params) {
    
    	long start = System.currentTimeMillis();
    	long end = 0;
    	
    String mdxCount = "SELECT NON EMPTY Hierarchize({DrilldownLevel({[Container].[ALL].[AllMember]})}) ON ROWS, " +
						" NON EMPTY Crossjoin(Hierarchize({[COB Date].[COB Date].Members}), Hierarchize({[Data Snapshot].[Data Snapshot].Members})) ON COLUMNS " +
						" FROM [MarketRiskCube] " +
						" WHERE ([Measures].[contributors.COUNT])";
						    
		    try {
				
		    	//OlapConnection olapConnection = Olap4jTutorial.getConnection("jdbc:xmla:Server=http://10.52.16.1:8485/cube/xmla", "apuser", "r3r4NZ");
		    	//OlapConnection olapConnection = Olap4jTutorial.getConnection("jdbc:xmla:Server=http://10.52.16.43:7200/cube/xmla", "apuser", "r3r4NZ");
		    	OlapConnection olapConnection = Olap4jTutorial.getConnection("jdbc:xmla:Server=http://mrp-ap.dev.anz:7200/cube/xmla", "apuser", "r3r4NZ");
		    	
		    		
		    	
		    	
			
		    	printCubeDetails(olapConnection);
		    	/*System.out.println("Getting connection:" + (System.currentTimeMillis() - start) + " ms");
		    	start = System.currentTimeMillis();
		    	 
		    	CellSet count1 = Olap4jTutorial.executeMdx(olapConnection, mdxCount );
		    	
		    	System.out.println("Exec 1:" + (System.currentTimeMillis() - start) + " ms");
		    	start = System.currentTimeMillis();
		    	
		    	
			  System.out.println(count1);
			  CellSet count2 = Olap4jTutorial.executeMdx(olapConnection, mdxCount );
			  System.out.println("Exec 2:" + (System.currentTimeMillis() - start) + " ms");
		    	start = System.currentTimeMillis();
		    	  
			  System.out.println(count2);
			  CellSet count3 = Olap4jTutorial.executeMdx(olapConnection, mdxCount );
			  System.out.println("Exec 3:" + (System.currentTimeMillis() - start) + " ms");
		    	start = System.currentTimeMillis();
		    	
			  
			  System.out.println(count3);
			  
			  CellSet count4 = Olap4jTutorial.executeMdx(olapConnection, mdxCount );
			  System.out.println("Exec 4:" + (System.currentTimeMillis() - start) + " ms");
		      start = System.currentTimeMillis();
		    	
			  System.out.println(count4);*/
				
		    } catch (ClassNotFoundException e) {
			
				e.printStackTrace();
			} catch (SQLException e) {
			
				e.printStackTrace();
			}
    
    
    }

    
   
    
    
    public  static void prettyPrint(CellSet result){
    	
    }
 
    
    public static void testNewMdxMethod(){
    	
    	try {
			OlapConnection olapConnection = getConnection("jdbc:xmla:Server=http://10.52.16.1:8485/cube/xmla", "apuser", "r3r4NZ");
			
			printCubeDetails(olapConnection);
			
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
    
    public static void printCubeDetails(OlapConnection olapConnection){
    	
    	 try {
			for(Catalog catalog : olapConnection.getOlapCatalogs()) {
				 
			     System.out.println("Discovered catalog: " + catalog.getName());
			     for(Schema schema : catalog.getSchemas()) {
			         System.out.println("  Discovered schema: " + schema.getName());
			         for(Cube cube : schema.getCubes()) {
			             System.out.println("    Discovered cube: " + cube.getName());
			             for(Dimension dimension : cube.getDimensions()) {
			            	 
			                 System.out.println("      Dimension: " + dimension.getName()  +"," + dimension.getUniqueName() );
			                		 /*+ "," + dimension.getDefaultHierarchy().getUniqueName() + "," + dimension.getDefaultHierarchy().getName() );*/
			                 for ( Hierarchy h  : dimension.getHierarchies()) {
								 System.out.println( h.getCaption()+ ","+ h.getName() + "," + h.getUniqueName() );
							} 
			                 
			                 
			             }
			             
			             for(Measure  measure :cube.getMeasures()) {
                        	 System.out.println("      Measure: " + measure.getName());
                         }
			         }
			     }
			 }
		} catch (OlapException e) {
		
			e.printStackTrace();
		}
    }
    
    
    public static OlapConnection getConnection(String xmlUr, String userName, String password) throws ClassNotFoundException, SQLException{
	     try{
	    	 Class.forName("org.olap4j.driver.xmla.XmlaOlap4jDriver");
	    	 String connectionString = "jdbc:xmla:Server=http://10.52.16.1:8485/cube/xmla"; //prelive2
	         OlapConnection connection = (OlapConnection) DriverManager.getConnection(connectionString,"apuser","r3r4NZ");
	         return connection.unwrap(OlapConnection.class);
	         
	     }catch( ClassNotFoundException c ){
	    	 throw c;
	     }  catch( SQLException s){
	    	 throw s;
	     }
    }
    
    
    public static  CellSet executeMdx(OlapConnection olapConnection, String mdxStatement) throws OlapException { 
    	
    	 OlapStatement statement;
		try {
			statement = olapConnection.createStatement();
			 return statement.executeOlapQuery(mdxStatement);
		    	
		} catch (OlapException e) {
			throw e;
		}

		
       
    }
    

}