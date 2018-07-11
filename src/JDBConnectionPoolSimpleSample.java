/*-

ADVANCED TOPIC

Creating and destroying database connections is expensive. In the real world, 
one uses connections pools to pre-create connections and the applications draws
connections from this connection pool and releases them back into the pool after
each use. This mechanism is far less taxing on the application performance than
creating them afresh on demand.

In the web application world, the application server (Tomcat, Jetty, JBoss etc.)
at start up time are configured to startup with a connection pool.

There are number of connections pools (dbcp2, tomcat, vilbur, hikari). For
our exploration, we'll use a connection pool called C3P0.

This sample demonstrates how to use this connection pool. Usage is extremely 
simple.

At a high-level, here are the steps:
===================================
Create C3P0 datasource/connection pool
   - Fetch connection from this pool
   - After use, release connection back into the pool (DO NOT CLOSE DATASOURCE/POOL HERE)
When application is ready to be shutdown
   - Now CLOSE the DATASOURCE and the associated connection pool

Download and Sample Usage:
==========================
1. Download C3P0 from http://sourceforge.net/projects/c3p0/
    - Unzip contents
    - Copy jars into into your lib folder (this sample already has these libs)
				- Read the documentation and explore the examples for further knowledge
 
2. Start MySql database
   - Confirm by logging into it via command line or using MySql Workbench UI
 
3. Compile and Run JDBConnectionPoolSimpleSample.java

NOTE: If you are using ant, then  you need to set the property correctly in 
   build.xml file.

@author Mike Prasad
 */

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import java.beans.PropertyVetoException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBConnectionPoolSimpleSample {

  private static final int MINPOOLSIZE = 5;
  private static final int MAXPOOLSIZE = 10;

  private static ComboPooledDataSource cpds = new ComboPooledDataSource();

  public JDBConnectionPoolSimpleSample() {
  }

  /**
   * Create the pool.
   *
   * As always, in real world we need to get these hard coded values from a
   * property file
   */
  public static void createPool() {
    System.out.println("-Creating Connection Pool");
    try {
      cpds = new ComboPooledDataSource();
      cpds.setDriverClass("com.mysql.cj.jdbc.Driver"); //loads driver
      cpds.setJdbcUrl("jdbc:mysql://localhost:3306/mysql");
      cpds.setUser("root");
      cpds.setPassword("root");
      cpds.setMaxPoolSize(MAXPOOLSIZE);
      cpds.setMinPoolSize(MINPOOLSIZE);
    } catch (PropertyVetoException pve) {
      //replace with log4 logging
      //log.error  (sqe.getClass()+": "+ sqe.getMessage(), sqe);	
      System.out.println(pve.getClass() + ": " + pve.getMessage());
      StringWriter errors;
      pve.printStackTrace(new PrintWriter(errors = new StringWriter()));
      System.out.println(errors.toString());
    }
    System.out.println("-Connection Pool creation completed");
  }

  /**
   * Retrieves a connection from the connection pool
   *
   * @return Connection if available or returns null
   */
  public static Connection getConnection() {
    Connection connection = null;
    try {
      connection = cpds.getConnection();
    } catch (SQLException sqe) {
      //replace with log4 logging
      //log.error  (sqe.getClass()+": "+ sqe.getMessage(), sqe);	
      System.out.println(sqe.getClass() + ": " + sqe.getMessage());
      StringWriter errors;
      sqe.printStackTrace(new PrintWriter(errors = new StringWriter()));
      System.out.println(errors.toString());
    }
    return connection;
  }

  /**
   * We want to close the DataSource *only* when the application is ready to be
   * shutdown.
   */
  public static void shutDown() {
    try {
      if (cpds != null) {
        System.out.println("Destroying datasource");
        DataSources.destroy(cpds);
      } //release datasource         
    } catch (SQLException e) {
      //replace with log4 logging
      //log.error  (sqe.getClass()+": "+ sqe.getMessage(), sqe);	
      System.out.println(e.getClass() + ": " + e.getMessage());
      StringWriter errors;
      e.printStackTrace(new PrintWriter(errors = new StringWriter()));
      System.out.println(errors.toString());
    }
  } //end shutDown

  /**
   * Fetches connection from database and lists contents of a USER table
   */
  public void listContents() {
    Statement stmt = null;
    Connection conn = null;

    try {
      //fetch db connection
      conn = getConnection();

      if (conn != null) //if we have a connection
      {
        stmt = conn.createStatement();

        // Select the columns from the USER table.
        ResultSet rset = stmt.executeQuery("select HOST, USER from USER");

        // Iterate through the result set and print its contents.
        System.out.println("\nResult set returned: ");

        while (rset.next()) {
          System.out.println("Host: " + rset.getString(1));
          System.out.println("User: " + rset.getString(2));
        }
      } else {
        System.out.println("Issue during fetching connection.");
      }
    } catch (SQLException e) {
      //replace with log4 logging
      //log.error  (sqe.getClass()+": "+ sqe.getMessage(), sqe);	
      System.out.println(e.getClass() + ": " + e.getMessage());
      StringWriter errors;
      e.printStackTrace(new PrintWriter(errors = new StringWriter()));
      System.out.println(errors.toString());
    } finally //must release all open resources in finally block
    {
      try {
        // check for null first before closing resources
        if (stmt != null) {
          stmt.close();
        }
        if (conn != null) {
        }
        DataSources.destroy(cpds);

      } catch (SQLException e) {
        //replace with log4 logging
        //log.error  (sqe.getClass()+": "+ sqe.getMessage(), sqe);	
        System.out.println(e.getClass() + ": " + e.getMessage());
        StringWriter errors;
        e.printStackTrace(new PrintWriter(errors = new StringWriter()));
        System.out.println(errors.toString());
      }
    }//try/catch/finally     
  } // end listContents()

  public static void main(String[] args) {
    JDBConnectionPoolSimpleSample sample = new JDBConnectionPoolSimpleSample();
    JDBConnectionPoolSimpleSample.createPool();
    sample.listContents();

    System.out.println("-Shutting down application");
    JDBConnectionPoolSimpleSample.shutDown();

  }//end main

}//end JDBConnectionPoolSimpleSample



/*-

Output:

-Creating Connection Pool
-Connection Pool creation completed
Jul 11, 2018 9:54:38 AM com.mchange.v2.c3p0.impl.AbstractPoolBackedDataSource getPoolManager
INFO: Initializing c3p0 pool... com.mchange.v2.c3p0.ComboPooledDataSource [ acquireIncrement -> 3, acquireRetryAttempts -> 30, acquireRetryDelay -> 1000, autoCommitOnClose -> false, automaticTestTable -> null, breakAfterAcquireFailure -> false, checkoutTimeout -> 0, connectionCustomizerClassName -> null, connectionTesterClassName -> com.mchange.v2.c3p0.impl.DefaultConnectionTester, dataSourceName -> 2s77yk9w12ayi3c1ut62dg|726f3b58, debugUnreturnedConnectionStackTraces -> false, description -> null, driverClass -> com.mysql.cj.jdbc.Driver, factoryClassLocation -> null, forceIgnoreUnresolvedTransactions -> false, identityToken -> 2s77yk9w12ayi3c1ut62dg|726f3b58, idleConnectionTestPeriod -> 0, initialPoolSize -> 3, jdbcUrl -> jdbc:mysql://localhost:3306/mysql, maxAdministrativeTaskTime -> 0, maxConnectionAge -> 0, maxIdleTime -> 0, maxIdleTimeExcessConnections -> 0, maxPoolSize -> 10, maxStatements -> 0, maxStatementsPerConnection -> 0, minPoolSize -> 5, numHelperThreads -> 3, preferredTestQuery -> null, properties -> {user=******, password=******}, propertyCycle -> 0, statementCacheNumDeferredCloseThreads -> 0, testConnectionOnCheckin -> false, testConnectionOnCheckout -> false, unreturnedConnectionTimeout -> 0, userOverrides -> {}, usesTraditionalReflectiveProxies -> false ]

Result set returned: 
Host: localhost
User: mysql.sys
Host: localhost
User: root
-Shutting down application
Destroying datasource
*/