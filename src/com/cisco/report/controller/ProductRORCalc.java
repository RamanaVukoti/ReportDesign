package com.cisco.report.controller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cisco.capital.capitalpricing.BOMLineType;
import com.cisco.capital.capitalpricing.GroupType;
import com.cisco.capital.capitalpricing.PricingHeaderType;
import com.cisco.capital.capitalpricing.PricingScenarioType;
import com.cisco.capital.capitalpricing.PricingScenarioType.BOM;
import com.cisco.capital.capitalpricing.PricingServiceResponseType;
import com.cisco.capital.dar.capitalpricingmodel.resp.LeaseTermType;
import com.cisco.capital.dar.capitalpricingmodel.resp.QuoteResponse;
import com.cisco.capital.dar.dao.IReportsDAO;
import com.cisco.capital.dar.exceptions.DARException;
import com.cisco.capital.dar.model.AssertInformationVO;
import com.cisco.capital.dar.model.LeaseInformationVO;
import com.cisco.capital.dar.model.PricingModelVO;
import com.cisco.capital.dar.util.GenericUtil;
import com.cisco.capital.dar.wsclient.CapitalPricingServiceClient;
import com.cisco.capital.dar.wsclient.GlobalPricingServiceClient;

public class ProductRORCalc {

	private static CapitalPricingServiceClient client = null;
	private static IReportsDAO reportDAO = null;
	private static GlobalPricingServiceClient globalPricing = null;
	
	
	
	private static ClassPathXmlApplicationContext offlineContext = null; 
	private static Properties props = new Properties();
	private static final Logger LOGGER=LoggerFactory.getLogger(ProductRORCalc.class);
	
	/**up the spring context and get the bean of passed reference
	 * @param beanName
	 * @return
	 */
	public static Object getBeanForOfflineJob(String beanName){
		String[] contextfiles = new String[]{"application-context.xml"};
		if(offlineContext==null){
			offlineContext =new ClassPathXmlApplicationContext(contextfiles);			
		}
		return offlineContext.getBean(beanName);
	}
	public static void main(String[] args) {
		 // Report 4 Web service call (test- developer purpose).. //TODO 
		List<String> leaseNumbers=new ArrayList<String>();
		try {
			props.load(ProductRORCalc.class.getClassLoader().getResourceAsStream("app.properties"));
			reportDAO = (IReportsDAO) getBeanForOfflineJob("reportsDAO");
			client = (CapitalPricingServiceClient) getBeanForOfflineJob("capitalPricingServiceClient");
			globalPricing=(GlobalPricingServiceClient) getBeanForOfflineJob("globalPricingModelServiceClient");
			
			leaseNumbers=reportDAO.getLeaseNumbersForWS();
			
			/* leaseNumbers.add("4000060557");
			 leaseNumbers.add("4000065026");
			 leaseNumbers.add("4000028184");
			 leaseNumbers.add("4000071604");
			 leaseNumbers.add("4000072446");
			*/
					
				//US leases
				//{"4000058507","4000058570","4000044962","4000047077","4000047503"};
					
				//Thailand leases
				//	{"DTREA10334","FHLEO20838","DTREA10317","4000065348","G1BRE30490"};
			
			LOGGER.info("############### ROR data insertion -- job START ###########");
			for(int i=0;i<leaseNumbers.size();i++){
				report4WSCall(leaseNumbers.get(i));	
			}
			offlineContext.close();
			LOGGER.info("############### ROR data insertion -- job END ###########");
		} catch (DARException e) {
			LOGGER.error(e.getMessage(),e);
			
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
		}
	}
	
	/**
	 * @throws DARException 
	 * 
	 */
	private static void report4WSCall(String leaseNumber) throws DARException {
		 LeaseInformationVO leaseVO=null;
			String capitalPricingurl=null;
			String capitalPricinguser=null;
			String capitalPricingpwd=null;
			QuoteResponse quoteResponse=null;
			PricingModelVO pricingModel=null;
			List<LeaseTermType> leaseTerms=null;
			List<LeaseInformationVO> leaseAndExts=null;
			List<AssertInformationVO> assets=null;
			Map<String , Object> map=null;
			List<AssertInformationVO>assets_new=null;		
			List<AssertInformationVO> finalAssets=new ArrayList<AssertInformationVO>();
			PricingServiceResponseType responseType=null;
			String globalPricingurl=null;
			String globalPricinguser=null;
			String globalPricingpwd=null;
			
			try {
				map=reportDAO.getLeaseInformation(leaseNumber);
				//Get lease Details
				leaseAndExts=(List<LeaseInformationVO>)map.get("leases");
				assets=(List<AssertInformationVO>)map.get("assets");
				
				
				globalPricingurl = props.getProperty("product.global.pricing.service.endpointurl");
				globalPricinguser = props.getProperty("product.global.pricing.service.username");
				globalPricingpwd = props.getProperty("product.global.pricing.service.password");
				
				 pricingModel = new PricingModelVO();
				 LeaseInformationVO leaVo=null;
				if(leaseAndExts!=null &&assets!=null && leaseAndExts.size() > 0) {
					responseType = globalPricing.getPricingDetails(	leaseAndExts.get(0), assets, globalPricingurl,	globalPricinguser, globalPricingpwd);
					
				for (int i = 0; i < leaseAndExts.size(); i++) {
					leaVo = leaseAndExts.get(i);
					pricingModel = new PricingModelVO();
					// quoteResponse=client.getPricingDetails(leaseAndExts.get(i),
					// assets, capitalPricingurl, capitalPricinguser,
					// capitalPricingpwd);
					
					
						Double paymentPricingModel = 0.0;
						List<PricingScenarioType> pricingScenarios = responseType.getPricingScenarios();
						PricingScenarioType scenarioType = pricingScenarios.get(0);
						PricingHeaderType headerType = scenarioType.getPricingHeader();//NULL CHeck TODO on headertype
						List<GroupType> groups = null;
						if(headerType!=null){
							groups = headerType.getGroupType();	
						}
						
						if(groups!=null){
							for(GroupType grpType:groups) {
								paymentPricingModel = grpType.getPaymentStream().get(0).getAmount().doubleValue();
							}
						}
						
	
						BOM bom = scenarioType.getBOM();
	
						List<BOMLineType> bomLines = null;
						if(bom!=null){
							bomLines = bom.getBOMLine();	
						}
						
						Double ahwValue = 0.0;
						Double bhwValue = 0.0;
						Double chwValue=0.0;
						Double cvhwValue = 0.0;
						Double dhwValue = 0.0;
						Double cfhwValue=0.0;
						Double eswValue = 0.0;
						Double evswValue = 0.0;
						Double sMaintenanceValue=0.0;
						Double xxvalue=0.0;
						Double rvBrktotal=0.0;

						Double bomAmount=0.0;
						String bomTier=null;
						
						if(bomLines!=null){
						for (BOMLineType bomType : bomLines) {
							
							bomAmount=bomType.getResidualOveriddenAmt().getValue().doubleValue();
							bomTier=bomType.getTier();
							if ("A".equalsIgnoreCase( bomTier=bomType.getTier())) {
								ahwValue += bomAmount;
							} else if ("B".equalsIgnoreCase(bomTier)) {
								bhwValue += bomAmount;
							}else if("C".equalsIgnoreCase(bomTier)) {
								chwValue+=bomAmount;
							}else if("CV".equalsIgnoreCase(bomTier)) {
								cvhwValue+=bomAmount;
							}else if("CF".equalsIgnoreCase(bomTier)) {
								cfhwValue+=bomAmount;
							}else if("DH".equalsIgnoreCase(bomTier)) {
								dhwValue+=bomAmount;
							}else if("E".equalsIgnoreCase(bomTier)) {
								eswValue+=bomAmount;
							}else if("EV".equalsIgnoreCase(bomTier)) {
								evswValue+=bomAmount;
							}else if("S".equalsIgnoreCase(bomTier)) {
								sMaintenanceValue+=bomAmount;
							}else if("X".equalsIgnoreCase(bomTier)) {
								xxvalue+=bomAmount;
							}
						}
						}
						rvBrktotal=ahwValue+bhwValue+chwValue+cvhwValue+cfhwValue+dhwValue+eswValue+evswValue+sMaintenanceValue+xxvalue;
						// WS values setting
						pricingModel.setAHW(ahwValue);
						pricingModel.setBHW(bhwValue);
						pricingModel.setCHW(chwValue);
						pricingModel.setCVHW(cvhwValue);
						pricingModel.setCFHW(cfhwValue);
						pricingModel.setDHW(dhwValue);
						pricingModel.setESW(eswValue);
						pricingModel.setEVSW(evswValue);
						pricingModel.setSMAINTENANCE(sMaintenanceValue);
						pricingModel.setXX(xxvalue);
						pricingModel.setTotal(rvBrktotal);
						pricingModel.setPricingPayment(paymentPricingModel);

					
					leaVo.setAssets(assets);
					// excel operations
					// calculate assets data using formula in excel
					assets_new = dealRVallocationExcelGen(leaVo, pricingModel);
					// adding all Assets for Batch Update
					finalAssets.addAll(assets_new);

				}
					//inserting data for each Lease (one shot  inserting )
					reportDAO.insertRORdata(leaVo,finalAssets);
				}
			}catch(DARException e) {
				LOGGER.error(e.getMessage(),e);	
				}
				//prepare excel
		}
	
	/**
	 * @param pricingModel 
	 * @param leaseVO 
	 * @throws DARException 
	 * 
	 */
	private static List<AssertInformationVO> dealRVallocationExcelGen(LeaseInformationVO leaseVO, PricingModelVO pricingModel) throws DARException {
		InputStream inputStream=null;
		Workbook workbook=null;
		Sheet sheet=null;
		List<AssertInformationVO> assets=null;
		AssertInformationVO assetVO=null;
		
		try {
			inputStream = ProductRORCalc.class.getResourceAsStream("/DealRV_allocationTemplate_NEW.xlsx");
			
			workbook=new XSSFWorkbook(inputStream);
			sheet=workbook.getSheetAt(0);
			
			Row pricingRow=null;
			assets=leaseVO.getAssets();
			
			
			DataFormat dataFormat = workbook.createDataFormat();
			// cell style for Number based cells
			CellStyle numberCell = workbook.createCellStyle();
			numberCell.setDataFormat(dataFormat.getFormat("#,##0.00"));
			numberCell.setAlignment(CellStyle.ALIGN_RIGHT);

			// cell style for Date based cells
			CellStyle dateCell = workbook.createCellStyle();
			dateCell.setDataFormat(dataFormat.getFormat("m/d/yy"));
			dateCell.setAlignment(CellStyle.ALIGN_RIGHT);
			FormulaEvaluator evaluator3=workbook.getCreationHelper().createFormulaEvaluator();
			
			//pricing block
			int pricingRowNum=2;
			Cell pricingPayCell=null;
			pricingRow=sheet.getRow(pricingRowNum);
			pricingPayCell=pricingRow.getCell(3);
			
			
				//pricing data filling
				if(pricingModel!=null) {
					//pushing WS-TYPE 1 resultant to  payment pricing cell-- 
					if(pricingPayCell!=null) {
						pricingPayCell.setCellValue(pricingModel.getPricingPayment());
						pricingPayCell.setCellStyle(numberCell);
					}
					
					// pushing WS-TYPE 2 resultant to  pricing model(RV Breakout) cells-- 
					Row rvRow1=sheet.getRow(2);
					Cell rvBreakOutCell1=rvRow1.getCell(1);
					rvBreakOutCell1.setCellValue(pricingModel.getPGMHW());
					
					Row rvRow2=sheet.getRow(3);
					Cell rvBreakOutCell2=rvRow2.getCell(1);
					rvBreakOutCell2.setCellValue(pricingModel.getAHW());
					rvBreakOutCell2.setCellStyle(numberCell);
					System.out.println("AHW" + pricingModel.getAHW());
					Row rvRow3=sheet.getRow(4);
					Cell rvBreakOutCell3=rvRow3.getCell(1);
					rvBreakOutCell3.setCellValue(pricingModel.getBHW());
					rvBreakOutCell3.setCellStyle(numberCell);
					System.out.println("BHW" + pricingModel.getBHW());
					
					Row rvRow4=sheet.getRow(5);
					Cell rvBreakOutCell4=rvRow4.getCell(1);
					rvBreakOutCell4.setCellValue(pricingModel.getCHW());
					rvBreakOutCell4.setCellStyle(numberCell);
					
					Row rvRow5=sheet.getRow(6);
					Cell rvBreakOutCell5=rvRow5.getCell(1);
					rvBreakOutCell5.setCellValue(pricingModel.getCVHW());
					rvBreakOutCell5.setCellStyle(numberCell);
					
					Row rvRow6=sheet.getRow(7);
					Cell rvBreakOutCell6=rvRow6.getCell(1);
					rvBreakOutCell6.setCellValue(pricingModel.getDHW());
					rvBreakOutCell6.setCellStyle(numberCell);
					
					Row rvRow7=sheet.getRow(8);
					Cell rvBreakOutCell7=rvRow7.getCell(1);
					rvBreakOutCell7.setCellValue(pricingModel.getCFHW());
					rvBreakOutCell7.setCellStyle(numberCell);
					
					Row rvRow8=sheet.getRow(9);
					Cell rvBreakOutCell8=rvRow8.getCell(1);
					rvBreakOutCell8.setCellValue(pricingModel.getESW());
					rvBreakOutCell8.setCellStyle(numberCell);
					
					Row rvRow9=sheet.getRow(10);
					Cell rvBreakOutCell9=rvRow9.getCell(1);
					rvBreakOutCell9.setCellValue(pricingModel.getEVSW());
					rvBreakOutCell9.setCellStyle(numberCell);
					
					Row rvRow10=sheet.getRow(11);
					Cell rvBreakOutCell10=rvRow10.getCell(1);
					rvBreakOutCell10.setCellValue(pricingModel.getSMAINTENANCE());
					rvBreakOutCell10.setCellStyle(numberCell);
					
					Row rvRow11=sheet.getRow(12);
					Cell rvBreakOutCell11=rvRow11.getCell(1);
					rvBreakOutCell11.setCellValue(pricingModel.getXX());
					rvBreakOutCell11.setCellStyle(numberCell);
					
					Row rvRow12=sheet.getRow(13);
					Cell rvBreakOutCell12=rvRow12.getCell(1);
					rvBreakOutCell12.setCellValue(pricingModel.getTotal());
					rvBreakOutCell12.setCellStyle(numberCell);
					//test purpose
					//rvBreakOutCell12.setCellValue(22.0);
					
				
			}
			
				//tier A contract Term
				Row row2=sheet.getRow(2);
				Cell cell = row2.getCell(7);
				cell.setCellValue(leaseVO.getContractTerm());
				cell.setCellStyle(numberCell);
				
				//tier B contract Term
				cell = row2.getCell(8);
				cell.setCellValue(leaseVO.getContractTerm());
				cell.setCellStyle(numberCell);
				
				//Rate Calculation from Model (term)
				Row row10=sheet.getRow(10);
				cell = row10.getCell(7);
				
				cell.setCellValue(leaseVO.getContractTerm());
				cell.setCellStyle(numberCell);
				
				//Rent from EOL form
				cell = row10.getCell(3);
				cell.setCellValue(leaseVO.getMonthlyRent());
				cell.setCellStyle(numberCell);
				
				//RV from EOL form
				Row row13=sheet.getRow(13);
				cell = row13.getCell(3);
				if(cell == null){
					cell = row13.createCell(3);
				}
				if(leaseVO.getRv()!=null)
					cell.setCellValue(leaseVO.getRv());
				
				cell.setCellStyle(numberCell);
				

				Double finAmtTierA=0.0;
				Double finAmtTierB=0.0;
				Double totalFin=0.0;
			//asset data filling 
			int assetsrowNum=20;
			Row asetsFrow =null;
			for (int i=0;i<assets.size();i++) {
				assetVO=assets.get(i);
				cell=null;
				int cellIndex=0;
				asetsFrow=sheet.getRow(assetsrowNum++);
				if(asetsFrow!=null) {
					
					cell=asetsFrow.getCell(cellIndex++);
					if(cell==null) {
						cell=asetsFrow.createCell(cellIndex-1);
					}
					cell.setCellValue(assetVO.getProductId());
					

					cell=asetsFrow.getCell(cellIndex++);
					if(cell==null) {
						cell=asetsFrow.createCell(cellIndex-1);
					}
					cell.setCellValue(assetVO.getListPrice());
					////TEST purpose
					//cell.setCellValue(207.0);
					
					cell=asetsFrow.getCell(cellIndex++);
					if(cell==null) {
						cell=asetsFrow.createCell(cellIndex-1);
					}
					cell.setCellValue(assetVO.getRvTier()!=null?assetVO.getRvTier():"");
					
					cell=asetsFrow.getCell(cellIndex++);
					if(cell==null) {
						cell=asetsFrow.createCell(cellIndex-1);
					}
					cell.setCellValue(assetVO.getProductType());
					
					cell=asetsFrow.getCell(cellIndex++);
					if(cell==null) {
						cell=asetsFrow.createCell(cellIndex-1);
					}
					cell.setCellValue(assetVO.getQuantity());
					
					
					if("A".equalsIgnoreCase(assetVO.getRvTier())) {
							finAmtTierA+=assetVO.getListPrice();
						}
						else if("B".equalsIgnoreCase(assetVO.getRvTier())) {
							finAmtTierB+=assetVO.getListPrice();
						}
					totalFin+=assetVO.getListPrice();
					
					evaluator3.evaluateAll();
				}
				
				
			}
			//financed cells filling
			Row finAmtRow=sheet.getRow(3);
			Cell cellFin=finAmtRow.getCell(7);
			cellFin.setCellValue(finAmtTierA*(-1));
			
			cellFin=finAmtRow.getCell(8);
			cellFin.setCellValue(finAmtTierB*(-1));
			
			Row pvRow=sheet.getRow(12);
			cellFin=pvRow.getCell(7);
			cellFin.setCellValue(totalFin*(-1));
			//mtm term filling
			if(leaseVO!=null) {
				Row mtmRow=sheet.getRow(17);
				Cell mtmtCell=mtmRow.getCell(9);
				if(mtmtCell!=null) {
					mtmtCell.setCellValue(leaseVO.getMtmTerm()!=null?leaseVO.getMtmTerm():0);
				}
				
			}
			
			
			//formula based asset data reading
			int rowIndex=20;
			for(int i=0;i<assets.size();i++) {
			AssertInformationVO	assetVOModified=assets.get(i);
				Row assetsRow=sheet.getRow(rowIndex++);
				int cellIndex=5;

				
				//DB--RESIDUAL_VALUE,, excel Cell--RV from PM

				Cell cell1=assetsRow.getCell(cellIndex++);
				//System.out.println(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell1),cell1)));
				assetVOModified.setRvFromPM(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell1),cell1)));
				
				//DB--**,, excel Cell--RvAllocation
				Cell cell2=assetsRow.getCell(cellIndex++);
				//System.out.println(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell2),cell2)));
				assetVOModified.setRvAllocation(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell2),cell2)));
				
				
				//DB--**,, excel Cell--RentFromPM
				Cell cell3=assetsRow.getCell(cellIndex++);
				//System.out.println(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell3),cell3)));
				assetVOModified.setRentFromPM(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell3),cell3)));
				
				
				//DB--**,, excel Cell--RentAllocation
				Cell cell4=assetsRow.getCell(cellIndex++);
				//System.out.println(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell4),cell4)));
				assetVOModified.setRentAllocation(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell4),cell4)));
				
				
				//DB--MTM_REVENUE,, excel Cell--MTMrevenue
				Cell cell5=assetsRow.getCell(cellIndex++);
				//System.out.println(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell5),cell5)));
				assetVOModified.setMTMrevenue(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell5),cell5)));
				

				//DB--BUYOUT_REVENUE,, excel Cell--buyout revenue
				Cell cell6=assetsRow.getCell(cellIndex++);
				//System.out.println(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell7),cell7)));
				assetVOModified.setBuyoutRevenue(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell6),cell6)));
				
				
				//DB--ROR,, excel Cell--RevenueByPID
				Cell cell8=assetsRow.getCell(cellIndex++);
				//System.out.println(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell8),cell8)));
				assetVOModified.setRevenueByPID(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell8),cell8)));
				
				Cell cellRor=assetsRow.getCell(cellIndex++);
				//System.out.println(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell8),cell8)));
				assetVOModified.setROR(roundCellValue(validateAndReadCell(evaluator3.evaluate(cellRor),cellRor)));
				
				
				assetVOModified.setResidualValuePercentage(0.0);//TODO
				assetVOModified.setProfit(0.0);//TODO
				assetVOModified.setFailrMarketValue(0.0);//TODO
				assetVOModified.setTotalMeotRevenue(0.0);//TODO
				assetVOModified.setMeotRevenuePerOEC(0.0);//TODO
				assetVOModified.setTotalRevenuePerOEC(0.0);//TODO
				assetVOModified.setFailrMarketValue(0.0);//TODO
				
				
				
				/*Row rorRow=sheet.getRow(5);
				//DB--ROR,, excel Cell--ROR
				Cell rorCell=rorRow.getCell(7);
				assetVOModified.setROR(roundCellValue(validateAndReadCell(evaluator3.evaluate(rorCell),rorCell)));*/
				
				//DB--cost qty ,,Excel Cost qty
				Cell cell9=assetsRow.getCell(14);
				assetVOModified.setCostQty(getLongValue(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell9), cell9))));
				
				//DB--return qty ,,Excel return qty
				Cell cell10=assetsRow.getCell(15);
				assetVOModified.setReturnQuantity(getLongValue(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell10), cell10))));
				
				//DB--rv qty ,,Excel rv qty
				Cell cell11=assetsRow.getCell(16);
				assetVOModified.setRvQty(getLongValue(roundCellValue(validateAndReadCell(evaluator3.evaluate(cell11), cell11))));
				
				
				
				
				
			}
			
		
			
			
			/*response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.addHeader("content-disposition",
					"attachment; filename=" + "PAAM_Report_4_test.xlsx");
			outputStream=response.getOutputStream();
			workbook.write(outputStream);
			outputStream.flush();
			outputStream.close();*/
			String s = Long.toString(System.currentTimeMillis());
			FileOutputStream fos = new FileOutputStream("C:/rv/abc"+s+".xlsx");
			workbook.write(fos);
			fos.flush();fos.close();
			
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
			throw new DARException(e.getMessage(),e);
			
		}
		return assets;
		
		
		
	}
	
	/**
	 * @param roundCellValue
	 * @return
	 */
	private static Long getLongValue(Double roundCellValue) {
		Long value=0l;
		if(roundCellValue!=null) {
			value=roundCellValue.longValue();
		}
		
		return value;
	}
	public static String validateAndReadCell(CellValue  cellTemp,Cell cell) {
		String cellValue = null;
		if (cellTemp != null) {
		
		
			int celType = cellTemp.getCellType();
			// if string
			if (celType == 1) {
				cellValue = cellTemp.getStringValue();
				// if the cell is Numeric & Date
			} else if (celType == 0) {	
				cellValue=cellTemp.getNumberValue()+"";
				// formula based cell
			} else if (celType == 2) {
				
						cellValue = new Double(cell.getNumericCellValue()).longValue() + "";
				
				// if the cell is Error cell
			} else if (celType == 5) {
				cellValue = null;
				// boolean cell
			} else if (celType == 4) {
				if (cellTemp.getBooleanValue()) {
					cellValue = "TRUE";
				} else {
					cellValue = "FALSE";
				}
			}
		}

		return cellValue;
	}
	
	/**
	 * @param doubleValuefromCell
	 * @return
	 */
	private static Double roundCellValue(String str) {
		Double value=null;
		if(GenericUtil.parseDouble(str)!=null) {
			value=Math.round(GenericUtil.parseDouble(str)*100.0)/100.0;
		}
		
		
		return value;
	}

/*	public static Double getDoubleValuefromCell(int  celType,Cell cell) {
		Double value = null;
			if (celType == 0) {
				if (!DateUtil.isCellDateFormatted(cell)) {
					value = new Double(cell.getNumericCellValue()).doubleValue();
				} 
			} else if (celType == 2) {
				if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC) {
					if (!DateUtil.isCellDateFormatted(cell)) {
						value = new Double(cell.getNumericCellValue()).doubleValue();
					} 
				}
			}
			else if (celType == 1) {
				value=-1.0;
			}

		return value;
	}*/

}
