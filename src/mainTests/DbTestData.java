/**
 * 
 */
package mainTests;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author vvukoti
 *
 */
public class DbTestData {
	public static void readData() {
		PreparedStatement preparedStatement=null;
		Connection connection=null;
		ResultSet resultSet=null;
		
		try {
			connection=DBConnection.getConnection();
			preparedStatement=connection.prepareStatement("select * from employee_test1 order by id DESC");
			resultSet=preparedStatement.executeQuery();
			while(resultSet.next()) {
				System.out.println("Name -- "+resultSet.getString("name"));
				System.out.println("Id -- "+resultSet.getString("id"));
			}
			
		} catch (SQLException e) {
			//logger.error(e.getMessage(),e);
			e.printStackTrace();
			
		}
		
		
	}
	public static void main(String[] args) {
		readData();
	}
	
}
