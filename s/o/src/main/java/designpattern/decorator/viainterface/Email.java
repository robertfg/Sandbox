package designpattern.decorator.viainterface;

//concrete component
public class Email implements IEmail
{
 private String content;
 
 public Email(String content)
 {
    this.content = content;
 }

 @Override
 public String getContents()
 {
    //general email stuff
    return content;

 }

}