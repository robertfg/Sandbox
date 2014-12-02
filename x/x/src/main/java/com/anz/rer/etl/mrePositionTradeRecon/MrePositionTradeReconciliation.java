package com.anz.rer.etl.mrePositionTradeRecon;

import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.scheduling.quartz.QuartzJobBean;


public class MrePositionTradeReconciliation extends QuartzJobBean {

	private final static Logger logger = Logger
			.getLogger(MrePositionTradeReconciliation.class);
	static final String JDBC_TEMPLATE = "jdbcTemplate";
	static final String RECONCILE_SQL = "reconcileSql";
	static final String JOB_STATUS_SQL = "jobStatusSql";
	static final String UPDATE_STATUS_SQL = "updateSql";
	private String updateSql;

	private String reconcileSql;
	private String jobStatusSql;
	private final String JOB_COMPLETE="Complete";
	private final String JOB_FAILED="Failed";
	private JdbcTemplate jdbcTemplate;
	private final String ETL_RWH = "ETL_RWH";

	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {

		Map<String, Object> jobDataMap = context.getMergedJobDataMap();
		JdbcTemplate jdbcTemplate = (JdbcTemplate) jobDataMap
				.get(JDBC_TEMPLATE);
		this.setJdbcTemplate(jdbcTemplate);

		setReconcileSql((String) jobDataMap.get(RECONCILE_SQL));
		setJobStatusSql((String) jobDataMap.get(JOB_STATUS_SQL));
		setUpdateSql((String) jobDataMap.get(UPDATE_STATUS_SQL));
		int jobId = -1;
		String murexVersion = null;
		String status = null; 
		try{		
			SqlRowSet jobStatusRS = jdbcTemplate.queryForRowSet(jobStatusSql);
			logger.info("Query - " + jobStatusSql);
			while(jobStatusRS != null && jobStatusRS.next()){	
				jobId = jobStatusRS.getInt(1);
				String businessDate = jobStatusRS.getString(2);
				murexVersion = jobStatusRS.getString(3);

				if( jobId != -1 && processReconciliation(jobId,businessDate,murexVersion  )) {
					status = JOB_COMPLETE;
				}else { 
					status = JOB_FAILED;
				}

				updateJobStatus(status, jobId);
			} 
		}catch (DataAccessException e){
			logger.error("Error to retrieve JOB ID for reconciliation : " + e.getMessage());
			e.printStackTrace();
			updateJobStatus(JOB_FAILED, jobId);
		}

	}

	public void updateJobStatus(String status, int jobId) {

		try {
			jdbcTemplate.update(updateSql, new Object[] { status, ETL_RWH,
					jobId });
		} catch (DataAccessException dataAccessException) {
			logger.error("Error while updating the job status");
		}
	}

	public boolean processReconciliation(int jobId, String businessDate,
			String murexVersion) {
		boolean result = true;
		try {
			logger.info("MRE Trade Reconciliation process : JobId - " + jobId
					+ " COB - " + businessDate + " Murex Version - "
					+ murexVersion);
			SqlRowSet resultRowSet = jdbcTemplate.queryForRowSet(reconcileSql,
					murexVersion, businessDate);

			while (resultRowSet != null && resultRowSet.next()) {
				int totalRows = resultRowSet.getInt("TotalCount");
				logger.info("MRE Trade Reconciliation process finished with total number of records : "
						+ totalRows);
			}
		} catch (DataAccessException e) {
			logger.error("Error in MRE Trade Reconciliation preocess : "
					+ e.getMessage());
			e.printStackTrace();
			result = false;
			updateJobStatus(JOB_FAILED, jobId);
		}
		return result;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public String getReconcileSql() {
		return reconcileSql;
	}

	public void setReconcileSql(String reconcileSql) {
		this.reconcileSql = reconcileSql;
	}

	public String getJobStatusSql() {
		return jobStatusSql;
	}

	public void setJobStatusSql(String jobStatusSql) {
		this.jobStatusSql = jobStatusSql;
	}

	public String getJOB_COMPLETE() {
		return JOB_COMPLETE;
	}

	public String getJOB_FAILED() {
		return JOB_FAILED;
	}

	public String getUpdateSql() {
		return updateSql;
	}

	public void setUpdateSql(String updateSql) {
		this.updateSql = updateSql;
	}
}
