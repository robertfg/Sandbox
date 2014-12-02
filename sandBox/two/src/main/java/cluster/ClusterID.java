package cluster;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import org.jgroups.Address;
import org.jgroups.util.Streamable;
import org.jgroups.util.Util;

/** ID which is unique across a cluster
 * @author Bela Ban
 * @version $Id: ClusterID.java,v 1.2 2008/09/05 06:23:44 belaban Exp $
 */
public class ClusterID implements Streamable {
    private Address creator;
    private  int id;

    private static int next_id=1;

    public ClusterID() {
    }
 
    public ClusterID(Address creator, int id) {
        this.creator=creator;
        this.id=id;
    }

    public Address getCreator() {  
        return creator;
    }

    public int getId() {
        return id;
    }

    public static synchronized ClusterID create(Address addr) {
        return new ClusterID(addr, next_id++);
    }

    public int hashCode() {
        return creator.hashCode() + id;
    }

    public boolean equals(Object obj) {
        ClusterID other=(ClusterID)obj;
        return creator.equals(other.creator) && id == other.id;
    }

    public String toString() {
        return creator + "::" + id;
    }

    
    public void writeTo(DataOutputStream out) throws IOException {
        try {
			Util.writeAddress(creator, out);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        out.writeInt(id);
    }

    public void readFrom(DataInputStream in) throws IOException, IllegalAccessException, InstantiationException {
        try {
			creator=Util.readAddress(in);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        id=in.readInt();
    }

	@Override
	public void readFrom(DataInput in) throws Exception {
		readFrom( (DataInputStream)in);
		
	}

	@Override
	public void writeTo(DataOutput out) throws Exception {
		writeTo( (DataOutputStream) out);
	}
}
