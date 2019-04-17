package com.ebla.viewone.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.filenet.api.collection.ObjectStoreSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.util.UserContext;
import com.ibm.ecm.security.Subject;

public class FNConnectionTest {
	
	
	public static List<String> TestFnConnection() throws IOException
	{
	    // Set connection parameters; substitute for the placeholders.
		String PARM_RESOURCE_BUNDLE_NAME = "com.ebla.viewone.services.resources.filenet_config";
		ClassLoader loader = GetWaterMarkAnnotations.class.getClassLoader();
		Locale currentLocale = Locale.ROOT;
		ResourceBundle props = ResourceBundle.getBundle(PARM_RESOURCE_BUNDLE_NAME, currentLocale, loader);
		String configFileLoc = props.getString("config_file_url");
	    File configFile = new File(configFileLoc);
	    List<String> results = new ArrayList<String>();

	    try {
	    	PropertiesConfiguration ex_properties = new PropertiesConfiguration(configFile);
		    String username = ex_properties.getString("AdminUser");
		    String password = ex_properties.getString("AdminPass");
		    String domainName = ex_properties.getString("DomainName");
		    String AnnotationsDocId = ex_properties.getString("AnnotationsDocId");
		    String Stanza = ex_properties.getString("Stanza");
		    String uri = ex_properties.getString("Uri");
		    Connection conn = Factory.Connection.getConnection(uri);
		    javax.security.auth.Subject subject = UserContext.createSubject(conn, username, password, Stanza);
		    UserContext.get().pushSubject(subject);

		    try
		    {
		       // Get default domain.
		       Domain domain = Factory.Domain.fetchInstance(conn, null, null);
		       System.out.println("Domain: " + domain.get_Name());
		       results.add("Domain: " + domain.get_Name());
		       // Get object stores for domain.
		       ObjectStoreSet osSet = domain.get_ObjectStores();
		       ObjectStore store;
		       Iterator osIter = osSet.iterator();

		       while (osIter.hasNext()) 
		       {
		          store = (ObjectStore) osIter.next();
		          System.out.println("Object store: " + store.get_Name());
		          results.add("Object store: " + store.get_Name());
		       }
		       System.out.println("Connection to Content Platform Engine successful");
		       results.add("Connection to Content Platform Engine successful");
		    }
		    finally
		    {
		       UserContext.get().popSubject();
		    }
		    

		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	    
	    // Make connection.
	    
	    return results;
	}

	public static void main(String[] args) {
		ImageIO.scanForPlugins();
		Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("TIF");
		while (readers.hasNext()) {
		    System.out.println("reader: " + readers.next());
		}

	}

}
