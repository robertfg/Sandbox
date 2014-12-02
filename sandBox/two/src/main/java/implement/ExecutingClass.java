package implement;

public class ExecutingClass {

	
	public static void main(String[] args){
		
		ExecutingClass xClass = new ExecutingClass();
		  IQuery iQuery = new QueryClassB();
		  
		    xClass.executeQuery(iQuery);
	}
	
	
	public void executeQuery(IQuery query){
		QueryClassB qB = (QueryClassB) query;
		query.execute();
		qB.myHeader();
		
		
	}
}
