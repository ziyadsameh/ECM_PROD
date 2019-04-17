package com.ebla.viewone.creator;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.ebla.viewone.services.GetWaterMarkAnnotations;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.DefineSecurityParentage;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Annotation;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.DynamicReferentialContainmentRelationship;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.ReferentialContainmentRelationship;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;

/**
 * Servlet implementation class CreateAnnotationsDoc
 */
@WebServlet("/CreateAnnotationsDoc")
public class CreateAnnotationsDoc extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateAnnotationsDoc() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String PARM_RESOURCE_BUNDLE_NAME = "com.ebla.viewone.services.resources.filenet_config";
		ClassLoader loader = AnnotationCreator.class.getClassLoader();
		Locale currentLocale = Locale.ROOT;
		ResourceBundle rs = ResourceBundle.getBundle(PARM_RESOURCE_BUNDLE_NAME, currentLocale, loader);
		String configFileLoc = rs.getString("config_file_url");
	    File configFile = new File(configFileLoc);
	    PropertiesConfiguration properties = null;
		try {
			properties = new PropertiesConfiguration(configFile);
	        System.out.println("config.properties loaded Successfully!!");

		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		
		String uri = properties.getString("Uri");
		Connection conn = Factory.Connection.getConnection(uri);
		Domain domain = Factory.Domain.getInstance(conn, properties.getString("DomainName"));
		UserContext uc = UserContext.get();
		Subject subject = UserContext.createSubject(conn, properties.getString("AdminUser"), properties.getString("AdminPass"), properties.getString("Stanza"));
		uc.pushSubject(subject);
		
		ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, properties.getString("ObjectStoreName"),null);
	    Folder annotationsFolder = null;
	    annotationsFolder = Factory.Folder.fetchInstance(objectStore, "/"+properties.getString("FolderName"), null);
	    
	    if(annotationsFolder == null) {
	    	Factory.Folder.createInstance(objectStore, null);
		    annotationsFolder.set_Parent(objectStore.get_RootFolder());
		    annotationsFolder.set_FolderName(properties.getString("FolderName"));
		    annotationsFolder.save(RefreshMode.REFRESH);
	    }
	    
	    
	    Document doc = Factory.Document.createInstance(objectStore, "Document");
	    doc.getProperties().putValue("DocumentTitle", properties.getString("AnnotationsDocName"));
	    doc.checkin(AutoClassify.AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
	    doc.save(RefreshMode.REFRESH);
		PropertyFilter docPF = new PropertyFilter();    
		docPF.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.ID, null) );
	    doc.fetchProperties(docPF);
	    System.out.println("Annotations Dummy Doc Id is : " + doc.get_Id());
	    String generatedDocId = doc.get_Id().toString();
	    
        try {
	        properties.setProperty("AnnotationsDocId", generatedDocId);
			properties.save();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		ReferentialContainmentRelationship drcr = 
			(DynamicReferentialContainmentRelationship)annotationsFolder.file(doc,
					AutoUniqueName.AUTO_UNIQUE, 
					"AnnotationsDummyDoc", DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
		drcr.save(RefreshMode.REFRESH);
	    
		for (int i = 0; i < 100; i++) {
		
			Annotation annObject = Factory.Annotation.createInstance(objectStore, "Annotation", Id.createId());
			annObject.set_DescriptiveText("Annotation applied to the document's 1st content element.");
			annObject.set_AnnotatedObject(doc);
			annObject.save(RefreshMode.REFRESH);
			
			System.out.println(annObject.get_Id());
		}
		
		
		uc.popSubject();
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	
	public static void main(String[] args) {
		
		
		
		
	}

}
