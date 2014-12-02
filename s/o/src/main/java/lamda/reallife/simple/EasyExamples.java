package lamda.reallife.simple;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * this class contains several java 8 lambdas examples
 * 
 * @author dgutierrez-diez
 */
public class EasyExamples
{
    public static void main( String[] args )
    {

        /* functions with two input parametes and one output can be implemented easily using lambdas */
        BiFunction<Integer, Integer, Integer> addition = ( x, y ) -> x + y;
        System.out.println( "calling addition of 2 and 3 resulting: " + addition.apply( 2, 3 ) );

        /*
         * Runnable is a functional interface, we have to implement the method run or provide a
         * function for it in Lambda style
         */
        Runnable r = ( ) -> System.out.println( "calling Runnable: run run run" );
        Thread t = new Thread( r );
        t.start();

        /* prints out in the console all elements of the list using the for each functionality */
        Arrays.asList( "element", "other", "another one" ).forEach( e -> System.out.println( e ) );

        /*
         * similar behaviour by passing a consumer instead of passing directly the lambda
         */
        Consumer<String> printString = ( String s ) -> {
            System.out.println( s );
        };
        Arrays.asList( "1", "2", "3" ).forEach( printString );

        /* sorts all elements in a collection using a lambda expression as comparator */
        List<String> names = Arrays.asList( "Prado", "Gugenheim", "Reina Sofia", "Louvre" );
        Collections.sort( names, ( String a, String b ) -> b.compareTo( a ) );
        names.forEach( e -> System.out.println( e ) );

        /*
         * we create a predicate that is going to be used to filter the elements of the list that
         * are null, for this we use the stream() method of the new Streams API and the :: operator
         * to access methods of functional interfaces
         */
        Predicate<String> isNotNull = ( String s ) -> {
            return s != null;
        };
        Arrays.asList( "1", "2", "3", null ).stream().filter( isNotNull ).forEach( System.out::println );

    }

}
