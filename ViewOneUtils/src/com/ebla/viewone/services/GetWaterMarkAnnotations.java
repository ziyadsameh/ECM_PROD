package com.ebla.viewone.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.CredentialExpiredException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.ebla.viewone.services.security.DocSecurityPermissionsEnum;
import com.ebla.viewone.services.security.SecurityPermissionsRetriever;
import com.ebla.viewone.services.security.SecurityPermissionsRetrieverImpl;
import com.filenet.api.collection.AnnotationSet;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.GroupSet;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.core.Annotation;
import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.security.Group;
import com.filenet.api.security.User;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;
import com.ibm.ecm.serviceability.Logger;
import com.ibm.ecm.util.p8.P8Annotation;
import com.ibm.ecm.util.p8.P8Connection;
import com.ibm.websphere.security.WSSecurityException;
import com.ibm.websphere.security.auth.WSSubject;
import com.ibm.websphere.security.cred.WSCredential;

public class GetWaterMarkAnnotations extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final static String sourceClass = GetWaterMarkAnnotations.class.getName();
	private String remoteAddr;
	private static  String adminUser;
	private static  String adminPass;
	private static  String domainName;
	private static  String AnnotationsDocId;
	private static  String Stanza;
	private static  String uri;
	private Subject subject = null;
	private String userID;
	private String watermarksPermission;
	private int noOfPages = 0;
	private static ResourceBundle filenet_props;

	private static  String docClassName = "";
	private static  String noPrintBtnGroup = "";
	private static  String annotationReadOnlyGroup = "";
	private static  String annotationReadWriteGroup = "";
	private static  String annotationsModifyGroup = "";
	
	private static final String ReadOnlyAnnotationPermission = AnnotationsPermissionsEnum.ReadOnlyAnnotationPermission.getAnnotationsPermission();
	private static final String ReadWriteAnnotationPermission = AnnotationsPermissionsEnum.ReadWriteAnnotationPermission.getAnnotationsPermission();
	private boolean noPermissionSelected = false;
	private User user = null;
	private String watermarkPosition = "";
	private static final String watermarkPositionOne = "1";
	private static final String watermarkPositionTwo = "2";
	private static final String watermarkPositionThree = "3";

	public GetWaterMarkAnnotations() throws Exception {
		String PARM_RESOURCE_BUNDLE_NAME = "com.ebla.viewone.services.resources.filenet_config";
		ClassLoader loader = GetWaterMarkAnnotations.class.getClassLoader();
		Locale currentLocale = Locale.ROOT;
		filenet_props = ResourceBundle.getBundle(PARM_RESOURCE_BUNDLE_NAME, currentLocale, loader);
		adminUser = filenet_props.getString("AdminUser");
		adminPass = filenet_props.getString("AdminPass");
		domainName = filenet_props.getString("DomainName");
		AnnotationsDocId = filenet_props.getString("AnnotationsDocId");
		Stanza = filenet_props.getString("Stanza");
		uri = filenet_props.getString("Uri");
	}
	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	    System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");	    
		Set<String> watermarkID = new HashSet<String>();
	    //new code
	    remoteAddr = request.getRemoteAddr();
		
		String docid = "";
		String osName = "";
		String osId = "";
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		Map params = request.getParameterMap();
	    
		System.out.println("Printing Daeja Request Parameters:::::");
		Iterator i = params.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			String value = ((String[]) params.get(key))[0];
			System.out.println("Daeja Request Key:\t" + key + "  Daeja Request Value:\t" + value);
		}
			    
	    if(params.get("watermarkPosition") != null){
	    	watermarkPosition =((String[]) params.get( "watermarkPosition" ))[ 0 ] ;
	    	System.out.println("watermarkPosition is : "+watermarkPosition);
	    }
	    
	    if(params.get("docid") != null){
	    	docid = (((String[]) params.get( "docid" ))[ 0 ]).split(",")[2] ;
	    	osId = (((String[]) params.get( "docid" ))[ 0 ]).split(",")[1];
	    }
	    
	    
	    if(params.get("noOfPages") != null){
	    	String noOfPagesString = ((String[]) params.get( "noOfPages" ))[ 0 ] ;
	    	noOfPages = Integer.parseInt(noOfPagesString);
	    	System.out.println("noOfPagesString is : "+noOfPagesString);
	    	System.out.println("noOfPages is : "+noOfPages);
	    }

	    if(params.get("template_name") != null){
	    	 docClassName = ((String[]) params.get( "template_name" ))[ 0 ] ;
	    	 System.out.println("docClassName is : "+docClassName);
	    }


	    Connection conn = Factory.Connection.getConnection(uri);
		user = Factory.User.fetchCurrent(conn, null);

		// Make connection.
		try {
			
			subject = WSSubject.getCallerSubject();
			Set<Object> creds = subject.getPublicCredentials();
			//WSCredentialImpl credential = (WSCredentialImpl) creds.iterator().next();
			userID = ((WSCredential) creds.iterator().next()).getSecurityName();
		} 
		catch (CredentialExpiredException e) {
			e.printStackTrace();
		}

		catch (WSSecurityException e1) {
		    e1.printStackTrace();
		}
		
	    try
	    {
			UserContext.get().pushSubject(subject);
	    	Domain domain = Factory.Domain.getInstance(conn, domainName);
		    //ObjectStore objectStore = Factory.ObjectStore.getInstance(domain, osName);
		    ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, osId, null);
		    osName = objectStore.get_Name();
		    PropertyFilter pf = new PropertyFilter();
		    pf.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.ANNOTATIONS, null));
		    pf.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.CONTENT_ELEMENTS, null));
		    pf.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.NAME, null));

		    Document doc = Factory.Document.fetchInstance(objectStore,new Id(docid) , pf);
		    
		    //get sec permissions 
			SecurityPermissionsRetriever secPersRetriever = new SecurityPermissionsRetrieverImpl();
			Map<Integer,String> secPermissionsMap = new HashMap<Integer,String>();
			secPermissionsMap = secPersRetriever.getDocSecPermission(doc);
			
			noPrintBtnGroup = secPermissionsMap.get(DocSecurityPermissionsEnum.VIEW_SEC_PERMISSION.value);
			annotationReadOnlyGroup = secPermissionsMap.get(DocSecurityPermissionsEnum.PRINT_SEC_PERMISSION.value);
			annotationReadWriteGroup = secPermissionsMap.get(DocSecurityPermissionsEnum.PRINTW_SEC_PERMISSION.value);
			annotationsModifyGroup = secPermissionsMap.get(DocSecurityPermissionsEnum.MODIFY_SEC_PERMISSION.value);

			System.out.println("noPrintBtnGroup: "+noPrintBtnGroup);
			System.out.println("annotationReadOnlyGroup: "+annotationReadOnlyGroup);
			System.out.println("annotationReadWriteGroup: "+ annotationReadWriteGroup);
			System.out.println("annotationsModifyGroup: "+annotationsModifyGroup);

		    //set watermark positions
		    if(params.get("watermarksPermission") != null){
		    	watermarksPermission =  ((String[]) params.get( "watermarksPermission" ))[ 0 ] ;
		    	System.out.println("Request watermarksPermission is --->> "+watermarksPermission);
		    } else {
		    	
				GroupSet groups = user.get_MemberOfGroups();
				List<String> groupNames = new ArrayList<String>();
				Iterator iterator = groups.iterator();
					while(iterator.hasNext()) {
						
					Group group = (Group) iterator.next();
					System.out.println("Security Group:\t" + group.get_DisplayName());
					groupNames.add(group.get_DisplayName());
					
				}
					
				//if user is member of view and printW groups
				if(groupNames.contains(annotationReadWriteGroup)) {
						System.out.println("Get Watermark Daeja Permission: Show Print Btn: Read Write Annotation");
						watermarksPermission = ReadWriteAnnotationPermission;
						noPermissionSelected = true;
				}
	
				//if user is member of view and print groups	
				else if(groupNames.contains(annotationReadOnlyGroup)) {
					System.out.println("Get Watermark Daeja Permission: Show Print Btn: Read Only Annotation");
					watermarksPermission = ReadOnlyAnnotationPermission;
					noPermissionSelected = true;
				}
				//if user is member of view group only
				else 
				{
				System.out.println("Get Watermark Daeja Permission: Don't Show Print Btn: Read Only Annotation");
				watermarksPermission = ReadOnlyAnnotationPermission;
				noPermissionSelected = true;
				}
				
				if(!noPermissionSelected) {
						System.out.println("Get Watermark : Daeja Permission: noPermissionSelected");
						watermarksPermission = ReadOnlyAnnotationPermission;
					}

		    }
		    
		    // Get doc pages count
		    ContentElementList docContentList = doc.get_ContentElements();
		    Iterator iter = docContentList.iterator();
		    while (iter.hasNext())
		    {
		        ContentTransfer ct = (ContentTransfer) iter.next();
		        DocumentPagesCounter dpc = new DocumentPagesCounter(ct);
		        if(noOfPages == 0) {
			        noOfPages = dpc.getDocPagesCount();
		        }
		        System.out.println("No of Pages in the document is ---> "+noOfPages);
		    }
		    // end Get doc pages count

		    P8Connection p8Connection; 
		    p8Connection = new P8Connection(conn, userID, subject, domain, osName, "", false, null);
		    List<P8Annotation> documentAnnotations = P8Annotation.getDocumentAnnotations(request, p8Connection, doc);
		    List<Annotation> watermarks = getWatermark(objectStore);
		    Iterator iterator = watermarks.iterator();
		    int count = 0;
		    while(iterator.hasNext())
		    {
				Annotation anno = (Annotation) iterator.next();
				watermarkID.add(anno.get_Id().toString());
				P8Annotation p8Annotation = new P8Annotation(request, p8Connection, anno);
				documentAnnotations.add(p8Annotation);
				count++;
		    	if(count >= noOfPages) break;
			}
		   
		    AnnotationExporter annotationExporter = new AnnotationExporter(documentAnnotations, Logger.getLoggingContext(request));
		    try {
				AnnotationExporter.initialize(getServletContext());
			    response.setContentType("application/octet-stream");
			    OutputStream outputStream = response.getOutputStream();
			    annotationExporter.export(outputStream,watermarkID,watermarksPermission);
			    
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		    
	    }
	    finally
	    {
	    	
	 	       UserContext.get().popSubject();
	    	
	    }

	    //end new code
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	
	}
	
	private List<Annotation> getWatermark(ObjectStore objectStore ){
		
		List<Annotation> result = new ArrayList<Annotation>();
		
	    PropertyFilter pf = new PropertyFilter();
	    pf.addIncludeProperty(new FilterElement(null, null, null, "Annotations", null));
	    
	    Document doc = Factory.Document.fetchInstance(objectStore,new Id(AnnotationsDocId) , pf);
	    
	    AnnotationSet annotationSet = doc.get_Annotations();
	    int count = 1;
	    for (Iterator iterator = annotationSet.iterator(); iterator.hasNext();) {
			Annotation annotation = (Annotation) iterator.next();
			result.add(getAnnotationContent(annotation, count));
			count = count + 1;
			if(count==10) break;
		}
	    
		
		return result;
	}


	private Annotation getAnnotationContent(Annotation annObject, int pageNumber){
		
		Date date = Calendar.getInstance().getTime();		
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm");
		String today = formatter.format(date);
		Set<Principal> Principals = subject.getPrincipals();
		Iterator itr = Principals.iterator();
		Principal Principal = (java.security.Principal) itr.next();
		String annoString = "";
		if(watermarkPosition.equalsIgnoreCase(watermarkPositionOne)) {
	    	System.out.println("puttin watermark in position 1");
			annoString = "<FnAnno STATE='add'>" + 
					"<PropDesc F_ANNOTATEDID='%ID%' F_BACKCOLOR='16777215' F_BORDER_BACKMODE='2'"
					+ " F_BORDER_COLOR='15395562' F_BORDER_STYLE='0' F_BORDER_WIDTH='1' F_CLASSID='{5CF1194C-018F-11D0-A87A-00A0246922A5}'"
					+ " F_CLASSNAME='Stamp' F_CREATOR='%CREATOR%' F_ENTRYDATE='2019-02-05T03:02:16.0000000-08:00' F_FONT_BOLD='true'"
					+ " F_FONT_ITALIC='false' F_FONT_NAME='arial' F_FONT_SIZE='27' F_FONT_STRIKETHROUGH='false'"
					+ " F_FONT_UNDERLINE='false' F_FORECOLOR='15395562' F_HASBORDER='true' F_HEIGHT='0'"
					+ " F_ID='%ID%' F_LEFT='0.7721518987341772'"
					+ " F_MODIFYDATE='2019-02-05T03:03:45.0000000-08:00' F_MULTIPAGETIFFPAGENUMBER='%NO%'"
					+ " F_NAME='-1-{BED09072-6C3F-C516-8447-68BD53500000}' F_ORDINAL='2' "
					+ "F_PAGENUMBER='%NO%' F_ROTATION='315' F_TEXT_BACKMODE='1' F_TOP='0.510548523206751' F_WIDTH='0'> " + 
					" <F_CUSTOM_BYTES/>" + 
					" <F_POINTS/>" + 
					" <F_TEXT Encoding='unicode'>%MESG%</F_TEXT>" + 
					" </PropDesc>" + 
					" </FnAnno>";
			
			
		}
		else if(watermarkPosition.equalsIgnoreCase(watermarkPositionTwo)) {
	    	System.out.println("puttin watermark in position 2");
			annoString = "<FnAnno STATE='add'>" + 
					"<PropDesc F_ANNOTATEDID='%ID%' F_BACKCOLOR='16777215' F_BORDER_BACKMODE='2' F_BORDER_COLOR='15395562' "
					+ " F_BORDER_STYLE='0' F_BORDER_WIDTH='1' F_CLASSID='{5CF1194C-018F-11D0-A87A-00A0246922A5}' "
					+ " F_CLASSNAME='Stamp' F_CREATOR='%CREATOR%' F_ENTRYDATE='2019-02-05T03:02:16.0000000-08:00' "
					+ " F_FONT_BOLD='true' F_FONT_ITALIC='false' F_FONT_NAME='arial' F_FONT_SIZE='27'"
					+ " F_FONT_STRIKETHROUGH='false' F_FONT_UNDERLINE='false' F_FORECOLOR='15395562' "
					+ " F_HASBORDER='true' F_HEIGHT='0' F_ID='%ID%'"
					+ " F_LEFT='2.4472573839662446' F_MODIFYDATE='2019-02-05T03:08:11.0000000-08:00'"
					+ " F_MULTIPAGETIFFPAGENUMBER='%NO%' F_NAME='-1-{BED09072-6C3F-C516-8447-68BD53500000}'"
					+ " F_ORDINAL='1' F_PAGENUMBER='1' F_ROTATION='315' F_TEXT_BACKMODE='1'"
					+ " F_TOP='0.05907172995780591' F_WIDTH='0'>" + 
					" <F_CUSTOM_BYTES/>" + 
					" <F_POINTS/>" + 
					" <F_TEXT Encoding='unicode'>%MESG%</F_TEXT>" + 
					" </PropDesc>" + 
					" </FnAnno>";
		}
		else if(watermarkPosition.equalsIgnoreCase(watermarkPositionThree)) {
	    	System.out.println("puttin watermark in position 3");
			annoString = "<FnAnno STATE='add'>" + 
					"<PropDesc F_ANNOTATEDID='%ID%' F_BACKCOLOR='16777215' F_BORDER_BACKMODE='2' F_BORDER_COLOR='15395562'"
					+ " F_BORDER_STYLE='0' F_BORDER_WIDTH='1' F_CLASSID='{5CF1194C-018F-11D0-A87A-00A0246922A5}'"
					+ " F_CLASSNAME='Stamp' F_CREATOR='%CREATOR%' F_ENTRYDATE='2019-02-05T03:02:16.0000000-08:00' "
					+ " F_FONT_BOLD='true' F_FONT_ITALIC='false' F_FONT_NAME='arial' F_FONT_SIZE='27'"
					+ " F_FONT_STRIKETHROUGH='false' F_FONT_UNDERLINE='false' F_FORECOLOR='15395562'"
					+ " F_HASBORDER='true' F_HEIGHT='0' F_ID='%ID%'"
					+ " F_LEFT='0.7426160337552743' F_MODIFYDATE='2019-02-05T03:09:19.0000000-08:00'"
					+ " F_MULTIPAGETIFFPAGENUMBER='%NO%' F_NAME='-1-{BED09072-6C3F-C516-8447-68BD53500000}'"
					+ " F_ORDINAL='1' F_PAGENUMBER='%NO%' F_ROTATION='315' F_TEXT_BACKMODE='1'"
					+ " F_TOP='2.0337552742616034' F_WIDTH='0'>" + 
					" <F_CUSTOM_BYTES/>" + 
					" <F_POINTS/>" + 
					" <F_TEXT Encoding='unicode'>%MESG%</F_TEXT>" + 
					" </PropDesc>" + 
					" </FnAnno>";
		}
		else {
	    	System.out.println("puttin watermark in default position 1");
			annoString = "<FnAnno STATE='add'>" + 
					"<PropDesc F_ANNOTATEDID='%ID%' F_BACKCOLOR='16777215' F_BORDER_BACKMODE='2'"
					+ " F_BORDER_COLOR='15395562' F_BORDER_STYLE='0' F_BORDER_WIDTH='1' F_CLASSID='{5CF1194C-018F-11D0-A87A-00A0246922A5}'"
					+ " F_CLASSNAME='Stamp' F_CREATOR='%CREATOR%' F_ENTRYDATE='2019-02-05T03:02:16.0000000-08:00' F_FONT_BOLD='true'"
					+ " F_FONT_ITALIC='false' F_FONT_NAME='arial' F_FONT_SIZE='27' F_FONT_STRIKETHROUGH='false'"
					+ " F_FONT_UNDERLINE='false' F_FORECOLOR='15395562' F_HASBORDER='true' F_HEIGHT='0'"
					+ " F_ID='%ID%' F_LEFT='0.7721518987341772'"
					+ " F_MODIFYDATE='2019-02-05T03:03:45.0000000-08:00' F_MULTIPAGETIFFPAGENUMBER='%NO%'"
					+ " F_NAME='-1-{BED09072-6C3F-C516-8447-68BD53500000}' F_ORDINAL='2' "
					+ "F_PAGENUMBER='%NO%' F_ROTATION='315' F_TEXT_BACKMODE='1' F_TOP='0.510548523206751' F_WIDTH='0'> " + 
					" <F_CUSTOM_BYTES/>" + 
					" <F_POINTS/>" + 
					" <F_TEXT Encoding='unicode'>%MESG%</F_TEXT>" + 
					" </PropDesc>" + 
					" </FnAnno>";
		}
		
		 /*annoString = "<FnAnno STATE='add'>"+
		"<PropDesc F_ANNOTATEDID='%ID%' "
		+ "F_BACKCOLOR='16777215' F_BORDER_BACKMODE='2' F_BORDER_COLOR='14013909' F_BORDER_STYLE='0' "
		+ " F_BORDER_WIDTH='1' F_CLASSID='{5CF1194C-018F-11D0-A87A-00A0246922A5}' "
		+ "F_CLASSNAME='Stamp' F_CREATOR='%CREATOR%' F_ENTRYDATE='2019-01-12T23:06:28.0000000-08:00' "
		+ "F_FONT_BOLD='true' F_FONT_ITALIC='false' F_FONT_NAME='arial' F_FONT_SIZE='27' "
		+ "F_FONT_STRIKETHROUGH='false' F_FONT_UNDERLINE='false' F_FORECOLOR='14013909' "
		+ "F_HASBORDER='true' F_HEIGHT='0' F_ID='%ID%'"
		+ " F_LEFT='0.8778625954198473' F_MODIFYDATE='2019-01-12T23:07:36.0000000-08:00'"
		+ " F_MULTIPAGETIFFPAGENUMBER='%NO%' F_NAME='-1-{BED09072-6C3F-C516-8447-68BD53500000}'"
		+ " F_ORDINAL='2' F_PAGENUMBER='%NO%' F_ROTATION='315'"
		+ " F_TEXT_BACKMODE='1' F_TOP='0.2366412213740458' F_WIDTH='0'> "
		+ "<F_CUSTOM_BYTES/>"
		+"<F_POINTS/>"
		+"<F_TEXT Encoding='unicode'>%MESG%</F_TEXT>"
		+"</PropDesc>"
		+"</FnAnno>";*/

		annoString = annoString.replaceAll("%ID%", annObject.get_Id().toString());
		annoString = annoString.replace("%NO%", pageNumber+"");
		annoString = annoString.replace("%MESG%",Principal.getName().split("/")[1] +" , "+ remoteAddr + " , "+ today );
		annoString = annoString.replace("%CREATOR%",userID);

		
		ContentElementList contentElementList = Factory.ContentElement.createList();
		ContentTransfer ctObject = Factory.ContentTransfer.createInstance();
		
		InputStream bis = new ByteArrayInputStream(annoString.getBytes());
		ctObject.setCaptureSource(bis);
		
		contentElementList.add(ctObject);
		annObject.set_ContentElements(contentElementList);
		return annObject;
		
	}
	
	
	public static void main(String[] args) {
		/*String PARM_RESOURCE_BUNDLE_NAME = "com.ebla.viewone.services.resources.filenet_config";
		ClassLoader loader = GetWaterMarkAnnotations.class.getClassLoader();
		Locale currentLocale = Locale.ROOT;
		ResourceBundle props = ResourceBundle.getBundle(PARM_RESOURCE_BUNDLE_NAME, currentLocale, loader);
	    adminUser = props.getString("AdminUser");
		adminPass = props.getString("AdminPass");
		domainName = props.getString("DomainName");
		AnnotationsDocId = props.getString("AnnotationsDocId");
		Stanza = props.getString("Stanza");
		uri = props.getString("Uri");
	    System.out.print(props.getKeys().toString());*/
		String ReadOnlyAnnotationPermission = AnnotationsPermissionsEnum.ReadOnlyAnnotationPermission.getAnnotationsPermission();
		String ReadWriteAnnotationPermission = AnnotationsPermissionsEnum.ReadWriteAnnotationPermission.getAnnotationsPermission();
		System.out.println(ReadOnlyAnnotationPermission);
		System.out.println(ReadWriteAnnotationPermission);


	}
	
}


 