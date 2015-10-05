/**
 * 
 */
package com.cisco.report.controller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFColor.WHITE;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;

import com.cisco.capital.dar.jobs.EOLEXTProcessing;
import com.cisco.capital.dar.util.DBUtil;
import com.cisco.capital.dar.util.GenericUtil;



/**
 * @author vvukoti
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main1(String[] args) {
		
	/*	TimeZone timeZone=TimeZone.getDefault();
		System.out.println(timeZone.getDisplayName());
		System.out.println(timeZone.getDisplayName(Locale.getDefault()));
		System.out.println(TimeZone.getTimeZone(timeZone.getDisplayName()).getID());
		*/
		TimeZone timeZone=TimeZone.getDefault();
		System.out.println(timeZone.getDisplayName(false,0,Locale.getDefault(Locale.Category.DISPLAY)));

		
		DateFormat dateFormat=new SimpleDateFormat("dd_MM_yyyy_HH_mm_zzz");
		dateFormat.setTimeZone(timeZone);
		String date=dateFormat.format(new Date());
		System.out.println(date);
		
	/*	
		Calendar cal = Calendar.getInstance();
		long milliDiff = cal.get(Calendar.ZONE_OFFSET);
		// Got local offset, now loop through available timezone id(s).
		String [] ids = TimeZone.getAvailableIDs();
		String name = null;
		for (String id : ids) {
		  TimeZone tz = TimeZone.getTimeZone(id);
		  if (tz.getRawOffset() == milliDiff) {
		    // Found a match.
		    name = id;
		    break;
		  }
		}
		System.out.println(name);*/
		

	}
	
	
	public static void main2(String[] args2) {
		 
		String regexPattern="^Hi";
		String content="Hi Ramana";
		Matcher matcher=null;
		Pattern pattern=null;
		boolean bb=false;
		try {
			pattern=Pattern.compile(regexPattern,Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(content);
			bb=matcher.matches();
			
			//compile example
			// bb=Pattern.matches(regexPattern, content);
			
			
			//start with
			bb=Pattern.matches(regexPattern, content);
			
			System.out.println(bb);
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}

	

	public static void main3(String[] args2) {
		 String str="122883 ";
		 Long val= null;
			if(str!=null && !"".equalsIgnoreCase(str)){
				try{
					val=Long.parseLong(str.trim());	
				}catch(Exception e){
					//val=Long.parseLong("-1");
					
				}
				
			} 
			System.out.println(val);
		
	}
	public static void main(String[] args2) {
		
ThreadClass threadClass=new ThreadClass();

threadClass.test();
threadClass.start();
threadClass.test();
//threadClass.run();
	}

	public static void main6(String[] args2) {
		BigDecimal value = new BigDecimal(12345);
		Locale.setDefault(Locale.US);
		System.out.printf("Default locale: %s\n", Locale.getDefault()
				.getDisplayName());
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		String formattedCurrency = nf.format(value);
		System.out.printf("%s\n", formattedCurrency);
		nf.setCurrency(Currency.getInstance(Locale.US));
		formattedCurrency = nf.format(value);
		System.out.printf("%s\n\n", formattedCurrency);
	}
	
	
	public static void main5(String[] args) {
		
		
			String value="123456";
			NumberFormat nf = NumberFormat.getCurrencyInstance();
		    String formattedCurrency = null;
		    nf.setCurrency(Currency.getInstance(Locale.US));
		    formattedCurrency = nf.format(value);
		    System.out.printf("%s\n\n", formattedCurrency);
	}
	
	public static void main4(String[] args) {
		String str="123456.123";
		Double value=0.0;
		Double d=Double.parseDouble(str);
		System.out.println(d.longValue());
		
		
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer Name','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Lease #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Commencement Date','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Original Maturity Date','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Original Lease Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Effective Date','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Marketing Program','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Currency Code','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Product ID','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Total Quantity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Total Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Total Residual Value','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Residual Value %','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Asset Type','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Tier','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Business Unit','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Product Family','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Product Group','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Technology','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Total MEOT Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Total MEOT Revenue %','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Net Total MEOT Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'MEOT ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Total Buyout Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Total MTM Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Total Fixed Extension Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Total % Purchased','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'% Purchased Prior to Maturity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'% Purchased At Maturity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'% Purchased After Maturity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'% Purchased After MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'% Purchased After Fix Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Total % Returned','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'% Returned Prior to Maturity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'% Returned At Maturity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'% Returned After Maturity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'% Returned After MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'% Returned After Fix Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'% Extended At or After Maturity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'% MTM At Maturity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'% Fixed Extension At Maturity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'% extended After MTM or Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Combined - Qty','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Combined - Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Combined - RV','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Combined - MTM Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Combined - Fixed Ext Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Combined - Buyout Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Combined - MEOT Net Rev','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Combined - ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Combined - Wtd MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Combined - Wtd Fixed Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Combined - Wtd Months MTM and Fixed Ext Combined','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Combined - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Combined - Wtd Months Original Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Combined - Wtd Months Total Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Purchased - Qty','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Purchased - Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Purchased - RV','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Purchased - MTM Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Purchased - Fixed Ext Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Purchased - Buyout Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Purchased - MEOT Net Rev','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Purchased - ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Purchased - Wtd MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Purchased - Wtd Fixed Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Purchased - Wtd Months MTM and Fixed Ext Combined','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Purchased - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Purchased - Wtd Months Original Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Purchased - Wtd Months Total Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Returned - Qty','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Returned - Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Returned - RV','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Returned - MTM Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Returned - Fixed Ext Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Returned - Buyout Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Returned - MEOT Net Rev','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Returned - ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Returned - Wtd MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Returned - Wtd Fixed Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Returned - Wtd Months MTM and Fixed Ext Combined','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Returned - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Returned - Wtd Months Original Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Returned - Wtd Months Total Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Extend - Qty','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Extend - Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Extend - RV','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Extend - MTM Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Extend - Fixed Ext Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Extend - Buyout Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Extend - MEOT Net Rev','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Extend - ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Extend - Wtd MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Extend - Wtd Fixed Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Extend - Wtd Months MTM and Fixed Ext Combined','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Extend - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Extend - Wtd Months Original Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Extend - Wtd Months Total Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Purchase - Qty','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Purchase - Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Purchase - RV','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Purchase - MTM Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Purchase - Fixed Ext Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Purchase - Buyout Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Purchase - MEOT Net Rev','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Purchase - ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Purchase - Wtd MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Purchase - Wtd Fixed Ext	','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Purchase - Wtd Months MTM and Fixed Ext Combined','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Purchase - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Purchase - Wtd Months Original Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Purchase - Wtd Months Total Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Purchase - Qty','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Purchase - Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Purchase - RV','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Purchase - MTM Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Purchase - Fixed Ext Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Purchase - Buyout Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Purchase - MEOT Net Rev','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Purchase - ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Purchase - Wtd MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Purchase - Wtd Fixed Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Purchase - Wtd Months MTM and Fixed Ext Combined','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Purchase - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Purchase - Wtd Months Original Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Purchase - Wtd Months Total Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Purchase - Qty','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Purchase - Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Purchase - RV','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Purchase - MTM Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Purchase - Fixed Ext Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Purchase - Buyout Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Purchase - MEOT Net Rev','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Purchase - ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Purchase - Wtd MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Purchase - Wtd Fixed Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Purchase - Wtd Months MTM and Fixed Ext Combined','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Purchase - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Purchase - Wtd Months Original Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Purchase - Wtd Months Total Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Return - Qty','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Return - Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Return - RV','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Return - MTM Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Return - Fixed Ext Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Return - Buyout Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Return - MEOT Net Rev','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Return - ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Return - Wtd MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Return - Wtd Fixed Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Return - Wtd Months MTM and Fixed Ext Combined','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Return - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Return - Wtd Months Original Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Early Return - Wtd Months Total Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Return - Qty','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Return - Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Return - RV','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Return - MTM Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Return - Fixed Ext Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Return - Buyout Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Return - MEOT Net Rev','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Return - ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Return - Wtd MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Return - Wtd Fixed Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Return - Wtd Months MTM and Fixed Ext Combined','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Return - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Return - Wtd Months Original Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Maturity Return - Wtd Months Total Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Return - Qty','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Return - Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Return - RV','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Return - MTM Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Return - Fixed Ext Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Return - Buyout Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Return - MEOT Net Rev','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Return - ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Return - Wtd MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Return - Wtd Fixed Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Return - Wtd Months MTM and Fixed Ext Combined','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Return - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Return - Wtd Months Original Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Extension Return - Wtd Months Total Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All MTM - Qty,'display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All MTM - Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All MTM - RV','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All MTM - MTM Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All MTM - Fixed Ext Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All MTM - Buyout Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All MTM - MEOT Net Rev','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All MTM - ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All MTM - Wtd MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All MTM - Wtd Fixed Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All MTM - Wtd Months MTM and Fixed Ext Combined','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All MTM - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All MTM - Wtd Months Original Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All MTM - Wtd Months Total Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Fixed Extension - Qty','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Fixed Extension - Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Fixed Extension - RV','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Fixed Extension - MTM Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Fixed Extension - Fixed Ext Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Fixed Extension - Buyout Revenue,'display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Fixed Extension - MEOT Net Rev','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Fixed Extension - ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Fixed Extension - Wtd MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Fixed Extension - Wtd Fixed Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Fixed Extension - Wtd Months MTM and Fixed Ext Combined','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');                                                                                                                        
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Fixed Extension - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Fixed Extension - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Fixed Extension - Wtd Months Total Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Early - Qty','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Early - Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Early - RV','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Early - MTM Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Early - Fixed Ext Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Early - Buyout Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Early - MEOT Net Rev','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Early - ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Early - Wtd MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Early - Wtd Fixed Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Early - Wtd Months MTM and Fixed Ext Combined','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Early - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Early - Wtd Months Original Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Early - Wtd Months Total Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Maturity - Qty','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Maturity - Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Maturity - RV','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Maturity - MTM Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Maturity - Fixed Ext Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Maturity - Buyout Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Maturity - MEOT Net Rev','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Maturity - ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Maturity - Wtd MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Maturity - Wtd Fixed Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Maturity - Wtd Months MTM and Fixed Ext Combined','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Maturity - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Maturity - Wtd Months Original Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All Maturity - Wtd Months Total Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All After Maturity - Qty','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All After Maturity - Financed Amount','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All After Maturity - RV','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All After Maturity - MTM Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All After Maturity - Fixed Ext Rent','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All After Maturity - Buyout Revenue','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All After Maturity - MEOT Net Rev','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All After Maturity - ROR','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All After Maturity - Wtd MTM','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All After Maturity - Wtd Fixed Ext','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All After Maturity - Wtd Months MTM and Fixed Ext Combined','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All After Maturity - Wtd Month Early','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All After Maturity - Wtd Months Original Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'All After Maturity - Wtd Months Total Term','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Country','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Region','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Theatre','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Market Segment','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Sales Territory','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'FY Maturity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'FQ Maturity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'FM Maturity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'CY Maturity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'CQ Maturity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'CM Maturity','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'FY closeout','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'FQ Closeout','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'FM Close out','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'CY closeout','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'CQ closeout','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'CM closeout','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Final Lease Closed Date','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');
		insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'Customer #','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,2,'N');

	}
}







Workbook workbook=null;
Sheet sheet=null;
try {
 workbook=new XSSFWorkbook(GenericUtil.class.getResourceAsStream("/Copy of RV Uplift Request  PIC-IC - Template.xlsx"));
sheet=workbook.getSheetAt(4);
Row row=sheet.getRow(4);
Row row2=sheet.getRow(4);
String str="";
int k=1;
PreparedStatement preparedStatement=null;
Connection connection=null;
String sql="insert into PAAM_REPORT_PANEL_HEADERS (PANEL_ID,REPORT_ID,HEADER_NAME,STYLE,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4,ATTRIBUTE5,CREATED_BY,CREATED_DATE,UPDATED_BY,UPDATED_DATE,DISPLAY_SEQUENCE,DISPLAY_HEADER)  values(9,4,?,'display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,?,'Y')";
EOLEXTProcessing	process2 = new EOLEXTProcessing();
JdbcTemplate jdbcTemplate=(JdbcTemplate) process2.getBeanForOfflineJob("jdbcTemplate");
try {
	 connection=jdbcTemplate.getDataSource().getConnection();
	 preparedStatement=connection.prepareStatement(sql);
	for(int i=2;i<row.getLastCellNum();i++) {
		if(row !=null && row.getCell(i)!=null && row.getCell(i).getStringCellValue()!=null) {
			Cell cell=row2.getCell(i);
			if(cell==null || (cell!=null && cell.getStringCellValue()!=null && !"will be $0 for this behavior".equalsIgnoreCase(cell.getStringCellValue().trim()))) {
			//str+="insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'"+row.getCell(i).getStringCellValue().trim()+"','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,"+(k++)+",'Y');"+"\n";
		//	str+="insert into PAAM_REPORT_PANEL_HEADERS (PANEL_ID,REPORT_ID,HEADER_NAME,STYLE,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4,ATTRIBUTE5,CREATED_BY,CREATED_DATE,UPDATED_BY,UPDATED_DATE,DISPLAY_SEQUENCE,DISPLAY_HEADER)  values(9,4,?,'display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,?,'Y');"+"\n";
			preparedStatement.setString(1, row.getCell(i).getStringCellValue().trim());
			preparedStatement.setLong(2, k++);
			preparedStatement.addBatch();
			if(k==10) {
				break;
			}
				
			}
		}
	}
	preparedStatement.executeBatch();
} catch (SQLException e) {
	LOGGER.error(e.getMessage(),e);
	
}finally {
	DBUtil.closeDBResources(connection, null, null);
}

System.out.println(str);













public static void main(String[] args) {
	Workbook workbook=null;
	Sheet sheet=null;
try {
	 workbook=new XSSFWorkbook(GenericUtil.class.getResourceAsStream("/Copy of RV Uplift Request  PIC-IC - Template.xlsx"));
	sheet=workbook.getSheetAt(4);
	Row row=sheet.getRow(4);
	Row row2=sheet.getRow(5);
	String str="";
	int k=1;
	for(int i=2;i<row.getLastCellNum();i++) {
		if(row !=null && row.getCell(i)!=null && row.getCell(i).getStringCellValue()!=null) {
			Cell cell=row2.getCell(i);
			
			if(cell==null || (cell!=null && cell.getStringCellValue()!=null && !"will be $0 for this behavior".equalsIgnoreCase(cell.getStringCellValue().trim()))) {
			//	System.out.println(cell.getStringCellValue());
				str+="insert into PAAM_REPORT_PANEL_HEADERS values(12,3,'"+row.getCell(i).getStringCellValue().trim()+"','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,"+(k++)+",'Y');"+"\n";
				//str+="insert into PAAM_REPORT_PANEL_HEADERS (PANEL_ID,REPORT_ID,HEADER_NAME,STYLE,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4,ATTRIBUTE5,CREATED_BY,CREATED_DATE,UPDATED_BY,UPDATED_DATE,DISPLAY_SEQUENCE,DISPLAY_HEADER)  values(9,4,'"+row.getCell(i).getStringCellValue().trim()+"','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,"+(k++)+",'Y');"+"\n";
				//str=row.getCell(i).getStringCellValue().trim()+""
				//str value=""
						
		//		str="update PAAM_REPORT_PANEL_HEADERS set ATTRIBUTE5='"+value+"' where report_id=4 and panel_id=9 and  DISPLAY_SEQUENCE='"+(k++)+"'
				
			}
			
			//str+="insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'"+row.getCell(i).getStringCellValue().trim()+"','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,"+(k++)+",'Y');"+"\n";
			//str+="insert into PAAM_REPORT_PANEL_HEADERS (PANEL_ID,REPORT_ID,HEADER_NAME,STYLE,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4,ATTRIBUTE5,CREATED_BY,CREATED_DATE,UPDATED_BY,UPDATED_DATE,DISPLAY_SEQUENCE,DISPLAY_HEADER)  values(9,4,'"+row.getCell(i).getStringCellValue().trim()+"','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,"+(k++)+",'Y');"+"\n";
		}
	}
	System.out.println(str);
} catch (IOException e) {
	//logger.error(e.getMessage(),e);
	
}


public static void main(String[] args) {
	Workbook workbook=null;
	Sheet sheet=null;
try {
	 workbook=new XSSFWorkbook(GenericUtil.class.getResourceAsStream("/Copy of RV Uplift Request  PIC-IC - Template.xlsx"));
	sheet=workbook.getSheetAt(3);
	Row row=sheet.getRow(1);
	Row row2=sheet.getRow(5);
	
	// CellStyle headerStyle  =workbook.createCellStyle();
	 
	 
	 
	  short s=HSSFColor.OLIVE_GREEN.index;
	  XSSFCellStyle headerStyle2=null;
	  
	  String str2="";
	
	String str="";
	int k=1;
	String s2=null;
	XSSFCellStyle headerStyle =null;
	for(int i=1;i<row.getLastCellNum();i++) {
		if(row !=null && row.getCell(i)!=null && row.getCell(i).getStringCellValue()!=null) {
			Cell cell=row2.getCell(i);
			
			//	System.out.println(cell.getStringCellValue());
				//str+="insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'"+row.getCell(i).getStringCellValue().trim()+"','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,"+(k++)+",'Y');"+"\n";
				//str+="insert into PAAM_REPORT_PANEL_HEADERS (PANEL_ID,REPORT_ID,HEADER_NAME,STYLE,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4,ATTRIBUTE5,CREATED_BY,CREATED_DATE,UPDATED_BY,UPDATED_DATE,DISPLAY_SEQUENCE,DISPLAY_HEADER)  values(9,4,'"+row.getCell(i).getStringCellValue().trim()+"','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,"+(k++)+",'Y');"+"\n";
				str=row.getCell(i).getStringCellValue().trim()+"";
				
				headerStyle2=(XSSFCellStyle)row.getCell(i).getCellStyle();
			//	
				//System.out.println("-----------------strt");
				//System.out.println(headerStyle2.getFillForegroundXSSFColor().getRgb()[0]<0?headerStyle2.getFillForegroundXSSFColor().getRgb()[0]+256:headerStyle2.getFillForegroundXSSFColor().getRgb()[0]);
				//System.out.println(headerStyle2.getFillForegroundXSSFColor().getRgb()[1]<0?headerStyle2.getFillForegroundXSSFColor().getRgb()[1]+256:headerStyle2.getFillForegroundXSSFColor().getRgb()[2]);
				//System.out.println(headerStyle2.getFillForegroundXSSFColor().getRgb()[2]<0?headerStyle2.getFillForegroundXSSFColor().getRgb()[2]+256:headerStyle2.getFillForegroundXSSFColor().getRgb()[2]);
				if(headerStyle2!=null && headerStyle2.getFillForegroundXSSFColor()!=null ) {
					
					short r=(short)(headerStyle2.getFillForegroundXSSFColor().getRgb()[0]<0?headerStyle2.getFillForegroundXSSFColor().getRgb()[0]+256:headerStyle2.getFillForegroundXSSFColor().getRgb()[0]);
					short g=(short)(headerStyle2.getFillForegroundXSSFColor().getRgb()[1]<0?headerStyle2.getFillForegroundXSSFColor().getRgb()[1]+256:headerStyle2.getFillForegroundXSSFColor().getRgb()[1]);
					short b=(short)(headerStyle2.getFillForegroundXSSFColor().getRgb()[2]<0?headerStyle2.getFillForegroundXSSFColor().getRgb()[2]+256:headerStyle2.getFillForegroundXSSFColor().getRgb()[2]);
				//	String str2=""+(headerStyle2.getFillForegroundXSSFColor().getRgb()[0]+256)+","+(headerStyle2.getFillForegroundXSSFColor().getRgb()[1]+256)+","+(headerStyle2.getFillForegroundXSSFColor().getRgb()[2]+256);
					
					  //  headerStyle.setLocked(true); 
					headerStyle = ((XSSFWorkbook)workbook).createCellStyle();
					
					String value="bg-color:("+r+","+g+","+b+")";
					str2+="update PAAM_REPORT_PANEL_HEADERS set ATTRIBUTE5='"+value+"' where report_id=4 and panel_id=12 and  DISPLAY_SEQUENCE='"+(k++)+"'"+";"+"\n";
					  
					  
					
				}
				  
				// get the color which most closely matches the color you want to use
				  //  headerStyle.setFillForegroundColor((short)s);
				    headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
				    Font font = workbook.createFont();
				    font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
				    font.setColor(WHITE.index);
				    headerStyle.setFont(font);
				    headerStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
				
				headerStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(r,g,b)));
				
				//System.out.println(str2);
				//System.out.println("-----------------end");
				
				
				//System.out.println(row.getCell(i).getCellStyle().);
			//	System.out.println(row.getCell(i).getCellStyle().getFillForegroundColorColor());
				//headerStyle.setFillForegroundColor(headerStyle2.getFillForegroundXSSFColor());
				
			cell.setCellStyle(headerStyle);
			//System.out.println(cell.getCellStyle().getIndex());
			
			 s2= Long.toString(System.currentTimeMillis());
			//str+="insert into PAAM_REPORT_PANEL_HEADERS values(9,4,'"+row.getCell(i).getStringCellValue().trim()+"','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,"+(k++)+",'Y');"+"\n";
			//str+="insert into PAAM_REPORT_PANEL_HEADERS (PANEL_ID,REPORT_ID,HEADER_NAME,STYLE,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4,ATTRIBUTE5,CREATED_BY,CREATED_DATE,UPDATED_BY,UPDATED_DATE,DISPLAY_SEQUENCE,DISPLAY_HEADER)  values(9,4,'"+row.getCell(i).getStringCellValue().trim()+"','display','','','','','','SYSTEM',SYSDATE,'SYSTEM',SYSDATE,"+(k++)+",'Y');"+"\n";
		}
	}
	System.out.println(str2);
	
	FileOutputStream fileOutputStream=new FileOutputStream("C:/file3"+s2+".xlsx");
	workbook.write(fileOutputStream);;
	
} catch (IOException e) {
	//logger.error(e.getMessage(),e);
		
}

}
}

public static void main(String[] args) {
	String sql="SELECT lease.customer_name,       lease.xos_number,       lease.LEASE_NUMBER,       TO_CHAR (lease.contract_comm_date, 'dd-Mon-yyyy') contract_comm_date,       TO_CHAR (lease.contract_end_date, 'dd-Mon-yyyy') contract_end_date,       lease.contract_term,       (SELECT PRIMARY_MARKETING_PROGRAM          FROM xxclffnd.csc_item_master         WHERE item_name = 'CAB-ACU=' AND ROWNUM = 1)          AS MARKETING_PROGRAM,       lease.currency_cd,       PRODUCT_ID,       TOTAL_QUANTITY,       TOTAL_FIN_AMT,       TOTAL_RESIDUAL_VALUE,       RESIDUAL_VALUE_PC,       NULL Standard_rv,       NULL AS STANDARD_RV_PC,       ASSET_TYPE,       (SELECT RV_TIER          FROM paam_item_master_mv         WHERE item_name = a.PRODUCT_ID AND ROWNUM = 1)          TIER,       business_unit,       PRODUCT_FAMILY,       product_group,       technology,       lease.country,       lease.region,       lease.theater,       LEASE.MARKET_SEGMENT,       NULL AS SALES_TERITORY,       (SELECT FISCAL_YEAR_NUMBER          FROM XXCLFFND.CSC_FISCAL_MONTHS_DIM         WHERE LEASE.CONTRACT_END_DATE BETWEEN FISCAL_MONTH_START_DATE                                           AND FISCAL_MONTH_END_DATE)          FY_MATURITY,       (SELECT FISCAL_QUARTER_CODE          FROM XXCLFFND.CSC_FISCAL_MONTHS_DIM         WHERE LEASE.CONTRACT_END_DATE BETWEEN FISCAL_MONTH_START_DATE                                           AND FISCAL_MONTH_END_DATE)          FQ_MATURITY,       (SELECT FISCAL_MONTH_NAME          FROM XXCLFFND.CSC_FISCAL_MONTHS_DIM         WHERE LEASE.CONTRACT_END_DATE BETWEEN FISCAL_MONTH_START_DATE                                           AND FISCAL_MONTH_END_DATE)          FM_MATURITY,       TO_CHAR (LEASE.CONTRACT_END_DATE, 'YYYY') CY_MATURITY,       CASE          WHEN (UPPER (TO_CHAR (LEASE.CONTRACT_END_DATE, 'Mon')) IN ('JAN',                                                                     'FEB',                                                                     'MAR'))          THEN             'Q1'          WHEN (UPPER (TO_CHAR (LEASE.CONTRACT_END_DATE, 'Mon')) IN ('APR',                                                                     'MAY',                                                                     'JUN'))          THEN             'Q2'          WHEN (UPPER (TO_CHAR (LEASE.CONTRACT_END_DATE, 'Mon')) IN ('JUL',                                                                     'AUG',                                                                     'SEP'))          THEN             'Q3'          WHEN (UPPER (TO_CHAR (LEASE.CONTRACT_END_DATE, 'Mon')) IN ('OCT',                                                                     'NOV',                                                                     'DEC'))          THEN             'Q4'       END          AS CQ_MATURITY,       TO_CHAR (LEASE.CONTRACT_END_DATE, 'Mon') CM_MATURITY,       (SELECT FISCAL_YEAR_NUMBER          FROM XXCLFFND.CSC_FISCAL_MONTHS_DIM         WHERE LEASE.updated_date BETWEEN FISCAL_MONTH_START_DATE                                      AND FISCAL_MONTH_END_DATE)          FY_CLOSEOUT,       (SELECT FISCAL_QUARTER_CODE          FROM XXCLFFND.CSC_FISCAL_MONTHS_DIM         WHERE LEASE.updated_date BETWEEN FISCAL_MONTH_START_DATE                                      AND FISCAL_MONTH_END_DATE)          FQ_CLOSEOUT,       (SELECT FISCAL_MONTH_NAME         FROM XXCLFFND.CSC_FISCAL_MONTHS_DIM       WHERE LEASE.updated_date BETWEEN FISCAL_MONTH_START_DATE                                      AND FISCAL_MONTH_END_DATE)          FM_CLOSEOUT,       TO_CHAR (LEASE.updated_date, 'YYYY') CY_CLOSEOUT,       CASE          WHEN (UPPER (TO_CHAR (LEASE.updated_date, 'Mon')) IN ('JAN',                                                                'FEB',                                                                'MAR'))          THEN             'Q1'          WHEN (UPPER (TO_CHAR (LEASE.updated_date, 'Mon')) IN ('APR',                                                                'MAY',                                                                'JUN'))          THEN             'Q2'          WHEN (UPPER (TO_CHAR (LEASE.updated_date, 'Mon')) IN ('JUL',                                                                'AUG',                                                                'SEP'))          THEN             'Q3'          WHEN (UPPER (TO_CHAR (LEASE.updated_date, 'Mon')) IN ('OCT',                                                                'NOV',                                                                'DEC'))          THEN             'Q4'       END          AS CQ_CLOSEOUT,       TO_CHAR (LEASE.updated_date, 'Mon') CM_CLOSEOUT,       TO_CHAR (LEASE.CONTRACT_END_DATE, 'dd-Mon-yyyy')          FINAL_LEASE_CLOSE_DATE,       NULL AS DATE_DISPLAY,       TOTAL_MEOT_REVENUE,       ROUND (TOTAL_MEOT_REVENUE_PC, 2) TOTAL_MEOT_REVENUE_PC,       NET_TOTAL_MEOT_REVENUE,       MEOT_ROR,       TOTAL_BUYOUT_REVENUE,       TOTAL_MTM_REVENUE,       TOTAL_FIXED_EXT_REVENUE  FROM paam_rv_report_data_req a, paam_lease_info lease WHERE lease.lease_number = a.lease_number AND a.RV_BATCH_ID = '1396'";
	EOLEXTProcessing process = null;
	PreparedStatement preparedStatement=null;
	ResultSetMetaData resultSetMetaData=null;
	process = new EOLEXTProcessing();
	Connection connection=null;
	JdbcTemplate jdbcTemplate=null;
	List<String> headers=new ArrayList<String>();
	jdbcTemplate=(JdbcTemplate)process.getBeanForOfflineJob("jdbcTemplate");
	try {
		connection=jdbcTemplate.getDataSource().getConnection();
		preparedStatement=connection.prepareStatement(sql);
		preparedStatement.executeQuery();
		resultSetMetaData=preparedStatement.getMetaData();
		 for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
			/* ReportHeaders reportHeaders=new ReportHeaders();
			 reportHeaders.setHeaderName(resultSetMetaData.getColumnLabel(i));*/
			 headers.add(resultSetMetaData.getColumnLabel(i));
			}
		
	} catch (SQLException e) {
		//logger.error(e.getMessage(),e);
		
	}
	
	
	
	String str="";
	int k=1;
	for(String sttt:headers) {
		if(sttt!=null) {
			
			str+="update PAAM_REPORT_PANEL_HEADERS set ATTRIBUTE1='"+sttt.trim()+"' where report_id=7 and panel_id=17 and DISPLAY_SEQUENCE ="+(k++)+";\n";
			
		}
	}
	System.out.println(str);
}


