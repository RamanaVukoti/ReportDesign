/**
 * 
 */
package com.cisco.report.controller;

/**
 * @author vvukoti
 *
 */
public class ThreadClass extends Thread {

	public void run() {
		try {
			for (int i = 0; i < 10; i++) {
				Thread.sleep(0);
				System.out.println("thread - "+i );
			}
			
		} catch (Exception e) {
			//LOGGER.error(e.getMessage(),e);
			e.printStackTrace();
			
		}
	}
	
	public void test() {
		try {
			for (int i = 0; i < 10; i++) {
				Thread.sleep(0);
				System.out.println("############################################");
			}
			
		} catch (Exception e) {
			//LOGGER.error(e.getMessage(),e);
			e.printStackTrace();
			
		}
	}
}
