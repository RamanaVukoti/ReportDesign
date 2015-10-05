/**
 * 
 */
package com.cisco.report.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author vvukoti
 *
 */
@Controller
public class ReportsController {
	
	@RequestMapping("/home.htm")
	public String homepage(){
		
		System.out.println("in home page loading");
		
		return "home";
	}
	
	
	@RequestMapping("/index.htm")
	public String phomepage(){
		
		System.out.println("in home page loading");
		
		return "project2";
	}
	
	

	@RequestMapping("/reportDesign.htm")
	public String searchPage(){
		
		System.out.println("in Report Design page loading");
		
		return "reportDesign";
	}
	
	@RequestMapping("/reportDesign2.htm")
	public String searchPage2(){
		
		System.out.println("in Report Design page loading");
		
		return "reportDesign2";
	}

	
	
	@RequestMapping("/login.htm")
	public String login(){
		
		System.out.println("in login page loading");
		
		return "login";
	}

	@RequestMapping("/saveFieldDetails.htm")
	public String saveFields(HttpServletRequest request){
		
		System.out.println(request.getParameter("reportName"));
		System.out.println(request.getParameter("visibility"));
		System.out.println(request.getParameter("finalFields"));
		System.out.println(request.getParameter("finalHeaders"));
		
		return "login";
	}

	
	@RequestMapping("/upload.htm")
	public String upload(){
		
		System.out.println("in upload page loading");
		
		return "home";
	}


}
