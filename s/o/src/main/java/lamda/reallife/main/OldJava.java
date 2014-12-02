package lamda.reallife.main;

import java.util.ArrayList;
import java.util.List;

import lamda.reallife.persons.Country;
import lamda.reallife.persons.Hobbie;
import lamda.reallife.persons.Language;
import lamda.reallife.persons.Person;

public class OldJava
{

    public static void main( String[] args )
    {
        List<Person> persons = populatePersons();
        for( Person p : persons )
        {
            showSpanish( p );
            showEnglishSpeakers( p );
            showPeopleThatLikeDancing( p );
            showAmericansThatLikeSports( p );
        }
    }

    private static void showAmericansThatLikeSports( Person p )
    {
        if( p != null && Country.USA.equals( p.getCountry() ) && Hobbie.SPORTS.equals( p.getFavouriteAction() ) )
        {
            System.out.println( p );
        }
    }

    private static void showPeopleThatLikeDancing( Person p )
    {
        if( p != null && Hobbie.DANCE.equals( p.getFavouriteAction() ) )
        {
            System.out.println( p );
        }

    }

    private static void showEnglishSpeakers( Person p )
    {
        if( p != null && Language.ENGLISH.equals( p.getLanguage() ) )
        {
            System.out.println( p );
        }

    }

    private static void showSpanish( Person p )
    {
        if( p != null && Country.SPAIN.equals( p.getCountry() ) )
        {
            System.out.println( p );
        }

    }

    private static List<Person> populatePersons()
    {
        List<Person> persons = new ArrayList<Person>();
        persons.add( new Person( "Dani", 22, Country.SPAIN, Language.SPANISH, Hobbie.DRINK ) );
        persons.add( new Person( "Joe", 24, Country.USA, Language.ENGLISH, Hobbie.DANCE ) );
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
