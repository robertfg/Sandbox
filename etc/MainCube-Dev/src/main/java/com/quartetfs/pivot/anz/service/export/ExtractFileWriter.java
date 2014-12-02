package com.quartetfs.pivot.anz.service.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.service.export.ExtractObject.ExtractType;
import com.quartetfs.pivot.anz.utils.ANZConstants;

public class ExtractFileWriter implements Callable<ExportTaskThreadInfo> {
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	private int fileWritePartition;
	private BlockingQueue<ExtractObject> fileDataQueue;
	private String filePath;
	private Long threadId;
	
	public ExtractFileWriter(ExtractObject extractObject, int fileWritePartition, int fileQueueSize) {
		
		this.fileWritePartition = fileWritePartition;
		fileDataQueue = new ArrayBlockingQueue<ExtractObject>(fileQueueSize,true);
		if( null != extractObject) {
			try {
				filePath = extractObject.getFilePath();
				threadId = extractObject.getId();
				fileDataQueue.put(extractObject);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public ExportTaskThreadInfo call() throws Exception {
		
		ExtractObject extractObject = null;
		BufferedWriter writer = null ;
		long ioTime = 0l;
		int totalLine = 0;
		try {
			
			//put header
			
			writer =  new BufferedWriter(new FileWriter(new File( filePath )));
			
			while ((extractObject = fileDataQueue.take()) != null) {
				if(extractObject.getRows()!=null && extractObject.getRows().size()>0){
					totalLine+=extractObject.getRows().size();
					ioTime+=  write(extractObject,writer);
				}
				if(extractObject.isDone()){
					break;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();  
		} finally {
			if(writer!=null) { 
				try {
					writer.close();
					writer = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			removelastCharinFile(filePath);
			if( extractObject.isFromFM()){
				renameFile( filePath, ANZConstants.FILE_EXTRACTION_TMP, ".TFM");	
			} else {
				renameFile( filePath, ANZConstants.FILE_EXTRACTION_TMP, ANZConstants.FILE_EXTRACTION_EXT);
			}
			LOGGER.info("container FilePath:" + extractObject.getFilePath() + ",Total IO time:" +  " " + ioTime + " ms" 	+ " ,Number of lines:" + totalLine  + " ,ID:" + extractObject.getId());
			fileDataQueue = null;
		}
		return new ExportTaskThreadInfo(ioTime, threadId);
	}
	
	
	private void removelastCharinFile(String fileName){
		
		   try{
			        RandomAccessFile file= new RandomAccessFile(fileName,"rw");
			        long length = file.length();
			        if(length>0){
			        	file.setLength(length - 1);
			        }
			        file.close();
			        file = null;
		        }catch(Exception ex){
		        	ex.printStackTrace();
		    }
		
	}
	
	@SuppressWarnings("finally")
	private long write( ExtractObject extractObject, BufferedWriter writer) {
		long start = System.currentTimeMillis();
	
		try {
				StringBuilder sb = new StringBuilder(5000);
				if(extractObject.getExtractType().equals(ExtractType.NON_VAR)  ){
				  sb.append("?UniqueID,BaseCurrency,NationalMarket,SecuritiesExchange,Sector,UnderlyingEQ,UnderlyingCCY,UnderlyingCCYPAIR,UnderlyingIRCurve,ScenarioEQSpotPrice,ScenarioEQVol,ScenarioFXSpotPrice,ScenarioFXVol,ScenarioIR,ScenarioANZStressScenarios,ScenarioTerm,ScenarioThetaDecay,PositionID,ScenarioCorrelationType,ScenarioCorrelationShock,ScenarioTermBucket,ScenarioCommodityVol,ScenarioIRGamma,ScenarioIRVol,ScenarioMoneyNess,ScenarioRecoveryRate,ScenarioSpreadType,ScenarioTermUnderlying,M_RESULTV,M_RESULT,ScenarioCommoditySpotPrice,ScenarioEQDividend,UnderlyingCreditRating,UnderlyingCommodity,CommodityProductGroup,ScenarioGenericTermPillar,ScenarioStress,DealCurrency,ComGrpType,M_RESULT_LOT,Spread-Type2,ExpiryDate,GrpComLongName,GrpEqType,GrpEqTier,GrpEqRegion,GrpCrRating");
				  sb.append(",GrpCcyFamFxoDealCurr,GrpCcyFamFxoUndCcy,GrpCcyGrpDealCurr,GrpCcyGrpUndCcy");
				  sb.append(",GrpCcyPair,GrpComUnit,GrpComStrLbl,GrpComIdxName,GrpCrAgency");
				  sb.append(",GrpCrInvstmntGrade,GrpCrRatBndLt,GrpCrRatBndOther,GrpCrRatBndSec,GrpCrIndexType,GrpEqFullName,CurveType,ProductSubType,RiskType");
				  sb.append("\n");
				}else if(extractObject.getExtractType().equals(ExtractType.VAR_CONFIDENCE)  ){
				  sb.append("?UniqueID,Date,Var_Type,BaseCCY,VaRConfidence,RiskHierarchyPath,VaRPnL");
				  sb.append("\n");
				
				}else  if(extractObject.getExtractType().equals(ExtractType.HYPO)  ){
					  sb.append("?UniqueID,Date,Var_Type,BaseCCY,VaRConfidence,RiskHierarchyPath,VaRPnL");
					  sb.append("\n");
			
				} else if(extractObject.getExtractType().equals(ExtractType.VAR_PNL) ){ /*position-level*/
					  sb.append("?UniqueID");
					  sb.append(",BaseCCY");
					  sb.append(",S1,S2,S3,S4,S5,S6,S7,S8,S9,S10,S11,S12,S13,S14,S15,S16,S17,S18,S19,S20,S21,S22,S23,S24,S25,S26,S27,S28,S29,S30,S31,S32,S33,S34,S35,S36,S37,S38,S39,S40,S41,S42,S43,S44,S45,S46,S47,S48,S49,S50,S51,S52,S53,S54,S55,S56,S57,S58,S59,S60,S61,S62,S63,S64,S65,S66,S67,S68,S69,S70,S71,S72,S73,S74,S75,S76,S77,S78,S79,S80,S81,S82,S83,S84,S85,S86,S87,S88,S89,S90,S91,S92,S93,S94,S95,S96,S97,S98,S99,S100,S101,S102,S103,S104,S105,S106,S107,S108,S109,S110,S111,S112,S113,S114,S115,S116,S117,S118,S119,S120,S121,S122,S123,S124,S125,S126,S127,S128,S129,S130,S131,S132,S133,S134,S135,S136,S137,S138,S139,S140,S141,S142,S143,S144,S145,S146,S147,S148,S149,S150,S151,S152,S153,S154,S155,S156,S157,S158,S159,S160,S161,S162,S163,S164,S165,S166,S167,S168,S169,S170,S171,S172,S173,S174,S175,S176,S177,S178,S179,S180,S181,S182,S183,S184,S185,S186,S187,S188,S189,S190,S191,S192,S193,S194,S195,S196,S197,S198,S199,S200,S201,S202,S203,S204,S205,S206,S207,S208,S209,S210,S211,S212,S213,S214,S215,S216,S217,S218,S219,S220,S221,S222,S223,S224,S225,S226,S227,S228,S229,S230,S231,S232,S233,S234,S235,S236,S237,S238,S239,S240,S241,S242,S243,S244,S245,S246,S247,S248,S249,S250,S251,S252,S253,S254,S255,S256,S257,S258,S259,S260,S261,S262,S263,S264,S265,S266,S267,S268,S269,S270,S271,S272,S273,S274,S275,S276,S277,S278,S279,S280,S281,S282,S283,S284,S285,S286,S287,S288,S289,S290,S291,S292,S293,S294,S295,S296,S297,S298,S299,S300,S301,S302,S303,S304,S305,S306,S307,S308,S309,S310,S311,S312,S313,S314,S315,S316,S317,S318,S319,S320,S321,S322,S323,S324,S325,S326,S327,S328,S329,S330,S331,S332,S333,S334,S335,S336,S337,S338,S339,S340,S341,S342,S343,S344,S345,S346,S347,S348,S349,S350,S351,S352,S353,S354,S355,S356,S357,S358,S359,S360,S361,S362,S363,S364,S365,S366,S367,S368,S369,S370,S371,S372,S373,S374,S375,S376,S377,S378,S379,S380,S381,S382,S383,S384,S385,S386,S387,S388,S389,S390,S391,S392,S393,S394,S395,S396,S397,S398,S399,S400,S401,S402,S403,S404,S405,S406,S407,S408,S409,S410,S411,S412,S413,S414,S415,S416,S417,S418,S419,S420,S421,S422,S423,S424,S425,S426,S427,S428,S429,S430,S431,S432,S433,S434,S435,S436,S437,S438,S439,S440,S441,S442,S443,S444,S445,S446,S447,S448,S449,S450,S451,S452,S453,S454,S455,S456,S457,S458,S459,S460,S461,S462,S463,S464,S465,S466,S467,S468,S469,S470,S471,S472,S473,S474,S475,S476,S477,S478,S479,S480,S481,S482,S483,S484,S485,S486,S487,S488,S489,S490,S491,S492,S493,S494,S495,S496,S497,S498,S499,S500");
					  sb.append(",PositionId");
					  sb.append("\n");
			
				} else if(extractObject.getExtractType().equals(ExtractType.VAR_PNL_PORTFOLIO ) ){
					  sb.append("?UniqueID");
					  sb.append(",BaseCCY");
					  sb.append(",S1,S2,S3,S4,S5,S6,S7,S8,S9,S10,S11,S12,S13,S14,S15,S16,S17,S18,S19,S20,S21,S22,S23,S24,S25,S26,S27,S28,S29,S30,S31,S32,S33,S34,S35,S36,S37,S38,S39,S40,S41,S42,S43,S44,S45,S46,S47,S48,S49,S50,S51,S52,S53,S54,S55,S56,S57,S58,S59,S60,S61,S62,S63,S64,S65,S66,S67,S68,S69,S70,S71,S72,S73,S74,S75,S76,S77,S78,S79,S80,S81,S82,S83,S84,S85,S86,S87,S88,S89,S90,S91,S92,S93,S94,S95,S96,S97,S98,S99,S100,S101,S102,S103,S104,S105,S106,S107,S108,S109,S110,S111,S112,S113,S114,S115,S116,S117,S118,S119,S120,S121,S122,S123,S124,S125,S126,S127,S128,S129,S130,S131,S132,S133,S134,S135,S136,S137,S138,S139,S140,S141,S142,S143,S144,S145,S146,S147,S148,S149,S150,S151,S152,S153,S154,S155,S156,S157,S158,S159,S160,S161,S162,S163,S164,S165,S166,S167,S168,S169,S170,S171,S172,S173,S174,S175,S176,S177,S178,S179,S180,S181,S182,S183,S184,S185,S186,S187,S188,S189,S190,S191,S192,S193,S194,S195,S196,S197,S198,S199,S200,S201,S202,S203,S204,S205,S206,S207,S208,S209,S210,S211,S212,S213,S214,S215,S216,S217,S218,S219,S220,S221,S222,S223,S224,S225,S226,S227,S228,S229,S230,S231,S232,S233,S234,S235,S236,S237,S238,S239,S240,S241,S242,S243,S244,S245,S246,S247,S248,S249,S250,S251,S252,S253,S254,S255,S256,S257,S258,S259,S260,S261,S262,S263,S264,S265,S266,S267,S268,S269,S270,S271,S272,S273,S274,S275,S276,S277,S278,S279,S280,S281,S282,S283,S284,S285,S286,S287,S288,S289,S290,S291,S292,S293,S294,S295,S296,S297,S298,S299,S300,S301,S302,S303,S304,S305,S306,S307,S308,S309,S310,S311,S312,S313,S314,S315,S316,S317,S318,S319,S320,S321,S322,S323,S324,S325,S326,S327,S328,S329,S330,S331,S332,S333,S334,S335,S336,S337,S338,S339,S340,S341,S342,S343,S344,S345,S346,S347,S348,S349,S350,S351,S352,S353,S354,S355,S356,S357,S358,S359,S360,S361,S362,S363,S364,S365,S366,S367,S368,S369,S370,S371,S372,S373,S374,S375,S376,S377,S378,S379,S380,S381,S382,S383,S384,S385,S386,S387,S388,S389,S390,S391,S392,S393,S394,S395,S396,S397,S398,S399,S400,S401,S402,S403,S404,S405,S406,S407,S408,S409,S410,S411,S412,S413,S414,S415,S416,S417,S418,S419,S420,S421,S422,S423,S424,S425,S426,S427,S428,S429,S430,S431,S432,S433,S434,S435,S436,S437,S438,S439,S440,S441,S442,S443,S444,S445,S446,S447,S448,S449,S450,S451,S452,S453,S454,S455,S456,S457,S458,S459,S460,S461,S462,S463,S464,S465,S466,S467,S468,S469,S470,S471,S472,S473,S474,S475,S476,S477,S478,S479,S480,S481,S482,S483,S484,S485,S486,S487,S488,S489,S490,S491,S492,S493,S494,S495,S496,S497,S498,S499,S500");
					  sb.append(",TradingPortfolio");
					  sb.append("\n");
				

				} else if(extractObject.getExtractType().equals(ExtractType.VAR_SIX_YEAR_PNL) ){ /*position-level*/
					  sb.append("?UniqueID");
					  sb.append(",BaseCCY");
					  sb.append(",").append(createVarColumn("S",1540));
					  sb.append("PositionId");
					  sb.append("\n");
			
				} else if(extractObject.getExtractType().equals(ExtractType.VAR_SIX_YEAR_PNL_PORTFOLIO ) ){
					  sb.append("?UniqueID");
					  sb.append(",BaseCCY");
					  sb.append(",").append(createVarColumn("S",1540));
					  sb.append("TradingPortfolio");
					  sb.append("\n");
					  
				} else if( extractObject.getExtractType().equals(ExtractType.VAR_STRESS_PNL) ){
					  sb.append("?UniqueID");
					  sb.append(",BaseCCY");
					  sb.append(",S1,S2,S3,S4,S5,S6,S7,S8,S9,S10,S11,S12,S13,S14,S15,S16,S17,S18,S19,S20,S21,S22,S23,S24,S25,S26,S27,S28,S29,S30,S31,S32,S33,S34,S35,S36,S37,S38,S39,S40,S41,S42,S43,S44,S45,S46,S47,S48,S49,S50,S51,S52,S53,S54,S55,S56,S57,S58,S59,S60,S61,S62,S63,S64,S65,S66,S67,S68,S69,S70,S71,S72,S73,S74,S75,S76,S77,S78,S79,S80,S81,S82,S83,S84,S85,S86,S87,S88,S89,S90,S91,S92,S93,S94,S95,S96,S97,S98,S99,S100,S101,S102,S103,S104,S105,S106,S107,S108,S109,S110,S111,S112,S113,S114,S115,S116,S117,S118,S119,S120,S121,S122,S123,S124,S125,S126,S127,S128,S129,S130,S131,S132,S133,S134,S135,S136,S137,S138,S139,S140,S141,S142,S143,S144,S145,S146,S147,S148,S149,S150,S151,S152,S153,S154,S155,S156,S157,S158,S159,S160,S161,S162,S163,S164,S165,S166,S167,S168,S169,S170,S171,S172,S173,S174,S175,S176,S177,S178,S179,S180,S181,S182,S183,S184,S185,S186,S187,S188,S189,S190,S191,S192,S193,S194,S195,S196,S197,S198,S199,S200,S201,S202,S203,S204,S205,S206,S207,S208,S209,S210,S211,S212,S213,S214,S215,S216,S217,S218,S219,S220,S221,S222,S223,S224,S225,S226,S227,S228,S229,S230,S231,S232,S233,S234,S235,S236,S237,S238,S239,S240,S241,S242,S243,S244,S245,S246,S247,S248,S249,S250,S251,S252,S253,S254,S255,S256,S257,S258,S259,S260,S261");
					  sb.append(",PositionId");
					  sb.append("\n");
			
				} else if(extractObject.getExtractType().equals(ExtractType.VAR_STRESS_PNL_PORTFOLIO) ){
					  sb.append("?UniqueID");
					  sb.append(",BaseCCY");
					  sb.append(",S1,S2,S3,S4,S5,S6,S7,S8,S9,S10,S11,S12,S13,S14,S15,S16,S17,S18,S19,S20,S21,S22,S23,S24,S25,S26,S27,S28,S29,S30,S31,S32,S33,S34,S35,S36,S37,S38,S39,S40,S41,S42,S43,S44,S45,S46,S47,S48,S49,S50,S51,S52,S53,S54,S55,S56,S57,S58,S59,S60,S61,S62,S63,S64,S65,S66,S67,S68,S69,S70,S71,S72,S73,S74,S75,S76,S77,S78,S79,S80,S81,S82,S83,S84,S85,S86,S87,S88,S89,S90,S91,S92,S93,S94,S95,S96,S97,S98,S99,S100,S101,S102,S103,S104,S105,S106,S107,S108,S109,S110,S111,S112,S113,S114,S115,S116,S117,S118,S119,S120,S121,S122,S123,S124,S125,S126,S127,S128,S129,S130,S131,S132,S133,S134,S135,S136,S137,S138,S139,S140,S141,S142,S143,S144,S145,S146,S147,S148,S149,S150,S151,S152,S153,S154,S155,S156,S157,S158,S159,S160,S161,S162,S163,S164,S165,S166,S167,S168,S169,S170,S171,S172,S173,S174,S175,S176,S177,S178,S179,S180,S181,S182,S183,S184,S185,S186,S187,S188,S189,S190,S191,S192,S193,S194,S195,S196,S197,S198,S199,S200,S201,S202,S203,S204,S205,S206,S207,S208,S209,S210,S211,S212,S213,S214,S215,S216,S217,S218,S219,S220,S221,S222,S223,S224,S225,S226,S227,S228,S229,S230,S231,S232,S233,S234,S235,S236,S237,S238,S239,S240,S241,S242,S243,S244,S245,S246,S247,S248,S249,S250,S251,S252,S253,S254,S255,S256,S257,S258,S259,S260,S261");
					  sb.append(",TradingPortfolio");
					  sb.append("\n");
					
				}else if(extractObject.getExtractType().equals(ExtractType.VAR_STRESS)  ){
					  sb.append("?UniqueID,Date,Var_Type,BaseCCY,VaRConfidence,RiskHierarchyPath,VaRPnL");
					  sb.append("\n");
				} 
				
				int ctr = 0;
					for (String[] row  : extractObject.getRows()) {
						sb.append(row[0]);
						for (int i = 1; i < row.length; i++) {
							sb.append( ANZConstants.COMMA_SEPARATOR  );	
							sb.append(row[i]);
						} 
						sb.append("\n");	
				if (ctr == fileWritePartition) {
					writer.write(sb.toString());
					ctr = 0;
					sb = new StringBuilder(1000);
				}	
					ctr++;
				}
			     
				if(sb!=null && sb.length()>0){
					writer.write(  sb.toString());
				}	
		} catch (IOException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			long endTime = (System.currentTimeMillis() - start);
			return endTime;
		}
	}
	
	 public boolean renameFile(String filePath, String find, String replace){
		    
		    File old = new File(filePath.toUpperCase());
			
		    filePath = filePath.toUpperCase().replace( find,replace );
			
			
			
			
			boolean success =  old.renameTo( new File(filePath) );
	         if(!success){
	         	LOGGER.log(Level.SEVERE,"Unable to rename the file");
	         }else{
	        	 LOGGER.log(Level.INFO,"File was successfully generated:" + filePath);
	         }
			return success;
	 }
	 
	 public BlockingQueue<ExtractObject> getFileDataQueue() {
			return fileDataQueue;
		}

	public void setFileDataQueue(BlockingQueue<ExtractObject> fileDataQueue) {
		this.fileDataQueue = fileDataQueue;
	}
	
	private StringBuilder createVarColumn(String suffix,int size){
		StringBuilder column = new StringBuilder();
		for (int i = 1; i <= size; i++) {
			
			column.append(suffix).append(i).append(",");
		}
		return column;
	}	 

    public static void main(String[] rk){
        try{


        RandomAccessFile file= new RandomAccessFile("c:\\apError.txt","rw");
        long length = file.length();

         file.setLength(length - 1);


        System.out.println("Now File Length is ="+file.length());
        file.close();


        }catch(Exception ex){


    }
}//end of psvm
}
