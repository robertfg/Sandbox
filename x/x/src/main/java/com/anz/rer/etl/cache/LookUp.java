package com.anz.rer.etl.cache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.anz.rer.etl.utils.DbUtils;
import com.anz.rer.etl.webservice.RwhInfo;

public class LookUp {

	private final static Logger logger = Logger.getLogger(LookUp.class);
	private ConcurrentHashMap<Integer,ConcurrentHashMap<Integer,RwhInfo>> cache = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer,RwhInfo>>();
	private boolean lock = false;

	private boolean buildCache = false;
	private DbUtils dbUtils;

	private ConcurrentMap<Integer,Boolean> cachedDate = new ConcurrentHashMap<Integer,Boolean>();
	private ConcurrentMap<Integer,Boolean> cachedDone = new ConcurrentHashMap<Integer,Boolean>();
	private ConcurrentMap<Integer,Boolean> cachedStarted = new ConcurrentHashMap<Integer,Boolean>();

	public LookUp(DbUtils dbUtils, boolean buildCache){
		logger.info("Cache ID:" + this.hashCode());		
		this.dbUtils = dbUtils;
		this.buildCache = buildCache;
		clearCache();
	}

	public void refreshCache(){
		cache.clear();
		cachedDate.clear();
		cachedDone.clear();
		cachedStarted.clear();

		logger.info("Cache is empty:" + cache.isEmpty());

		if(buildCache) {
			logger.info("Refreshing Cache" );
			buildXcache();
		}
	}

	public void clearCache(){
		cache.clear();
		cachedDate.clear();
		cachedDone.clear();
		logger.info("Cache is already clear before process : " + cache.isEmpty());
	}


	private void buildXcache(){
		lock();

		String dateSql = "select * from dw.dailybatchlog  where isApAvailable = 1";

		List<Map<String, Object>> dates = null;
		try {
			dates = dbUtils.executeSql(dateSql);
			for (Map<String, Object> date : dates) {
				Integer cobDate = (Integer)date.get("DimDateKey");

				ConcurrentHashMap<Integer,RwhInfo> dayCache = this.getCacheFromDB(cobDate);
				if(dayCache==null){
					logger.info("Something wrong with the data in the database");
				} else {
					cache.put(cobDate,dayCache);
				}
				dayCache = null;
			}	 

		} catch (SQLException e) {
			logger.info("Something wrong with the database:" + e.getMessage());
			logger.info("The service will retry after 10 mins");
			try {
				Thread.sleep(600);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		releaseLock();
	}



	public RwhInfo getByPositionID(int cobDate, String posId){

		if(cache!=null && cache.get(Integer.valueOf(cobDate)) == null ){
			logger.info("Going to build cache since no cache was built yet for cobDate:" + cobDate); 
			cache.put(  cobDate ,  this.getCacheFromDB( cobDate ) );
			logger.info("Cache was already done for cobDate:" + cobDate + ", Releasing the lock"); 

			releaseLock();
		} else {

		}
		return cache.get(Integer.valueOf(cobDate)).get( Integer.valueOf( posId ));

	}

	public RwhInfo getRwInfo( int posId, int cobDate){
		aquireLock();
		RwhInfo rwhInfo = null;
		try{
			rwhInfo =  getRwInfoFromDB( posId, cobDate);
			releaseLock();
			if(rwhInfo!=null){
				if(cache.get(Integer.valueOf(cobDate)) == null ){
					cache.put(Integer.valueOf(cobDate), new ConcurrentHashMap<Integer,RwhInfo>());
				}
				cache.get(Integer.valueOf(cobDate)).put(Integer.valueOf(posId), rwhInfo);
			} else {
				logger.info("Still no RPH for posID:" + posId + ",cobDate:" + cobDate );
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return rwhInfo;
	}

	private synchronized  RwhInfo  getRwInfoFromDB(int positionID, int cob) {
		lock();
		RwhInfo rwhInfo = null;
		String posMaster = " select PositionId, a.Portfolio, a.InstrumentCCY, a.MX_Family, a.MX_Group, a.MX_Type," +
		" a.Instrument from DW.vw_GetPositionList a where PositionId=" + positionID;

		try {

			ConcurrentHashMap<Object, ConcurrentHashMap<String,Object>> posMasterData=  dbUtils.executeSql(posMaster,"PositionId");

			for( Entry<Object, ConcurrentHashMap<String, Object>> positionData: posMasterData.entrySet()  ) {
				rwhInfo = new RwhInfo();
				rwhInfo.setPortfolio( (String) positionData.getValue().get("Portfolio") );
				rwhInfo.setInstrument( (String) positionData.getValue().get("Instrument"));
				rwhInfo.setInstrumentCCY( (String) positionData.getValue().get("InstrumentCCY") );

				rwhInfo.setPositionID( (Integer) positionData.getValue().get("PositionId"));

				rwhInfo.setMxFamily( (String) positionData.getValue().get("MX_Family") );
				rwhInfo.setMxGroup( (String) positionData.getValue().get("MX_Group") );
				rwhInfo.setMxType(  (String) positionData.getValue().get("MX_Type"));

				String rph = "select ChildNode, ReverseFullPathWithLeaf  from DW.udf_GetLatestHierarchyLeafMapByCOB(1,"+ cob +" ) b " +
				" where b.IsActive=1 and ChildNode='" + rwhInfo.getPortfolio() + "'";

				String geoHie ="select ChildNode, ReverseFullPathWithLeaf  from DW.udf_GetLatestHierarchyLeafMapByCOB(7,"+ cob +") b " +
				" where b.IsActive=1  and ChildNode='" + rwhInfo.getPortfolio() + "'";

				String legalHie = "select ChildNode, ReverseFullPathWithLeaf from DW.udf_GetLatestHierarchyLeafMapByCOB(9,"+ cob +") b " +
				" where b.IsActive=1 and ChildNode='" + rwhInfo.getPortfolio() + "'";

				Map<String,Object> rphData      =  dbUtils.executeSql(rph,      "ChildNode","ReverseFullPathWithLeaf");
				Map<String,Object> geoHieData   =  dbUtils.executeSql(geoHie,   "ChildNode","ReverseFullPathWithLeaf");
				Map<String,Object> legalHieData =  dbUtils.executeSql(legalHie, "ChildNode","ReverseFullPathWithLeaf");

				rwhInfo.setGeographyHierarchyPath( normalise(rwhInfo.getPortfolio(),   (String)geoHieData.get( rwhInfo.getPortfolio() ),8)  );
				rwhInfo.setPortfolioHierarchyPath( normalise(rwhInfo.getPortfolio(),   (String)rphData.get( rwhInfo.getPortfolio() ),20)  );
				rwhInfo.setLegalEntityHierarchyPath( normalise(rwhInfo.getPortfolio(), (String)legalHieData.get( rwhInfo.getPortfolio()),7)  );

			}

		} catch (SQLException e) {
			logger.info("Something wrong with the database:" + e.getMessage());
			logger.info("The service will retry after 10 mins");
			try {
				Thread.sleep(600);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e){
			e.printStackTrace();
		}

		releaseLock();	 
		return rwhInfo ;

	}

	public synchronized  ConcurrentHashMap<Integer,RwhInfo>  getCacheFromDB(int cob){

		lock();
		ConcurrentHashMap<Integer,RwhInfo>  result = new ConcurrentHashMap<Integer,RwhInfo>();
		String posMaster = " select PositionId, a.Portfolio, a.InstrumentCCY, a.MX_Family, a.MX_Group, a.MX_Type," +
		" a.Instrument from DW.vw_GetPositionList a order by PositionId";

		String rph = "select ChildNode, ReverseFullPathWithLeaf from DW.udf_GetLatestHierarchyLeafMapByCOB(1,"+ cob +" ) b " +
		" where b.IsActive=1";

		String geoHie ="select ChildNode, ReverseFullPathWithLeaf from DW.udf_GetLatestHierarchyLeafMapByCOB(7,"+ cob +") b " +
		" where b.IsActive=1";

		String legalHie = "select ChildNode, ReverseFullPathWithLeaf from DW.udf_GetLatestHierarchyLeafMapByCOB(9,"+ cob +") b " +
		" where b.IsActive=1";


		try {
			ConcurrentHashMap<Object, ConcurrentHashMap<String,Object>> posMasterData=  dbUtils.executeSql(posMaster,"PositionId");
			Map<String,Object> rphData=       dbUtils.executeSql(rph,          "ChildNode","ReverseFullPathWithLeaf");
			Map<String,Object> geoHieData=    dbUtils.executeSql(geoHie,    "ChildNode","ReverseFullPathWithLeaf");
			Map<String,Object> legalHieData=  dbUtils.executeSql(legalHie,"ChildNode","ReverseFullPathWithLeaf");

			if(posMasterData==null || rphData==null || geoHieData==null || legalHieData == null ||  
					posMasterData.isEmpty() || rphData.isEmpty()|| geoHieData.isEmpty() || legalHieData.isEmpty()){

				logger.info("Something wrong with the data in the Database for CobDate:" + cob);

			} else {
				cachedDate.put(Integer.valueOf(cob), true);
				logger.info("PUTTING the COB:" + cob + ",:" + cachedDate.get(cob));

				for(Entry<Object, ConcurrentHashMap<String,Object>> positionData: posMasterData.entrySet() ) {
					RwhInfo rwhInfo = new RwhInfo();
					rwhInfo.setPortfolio( (String) positionData.getValue().get("Portfolio") );
					rwhInfo.setInstrument( (String) positionData.getValue().get("Instrument"));
					rwhInfo.setInstrumentCCY( (String) positionData.getValue().get("InstrumentCCY") );

					rwhInfo.setPositionID( (Integer) positionData.getValue().get("PositionId"));

					rwhInfo.setMxFamily( (String) positionData.getValue().get("MX_Family") );
					rwhInfo.setMxGroup( (String) positionData.getValue().get("MX_Group") );
					rwhInfo.setMxType(  (String) positionData.getValue().get("MX_Type"));

					rwhInfo.setGeographyHierarchyPath( normalise(rwhInfo.getPortfolio(), (String)geoHieData.get( rwhInfo.getPortfolio() ),8)  );
					rwhInfo.setPortfolioHierarchyPath( normalise(rwhInfo.getPortfolio(), (String)rphData.get( rwhInfo.getPortfolio() ),20)  );
					rwhInfo.setLegalEntityHierarchyPath( normalise(rwhInfo.getPortfolio(), (String)legalHieData.get( rwhInfo.getPortfolio()),7)  );

					result.put(rwhInfo.getPositionID(), rwhInfo);
					
				}
				logger.info("RwhInfo cache:" + result.size() +  ",posMaster count:" + posMasterData.size()
						+ ",RPH count:" + rphData.size() + ",Geo count:" + geoHieData.size()
						+ ",Legal count:" +  legalHieData.size());

			}



		} catch (SQLException e) {
			logger.info("Something wrong with the database:" + e.getMessage());
			logger.info("The service will retry after 10 mins");
			try {
				Thread.sleep(600);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e){
			e.printStackTrace();
		}

		releaseLock();
		cachedDone.put(Integer.valueOf(cob), true);

		return result;

	}

	public String normalise(String key, String path, int depth ){		//&#x0020


		path=StringUtils.replaceChars(path, ',',  ' ' );
		List<String> list = new ArrayList<String>();

		if(path==null||path.isEmpty())
			return "N/A";

		StringTokenizer st = new StringTokenizer(path,"|");
		while(st.hasMoreElements()){
			String element = (String) st.nextElement();
			if(element.toLowerCase().trim().equals("root")) continue;			
			list.add(element);
		}
		int length=list.size();		
		for(int i=0;i<depth-length;i++){

			list.add(0,"~"+key);
		}	
		String[] normalise  = list.toArray(new String[depth>=length?depth:length]);

		list.clear();
		list=null;st=null;
		return StringUtils.join(normalise,"|");
	}


	public void lock(){
		lock=true;
		logger.info("locking.............................");
	}

	public void releaseLock(){
		lock=false;
		logger.info("releasing the lock..............................");
	}

	public void aquireLock() {	
		logger.info("...............Acquiring the Lock............................");
		while(lock){
			try {
				logger.info("Cached is rebuilding....... ");
				Thread.sleep(75000);
			} catch (InterruptedException e) {
				logger.error("Error while thread waiting to lock :" +e.getMessage());
				e.printStackTrace();
			}
		}
	}


	public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, RwhInfo>> getCache() {
		return cache;
	}

	public void setCache(ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, RwhInfo>> cache) {
		this.cache = cache;
	}

	public ConcurrentMap<Integer, Boolean> getCachedDate() {
		return cachedDate;
	}

	public void setCachedDate(ConcurrentMap<Integer, Boolean> cachedDate) {
		this.cachedDate = cachedDate;
	}

	public ConcurrentMap<Integer, Boolean> getCachedDone() {
		return cachedDone;
	}

	public void setCachedDone(ConcurrentMap<Integer, Boolean> cachedDone) {
		this.cachedDone = cachedDone;
	}

	public ConcurrentMap<Integer,Boolean> getCachedStarted() {
		return cachedStarted;
	}

	public void setCachedStarted(ConcurrentMap<Integer,Boolean> cachedStarted) {
		this.cachedStarted = cachedStarted;
	}




}
