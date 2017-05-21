package entg.util;
import java.security.*;
import java.security.spec.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;

import javax.crypto.*;
import javax.crypto.spec.*;

import entg.test.TCAgentMain;

import java.util.*;
import java.io.*;

public class PasswordEncryption {
	  Cipher ecipher;
	    Cipher dcipher; 
	    
	    // 8-byte Salt
	    byte[] salt = {
	        (byte)0xA9, (byte)0x9B, (byte)0xC8, (byte)0x32,
	        (byte)0x56, (byte)0x35, (byte)0xE3, (byte)0x03
	    };
	     
	    // Iteration count
	    int iterationCount = 19;
	     
	  public PasswordEncryption() {
	        try {
	            // Create the key
	    String passPhrase = "ENTEGRATION";
	            KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), 
	salt, iterationCount);
	            SecretKey key = SecretKeyFactory.getInstance(
	            "PBEWithMD5AndDES").generateSecret(keySpec);
	            ecipher = Cipher.getInstance(key.getAlgorithm());
	            dcipher = Cipher.getInstance(key.getAlgorithm());
	            
	            // Prepare the parameter to the ciphers
	            AlgorithmParameterSpec paramSpec = new 
	PBEParameterSpec(salt, iterationCount);
	            
	            // Create the ciphers
	            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
	            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
	        } catch (java.security.InvalidAlgorithmParameterException e) {
	        } catch (java.security.spec.InvalidKeySpecException e) {
	        } catch (javax.crypto.NoSuchPaddingException e) {
	        } catch (java.security.NoSuchAlgorithmException e) {
	        } catch (java.security.InvalidKeyException e) {
	        }
	    }
	    
	    public String encrypt(String str) {
	        try {
	            // Encode the string into bytes using utf-8
	            byte[] utf8 = str.getBytes("UTF8");
	            
	            // Encrypt
	            byte[] enc = ecipher.doFinal(utf8);
	            
	            // Encode bytes to base64 to get a string
	            return new sun.misc.BASE64Encoder().encode(enc);
	        } catch (javax.crypto.BadPaddingException e) {
	        } catch (IllegalBlockSizeException e) {
	        } catch (UnsupportedEncodingException e) {
	        } catch (java.io.IOException e) {
	        }
	        return null;
	    }
	    
	    public String decrypt1(String str) throws Exception {
	    	
	    	byte[] dec =  str.getBytes("UTF8");
	    	byte[] utf8 = dcipher.doFinal(dec);
	    	return new String(utf8, "UTF8");
	    }
	    
	    public String decrypt(String str) {
	        try {
	            // Decode base64 to get bytes
	        	
	            byte[] dec = new 
	sun.misc.BASE64Decoder().decodeBuffer(str);
	            
	            // Decrypt
	            byte[] utf8 = dcipher.doFinal(dec);
	            
	            // Decode using utf-8
	            return new String(utf8, "UTF8");
	        } catch (javax.crypto.BadPaddingException e) {
	        	e.printStackTrace();
	        } catch (IllegalBlockSizeException e) {
	        	e.printStackTrace();
	        } catch (UnsupportedEncodingException e) {
	        	e.printStackTrace();
	        } catch (java.io.IOException e) {
	        	e.printStackTrace();
	        }
	        return null;
	    }
	    

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			 PasswordEncryption encrypter = new PasswordEncryption();
	            
	            // Encrypt
	/*            String encrypted = encrypter.encrypt(args[0]);
	            System.err.println("Ecnrypted String: #"+ encrypted+"#,Length "+encrypted.length());
	            String decrypted = encrypter.decrypt(encrypted);
	            System.err.println("Denrypted String: "+ decrypted);
*/
			 System.out.println("#"+args[0]+"#, length: "+args[0].length());
			 String encrypted1 = encrypter.encrypt(args[0]);
	            System.out.println("Ecnrypted String: "+ encrypted1);
	        } catch (Exception e) {
	        	e.printStackTrace(); 
	        }

	}

	public static String loadKey(String file) {
        try {
            FileInputStream fin=new FileInputStream(file);
            int byteCount=fin.available();
            byte array[]=new byte[byteCount];
            fin.read(array);
            return new String(array);
        }
        catch(Exception ex) {
            return "";
        }
        
        
    }
	  public static String getDBPassword(String encPwd) throws Exception {
	    	
	    	Connection conn = getMetricDBConn();
	    	
	    	Statement stmt = conn.createStatement();
	    	ResultSet rst = stmt.executeQuery("select entg_utils.getDataSourcePassword("+encPwd+") from dual");
	    	rst.next();
	    	String ret = rst.getString(1);
	    	stmt.close();
	    	
	    	return ret;
	    }
	
	  public static String getAppSigPassword(String userId, String instanceId) throws Exception {
	    	
	    	Connection conn = getMetricDBConn();
	    	
	    	Statement stmt = conn.createStatement();
	    	ResultSet rst = stmt.executeQuery("select entg_utils.getAppSigPassword("+userId+","+instanceId+") from dual");
	    	rst.next();
	    	String ret = rst.getString(1);
	    	stmt.close();
	    	closeMetricDBConn();
	    	return ret;
	    }
	  
	  static Connection metricDBConn;
		public static void makeMetricDBConn() throws Exception {

	  	 String encryptedPassword = TCAgentMain.TCProperties
	                    	.getProperty("METRIC_DB_PW");
	     PasswordEncryption encrypter = new PasswordEncryption();
	     String password = encrypter.decrypt(encryptedPassword);

	     String userName = TCAgentMain.TCProperties
		           .getProperty("METRIC_DB_USERNAME");

	     String dbUrl = TCAgentMain.TCProperties.getProperty("METRIC_DB_URL");

	     DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
	     metricDBConn = DriverManager.getConnection(dbUrl, userName,
		    	password);

		}

		public static Connection getMetricDBConn() {
			return metricDBConn;
		}
		
		public static void closeMetricDBConn() throws Exception {
			metricDBConn.close();
		}
	
}
