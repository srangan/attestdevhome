package entg.test.plugin.selenium;


import com.thoughtworks.selenium.*;
import java.io.*;
import java.util.*;

public interface EntgSeleniumTest {
 

	public Hashtable run(Selenium browser, Hashtable has, PrintWriter log) throws Exception;
}
