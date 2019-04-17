package com.ebla.viewone.creator;


import java.io.File;

import javax.security.auth.Subject;

import org.apache.commons.configuration.PropertiesConfiguration;

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

public class AnnotationCreator {

	public static void main(String[] args)  throws Exception   {

		String uri = "http://localhost:9080/wsi/FNCEWS40MTOM";
		Connection conn = Factory.Connection.getConnection(uri);
		Domain domain = Factory.Domain.getInstance(conn, "IBM ECM");
		UserContext uc = UserContext.get();
		Subject subject = UserContext.createSubject(conn, "p8admin", "filenet", "FileNetP8WSI");
		uc.pushSubject(subject);
		
		//PropertyFilter osPF = new PropertyFilter();    
		//osPF.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.CLASS_DEFINITION, null) );
		ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, "CMTOS",null);
	    Folder annotationsFolder = null;
	    annotationsFolder = Factory.Folder.fetchInstance(objectStore, "/WatermarkAnnotationsFolder", null);
	    
	    if(annotationsFolder == null) {
	    	Factory.Folder.createInstance(objectStore, null);
		    // Specify the parent folder. Here a top folder is created.
		    annotationsFolder.set_Parent(objectStore.get_RootFolder());
		    annotationsFolder.set_FolderName("WatermarkAnnotationsFolder");
		    annotationsFolder.save(RefreshMode.REFRESH);
	    }
	    
	    
	    Document doc = Factory.Document.createInstance(objectStore, "Document");
	    doc.getProperties().putValue("DocumentTitle", "AnnotationsDummyDoc");
	    doc.checkin(AutoClassify.AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
	    doc.save(RefreshMode.REFRESH);
		PropertyFilter docPF = new PropertyFilter();    
		docPF.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.ID, null) );
	    doc.fetchProperties(docPF);
	    System.out.println("Annotations Dummy Doc Id is : " + doc.get_Id());
	    String generatedDocId = doc.get_Id().toString();
	    
	    File file = new File("src/com/ebla/config/config.properties");
	    PropertiesConfiguration properties = new PropertiesConfiguration(file);
        properties.setProperty("AnnotationsDocId", generatedDocId);
        properties.save();
        System.out.println("config.properties updated Successfully!!");
		
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
		
		//test read document Annotation
		
	    // Creates a top folder. You must explicitly set the Parent and FolderName properties  
	    // when you use Folder.createInstance.
	    
		/*PropertyFilter docPF = new PropertyFilter();    
		docPF.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.CLASS_DEFINITION, null) );
		Document testDoc = Factory.Document.fetchInstance(objectStore, new Id("{D472B635-4F85-CC54-87E6-68C6D6D00000}"),docPF );
		PropertyFilter docClassPF = new PropertyFilter();    
		docPF.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.DEFAULT_INSTANCE_PERMISSIONS, null) );
		ClassDefinition testClassDefinition = Factory.ClassDefinition.fetchInstance(objectStore, testDoc.getClassName(), docClassPF);		
		AccessPermissionList accessPermissionList = testClassDefinition.get_DefaultInstancePermissions();
		Iterator accessPermissionListIter = accessPermissionList.iterator();

		
		while(accessPermissionListIter.hasNext()) {
			AccessPermission ap = (AccessPermission)accessPermissionListIter.next();
			System.out.println(ap.get_GranteeName().substring(0, ap.get_GranteeName().indexOf("@")));
		}*/
		
		//end test
		
		uc.popSubject();

	}

}
