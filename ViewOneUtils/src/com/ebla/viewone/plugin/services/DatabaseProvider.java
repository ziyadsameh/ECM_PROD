/**
 * 
 */
package com.ebla.viewone.plugin.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import com.ebla.viewone.services.GetWaterMarkAnnotations;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

/**
 * @author sachith
 *
 */
public class DatabaseProvider {
	private static final Logger	logger	= Logger.getLogger(DatabaseProvider.class);
	private static ResourceBundle props;											
	private static String DB_DataSource = null;
	private static String storedProcedureName = null;

	public DatabaseProvider() throws FileNotFoundException, IOException {
		String PARM_RESOURCE_BUNDLE_NAME = "com.ebla.viewone.services.resources.db_config";
		ClassLoader loader = GetWaterMarkAnnotations.class.getClassLoader();
		Locale currentLocale = Locale.ROOT;
		props = ResourceBundle.getBundle(PARM_RESOURCE_BUNDLE_NAME, currentLocale, loader);
		DB_DataSource = props.getString("DB_DataSource");
		storedProcedureName = props.getString("StoredProcedureName");
		System.out.println("DB_DataSource : " + DB_DataSource);

	}

	
	/**
	 * Queries Content Classification table and returns the Content Category value
	 * 
	 * @param userName
	 * @param contentCategory
	 * @return
	 * @throws Exception
	 */

	public void insertDocPrintActionRecord(String userId,String docId,String docName , String osSumbolicName, String timeStamp) throws Exception {

		System.out.println("In insertDocPrintActionRecord Start ");

		Statement stmt = null;
		ResultSet rs = null;
		Connection con = null;
	    CallableStatement callableStatement = null;
		try {
			con = getConnection();
		    callableStatement = con.prepareCall(storedProcedureName);
		    callableStatement.setString(1, userId);
		    callableStatement.setString(2, docId);
		    callableStatement.setString(3, docName);
		    callableStatement.setString(4, osSumbolicName);
		    callableStatement.setString(5, timeStamp);
		    callableStatement.execute();
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("In getDIMSDepartmentsLookup end: " + convertStackTraceToString(e));
			throw e;
		} finally {
			closeConnection(rs, stmt, con);
		}


	}
	

	public String convertStackTraceToString(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();
		return stacktrace;
	}



	private Connection getConnection() throws Exception {
		Connection con = null;
		try {
			con = getDSConnection();
			if (con == null) throw new Exception("Couldn't open connection to database");
		} catch (Exception ex) {
			throw ex;
		}
		return con;
	}
		
	public Connection getDSConnection() throws Exception {
		Connection con = null;
		
		try {
			Context webContext = new InitialContext();
			DataSource ds = (DataSource) webContext.lookup(DB_DataSource);
			con = ds.getConnection();
			logger.debug("con " + con.toString());
			logger.debug("Created DS connection to database.");
		} catch (Exception ex) {
			logger.error("Couldn't create DS connection." + ex.getMessage());
		}
		
		return con;
	}
	
	public void closeConnection(ResultSet rs, CallableStatement cstmt, Connection con) {
		logger.debug("Closing the DB connection.");
		try {
			rs.close();
			cstmt.close();
			con.close();
		} catch (SQLException ex) {
			logger.error("Error while closing connections : " + ex);
			ex.printStackTrace();
		}
		logger.debug("Closed the DB connection.");
	}
	
	
	private void closeConnection(ResultSet rs, Statement stmt, Connection con) {
		logger.debug("Closing the DB connection.");
		try {
			rs.close();
			stmt.close();
			con.close();
		} catch (SQLException ex) {
			logger.error("Error while closing connections : " + ex);
			ex.printStackTrace();
		}
		logger.debug("Closed the DB connection.");
		
	}



	public static void main(String[] args) throws Exception {
		DatabaseProvider  x = new DatabaseProvider();
	}
}
