package com.quartetfs.pivot.anz.drillthrough;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.quartetfs.pivot.anz.utils.DBUtils;
import com.quartetfs.pivot.anz.utils.ListProperties;

public class DrillThroughUtil {

	private DBUtils dbUtils;

	private Map<String, Properties> defHeaders;

	private final String DRILLTHROUGH_HEADER = " select ContainerName,ColToExtract,ColHeaderAlias " + 
											   " from Reporting.APDrillThroughDetails R " +
											   " inner join DW.DimRiskContainer D " +
											   " on D.DimRiskContainerID = R.containerId ";

	private Map<String, ListProperties> drillThroughHeaders = new HashMap<String, ListProperties>();

	public DrillThroughUtil(DBUtils dbUtils, Map<String, Properties> defHeaders) {
		this.dbUtils = dbUtils;
		this.defHeaders = defHeaders;
		populateDrillThroughHeaders(null);
	}

	/**
	 * 
	 * @return<CONTAINER,<ColToExtract,ColAliasing>
	 */
	public Map<String, ListProperties> getDrillThroughHeaders() {
		refreshHeadersFromDB(null);
		return drillThroughHeaders;
	}
	
	public ListProperties getDrillThroughHeaders(String containerName) {
		return drillThroughHeaders.get(containerName);
	}
	

	public Map<String, Properties> getDefHeaders() {
		return defHeaders;
	}

	/**
	 * populate drillthrough headers from database
	 * @return
	 */
	private boolean populateDrillThroughHeaders(String container) {

		try {
			
			
			List<Map<String, Object>> headers = dbUtils.executeSQL(DRILLTHROUGH_HEADER
					 + (container!=null?" where D.ContainerName = '" + container + "'":"") );
	
			for (Map<String, Object> columns : headers) {
				String containerName = (String) columns.get("ContainerName");
				String[] colToExtract = ((String) columns.get("ColToExtract"))
						.split("\\|");
				String[] colAliasing = ((String) columns.get("ColHeaderAlias"))
						.split("\\|");

				ListProperties columnDetails = new ListProperties();
				for (int i = 0; i < colToExtract.length;  i++) {
					columnDetails.put(colToExtract[i], colAliasing[i]);
				}
				drillThroughHeaders.put(containerName, columnDetails);
			}

			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}

		return false;
	}
	
	public void refreshHeadersFromDB(String containerName){
		drillThroughHeaders.clear();
		this.populateDrillThroughHeaders(containerName);
	}
	
	
	public void refreshHeadersFromDB(){
		drillThroughHeaders.clear();
		this.populateDrillThroughHeaders(null);
	}

	
}
