package mdx;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.List;

import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;
import org.olap4j.layout.RectangularCellSetFormatter;
import org.olap4j.metadata.Catalog;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Schema;

public class Olap4jTutorial {
 
    public static void main(String[] params) {
    	
    //	Olap4jTutorial.testRPHFilter();
   // 	Olap4jTutorial.apRphFilter();
      
    	Olap4jTutorial.test();
    }

    public static void testRPHFilter(){
    	try {
            Class.forName("org.olap4j.driver.xmla.XmlaOlap4jDriver");
            String connectionString = "jdbc:xmla:Server=http://10.52.16.43:7200/cube/xmla"; //prelive2
            OlapConnection connection = (OlapConnection) DriverManager.getConnection(connectionString,"apuser","r3r4NZ");
            
           long start = System.currentTimeMillis(); 
          
            OlapStatement statement = connection.createStatement();
       
            
            String rphMdx = "WITH " +
            				" SET [-SpecialSet-] as {[Risk Portfolio Hierarchy].Members} " +
            				" SET [Risk Portfolio Hierarchy0] as {                                      Subset([Risk Portfolio Hierarchy].Levels(0).Members,0,501)} " +
							" SET [Risk Portfolio Hierarchy1] as {Generate([Risk Portfolio Hierarchy0],(Subset([Risk Portfolio Hierarchy].Levels(1).Members,0,501)))}  " +
							" SET [Risk Portfolio Hierarchy2] as {Generate([Risk Portfolio Hierarchy1],(Subset([Risk Portfolio Hierarchy].Levels(2).Members,0,501)))} " +
							" SET [Risk Portfolio Hierarchy3] as {Generate([Risk Portfolio Hierarchy2],(Subset([Risk Portfolio Hierarchy].Levels(3).Members,0,501)))} " +
							" SET [Risk Portfolio Hierarchy4] as {Generate([Risk Portfolio Hierarchy3],(Subset([Risk Portfolio Hierarchy].Levels(4).Members,0,501)))} " +
							" SET [Risk Portfolio Hierarchy5] as {Generate([Risk Portfolio Hierarchy4],(Subset([Risk Portfolio Hierarchy].Levels(5).Members,0,501)))} " +
							" Set [Risk Portfolio Hierarchy6] as {" +
							" [Risk Portfolio Hierarchy0]," +
							" [Risk Portfolio Hierarchy1]," +
							" [Risk Portfolio Hierarchy2]," +
							" [Risk Portfolio Hierarchy3]," +
							" [Risk Portfolio Hierarchy4]," +
							" [Risk Portfolio Hierarchy5]} " +
							" MEMBER [Measures].[-Special_ChildrenCount-] AS 'Intersect([-SpecialSet-], {[Risk Portfolio Hierarchy].CurrentMember.Children}).Count' " +
							" Select Hierarchize([Risk Portfolio Hierarchy6])  Dimension Properties HIERARCHY_UNIQUE_NAME, PARENT_UNIQUE_NAME on 0, " +
							" {[Measures].[-Special_ChildrenCount-]} on 1 from [MarketRiskCube]";
							            
			/*	rphMdx = "WITH SET [-SpecialSet-] " +
						" as {[Product Hierarchy].Members} SET [Product Hierarchy0] as {Subset([Product Hierarchy].Levels(0).Members,0,501)} SET [Product Hierarchy1] as {Generate([Product Hierarchy0], " +
						" (Subset([Product Hierarchy].Levels(1).Members,0,501)))} Set [Product Hierarchy2] as {[Product Hierarchy0],[Product Hierarchy1]} " +
						" MEMBER [Risk Portfolio Hierarchy].CurrentMember.Children.Count on 1 from [MarketRiskCube]"; 
*/
					/*	" MEMBER [Measures].[-Special_ChildrenCount-] AS  " +
						" 'Intersect([-SpecialSet-], {[Product Hierarchy].CurrentMember.Children}).Count' " + 
						" Select Hierarchize([Product Hierarchy2])  Dimension Properties HIERARCHY_UNIQUE_NAME, PARENT_UNIQUE_NAME on 0, {[Measures].[-Special_ChildrenCount-]} on 1 from [MarketRiskCube]";
            */
            CellSet cellSet = statement.executeOlapQuery(rphMdx);
            
            System.out.println( "Execution Time including connecting to CUBE:" + (System.currentTimeMillis() - start  ) );
            
           /* Execution Time including connecting to CUBE:-204592*/
           
            
            RectangularCellSetFormatter formatter =
                    new RectangularCellSetFormatter(false);

            
                PrintWriter writer = new PrintWriter(System.out);
                formatter.format(cellSet, writer);
                writer.flush();
                
        
			
			//System.out.println( cellSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    	
    	
    	
    }
    
    
    public static void testMDX(){
    	
    	try {
            // Load the Olap4J XMLA driver
            Class.forName("org.olap4j.driver.xmla.XmlaOlap4jDriver");
 
            // Use the driver to obtain a connection to ActivePivot
            //For user and password you need to : DriverManager.getConnection(connectionString, user, password);
            String connectionString = "jdbc:xmla:Server=http://10.52.16.43:7200/cube/xmla"; //prelive2
            OlapConnection connection = (OlapConnection) DriverManager.getConnection(connectionString,"apuser","r3r4NZ");
 
 
            // The connection object can be used to discover ActivePivot catalogs,
            // schema, cubes, dimensions...
       /*     for(Catalog catalog : connection.getOlapCatalogs()) {
                System.out.println("Discovered catalog: " + catalog.getName());
                for(Schema schema : catalog.getSchemas()) {
                    System.out.println("  Discovered schema: " + schema.getName());
                    for(Cube cube : schema.getCubes()) { 
                        System.out.println("    Discovered cube: " + cube.getName());
                        for(Dimension dimension : cube.getDimensions()) {
                            System.out.println("      Dimension: " + dimension.getName());
                        }
                    }
                }
            }*/
 
 
            // MDX queries can be submitted to ActivePivot,
            // a special result set called 'cell set' is returned.
            OlapStatement statement = connection.createStatement();
            String mdx =
            
            		"SELECT " +
            		" NON EMPTY Hierarchize({DrilldownLevel({[Container].[ALL].[AllMember]})}) ON ROWS,"+
            		" NON EMPTY Hierarchize({[COB Date].[COB Date].Members}) ON COLUMNS"	+
            		" FROM [MarketRiskCube]"	+
            		" WHERE ([Measures].[contributors.COUNT])";
            
            String b2b = " WITH SET [PL_COB Date_AMS] AS " + 
						 " VisualTotals(Distinct(Hierarchize({Ascendants([COB Date].[COB Date].[11/11/13])," +
						 " Descendants([COB Date].[COB Date].[11/11/13])})))  " +
						 " SELECT  NON EMPTY " +
						 " Crossjoin(" +
						 " Hierarchize({DrilldownLevel({[Container].[ALL].[AllMember]})})," +
						 " Hierarchize({DrilldownLevel({[Trading Portfolio].[ALL].[AllMember]})})" +
						 " ) ON ROWS,  " +
						 " NON EMPTY Crossjoin(Hierarchize(Intersect({[COB Date].[COB Date].Members}, [PL_COB Date_AMS])), " +
						 " {[Measures].[contributors.COUNT], [Measures].[M_RESULT.SUM], [Measures].[M_RESULTV.SUM]}) ON COLUMNS  " +
						 " FROM [MarketRiskCube]";
		
            
            String rphMdx = "WITH SET [-SpecialSet-] as {[Risk Portfolio Hierarchy].Members}" +
            				" SET [Risk Portfolio Hierarchy0] as {Subset([Risk Portfolio Hierarchy].Levels(0).Members,0,501)} " +
							" SET [Risk Portfolio Hierarchy1] as {Generate([Risk Portfolio Hierarchy0],(Subset([Risk Portfolio Hierarchy].Levels(1).Members,0,501)))}  " +
							" SET [Risk Portfolio Hierarchy2] as {Generate([Risk Portfolio Hierarchy1],(Subset([Risk Portfolio Hierarchy].Levels(2).Members,0,501)))} " +
							" SET [Risk Portfolio Hierarchy3] as {Generate([Risk Portfolio Hierarchy2],(Subset([Risk Portfolio Hierarchy].Levels(3).Members,0,501)))} " +
							" SET [Risk Portfolio Hierarchy4] as {Generate([Risk Portfolio Hierarchy3],(Subset([Risk Portfolio Hierarchy].Levels(4).Members,0,501)))} " +
							" SET [Risk Portfolio Hierarchy5] as {Generate([Risk Portfolio Hierarchy4],(Subset([Risk Portfolio Hierarchy].Levels(5).Members,0,501)))} " +
							" Set [Risk Portfolio Hierarchy6] as {[Risk Portfolio Hierarchy0],[Risk Portfolio Hierarchy1],[Risk Portfolio Hierarchy2],[Risk Portfolio Hierarchy3]," +
							" [Risk Portfolio Hierarchy4],[Risk Portfolio Hierarchy5]} " +
							" MEMBER [Measures].[-Special_ChildrenCount-] AS 'Intersect([-SpecialSet-], {[Risk Portfolio Hierarchy].CurrentMember.Children}).Count' " +
							" Select Hierarchize([Risk Portfolio Hierarchy6])  Dimension Properties HIERARCHY_UNIQUE_NAME, PARENT_UNIQUE_NAME on 0, {[Measures].[-Special_ChildrenCount-]} on 1 from [MarketRiskCube]";
							            

            
            CellSet cellSet = statement.executeOlapQuery(rphMdx);
            
            
            MDXResult mdxRes = new MDXResult();
            List<List<MDXResult>> results =  mdxRes.getListFromCellSet(cellSet);
            for (List<MDXResult> list : results) {
				for (MDXResult mdxResult : list) {
					//System.out.println( mdxResult. ); 
				}
			}
            
            
            RectangularCellSetFormatter formatter =
                    new RectangularCellSetFormatter(false);

            
                // Print out.
                PrintWriter writer = new PrintWriter(System.out);
                formatter.format(cellSet, writer);
                writer.flush();
                
        
			
			//System.out.println( cellSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    
    private static void apRphFilter(){
    	
    	
    	
    	
    	try {
            Class.forName("org.olap4j.driver.xmla.XmlaOlap4jDriver");
            String connectionString = "jdbc:xmla:Server=http://10.52.16.43:7200/cube/xmla"; //prelive2
            
           long start = System.currentTimeMillis(); 
          
           final Connection connection =
        		   DriverManager.getConnection(
                   
                   // This is the SQL Server service end point.
        		   connectionString

                  // Tells the XMLA driver to use a SOAP request cache layer.
                  // We will use an in-memory static cache.
                  + ";Cache=org.olap4j.driver.xmla.cache.XmlaOlap4jNamedMemoryCache"

                  // Sets the cache name to use. This allows cross-connection
                  // cache sharing. Don't give the driver a cache name and it
                  // disables sharing.
                  + ";Cache.Name=MyNiftyConnection"

                  // Some cache performance tweaks.
                  // Look at the javadoc for details.
                  + ";Cache.Mode=LFU;Cache.Timeout=600;Cache.Size=100",

                  // XMLA is over HTTP, so BASIC authentication is used.
                  "apuser",
                  "r3r4NZ");

          // We are dealing with an olap connection. we must unwrap it.
          final OlapConnection olapConnection = connection.unwrap(OlapConnection.class);

          // Check if it's all groovy
          ResultSet databases = olapConnection.getMetaData().getDatabases();
          databases.first();
          System.out.println(
                  olapConnection.getMetaData().getDriverName()
                  + " -> "
                  + databases.getString(1));

          OlapStatement statement = olapConnection.createStatement();
          
          
          String level1 = " SELECT " +
					" NON EMPTY Hierarchize({DrilldownLevel({[Risk Portfolio Hierarchy].[ALL].[AllMember]})}) ON ROWS  " +
					" FROM [MarketRiskCube]  " +
					" WHERE ([Measures].[contributors.COUNT]) ";
          
          CellSet cellSet = statement.executeOlapQuery(level1  );
          
          
          
          
           
      
          // Done
          connection.close();

           /* OlapStatement statement = connection.createStatement( );
       
            
            String level1 = " SELECT " +
					" NON EMPTY Hierarchize({DrilldownLevel({[Risk Portfolio Hierarchy].[ALL].[AllMember]})}) ON ROWS  " +
					" FROM [MarketRiskCube]  " +
					" WHERE ([Measures].[contributors.COUNT]) ";		
	
            
            System.out.println( "Execution Time including connecting to CUBE:" + (System.currentTimeMillis() - start  ) );
           
            String level2 = " SELECT "+
            		" NON EMPTY Hierarchize(DrilldownMember({DrilldownLevel({[Risk Portfolio Hierarchy].[ALL].[AllMember]})}, {[Risk Portfolio Hierarchy].[ALL].[AllMember].[ANZ Group]})) ON ROWS "+
            		" FROM [MarketRiskCube] "+
            		" WHERE ([Measures].[contributors.COUNT])";
           
            String level3 = "SELECT " +
            				" NON EMPTY Hierarchize(DrilldownMember(DrilldownMember({DrilldownLevel({[Risk Portfolio Hierarchy].[ALL].[AllMember]})}," +
            				" {[Risk Portfolio Hierarchy].[ALL].[AllMember].[ANZ Group]}), {[Risk Portfolio Hierarchy].[ALL].[AllMember].[ANZ Group].[Monitored Portfolios], [Risk Portfolio Hierarchy].[ALL].[AllMember].[ANZ Group].[Non Traded], [Risk Portfolio Hierarchy].[ALL].[AllMember].[ANZ Group].[Other], [Risk Portfolio Hierarchy].[ALL].[AllMember].[ANZ Group].[TradedCAPM]})) ON ROWS   " +
            				" FROM [MarketRiskCube]  " +
            				" WHERE ([Measures].[contributors.COUNT])";
            
            Execution Time including connecting to CUBE:-204592
           
            
            RectangularCellSetFormatter formatter =
                    new RectangularCellSetFormatter(false);

            
                PrintWriter writer = new PrintWriter(System.out);
                formatter.format(cellSet, writer);
                writer.flush();
*/                
        
			
			//System.out.println( cellSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    	
    	
    	
    	
    	
    }

    public static void test(){
    	
    	 try {
             // Load the Olap4J XMLA driver
             Class.forName("org.olap4j.driver.xmla.XmlaOlap4jDriver");
  
             // Use the driver to obtain a connection to ActivePivot
             //For user and password you need to : DriverManager.getConnection(connectionString, user, password);
             
             String connectionString = "jdbc:xmla:Server=http://10.52.16.43:7200/cube/xmla"; //prelive2
             OlapConnection connection = (OlapConnection) DriverManager.getConnection(connectionString,"apuser","r3r4NZ");
             
             
  
             // The connection object can be used to discover ActivePivot catalogs,
             // schema, cubes, dimensions...
             for(Catalog catalog : connection.getOlapCatalogs()) {
                 System.out.println("Discovered catalog: " + catalog.getName());
                 for(Schema schema : catalog.getSchemas()) {
                     System.out.println("  Discovered schema: " + schema.getName());
                     for(Cube cube : schema.getCubes()) {
                         System.out.println("    Discovered cube: " + cube.getName());
                         for(Dimension dimension : cube.getDimensions()) {
                             System.out.println("      Dimension: " + dimension.getName());
                         }
                     }
                 }
             }
  
  
             // MDX queries can be submitted to ActivePivot,
             // a special result set called 'cell set' is returned.
             OlapStatement statement = connection.createStatement();
             String mdx = "SELECT [Container].[Container].Members ON 1 FROM MarketRiskCube";
             CellSet cellSet = statement.executeOlapQuery(mdx);
  
         } catch (Exception e) {
             e.printStackTrace();
         }
    }

}