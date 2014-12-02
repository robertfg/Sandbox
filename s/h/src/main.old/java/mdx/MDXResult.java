package mdx;


import java.util.ArrayList;
import java.util.List;

import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.Position;
import org.olap4j.metadata.Member;


public class MDXResult {
	
	private final static int COLUMNS = 0; //see Cellset javadoc
	private final static int ROWS= 1; //see Cellset javadoc
	/**
	* Outer list: rows, inner list: elements in a row
	*/
	
	
	private Member member;
	private Cell cell;
	
	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Cell getCell() {
		return cell;
	}

	public void setCell(Cell cell) {
		this.cell = cell;
	}

	
	
	public MDXResult(){}
	
    public MDXResult(Member m){
    	this.member = m;
    }
    public MDXResult(Cell c){
    	this.cell = c;
    } 
    
    
	public List<List<MDXResult>> getListFromCellSet(CellSet cellSet) {
	    List<List<MDXResult>> toReturn= new ArrayList<List<MDXResult>>();
	    //Column header
	    //See http://www.olap4j.org/api/index.html?org/olap4j/Position.html on how Position works, it helps a lot
	    //Every position will be a column in the header
	    for (Position pos : cellSet.getAxes().get(COLUMNS).getPositions()) {
	        for (int i = 0; i < pos.getMembers().size(); i++) {
	            if (toReturn.size() <= i) {
	                toReturn.add(i, new ArrayList<MDXResult>());
	            }
	            Member m = pos.getMembers().get(i);
	            MDXResult myCell = new MDXResult(m); //use m.getCaption() for display
	            toReturn.get(i).add(myCell );
	        }
	    }
	    //Put empty elements to the beginning of the list, so there will be place for the rows header
	    if (cellSet.getAxes().get(ROWS).getPositions().size() > 0) {
	        for (int count=0; count < cellSet.getAxes().get(1).getPositions().get(0).getMembers().size(); count++) {
	            for (int i = 0; i < toReturn.size(); i++) {
	                toReturn.get(i).add(0, new MDXResult());
	            }
	        }
	    }
	    //Content + row header
	    for(int i = 0; i < cellSet.getAxes().get(ROWS).getPositionCount(); i++) {
	        List<MDXResult> row = new ArrayList<MDXResult>();
	        //Header
	        for (org.olap4j.metadata.Member m : cellSet.getAxes().get(ROWS).getPositions().get(i).getMembers()) {
	            row.add(new MDXResult(m));
	        }
	        //Content
	        for (int j = 0; j < cellSet.getAxes().get(COLUMNS).getPositionCount(); j++) {
	            ArrayList<Integer> list = new ArrayList<Integer>();
	            list.add(j); //coordinte
	            list.add(i); //coordinte
	            row.add(new MDXResult(cellSet.getCell(list))); //use cell.getFormattedValue() for display
	        }
	        toReturn.add(row);
	    }
	    return toReturn;
	}
	
	public static void main(String[] args){
		
	}

}