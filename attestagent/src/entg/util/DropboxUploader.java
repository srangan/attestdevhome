package entg.util;

// Include the Dropbox SDK.
import com.dropbox.core.*;

import entg.test.TCAgentMain;

import java.io.*;
import java.util.Locale;
import java.util.Properties;

public class DropboxUploader{
	static String accessToken = "";
    
	static final String APP_KEY = "64y9snm2jvke6pl";
	static final String APP_SECRET = "a8evzmrq75e8i78";
    public static void genAccessCode() throws Exception {

        
        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

        DbxRequestConfig config = new DbxRequestConfig("Attest/1.0",
            Locale.getDefault().toString());
        
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);

        String authorizeUrl = webAuth.start();
        System.out.println("1. Go to: " + authorizeUrl);
        System.out.println("2. Click \"Allow\" (you might have to log in first)");
        System.out.println("3. Copy the authorization code.");
        
        System.out.print("\nPaste the code here and hit Enter: ");

        String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
        DbxAuthFinish authFinish = webAuth.finish(code);
        accessToken = authFinish.accessToken;
        
        // System.out.println("accessToken="+accessToken);
        
        entg.util.PasswordEncryption pwp = new entg.util.PasswordEncryption();
		String encAccessToken= pwp.encrypt(accessToken);
		
		String parserDir=new String(System.getProperty("attest.path"));
		PrintWriter out=new PrintWriter(new BufferedWriter(new FileWriter(parserDir+"\\dropbox.properties")));
		out.println(encAccessToken);
		
		out.flush();
		out.close();
		System.out.println("Access Token Saved successfully");
    }
    
    public static String uploadFile(String upFile) throws Exception {
    	
    	String parserDir=new String(System.getProperty("attest.path"));
    	FileInputStream fin = new FileInputStream(parserDir+"\\dropbox.properties");
    	StringBuffer strContent = new StringBuffer("");
    	int ch;
    	while( (ch = fin.read()) != -1)
            strContent.append((char)ch);
    	
    	String encAccessToken = strContent.toString();
    	System.out.println("encAccessToken="+encAccessToken);
    	
    	entg.util.PasswordEncryption pwp = new entg.util.PasswordEncryption();
		String accessToken= pwp.decrypt(encAccessToken);
		
		System.out.println("accessToken="+accessToken);
    	DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
    	

        DbxRequestConfig config = new DbxRequestConfig("Attest/1.0",
            Locale.getDefault().toString());
    

        DbxClient client = new DbxClient(config, accessToken);

        System.out.println("Linked account: " + client.getAccountInfo().displayName);

        File fileObj = new File(upFile);
        FileInputStream inputStream = new FileInputStream(fileObj);
      
        String uplUrl="";
        try {
            DbxEntry.File uploadedFile = client.uploadFile("/"+fileObj.getName(),
                DbxWriteMode.add(), fileObj.length(), inputStream);
            System.out.println("Uploaded: " + uploadedFile.toString());
            uplUrl = client.createShareableUrl("/"+fileObj.getName());
        } finally {
            inputStream.close();
        }        

    	
    	return uplUrl;
    }
	
    public static void main(String[] args) throws Exception {
               
    	genAccessCode();
    	/*
        String mode = (args.length==0?"USAGE":args[0]);
        
        if (mode.equals("USAGE")) {
        	System.out.println("java entg.util.DropBoxUploader <GEN_ACCESS_TOK|UPLOADFILE> <file-path>");
        }

        
        if (mode.equals("GEN_ACCESS_TOK")) {
        	genAccessCode();
        } 
        if (mode.equals("UPLOADFILE")) {
        	System.out.println(uploadFile(args[1]));
        } */
               
    }
}