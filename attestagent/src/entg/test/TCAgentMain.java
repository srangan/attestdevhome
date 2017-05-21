package entg.test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.io.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import java.awt.event.*;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.CoolBar; 

import entg.util.PasswordEncryption;
public class TCAgentMain {
    int lastClick=0;
	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint= "40,38"
    TestcaseInfo tst1;
    ScenarioInfo  s1;
    TCAgentMain thisClass;
    Display display ;
    boolean paused=false;
    boolean aborted=false;
    int toggleRunningColor = 0;
    public static Properties TCProperties;
	/*
	 * 
	 * This method initializes testCasesTable	
	 *
	 */ 
 
	/**
	 * This method initializes Testcases	
	 *
	 */
    boolean repaintFlag=false;
    public void repaint () {    	
       if (repaintFlag) {
    		this.paintTestrun(testrun);
    		repaintFlag=false; 
    	}
    	

    }
    public void setRepaint() {
    	repaintFlag=true;
    }
    
    public void setFocus() {
    	//System.out.println("In setFocus");
    	 display.asyncExec(new Runnable(){
    		public void run(){
    			sShell.forceFocus();
    	    	sShell.setActive();
    	    	sShell.forceActive();
    		}
    	}
    	);
    }
 
	private void createTestcases() {
		Testcases = new Table(sShell, SWT.NONE);
		Testcases.setHeaderVisible(true);
		Testcases.setLinesVisible(true);
		Testcases.setBounds(new org.eclipse.swt.graphics.Rectangle(4,208,447,266));
		TableColumn Scenario = new TableColumn(Testcases, SWT.NONE);
		
		
		
		TableColumn Testcase_ID = new TableColumn(Testcases, SWT.NONE);
		TableColumn Function = new TableColumn(Testcases, SWT.NONE);
		
		TableColumn Status = new TableColumn(Testcases, SWT.NONE);
		
		Testcase_ID.setResizable(true);
		Testcase_ID.setWidth(65);
		Testcase_ID.setText("ID");
		Scenario.setWidth(130);
		Scenario.setResizable(true);
		Scenario.setText("Scenario");
		Function.setWidth(170);
		Function.setText("Function");
		Function.setResizable(true);
		Status.setWidth(82);
		Status.setText("Status");
		Status.setResizable(true);

	}
	
	public static boolean sfdcConsole() {

		String sfdcProp = TCAgentMain.TCProperties.getProperty("SFDC_CONSOLE");
		if (sfdcProp!=null && sfdcProp.equalsIgnoreCase("yes")) return true;
		return false;

	}
	public static boolean dbMethod() {
		String dbMethodProp = TCAgentMain.TCProperties.getProperty("AGENT_USES_DB");
		boolean dbMethod = true;
		if (dbMethodProp!=null && dbMethodProp.equalsIgnoreCase("no")) dbMethod = false;
		return dbMethod;
	}
	
	
	
	static Connection metricDBConn;
	public static void makeMetricDBConn() throws Exception {
	 System.out.println ("***** Making DB Connection in metricDBConn");
	 
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
	
	public void paintTestrun(TestRunInfo testrun) {
		Run_Id.setText("" +testrun.getRunId());
	    runName.setText(""+testrun.getRunName());
	    runStatus.setText(""+testrun.getRunStatus());
	    if (testrun.getRunStatus().equals("Completed" )){
	    	runStatus.setBackground(new Color (Display.getDefault(),0,255,0));
	    }
	    if (testrun.getRunStatus().equals("Complete With Errors" )){
	    	runStatus.setBackground(new Color (Display.getDefault(),255,0,0));
	    }
		progressBar.setSelection(testrun.getPercenatgeComplete());
		percComplete.setText(testrun.getPercenatgeComplete()+"%");
		text.setText(testrun.getSuiteName());
		pendVal.setText(""+(testrun.getTotalTestcases()-(testrun.getPassTestcases()+testrun.getFailTestcases())));
		failVal.setText(""+testrun.getFailTestcases());
		passVal.setText(""+testrun.getPassTestcases());
		totalVal.setText(""+testrun.getTotalTestcases());
		
		Testcases.removeAll();
		Vector scenarios = testrun.getScenarios();
		for (int j = 0; j < scenarios.size(); j++) {
            ScenarioInfo s1 = (ScenarioInfo) scenarios.elementAt(j);
            Vector testcases = s1.getTestCases();
            Vector functions = s1.getFunctions();
        	
    		TableItem tableItem = new TableItem(Testcases,SWT.NONE);
    		
    		tableItem.setText(0,s1.getScenarioName());
    		tableItem.setForeground(new Color (Display.getDefault(),0,0,0));
    		tableItem.setBackground(  
            		new Color (
            				Display.getDefault(),204,204,255  ));
            for (int i = 0; i < testcases.size(); i++) {
            	TestcaseInfo t = (TestcaseInfo) testcases.elementAt(i);
            	tableItem = new TableItem(Testcases,SWT.NONE);
            	tableItem.setText(1,""+t.getTestcaseId());
        		tableItem.setBackground(  
                		new Color (
                				Display.getDefault(),255,204,153)  );
            	
            	tableItem.setText(3,t.getStatus());
            	for (int k = 0; k < functions.size(); k++) {
            		
                    
            		FunctionInfo f1 = (FunctionInfo) functions.elementAt(k);

            		
                	tableItem = new TableItem(Testcases,SWT.NONE);;
                    tableItem.setBackground(  
                    		new Color (
                    				Display.getDefault(),153,187,238 ));            		
            		tableItem.setText(2,f1.getFunctionName());
            		tableItem.setText(3,t.getFunctionStatus(k));
            		if (t.getFunctionStatus(k).equals ("PASS") ){
            			tableItem.setBackground(3,new Color (
                				Display.getDefault(),0, 255,0)); 
            		  } else if ( t.getFunctionStatus(k).equals ("FAIL") ){
            				tableItem.setBackground(3,new Color (
                    				Display.getDefault(),255, 0,0)); 
            		  } else if ( t.getFunctionStatus(k).equals ("RUNNING") ){
            			if (this.toggleRunningColor++%2 == 0)
          				   tableItem.setBackground(3,new Color (
                				Display.getDefault (),0, 255,0));
            			else
            				tableItem.setBackground(3,new Color (
                    				Display.getDefault(),153,187,238));
            				
        		      }
            		}
            	}
            }
	}
	

	
	
	
	/**
	 * This method initializes progressBarCanvas	
	 *
	 */

	/**
	 * This method initializes group	
	 *
	 */
	private void createGroup() {
/*		group = new Group(sShell, SWT.NONE);
		group.setText("  ");
		group.setBounds(new org.eclipse.swt.graphics.Rectangle(12,87,469,77));
progressBar1 = new ProgressBar(group, SWT.NONE);
		progressBar1.setBounds(new org.eclipse.swt.graphics.Rectangle(6,10,414,31));
		cLabel1 = new CLabel(group, SWT.NONE);
		cLabel1.setFont(new Font(sShell.getDisplay(), new FontData("Arial", 8, SWT.NONE)));
		cLabel1.setText("Total:");
		cLabel1.setBounds(new org.eclipse.swt.graphics.Rectangle(119,49,32,20));
		*/
	}
	
	static String parserDir,tmpDir;
	
	public static String TCAgentproperties(String PropertyName)
	{
		
		return TCProperties.getProperty(PropertyName);
         	
		 
	}
	public static String getTmpDir() {
	
		
		return tmpDir;
	}
	
	public static String getParserDir() {
		return parserDir;
	}	
	/**
	 * @param args
	 */
	Shell splash=null;
	Display display1;
	Label progressLabel = null;
	public void paintSplashScreen() {
		final Image image = new Image(display, TCAgentMain.getParserDir()+File.separator+"attest.jpg");
	    
		splash = new Shell(SWT.ON_TOP);
	    //final ProgressBar bar = new ProgressBar(splash, SWT.NONE);
	    progressLabel = new Label(splash,SWT.NONE);
	    progressLabel.setText("Please wait while the testrun is initialized ..");
	    Label label = new Label(splash, SWT.NONE);
	    label.setImage(image);
	    FormLayout layout = new FormLayout();
	    splash.setLayout(layout);
	    FormData labelData = new FormData();
	    labelData.right = new FormAttachment(100, 0);
	    labelData.bottom = new FormAttachment(100, 0);
	    label.setLayoutData(labelData);
	    FormData progressData = new FormData();
	    progressData.left = new FormAttachment(0, 5);
	    progressData.right = new FormAttachment(100, -5);
	    progressData.bottom = new FormAttachment(100, -5);
	    progressLabel.setLayoutData(progressData);

	    splash.pack();
	    Rectangle splashRect = splash.getBounds();
	      
	    Rectangle displayRect = display1.getBounds();
	    int x = (displayRect.width - splashRect.width) / 2;
	    int y = (displayRect.height - splashRect.height) / 2;
	    splash.setLocation(x, y);
	    splash.open();
	}
	public void closeSplashScreen() {
		splash.close();
	}
	
	
	public static void loadProps() throws Exception {
	parserDir=new String(System.getProperty("attest.path"));
	
	FileInputStream fin = new FileInputStream(parserDir+"\\attest.properties");
	TCProperties = new Properties();
	TCProperties.load(fin);
	}
	
	static String runIdStr = "runDebug";
	public static void main(String[] args)  throws IOException, Exception {
		// TODO Auto-generated method stub
		/* Before this is run, be sure to set up the launch configuration (Arguments->VM Arguments)
		 * for the correct SWT library path in order to run with the SWT dlls. 
		 * The dlls are located in the SWT plugin jar.  
		 * For example, on Windows the Eclipse SWT 3.1 plugin jar is:
		 *       installation_directory\plugins\org.eclipse.swt.win32_3.1.0.jar
		 */
		
		
		
		try {  
		  runIdStr = args[1];
		  
		  if (runIdStr==null||runIdStr.equals("")) runIdStr = "runDebug";
		  
		} catch (Exception e) {
			e.printStackTrace();
			 runIdStr = "runDebug";
		}
		
		entg.test.plugin.FunctionPluginManager.loadAllPlugins();
		TCAgentMain thisClass = new TCAgentMain();
		
		thisClass.display1 = new Display();
	   // thisClass.display1 = new Display();
		//parserDir = args[1];
		//tmpDir = args[2];
		
		loadProps();
		// thisClass.paintSplashScreen();
		
		System.out.println(parserDir);
		
		
		tmpDir = new String(TCProperties.getProperty("TMPDIR"));
		 
		TestRunner.createLogFile();
		System.out.println(parserDir);
		System.out.println(tmpDir);
	    Display display = Display.getDefault();
		
		
		thisClass.thisClass=thisClass;
		thisClass.runTest(args[0]);
		thisClass.createSShell();
		// thisClass.closeSplashScreen();
		thisClass.sShell.open();
		//System.out.println("Here1");
		TestRunner runner = new TestRunner(thisClass.testrun,thisClass);
		runner.start();
		//System.out.println("Here2");
		thisClass.paintTestrun(thisClass.testrun);
		while (!thisClass.sShell.isDisposed()) {
			if (!display.readAndDispatch()) 
				display.sleep();
			thisClass.repaint();    
		}
		
		display.dispose();
		//System.out.println("Here3");
	}

	public static void jobMain(String[] args,PrintWriter out)  throws IOException, Exception {
		// TODO Auto-generated method stub
		/* Before this is run, be sure to set up the launch configuration (Arguments->VM Arguments)
		 * for the correct SWT library path in order to run with the SWT dlls. 
		 * The dlls are located in the SWT plugin jar.  
		 * For example, on Windows the Eclipse SWT 3.1 plugin jar is:
		 *       installation_directory\plugins\org.eclipse.swt.win32_3.1.0.jar
		 */
		entg.test.plugin.FunctionPluginManager.loadAllPlugins();
		TCAgentMain thisClass = new TCAgentMain();
	    
		//parserDir = args[1];
		//tmpDir = args[2];
		parserDir=new String(System.getProperty("attest.path"));
		// thisClass.paintSplashScreen();
		out.println("");
		out.println(parserDir);
		FileInputStream fin = new FileInputStream(parserDir+"\\attest.properties");
		TCProperties = new Properties();
		TCProperties.load(fin);	
		
		tmpDir = new String(TCProperties.getProperty("TMPDIR"));
		out.println(tmpDir); 
		//TestRunner.createLogFile();
		TestRunner.out=out; 	
	    //Display display = Display.getDefault();
		
		
		thisClass.thisClass=thisClass;
		thisClass.runTest(args[0]);
		// thisClass.createSShell();
		// thisClass.closeSplashScreen();
		// thisClass.sShell.open();
		//System.out.println("Here1");
		TestRunner runner = new TestRunner(thisClass.testrun,thisClass);
		runner.runTest();
		//System.out.println("Here2");
		/* thisClass.paintTestrun(thisClass.testrun);
		while (!thisClass.sShell.isDisposed()) {
			if (!display.readAndDispatch()) 
				display.sleep();
			thisClass.repaint();    
		}
		display.dispose();
		thisClass.display1.dispose();*/
		//System.out.println("Here3");
	}
	
	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		Display display = Display.getDefault(); 
		sShell = new Shell(display);
		sShell.setText("Attest Agent");
		sShell.setSize(new org.eclipse.swt.graphics.Point(464,546));
	    Image img=new Image(display , TCAgentMain.getParserDir()+File.separator+"enticon.jpg");
		sShell.setImage(img);
		runIdLabel = new Label(sShell, SWT.RIGHT);
		runIdLabel.setBounds(new org.eclipse.swt.graphics.Rectangle(25,14,19,17));
		runIdLabel.setText("ID");
		suiteNameLabel = new Label(sShell, SWT.RIGHT); 
		suiteNameLabel.setBounds(new org.eclipse.swt.graphics.Rectangle(5,69,39,16));
		suiteNameLabel.setText ("Suite");
		lineLabel = new Label(sShell, SWT.SEPARATOR);
		lineLabel.setBounds(new org.eclipse.swt.graphics.Rectangle(26,105,406,1));
		
		group = new Group(sShell, SWT.NONE);
		//group.setText("Status");
		group.setBounds(new org.eclipse.swt.graphics.Rectangle(3,95,453,77));
		//group.setBackground(new Color (Display.getDefault(),153,153,255));
		


		Run_Id = new Text(sShell, SWT.BORDER);
		
		Run_Id.setBounds(new org.eclipse.swt.graphics.Rectangle(49,11,76,22));
		
		Run_Id.setEditable(false);
		Run_Id.setText("");
		Run_Id.setBackground(new Color (Display.getDefault(),192,192,192));
		text = new Text(sShell, SWT.BORDER);
		text.setBounds(new org.eclipse.swt.graphics.Rectangle(49,66,378,22));
		
		text.setEditable(false);
		text.setBackground(new Color (Display.getDefault(),192,192,192));
		//text.setForeground(new Color (Display.getDefault(),255, 255,  255));
		//Run_Id.setBackground(  new Color (Display.getDefault(),255, 255,  255));
		text.setBackground(  new Color (Display.getDefault(),192,192,192));
		
		
		
		
		createTestcases();
		totalLabel = new CLabel(group, SWT.NONE);
		totalLabel.setFont(new Font(sShell.getDisplay(),new FontData("Arial",8,SWT.BOLD)));
		passLabel = new CLabel(group, SWT.NONE);
		passLabel.setFont(new Font(sShell.getDisplay(),new FontData("Arial",8,SWT.BOLD)));
         
		
		
		
		
		
		
		pause_button = new Button(sShell, SWT.NONE);
		pause_button.setBounds(new org.eclipse.swt.graphics.Rectangle(220,486,46,22));
		pause_button.setText("Pause");
		/*button = new Button(sShell, SWT.NONE);
		button.setBounds(new org.eclipse.swt.graphics.Rectangle(230,489,38,22));
		button.setText("Pause");*/
		button1 = new Button(sShell, SWT.NONE);
		button1.setBounds(new org.eclipse.swt.graphics.Rectangle(279,486,46,22));
		button1.setText("Abort");
		button2 = new Button(sShell, SWT.NONE);
		button2.setBounds(new org.eclipse.swt.graphics.Rectangle(336,487,65,22));
		button2.setText("Show Log");
		tableLabel = new CLabel(sShell, SWT.NONE);
		tableLabel.setText("Testcase Details");
		tableLabel.setBounds(new org.eclipse.swt.graphics.Rectangle(5,186,126,17));
		runNamelabel = new Label(sShell, SWT.LEFT);
		runNamelabel.setBounds(new org.eclipse.swt.graphics.Rectangle(9,40,35,20));
		runNamelabel.setText("Name");
		runNamelabel.setFont(new Font(sShell.getDisplay(), new FontData("Arial", 10, SWT.NONE)));
		runName = new Text(sShell, SWT.BORDER);
		runName.setBounds(new org.eclipse.swt.graphics.Rectangle(49,39,378,22));
		runName.setFont(new Font(sShell.getDisplay(), new FontData("Arial", 10, SWT.NONE)));
		runName.setEditable(false);
		
		runName.setBackground(new Color(Display.getDefault(), 192,192,192));
		ExitButton = new Button(sShell, SWT.NONE);
		ExitButton.setBounds(new org.eclipse.swt.graphics.Rectangle(410,487,35,22));
		ExitButton.setText("Exit");
		
		createGroup();
		
		
		totalLabel.setText("Total");
		totalLabel.setFont(new Font(sShell.getDisplay(),new FontData("Arial",8,SWT.BOLD)));
		
		passLabel.setText("Pass");
		passLabel.setFont(new Font(sShell.getDisplay(),new FontData("Arial",8,SWT.BOLD)));
		
		
		passVal = new CLabel(group, SWT.SHADOW_IN);
		passVal.setFont(new Font(sShell.getDisplay(),new FontData("Arial",8,SWT.NONE)));
		/* if (Integer.parseInt(passVal.getText()) > 0){
		 passVal.setBackground(new Color (Display.getDefault(),0, 255,  0));
	        }*/
		
		
		failLabel = new CLabel(group, SWT.RIGHT);
		failLabel.setText("Fail");
		failLabel.setFont(new Font(sShell.getDisplay(),new FontData("Arial",8,SWT.BOLD)));
		
		failVal = new CLabel(group, SWT.SHADOW_IN);
		failVal.setBackground(new Color(Display.getDefault(), 192,192,192));
		failVal.setFont(new Font(sShell.getDisplay(),new FontData("Arial",8,SWT.NONE)));
		failVal.setAlignment(SWT.RIGHT);
		//System.out.println(passVal.getText());
		//System.out.println(failVal.getText());
		/*if (Integer.parseInt(failVal.getText()) > 0){
		 failVal.setBackground(new Color (Display.getDefault(),255, 0,  0));
		}*/
		
		failVal.setAlignment(SWT.LEFT);
		pendLabel = new CLabel(group, SWT.BOLD);
		pendLabel.setText("Pending");
		pendLabel.setAlignment(SWT.LEFT);
		pendLabel.setFont(new Font(sShell.getDisplay(),new FontData("Arial",8,SWT.BOLD)));		
		
		pendVal = new CLabel(group, SWT.SHADOW_IN);
		pendVal.setFont(new Font(sShell.getDisplay(),new FontData("Arial",8,SWT.NONE)));		
		
		percComplete = new CLabel(group, SWT.NONE);
		percComplete.setBounds(new org.eclipse.swt.graphics.Rectangle(420,17,26,22));
		totalVal = new CLabel(group, SWT.LEFT|SWT.SHADOW_IN);
		totalVal.setFont(new Font(sShell.getDisplay(),new FontData("Arial",8,SWT.NONE)));
		
		//System.out.println(totalVal.getText());
		progressBar = new ProgressBar(group, SWT.HORIZONTAL);
		progressBar.setMinimum(0);
		
		progressBar.setToolTipText("Status Bar");
		progressBar.setBounds(new org.eclipse.swt.graphics.Rectangle(9,14,409,28));
		progressBar.setMaximum(100);
		
		
		
		runstatus = new CLabel(group, SWT.BOLD);
		runstatus.setFont(new Font(sShell.getDisplay(),new FontData("Arial",8,SWT.BOLD)));		
	 	runstatus.setText("Status");

		
		runStatus = new Text(group, SWT.BORDER);
		runStatus.setFont(new Font(sShell.getDisplay(), new FontData("Arial", 8, SWT.BOLD)));
		runStatus.setBackground(new Color(Display.getDefault(), 255, 255, 255));
		
		runStatus.setEditable(false);
		runStatus.setBackground(new Color(Display.getDefault(), 192,192,192));
		
		runIdLabel.setFont(new Font(sShell.getDisplay(),new FontData("Arial",10,SWT.NONE)));
		suiteNameLabel.setFont(new Font(sShell.getDisplay(),new FontData("Arial",10,SWT.NONE)));

		Run_Id.setFont(new Font(sShell.getDisplay(),new FontData("Arial",10,SWT.NONE)));
		text.setFont(new Font(sShell.getDisplay(),new FontData("Arial",10,SWT.NONE)));
		tableLabel.setFont(new Font(sShell.getDisplay(),new FontData("Arial",10,SWT.BOLD)));
		//button1.addListener()
		
		
		runstatus.setBounds(new org.eclipse.swt.graphics.Rectangle(8,50,44,20));
		runStatus.setBounds(new org.eclipse.swt.graphics.Rectangle(51,50,76,20));
		
		totalLabel.setBounds(new org.eclipse.swt.graphics.Rectangle(170,50,36,20));
		totalVal.setBounds(new org.eclipse.swt.graphics.Rectangle(207,50,20,20));
		
		passLabel.setBounds(new org.eclipse.swt.graphics.Rectangle(232,50,36,20));
		passVal.setBounds(new org.eclipse.swt.graphics.Rectangle(270,50,20,20));
		
		failLabel.setBounds(new org.eclipse.swt.graphics.Rectangle(296,50,27,20));
		failVal.setBounds(new org.eclipse.swt.graphics.Rectangle(325,50,20,20));
		
		pendLabel.setBounds(new org.eclipse.swt.graphics.Rectangle(349,50,54,20));
		pendVal.setBounds(new org.eclipse.swt.graphics.Rectangle(405,50,20,20));		
		
		totalVal.setBackground(new Color(Display.getDefault(), 192,192,192));
		passVal.setBackground(new Color(Display.getDefault(), 192,192,192));
		pendVal.setBackground(new Color(Display.getDefault(), 192,192,192));
		
		
		
		pause_button.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(
					org.eclipse.swt.events.SelectionEvent e) {

				MessageBox mb = new MessageBox(sShell, SWT.OK);
				mb.setText("Alert");
		        mb.setMessage("The testrun has been paused. Pause will happen at the end of the testcase ");
		        mb.open();


				if (paused){
					paused=false;
					pause_button.setText("Pause");
				}
				else {
					paused=true;
					pause_button.setText("Continue");
				}
				
				
		     
			}
		});				
		button1.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(
					org.eclipse.swt.events.SelectionEvent e) {

				MessageBox mb = new MessageBox(sShell, SWT.OK);
				mb.setText("Alert");
		        mb.setMessage("The testrun has been aborted. Abort will happen at the end of the testcase ");
		        //mb.setMessage()
		        mb.open();
				aborted=true;
				button1.setEnabled(false);

			}
		});			
		button2.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(
					org.eclipse.swt.events.SelectionEvent e) {
				 TestRunner.out.flush();
				 Runtime rt = Runtime.getRuntime(); 
				 String cmd = "notepad.exe "+TestRunner.logFileName;
				 try {
				   Process proc = rt.exec(cmd);
				 } catch (IOException e1) {
					 e1.printStackTrace();
				 }
				 
			}
        
		});

		ExitButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(
					org.eclipse.swt.events.SelectionEvent e) {
				if (runStatus.getText().equals ("Completed") || runStatus.getText().equals ("Complete With Errors") ){
				System.exit(0);
				}
				else
				{
					System.exit(0);
						/*	MessageBox mb1 = new MessageBox(sShell, SWT.OK);
							mb1.setText("Alert");
					        mb1.setMessage("The program is still running.Cannot close the window ");
					        //mb.setMessage()	
					        mb1.open(); */
			}
			}		
        
		});
	}
	private  TestRunInfo testrun;
	private  TestcaseInfo testcase;
	private Label runIdLabel = null;
	private Label suiteNameLabel = null;
	private Label lineLabel = null;
	private ProgressBar progressBar = null;
	private Table testCasesTable = null;
	private Table Testcases = null;
	private Text Run_Id = null;
	private Text text = null;
	private CLabel cLabel = null;
	private CLabel totalLabel = null;
	private CLabel passLabel = null;
	private CLabel failLabel = null;
	private CLabel pendLabel = null;
	private CLabel totalVal = null;
	private CLabel failVal = null;
	private CLabel passVal = null;
	private CLabel pendVal = null;
	private CLabel percComplete = null;
	private Button pause_button = null;
	private Button button = null;
	private Button button1 = null;
	private Button button2 = null;
	private CLabel tableLabel = null;
	private Label runNamelabel = null;
	private Text runName = null;
	private CLabel runstatus = null;
	private Text runStatus = null;
	private Group group = null;
	private ProgressBar progressBar1 = null;
	private CLabel cLabel1 = null;
	private Button ExitButton = null;


	public static String strip( final String s )
	   {
	     String res = "";
	     for ( int i=0; i<s.length(); i++ )
	     {
	       if ( s.charAt(i)!=' ' )
	         res = res +  s.charAt(i);
	     }
	     return res;
	   }

	public static void exmlFix(String inExmlFile, String outExmlFile) throws IOException {
	      File f = new File(inExmlFile);
	      BufferedReader br = new BufferedReader(new FileReader(inExmlFile)); 
	     PrintWriter bw = new PrintWriter(new BufferedWriter(new FileWriter(outExmlFile))); 
	      String lin = br.readLine();
	      while (lin != null)  {
	        
	        if (!strip(lin).equals("")) 
	          bw.println(lin);
	        lin = br.readLine();
	      }
	      br.close();
	      bw.close();
	   
	    } 
	
	 public void runTest(String exmlFile) throws Exception {
	  
	  
	  File f = new File(exmlFile);
	  String outExmlFile = TCAgentMain.getTmpDir()+File.separator+"mod"+f.getName();
	  exmlFix(exmlFile,outExmlFile);	

		testrun = TestRunInfo.initRunInfo(outExmlFile);
		testrun.initializeTestRunStatus();
		testrun.setTCAgent(this);
		printTestrun();
		
		
		
	}
	 
	public void printTestrun() {
		

        TestRunner.out.println("TestRun");
        TestRunner.out.println("Run ID: "+testrun.getRunId());
        TestRunner.out.println("Run Name:"+testrun.getRunName());
        TestRunner.out.println(testrun.getSuiteName());
		Vector scenarios = testrun.getScenarios();
		
		TestRunner.out.println("Scenarios");
		TestRunner.out.println(scenarios.size());
		for (int j = 0; j < scenarios.size(); j++) {
            ScenarioInfo s1 = (ScenarioInfo) scenarios.elementAt(j);
            TestRunner.out.println("	"+s1.getScenarioName());
			
			Vector testcases = s1.getTestCases();
			TestRunner.out.println("	Testcases"); 
			for (int i = 0; i < testcases.size(); i++) {
			tst1 = (TestcaseInfo) testcases.elementAt(i);
		
			  TestRunner.out.println("		"+tst1.getTestcaseName());
			  TestRunner.out.println("		"+tst1.getStatus());
			}			
			Vector functions = s1.getFunctions();
			TestRunner.out.println("	Functions");
			for (int i = 0; i < functions.size(); i++) {
				FunctionInfo f1 = (FunctionInfo) functions.elementAt(i);
				TestRunner.out.println("		"+f1.getFunctionName());
				TestRunner.out.println("		"+f1.getFunctionType());
			}					
		}
		
		TestRunner.out.println("After Testruninfo");
		TestRunner.out.flush();
        
	}

	
}
