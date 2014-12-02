package mdx;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;

public class ExecMdx implements Callable<Object> {

	private String mdx;
	private String xmlaUrl;
	private String userName;
	private String password;

	public ExecMdx(String mdx, String xmlaUrl, String userName, String password) {

		this.mdx = mdx;
		this.xmlaUrl = xmlaUrl;
		this.userName = userName;
		this.password = password;
	}

	@Override
	public Object call() throws Exception {
		return executeMdx(getConnection(xmlaUrl, userName, password), mdx);
	}

	private OlapConnection getConnection(String xmlUr, String userName,
			String password) throws ClassNotFoundException, SQLException {
		try {
			Class.forName("org.olap4j.driver.xmla.XmlaOlap4jDriver");
			String connectionString = "jdbc:xmla:Server=" + xmlUr;
			OlapConnection connection = (OlapConnection) DriverManager
					.getConnection(connectionString, userName, password);
			return connection.unwrap(OlapConnection.class);

		} catch (ClassNotFoundException c) {
			throw c;
		} catch (SQLException s) {
			throw s;
		}
	}
	
	 

	private CellSet executeMdx(OlapConnection olapConnection,
			String mdxStatement) throws OlapException {

		OlapStatement statement;
		try {
			statement = olapConnection.createStatement();
			return statement.executeOlapQuery(mdxStatement);

		} catch (OlapException e) {
			throw e;
		}

	}

}