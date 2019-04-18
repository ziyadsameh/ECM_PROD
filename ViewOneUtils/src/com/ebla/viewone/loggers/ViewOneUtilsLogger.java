package com.ebla.viewone.loggers;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;


public class ViewOneUtilsLogger {

    // static variable single_instance of type Singleton 
    private static ViewOneUtilsLogger single_instance = null; 
    
    // private constructor restricted to this class itself 
    private ViewOneUtilsLogger() 
    { 
    	DOMConfigurator.configure("C:\\ViewOneUtils\\log4j.xml");
    } 
  
    // static method to create instance of Singleton class 
    public static ViewOneUtilsLogger getInstance() 
    { 
        if (single_instance == null) 
            single_instance = new ViewOneUtilsLogger();
        
        return single_instance; 
    } 
    
    public static Logger getLogger(Class<?> className){
        if (single_instance == null) 
            single_instance = new ViewOneUtilsLogger(); 
    	Logger	logger	= Logger.getLogger(className);
    	return logger;
    }
    	
}
