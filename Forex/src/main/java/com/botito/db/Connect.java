package com.botito.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class Connect {
	private com.mysql.jdbc.Connection connect = null;

	public Connection connectDB(){
		try {
		      // This will load the MySQL driver, each DB has its own driver
		      Class.forName("com.mysql.jdbc.Driver");
		      // Setup the connection with the DB
		      connect = (com.mysql.jdbc.Connection)DriverManager
		          .getConnection("jdbc:mysql://localhost/datos_forex?"
		              + "user=root&password=andres");
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return connect;
	}
	
	public void close(){
		try {
			connect.close();
			connect = null;
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}

