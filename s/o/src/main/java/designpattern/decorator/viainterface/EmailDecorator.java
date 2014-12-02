package designpattern.decorator.viainterface;

public abstract class EmailDecorator implements IEmail
{
   //wrapped component
   IEmail originalEmail;
   
   
}