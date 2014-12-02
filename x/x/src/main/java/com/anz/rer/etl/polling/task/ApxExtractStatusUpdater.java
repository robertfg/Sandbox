package com.anz.rer.etl.polling.task;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.anz.rer.etl.dto.ApFile;
import com.anz.rer.etl.utils.DbUtils;
import com.anz.rer.etl.utils.FileUtils;

public class ApxExtractStatusUpdater implements IDirectoryTask<Long> {

	private DbUtils dbUtils;
	private Map<String, String> dbMapStatus;
	private Map<String, String> varContainerMapping;

	private Set<String> alreadyUpdated = new TreeSet<String>();

	public ApxExtractStatusUpdater(Map<String, String> dbMapStatus,
			Map<String, String> varContainerMapping) {

		    this.dbMapStatus = dbMapStatus;
		    this.varContainerMapping = varContainerMapping;
	}
 
	
	private boolean updateStatusInDB(ApFile apFile) {
		String containerName = apFile.getContainerName();

		String cobDate = apFile.getCobDate();
		String status = this.dbMapStatus.get(apFile.getFileExtension());

		try {
			if (containerName.equalsIgnoreCase("VAR AND P&L")) {
				containerName = varContainerMapping.get(apFile.getPsrCode());
			} else if (containerName.equalsIgnoreCase("VAR_STRESS")) {
				containerName = "VAR_STRESS";
			}else if(containerName.equalsIgnoreCase( "VAR_1540" )){
	    		containerName = "VAR_1540_AGG";
	    	}
			
//	String sql = "{call [ETL].[UpdateJobStatus]('" + jobId.toString()+ "','"+ this.successStatus +"',1)}";
			
			return dbUtils.executeSp("{ call [DW].[UpdateSignoffAndExclude] ("
					+ cobDate + ",'" + containerName + "','" + status + "') }");

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	public DbUtils getDbUtils() {
		return dbUtils;
	}

	public void setDbUtils(DbUtils dbUtils) {
		this.dbUtils = dbUtils;
	}

	@Override
	public boolean execute(File file, String action) {
		if (!alreadyUpdated.contains(file.getName())) {
			String oldFileName = file.getName();

			FileUtils.renameFile(
					file,
					oldFileName.toUpperCase().replace(".TFM", ".FMX"));

			return updateStatusInDB(new ApFile(file.getName(), "#"));

		}
		alreadyUpdated.add(file.getName());
		return true;
	}

	public static void main(String[] args) {

		Map<String, String> mapp = new HashMap<String, String>();
		mapp.put("done", "doneccc");
		mapp.put("DONE", "ccccdoneccc");

		ApFile a = new ApFile(
				"SECR0#EQ_CORRELATIONS#NON-VAR#1680476621846184#20121219#1680476621846184.APX.gz.tmp_UVR.gz.1356923722039.DONE",
				"#");
		System.out.println(mapp.get(a.getFileExtension()));
		System.out.println(mapp.size());
	}

}
