package lamda.reallife.main;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import lamda.reallife.persons.Country;
import lamda.reallife.persons.Hobbie;
import lamda.reallife.persons.Language;
import lamda.reallife.persons.Person;

/*

http://examples.javacodegeeks.com/java-basics/lambdas/java-8-lambda-expressions-tutorial/

@FunctionalInterface: Annotates an interface with just one abstract method.
Functions: Functional interface that has an apply() method.
Predicates: Functional interface that has an test() method. Useful for filtering mechanisms.
Suppliers: Contains get methods. They work as supplier of objects (for example in factories).
Consumers: Contains an accept() method. It is expected to produce side effects since it returns no results.
*/
public class UsingLambdas
{

    public static void main( String[] args )
    {
        List<Person> persons = populatePersons();

        // print all persons using lambdas
        persons.forEach( ( s ) -> {
            System.out.println( s );
        } );

        // using an instantiated predicate
        Predicate<Person> spanish = p -> {  return Country.SPAIN.equals( p.getCountry() );  };
        
        persons.stream().filter( spanish ).forEach( ( person ) -> {
            System.out.println( person );
        } );

        // using Lambdas directly for filtering the spanish ones
        persons.stream().filter( person -> {
            return Country.SPAIN.equals( person.getCountry() );
        } ).forEach( ( eachPerson ) -> {
            System.out.println( eachPerson );
        } );

        // using Lambdas directly for filtering the chinesse ones that love to eat
        persons.stream().filter( person -> {
            return Country.CHINA.equals( person.getCountry() ) && Hobbie.EAT.equals( person.getFavouriteAction() );
        } ).forEach( ( eachPerson ) -> {
            System.out.println( eachPerson );
        } );

        // using Lambdas directly for filtering the chinesse ones that love to eat
        persons.stream().filter( person -> {
            return Country.CHINA.equals( person.getCountry() ) && Hobbie.EAT.equals( person.getFavouriteAction() );
        } ).forEach( ( eachPerson ) -> {
            System.out.println( eachPerson );
        } );

        Predicate<Person> germans = person -> {
            return Country.GERMANY.equals( person.getCountry() );
        };

        Predicate<Person> dancers = person -> {
            return Hobbie.DANCE.equals( person.getFavouriteAction() );
        };

        // combining different predicates is also possible
        persons.stream().filter( germans.and( dancers ) ).forEach( ( eachPerson ) -> {
            System.out.println( eachPerson );
        } );

        // using streams and filters is possible to get statistics directly like number of Persons
        // based of different criteria
        persons.stream().filter( germans.and( dancers ) ).count();

        // oldest person
        System.out.println( "oldest one: "
            + persons.stream().filter( germans.and( dancers ) ).max( ( p1, p2 ) -> p1.getAge() - p2.getAge() ) );

    }

    private static List<Person> populatePersons()
    {
        List<Person> persons = new ArrayList<Person>();
        persons.add( new Person( "Dani", 22, Country.SPAIN, Language.SPANISH, Hobbie.DRINK ) );
        persons.add( new Person( "Joe", 24, Country.GERMANY, Language.ENGLISH, Hobbie.DANCE ) );
        persons.add( new Person( "Matt", 26, Country.USA, Language.ENGLISH, Hobbie.TALK ) );
        persons.add( new Person( "Jolie", 21, Country.AUSTRALIA, Language.ENGLISH, Hobbie.TRAVEL ) );
        persons.add( new Person( "Juan", 25, Country.SPAIN, Language.SPANISH, Hobbie.SPORTS ) );
        persons.add( new Person( "Francisca", 35, Country.AUSTRALIA, Language.ENGLISH, Hobbie.TALK ) );
        persons.add( new Person( "Davide", 18, Country.GERMANY, Language.GERMAN, Hobbie.DANCE ) );
        persons.add( new Person( "David", 50, Country.AUSTRALIA, Language.ENGLISH, Hobbie.SPORTS ) );
        persons.add( new Person( "Tercia", 65, Country.PORTUGAL, Language.PORTUGUESE, Hobbie.DANCE ) );
        persons.add( new Person( "Steven", 15, Country.AUSTRALIA, Language.ENGLISH, Hobbie.SPORTS ) );
        persons.add( new Person( "Jorge", 22, Country.AUSTRALIA, Language.ENGLISH, Hobbie.PLAY ) );
        persons.add( new Person( "Adam", 22, Country.USA, Language.ENGLISH, Hobbie.DANCE ) );
        persons.add( new Person( "Alexei", 22, Country.AUSTRALIA, Language.ENGLISH, Hobbie.TALK ) );
        persons.add( new Person( "Igor", 22, Country.AUSTRALIA, Language.ENGLISH, Hobbie.DRINK ) );
        persons.add( new Person( "Pan", 41, Country.CHINA, Language.CHINESSE, Hobbie.DANCE ) );
        persons.add( new Person( "Jorge", 22, Country.SPAIN, Language.SPANISH, Hobbie.PLAY ) );
        persons.add( new Person( "Siu", 22, Country.CHINA, Language.CHINESSE, Hobbie.DRINK ) );
        persons.add( new Person( "Yan", 29, Country.CHINA, Language.CHINESSE, Hobbie.DANCE ) );
        persons.add( new Person( "Rolf", 22, Country.GERMANY, Language.GERMAN, Hobbie.DRINK ) );
        persons.add( new Person( "Helmut", 22, Country.GERMANY, Language.GERMAN, Hobbie.DRINK ) );
        persons.add( new Person( "Maria", 16, Country.AUSTRALIA, Language.ENGLISH, Hobbie.DANCE ) );
        persons.add( new Person( "Jorge", 32, Country.SPAIN, Language.SPANISH, Hobbie.PLAY ) );
        persons.add( new Person( "Inga", 22, Country.GERMANY, Language.GERMAN, Hobbie.DRINK ) );
        persons.add( new Person( "Oscar", 80, Country.SPAIN, Language.SPANISH, Hobbie.DANCE ) );
        persons.add( new Person( "Jorge", 78, Country.SPAIN, Language.SPANISH, Hobbie.PLAY ) );
        persons.add( new Person( "Dimitris", 22, Country.GREECE, Language.GREEK, Hobbie.TRAVEL ) );
        persons.add( new Person( "Socatres", 30, Country.GREECE, Language.GREEK, Hobbie.TALK ) );
        return persons;
    }

}
