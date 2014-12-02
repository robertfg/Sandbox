package lamda;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

//http://www.informit.com/articles/article.aspx?p=2191424&seqNum=2


@FunctionalInterface
interface Formatter {
	String format(String fmtString, Object... arguments);
}

public class MethodRefDemo2 {
	public static void main(String[] args) {
		
		List<String> names = Arrays.asList("Charlie Brown", "Snoopy", "Lucy","Linus", "Woodstock");
		
		forEach(names, String::format);

		forEach(names, (fmt, arg) -> String.format(fmt, arg));

	}

	public static void forEach(List<String> list, Formatter formatter) {
		for (String item : list){
			System.out.print(formatter.format("%s%n", item));
		}
		System.out.println();

	}
}
