package memory.cache;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class User implements Externalizable {
	  
	  private int id;
	  private String username;
	  private String email;
	  
	  public User(int id, String username, String email) {
	    this.id = id;
	    this.username = username;
	    this.email = email;
	  }
	  
	  public User(){
	    
	  }

	  public int getId() {
	    return id;
	  }

	  public String getUsername() {
	    return username;
	  }

	  public String getEmail() {
	    return email;
	  }

	  @Override
	  public void writeExternal(ObjectOutput out) throws IOException {
	    out.writeInt(id);
	    out.writeObject(username);
	    out.writeObject(email);
	  }

	  @Override
	  public void readExternal(ObjectInput in) throws IOException,
	      ClassNotFoundException {
	    id = in.readInt();
	    username = (String) in.readObject();
	    email = (String) in.readObject();
	  }
	  
	}