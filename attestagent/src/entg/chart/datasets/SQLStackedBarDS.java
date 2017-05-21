package entg.chart.datasets;


import de.laures.cewolf.DatasetProducer;
import de.laures.cewolf.tooltips.CategoryToolTipGenerator;

import java.io.Serializable;

import java.util.Date;
import java.util.Map;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SQLStackedBarDS 
   implements DatasetProducer, CategoryToolTipGenerator, Serializable
{
  

	String username;
	String password;
	String jdbcUrl;
	
	String sql;
   public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

    public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

/**
    *
   *
    * @param aMap
    */
   public Object produceDataset(Map aMap)
   {
	   DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
	   try {
System.out.println("In gen data ");
System.out.println("URL: "+jdbcUrl);
System.out.println("UN: "+username);
System.out.println("PW: "+password);
	   
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			Connection conn = DriverManager.getConnection(jdbcUrl, username,
					password);
System.out.println("After conn");
System.out.println("SQL: "+sql);
			Statement stmt = conn.createStatement();
			ResultSet rs = null;
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
			  categoryDataset.addValue(rs.getInt(1),
					    rs.getString(2),
					    rs.getString(3));
			}
  	  	  
			stmt.close();
			conn.close();
System.out.println("All done");
	   } catch (java.sql.SQLException ex) {
	     System.out.println("SQL Error: "+ex.getMessage());	   
ex.printStackTrace(System.out);
	   }
      return categoryDataset;
   }

   /**
    * Not currently implemented - returns false.
    */
   public boolean hasExpired(Map map, Date date)
   {
      return false;
   }

   /**
    * Not currently implemented - returns null.
    */
   public String getProducerId()
   {
      return null;
   }

   /**
    * Not currently implemented - returns null.
    */
   public String generateToolTip(CategoryDataset categoryDataset, int i, int i2)
   {
      return null;
   }

 
}
