package entg.test.plugin.selenium;


import com.thoughtworks.selenium.*;
import java.io.*;
import java.util.*;

public interface EntgWDSeleniumTest {
 

	public Hashtable run(org.openqa.selenium.remote.RemoteWebDriver browser, Hashtable has, PrintWriter log) throws Exception;
}
