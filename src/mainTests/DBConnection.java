/**
 * 
 */
package mainTests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author vvukoti
 *
 */
public  class DBConnection {
	
	/*public static void main(String[] args) {*/
	public static Connection getConnection() throws SQLException {
		Connection connection=null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			
														//host[:port][/service_name]
			connection=DriverManager.getConnection("jdbc:oracle:thin:@//localhost:1522/oracledb","HR","Newp@ssw0rd");
			if(connection!=null) {
				
				System.out.println("connection established successfully");
			}else {
				
			}
		} catch (ClassNotFoundException e) {
		//	logger.error(e.getMessage(),e);
			e.printStackTrace();
			
		}
		return connection;
	}

}
