package com.quartetfs.pivot.anz.utils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ANZConstants {

	public static final String RESULTV_COL="resultV";
	public static final String RESULT_COL="result";
	public static final String VAR_COL="scenarioValue";
	public static final String FXO_RESULT_COL="FxResultVector";
	public static final String FXO_RESULTV_COL="FxResultVVector";
	public static final String UNAVAILABLE = "N/A";
	public static final String BLANK = "";
	public static final int RESULT_INDEX = 0;
	public static final int RESULTV_INDEX = 1;
	public static final String DATE_COLUMN = "date";
	public static final Double ZERO = 0d;

	public static final int HIERARCHY_LENGTH = 9;
	public static final int FACT_SCENARIO_NAME_INDEX = 0;
	public static final int FACT_SCENARIO_PSR_NAME_INDEX = 1;
	public static final int FACT_DEAL_INDEX = 2;
	public static final int FACT_VALUE_INDEX = 3;
	public static final int FACT_PORTFOLIO_INDEX = 4;
	public static final String VAR_CONTAINER="VaR and P&L";
	 
	public static final String UAT_VAR_CONTAINER="UAT VaR and P&L";
	
	public static final String UAT_VAR_STRESS_CONTAINER="UAT_VAR_STRESS";
	public static final String VAR_STRESS_CONTAINER="VAR_STRESS";
	 
	
	public static final String BACK_DISCRIMINATOR = "Y";

	public static final String NO_SCENARIO = "NO_SCENARIO";
	public static final String PIPE_SEPARATOR= "\\|";
	public static final String COMMA_SEPARATOR=",";
	public static final String UNDERSCORE_SEPARATOR = "_";
	public static final String HASH_SEPARATOR= "#";
	public static final String DATA_EXTRACT_SEPARATOR = ",";
	 

	public static final int FACT_SIZE=27; 
	public static final int VECTOR_LENGTH=500;   
	public static final String DATE_PATTERN="dd/MM/yyyy";
	public static final String YYYYMMDD="yyyyMMdd";
	
	public static final int STATIC_FILES_QUEUE_CAPACITY=3000;
	public static final String GLOBAL_PROPERITES_FILE_NAME = "properties/GlobalProperties.properties";

	//used by analysis dimensions
	public static final String DEFAULT_DISCRIMINATOR = UNAVAILABLE;
	public static final String FIRST_DISCRIMINATOR_LEVEL="firstDiscriminatorLevel";
	public static final String OTHER_DISCRIMINATOR_LEVEL="otherDiscriminatorLevel";
	public static final String LEVEL_DIM_SEPARATOR="@";
	public static final String OTHER_DISCRIMINATORS_SEPARATOR=":";
	public static final String PARENT_DIM_PROP="parentDimension";

	//used by hierarchies
	public static final int PORTFOLIOHIERARCHY_DEPTH=20;
	public static final int CURRENCYPAIR_DEPTH=5;
	public static final int EQUITY_DEPTH=4;
	public static final int CURGROUPING_DEPTH=10;
	public static final int CURRENCY_DEPTH=5;
	public static final int GEOHIERARCHY_DEPTH=8;
	public static final int COMMODITY_DEPTH=3;
	public static final int ISSUER_DEPTH=5;
	public static final int LEGAL_ENTITY_DEPTH=7;
	public static final int DEAL_CURRENCY_DEPTH=10;
	public static final int FINANCE_HIERARCHY_DEPTH=10;
	public static final int REVENUE_HIERARCHY_DEPTH=10; 
	

	//used by the deal
	public static final String EXCHANGE ="exchange";
	public static final String NATIONAL_MARKET = "nationalMarket";
	public static final String EQUITY="equity";
	public static final String CURRENCY = "currency";
	public static final String CURRENCY_PAIR="currencypair";
	public static final String IR_CURVE="ircurve";
	public static final String FX_SPOT="fxspot";
	public static final String EQUITY_VOL="equityvol";
	public static final String FX_VOL="fxvol";
	public static final String SHOCK_DAYS="shockdays";
	public static final String TERM="term";
	public static final String STRESS_SCENARIOS="stressscenarios";
	public static final String EQUITY_TYPE="equityType";
	public static final String EQUITY_REGION="equityRegion";
	public static final String EQUITY_TIER="equityTier";
	public static final String CURRENCY_GLOBAL_PREC="currencyGlobalPrec";
	public static final String CURRENCY_ON_SHORE_OFF="currencyOnShoreOff";
	public static final String EQUITY_SPOT="equityspot";
	public static final String IR="ir";

	//used by the bespoke webservices
	public static final String VERSION="version";
	public static final String ONE_DAY="1D";
	public static final String TEN_DAYS="10D";  
	public static final String STRESS="STRESS";
	
	
	public static final String UAT_ONE_DAY  = "UAT_1D";
	public static final String UAT_TEN_DAYS = "UAT_10D";  
	
	
	public static final String UAT_STRESS  = "UAT_STRESS";
	
	public static final String VAR_1540  = "VAR_1540";
	
	
	

	public static final List<String> VAR_TYPES = Arrays.asList(ONE_DAY,TEN_DAYS,STRESS,UAT_ONE_DAY,UAT_TEN_DAYS,UAT_STRESS,VAR_1540);
	public static final List<Double> CONFIDENCE_LEVELS = Arrays.asList(0d, 0.01d, 0.025d, 0.975d, 0.99d, 1d);
	
	public static final ConcurrentMap<String,Integer> OFFSET_MAPPING= new ConcurrentHashMap<String,Integer>();
	public static final String SCHEMA_NAME = "MarketRiskSchema";
	
	public static final String ONE_DAY_VECTOR_MEASURE="1DVaRPLVector_AUD.SUM";
	public static final String TEN_DAYS_VECTOR_MEASURE="10DVaRPLVector_AUD.SUM";
	public static final String STRESS_VAR_VECTOR_MEASURE=  "StressVector_AUD.SUM";
	public static final String VAR_1540_VECTOR_MEASURE=  "PLSixYearMeasure.SUM";
	
	 
	
	
	public static final String DEAL_NUM="DEAL_NUM";
	public static final String MRESULT="M_RESULT";
	public static final String MRESULTV="M_RESULTV";
	public static final String DEAL_NUM_FIELD="M_DEALNUM";  
	public static final String ONE_DAY_FIELD="1DVaRPL_AUD";
	public static final String TEN_DAYS_FIELD="10DVaRPL_AUD";
	
	public static final String STRESS_FIELD="STRESS_VAR_VECTOR";
	public static final String VAR_1540_FIELD="SixYearVaRPL_AUD";
	
	
	
	
	
	public static final String COB_DATE_DIM_NAME = "COB Date";
	public static final String CONTAINER_DIM_NAME = "Container";

	//used by the cleaner
	public static final String COB="COB";
	public static final String CONTAINER_NAME="CONTAINER";
	public static final String COBDATE_IDX_COL_NAME="M_DATE";
	public static final String PORTFOLIO_IDX_COL_NAME="M_PTFOLIO";
	public static final String CONTAINER_IDX_COL_NAME="container";
	public static final String BATCH_SIZE_PROPS="removalBatchSize";
	public static final String DEFAULT_BATCH_SIZE="100000";
	public static final String REBUILD_PROPS="doRebuild";
	public static final String DEFAULT_REBUILD="true";

	
	public static final String FILE_EXT_SUCCESS=".DONE";
	public static final String FILE_EXT_FAILURE=".ERR";
	public static final String FILE_EMPTY=".EMT";
	

	public static final String FILE_EXTRACTION_EXT=".APX";
	public static final String FILE_EXTRACTION_ORG=".org";
	public static final String FILE_EXTRACTION_TMP=".TMP";
	
	
	public static final String IR_GAMMA_CONTAINER = "IR_GAMMA";
	public static final String GAMMA_BASIS_CONTAINER = "GAMMA_BASIS";
   	public static final String FXO_VECTOR_COLUMN = "scenarioValue";
	public static final int FXO_VECTOR_LENGTH=40;
	
	
	//Jmx Method Invocation to Rebuild and compress the structure
	public static final String JMX_USERNAME   = "jmxUserName";
	public static final String JMX_PASSWORD   = "jmxPassword";
	public static final String JMX_HOST       = "jmxHost";
	public static final String JMX_PORT       = "jmxPort";
	public static final String JMX_BEANNAME   = "jmxBeanName";
	public static final String JMX_METHOD     = "jmxMethodName"; 
	
	
	public static final String LABEL_CONTAINER = "containerName";
	public static final String LABEL_DIMENSION = "dateDimension";
	public static final String EQUALS_OP="=";
	public static final String FXO_CONTAINER = "FXO_DELGAMMA";
	public static final String EMPTY_STRING = "";
	
	public static final String FXDELGAMMAV_RESULTV = "FXDELGAMMAV.VECTOR_RESULTV";
	public static final String FXDELGAMMA_RESULT = "FXDELGAMMA.VECTOR_RESULT";
	public static final String M_RESULTV_MEASURE="M_RESULTV.SUM";
	public static final String M_RESULT_MEASURE="M_RESULT.SUM";
	
	public static String CONTEXT="context";
	public static final String PSRNAME="psrname";
	public static final String KEYID="ANZkey";

	public static final int VECTOR_STRESS_LENGTH=261;   
	 
	public static final int IRGAMMA_VECTOR_LENGTH=7;   
	
	public static final String PSR_NAME="psrName";

	public static final String OBJECTKEY = "objectKey";
	    
    public static final String NA ="N/A";
    
    public static final String IRGAMMA_VECTOR_MRESULTV = "IRGAMMA_MRESULTV.VECTOR_RESULTV";
	public static final String IRGAMMA_VECTOR_MRESULT = "IRGAMMA_MRESULT.VECTOR_RESULT";
	
	public static final String GAMMA_BASIS_VECTOR_RESULTV = "GAMMA_BASISV.VECTOR_RESULTV";
	public static final String GAMMA_BASIS_VECTOR_RESULT  = "GAMMA_BASIS.VECTOR_RESULT"; 
	
	public static final String VAR_DATE_TYPE = "varDateType";
	
	public static final int VAR_1540_VECTOR_LENGTH = 1540;    
	
	public static final String SIX_YEAR_VAR_CONTAINER="VAR_1540";
	
	public static final String VAR_SIX_YEAR_CONTAINER="VAR_1540";
	
	public static final String VAR_1540_CONTAINER="VAR_1540";
	
	public static final String END_OF_DAY ="EOD";
	 
	
	
	
}
