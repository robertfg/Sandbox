package file.dir;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class DirectoryFilterWatcher implements FileFilter {
	private String filter;
	private Pattern refPattern;
	
	
	public DirectoryFilterWatcher() {
		this.filter = "*.csv";
	}

	public DirectoryFilterWatcher(String filter) {
		this.filter = filter;
		refPattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
	}
	
	public boolean accept(File file) {
		if ("*".equals(filter)) {
			return true;
		}
		String name = file.getName().replace("#", "");
		return refPattern.matcher(name.trim()).matches();
	}
	
	public static void main(String[] args){
		String filter = ".*MRE_POSITION*.*.gz";
		 Pattern refPattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
	
			String name = "MRE_POSITION_20120712_121212121.gz";
		    System.out.println( "" + refPattern.matcher(name).matches() );;
		
	}
}