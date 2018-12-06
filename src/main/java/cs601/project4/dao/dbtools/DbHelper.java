package cs601.project4.dao.dbtools;

import java.io.File;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import cs601.project4.exception.DAOException;
import cs601.project4.tools.PropertyReader;

/**
 * database sql statement execution helper
 * used org.apache.commons.dbutils as reference
 * used Reflection to translate resultSet to java bean or a list of java bean
 * @author yangzun
 */
public class DbHelper {
	private static Logger logger = Logger.getLogger(RowProcessor.class);
	//ThreadLocal variable, for every thread, maintain one and only one connection
	private static ThreadLocal<Connection> conns = new ThreadLocal<Connection>();
	private static PropertyReader reader = new PropertyReader("./config","project4.properties");
	
	static String timeZoneSettings = "?useSSL=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
//	static String urlString = "jdbc:mysql://127.0.0.1:3306/event_ticket";
//	static String urlString = "jdbc:mysql://sql.cs.usfca.edu:3306/user49";
//	static String urlString = "jdbc:mysql://127.0.0.1:3307/user49";
	static String urlString = null;
	
	static String utfSetting = "&characterEncoding=utf8";
	static String userName = null;
	static String pwd = null;
	
	static {
		File file = new File("./config/log4j.properties");
		System.out.println(file.getAbsolutePath());
		PropertyConfigurator.configure("./config/log4j.properties");
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (Exception e) {
			logger.error("mysql driver not existed");
			System.exit(1);
		}
		urlString = reader.readStringValue("mysqlurl");
		userName = reader.readStringValue("username");
		pwd = reader.readStringValue("pwd");
	}
	
	

	public static Connection getConnection() {
		Connection conn = conns.get();
		if(conn == null) {
			try {
//				conn = DriverManager.getConnection(urlString + timeZoneSettings + utfSetting, "root","root");
//				conn = DriverManager.getConnection(urlString + timeZoneSettings + utfSetting, "user49","user49");
				conn = DriverManager.getConnection(urlString + timeZoneSettings + utfSetting, userName, pwd);
				logger.debug("connected to db");
				conns.set(conn);
			} catch (SQLException e) {
				throw new DAOException("database connection error",e);
			}
		}
		return conn;
	}
	
	public static void main(String[] args) throws SQLException {
		Connection con = DbHelper.getConnection();
		
		String selectStmt = "SELECT * FROM t_test"; 
		
		//create a statement object
		PreparedStatement stmt = con.prepareStatement(selectStmt);
		
		//execute a query, which returns a ResultSet object
		ResultSet result = stmt.executeQuery();

		//iterate over the ResultSet
		while (result.next()) {
			//for each result, get the value of the columns name and id
			int id = result.getInt("id");
			System.out.println(id);
		}
		
	}
	
	/**
	 * close connection
	 * @param connection
	 * @throws SQLException
	 */
	public static void closeConnection(Connection connection) throws SQLException {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				conns.remove();
			}
		} catch (SQLException e) {
			throw e;
		}
	}

	
	/**
	 * execute a sql statement for insertion, deletion or updating
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static int executeSQL(String sql, Object... params) throws SQLException {
		SqlExecuter sqlExecuter = new SqlExecuter();
		try {
			return sqlExecuter.update(getConnection(), sql, params);
		} catch (SQLException e) {
			throw e;
		}
	}

	
	/**
	 * execute query statement for a set of multiple results
	 * @param sql
	 * @param c
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static <T> List<T> getResult(String sql, Class<T> c, Object... params) throws SQLException {
		SqlExecuter sqlExecuter = new SqlExecuter();
		try {
			return sqlExecuter.query(getConnection(), sql, new BeanListHandler<>(c), params);
		} catch (SQLException e) {
			throw e;
		}
	}

	
	/**
	 * execute query statement for single result
	 * @param sql
	 * @param c
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static <T> T getSingleResult(String sql, Class<T> c, Object... params) throws SQLException {
		SqlExecuter sqlExecuter = new SqlExecuter();
		try {
			return sqlExecuter.query(getConnection(), sql, new BeanHandler<>(c), params);
		} catch (SQLException e) {
			throw e;
		}
	}
	
	public static <T> T getScalarResult(String sql, Class<T> c, Object... params) {
		SqlExecuter sqlExecuter = new SqlExecuter();
		try {
			 T query = sqlExecuter.query(getConnection(), sql, new ScalarHandler<T>(), params);
			 return query;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * Get the last self-increased id in one db connection
	 * @return
	 */
	public static int getLastIncreasedID() {
		String sql = "SELECT LAST_INSERT_ID()";
		return getScalarResult(sql, BigInteger.class).intValue();
	}


}

