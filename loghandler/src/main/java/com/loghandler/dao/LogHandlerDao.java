package com.loghandler.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hsqldb.Server;

import com.loghandler.model.DBEvent;
	

	public class LogHandlerDao {

		private static final Logger LOGGER = Logger.getLogger("DBManager");
		private static final String DB_NAME = "logEvents";
		private static final String DB_PATH = "file:events";
		private static final String HOSTNAME = "localhost";
		private static final String TABLE_NAME = "EventDetails";
		private static final String TABLE_CREATION_SQL = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"(id VARCHAR(50), duration BIGINT, host VARCHAR(50), type VARCHAR(50), alert BOOLEAN)";
		
		private Server hsqlServer = new Server();
		private Connection connection;

		public void handleSave(List<DBEvent> dbEventList) {
			startHSQLDB();
			createHSQLDBTable();
			saveEvents(dbEventList);
			readEvents();
			stopHSQLDB();
		}
		
		private void startHSQLDB() {
			hsqlServer.setSilent(true);
			hsqlServer.setDatabaseName(0, DB_NAME);
			hsqlServer.setDatabasePath(0, DB_PATH);

			Logger.getLogger("hsqldb.db").setLevel(Level.WARNING);
			System.setProperty("hsqldb.reconfig_logging", "false");
			hsqlServer.start();
			LOGGER.info("HSQL Server started");
		}
		
		public void stopHSQLDB() {
			
			if (connection != null) {
				try {
					connection.close();
					LOGGER.info("Connection to HSQL Server closed");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			hsqlServer.stop();
			LOGGER.info("HSQL Server stopped");
		}
		
		private Connection openConnection() {	
			try {
				Class.forName("org.hsqldb.jdbc.JDBCDriver");
				return DriverManager.getConnection("jdbc:hsqldb:hsql://"+HOSTNAME+"/"+DB_NAME, "SA", "");
			} catch (SQLException | ClassNotFoundException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
			return null;
		}

		
		public void dropHSQLDBLTable() {
			Statement stmt = null;

			try {
				if (connection == null) {
					connection = openConnection();
				}
				stmt = connection.createStatement();
				stmt.executeUpdate("DROP TABLE "+TABLE_NAME);

			}  catch (SQLException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			} 
		}


		public void createHSQLDBTable() {
			Statement stmt = null;

			try {
				if (connection == null) {
					connection = openConnection();
				}

				//Check first if table exists
				DatabaseMetaData dbm = connection.getMetaData();
				ResultSet tables = dbm.getTables(null, null, TABLE_NAME, new String[] {"TABLE"});
				
				if (!tables.next()) {
					stmt = connection.createStatement();
					stmt.executeUpdate(TABLE_CREATION_SQL);
					LOGGER.fine("Table "+TABLE_NAME+" created");
				}

			}  catch (SQLException  e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			} 
		}
		
		
		public void saveEvents(List<DBEvent> dbEventList) {
			Statement stmt = null; 
			int successfulSave = 0;

			try {
				if (connection == null) {
					connection = openConnection();
				}
				stmt = connection.createStatement(); 
				
				for (DBEvent event : dbEventList) {

					ResultSet rowExists = stmt.executeQuery("SELECT * FROM "+TABLE_NAME+" WHERE ID='"+event.getId()+"'");

					if (!rowExists.next()) {
						String statementStr="INSERT INTO "+TABLE_NAME+" (id, duration, host, type, alert) VALUES('"+event.getId()+"',"+event.getDuration()+",'"+event.getHost()+"','"+event.getType()+"',"+event.isAlert()+")";
						successfulSave += stmt.executeUpdate(statementStr);
					} else {
						LOGGER.info("Data already exists for ID "+event.getId()+ " skipping..");
					}

				}
				connection.commit(); 
			} catch (SQLException e) {
				LOGGER.severe("DB Exception occured - "+e.getMessage());
			} 
			LOGGER.info("Total number of rows inserted ="+successfulSave);
		}
		
	
		public void readEvents() {
			StringBuilder sb  = new StringBuilder();
			Statement stmt = null; 
			ResultSet result;

			if (connection == null) {
				connection = openConnection();
			}

			try {
				stmt = connection.createStatement();
				result = stmt.executeQuery("SELECT * FROM "+TABLE_NAME);
				ResultSetMetaData rsmd = result.getMetaData();
				

				while (result.next()) {
				    for (int i = 1; i <= 5; i++) {
				        if (i > 1) {
				        	sb.append(",  ");
				        }
				        String columnValue = result.getString(i);
				        sb.append(rsmd.getColumnName(i)+":"+columnValue);
				    }
				    sb.append("\n");
				}
			} catch (SQLException e) {
				LOGGER.severe("Exceptionoccured while reading data");
			}
			LOGGER.info("Data Read from DB - " +sb.toString());
		}

	}

	
