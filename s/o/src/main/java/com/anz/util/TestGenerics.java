package com.anz.util;

public class TestGenerics
{
    public static void main(String[] _args)
    {
        // Number d = getInstance(Boolean.class);  // Fail !
        String d = getInstance(String.class); // OKay ! 
    }

    // Instantiate a Generic Type 
    public static <D> D getInstance(Class<D> _class)
    {
        try
        {
            return _class.newInstance();
        }
        catch (Exception _ex)
        {
            _ex.printStackTrace();
        }
        return null;
    }
}