package entg.util.objrep;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.util.ValidationEventCollector;

public class ParseObjRep {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		JAXBContext jc = JAXBContext.newInstance( "entg.util.objrep");
	
		Unmarshaller u = jc.createUnmarshaller();
		
	//	u.setValidating( false ); 
		
		ObjectRepository o =
			  (ObjectRepository)u.unmarshal(
			    new FileInputStream(args[0])); 

    	DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
    	String dbUrl = "jdbc:oracle:thin:@yamuna.entegration.com:1521:VIS";
    	String userName = "metricstream";
    	String password="metricstream";
    	
    	 
	    Connection conn = DriverManager.getConnection(dbUrl, userName, password); 

		o.insertObjRep(conn, args[0]);
		conn.close();

	}

}
