package lamda.reallife.persons;

public class Person
{
    String   name;

    Integer  age;

    Country  country;

    Language language;

    Hobbie   favouriteAction;

    public Person( String name, int age, Country country, Language language, Hobbie hobbie )
    {
        setName( name );
        setAge( age );
        setCountry( country );
        setLanguage( language );
        setFavouriteAction( hobbie );
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public Integer getAge()
    {
        return age;
    }

    public void setAge( Integer age )
    {
        this.age = age;
    }

    public Country getCountry()
    {
        return country;
    }

    public void setCountry( Country country )
    {
        this.country = country;
    }

    public Language getLanguage()
    {
        return language;
    }

    public void setLanguage( Language language )
    {
        this.language = language;
    }

    public Hobbie getFavouriteAction()
    {
        return favouriteAction;
    }

    public void setFavouriteAction( Hobbie favouriteAction )
    {
        this.favouriteAction = favouriteAction;
    }

    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer( "" );
        buffer.append( " name: " + getName() );
        buffer.append( " age: " + getAge() );
        buffer.append( " country: " + getCountry() );
        buffer.append( " language" + ": " + getLanguage() );
        buffer.append( " hobbie: " + getFavouriteAction() );

        return buffer.toString();

    }
}
