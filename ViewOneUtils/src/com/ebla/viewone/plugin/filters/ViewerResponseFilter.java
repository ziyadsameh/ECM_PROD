package com.ebla.viewone.plugin.filters;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.ebla.viewone.loggers.ViewOneUtilsLogger;
import com.ebla.viewone.services.security.DocSecurityPermissionsEnum;
import com.ebla.viewone.services.security.SecurityPermissionsRetriever;
import com.ebla.viewone.services.security.SecurityPermissionsRetrieverImpl;
import com.filenet.api.collection.GroupSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.security.Group;
import com.filenet.api.security.User;
import com.ibm.ecm.extension.PluginResponseFilter;
import com.ibm.ecm.extension.PluginServiceCallbacks;
import com.ibm.ecm.json.JSONViewoneBootstrapResponse;
import com.ibm.json.java.JSONObject;


public class ViewerResponseFilter extends PluginResponseFilter {

	private Logger logger = ViewOneUtilsLogger.getLogger(ViewerResponseFilter.class);
	private static final String ReadOnlyAnnotationPermission = "Read";
	private static final String ReadWriteAnnotationPermission = "Read/Write";
	private String annotationPermission;
	private boolean noPermissionSelected = false;

	public String[] getFilteredServices() {
		return new String[] { "/p8/getViewoneBootstrap" };
	}

	
	public void filter(String serverType, PluginServiceCallbacks callbacks,
			HttpServletRequest request, JSONObject jsonResponse) throws Exception {
		
		logger.info("Server Base Url:\t" + callbacks.getServerBaseUrl());		
		logger.info("**********************************************************************************");


			if (jsonResponse instanceof JSONViewoneBootstrapResponse) {
				Map params = request.getParameterMap();
				logger.info("**********************************************************************************");				
				JSONViewoneBootstrapResponse jvbr = (JSONViewoneBootstrapResponse) jsonResponse;
				JSONObject viewOneBootstrap = (JSONObject) jvbr.get("viewOneBootstrap");
				logger.info("viewOneBootstrap-->" + jvbr.toString());
				Iterator i = params.keySet().iterator();
				while (i.hasNext()) {
					String key = (String) i.next();
					String value = ((String[]) params.get(key))[0];
					logger.info("Key:\t" + key + "            Value:\t" + value);
				}

				String repositoryId = (String) request.getParameter("repositoryId");
				String watermarkPosition = "";
			    if(request.getParameter("watermarkPosition") != null){
			    	watermarkPosition = request.getParameter("watermarkPosition");
			    }
			    logger.info("repositoryId:\t" + repositoryId);
				String docUrl  = request.getParameter("docUrl");
				docUrl = URLDecoder.decode(docUrl);
				logger.info("getDocUrl:\t" + docUrl);
				String[] docUrlParamaters = docUrl.split("&");
				String objectStoreName = "";
				String docClassName = "";
				String vsId = "";
				String documentId = "";
				
				for(String docUrlParamater: docUrlParamaters ){
					if(docUrlParamater.contains("objectStoreName")){
						objectStoreName = docUrlParamater.substring(docUrlParamater.indexOf("&objectStoreName=")+17);
					}
					if(docUrlParamater.contains("template_name")){
						docClassName = docUrlParamater.substring(docUrlParamater.indexOf("&template_name=")+15);
					}
					if(docUrlParamater.contains("vsId")){
						vsId = docUrlParamater.substring(docUrlParamater.indexOf("&vsId=")+7);
					}
					if(docUrlParamater.contains("docid")){
						documentId = docUrlParamater.substring(docUrlParamater.indexOf("docid=")+7);
						documentId = documentId.split(",")[2];
					}
				}
				 
				logger.info("getVsId:\t" + vsId);
				logger.info("getDocumentId:\t" + documentId);
				logger.info("getP8ObjectStore:\t" + objectStoreName);
				logger.info("getDocClassName:\t" + docClassName);



				Connection connection = callbacks.getP8Connection(repositoryId);
				ObjectStore objectStore = callbacks.getP8ObjectStore(repositoryId);
				Document doc = Factory.Document.fetchInstance(objectStore, documentId, null);
				//Document doc = callbacks.getP8Document(repositoryId, documentId, vsId, "current");
				SecurityPermissionsRetriever secPersRetriever = new SecurityPermissionsRetrieverImpl();
				Map<Integer,String> secPermissionsMap = new HashMap<Integer,String>();
				secPermissionsMap = secPersRetriever.getDocSecPermission(doc);
				
				String noPrintBtnGroup = secPermissionsMap.get(DocSecurityPermissionsEnum.VIEW_SEC_PERMISSION.value);
				String annotationReadOnlyGroup = secPermissionsMap.get(DocSecurityPermissionsEnum.PRINT_SEC_PERMISSION.value);
				String annotationReadWriteGroup = secPermissionsMap.get(DocSecurityPermissionsEnum.PRINTW_SEC_PERMISSION.value);
				String annotationsModifyGroup = secPermissionsMap.get(DocSecurityPermissionsEnum.MODIFY_SEC_PERMISSION.value);

				logger.info("noPrintBtnGroup: "+noPrintBtnGroup);
				logger.info("annotationReadOnlyGroup: "+annotationReadOnlyGroup);
				logger.info("annotationReadWriteGroup: "+ annotationReadWriteGroup);
				logger.info("annotationsModifyGroup: "+annotationsModifyGroup);

				User user = Factory.User.fetchCurrent(connection, null);
				GroupSet groups = user.get_MemberOfGroups();
				List<String> groupNames = new ArrayList<String>();
				Iterator iterator = groups.iterator();
					while(iterator.hasNext()) {
						
					Group group = (Group) iterator.next();
					logger.info("Security Group:\t" + group.get_DisplayName());
					groupNames.add(group.get_DisplayName());
				}
					
					

					//if user is member of view and printW groups
					 if(groupNames.contains(annotationReadWriteGroup)) {
						logger.info("Daeja Permission: Show Print Btn : Read Write Annotation");
						//jvbr.setViewOneParameter("printButtons", "true");
						annotationPermission = ReadWriteAnnotationPermission;
						//jvbr.setAnnotationHideButtons("show,save");
						noPermissionSelected = true;
					}

					
					//if user is member of view and print groups
					else if(groupNames.contains(annotationReadOnlyGroup)) {
						logger.info("Daeja Permission: Show Print Btn : Read Only Annotation");
						//jvbr.setViewOneParameter("printButtons", "true");
						annotationPermission = ReadOnlyAnnotationPermission;
						//jvbr.setAnnotationHideButtons("show");
						noPermissionSelected = true;
					}
					
				//if user is member of view group only
				else 
					{
						logger.info("Daeja Permission: Don't Show Print Btn : Read Only Annotation");
						//jvbr.setViewOneParameter("printButtons", "false");
						annotationPermission = ReadOnlyAnnotationPermission;
						//jvbr.setAnnotationHideButtons("show");
						noPermissionSelected = true;
					}
					
					if(!noPermissionSelected) {
						logger.info("Daeja Permission: noPermissionSelected");
						//jvbr.setViewOneParameter("printButtons", "false");
						annotationPermission = ReadOnlyAnnotationPermission;
						//jvbr.setAnnotationHideButtons("show,save");
					}
				
				String annotationUrl = (String) viewOneBootstrap.get("getAnnotationUrl");
				logger.info("Annotation Url:\t" + annotationUrl);
				//https://host/navigator/plugin/pluginid/serviceId
				annotationUrl = annotationUrl.replace("/navigator/jaxrs/p8/getDocument", "/ViewOneUtils/GetWaterMarkAnnotations");
				annotationUrl+= "&objectStoreName="+repositoryId;
				annotationUrl+= "&watermarksPermission="+annotationPermission;
				if(watermarkPosition != null && watermarkPosition != "") {
					annotationUrl+= "&watermarkPosition="+watermarkPosition;	
				}
				
				logger.info("Annotation Url After modify:\t" + annotationUrl);
				//viewOneBootstrap.put("getAnnotationUrl",annotationUrl);
				//viewOneBootstrap.put("printAnnotations", false);
				//viewOneBootstrap.put("printButtons", false);
				//jsonResponse.put("viewOneBootstrap", viewOneBootstrap);
				jvbr.setGetAnnotationUrl(annotationUrl); 
				//jvbr.setViewOneParameter("annotateEdit", "false");
				logger.info(jvbr.toString());
				//jvbr.setAnnotationHideButtons("show");
				//jvbr.setAnnotationHideButtons("show,save");
				//jvbr.setViewOneParameter("annotationAllowHideAll", "false");
				//jvbr.setViewOneParameter("annotationSecurityModel", "2");
				//jvbr.setViewOneParameter("annotationHideContextButtons", "delete");
				//jvbr.setViewOneParameter("printAnnotations", "true");
				logger.info("After ********************* After");
				logger.info(jvbr.toString());
				// jvbr.setViewOneParameter("annotationHideContextButtons","save");	
				
			}

			logger.info("**********************************************************************************");
		

		
	}
	
	public static void main(String[] args) {
		String docUrl = "/navigator/jaxrs/p8/getDocument?docid=TestClass%2C%7B9AEADB06-CA68-49F2-B6EC-6D6AD694538A%7D%2C%7B6AAF14D8-679A-45D4-8BBF-72D72BF72B5C%7D&template_name=TestClass&repositoryId=icmcmtos&vsId=%7BC07AEB3D-3D85-CC26-89F7-68B2F0800000%7D&version=released&objectStoreName=CMTOS&security_token=-7782137135586970785";
		docUrl = URLDecoder.decode(docUrl);
		System.out.println("getDocUrl:\t" + docUrl);
		String objectStoreName = docUrl.substring(docUrl.indexOf("&objectStoreName="), docUrl.indexOf("&security_token"));
		objectStoreName = objectStoreName.substring(objectStoreName.indexOf("&objectStoreName=")+17);
		String docClassName = docUrl.substring(docUrl.indexOf("&template_name="), docUrl.indexOf("&repositoryId"));
		docClassName = docClassName.substring(docClassName.indexOf("&template_name=")+15);
		String vsId = docUrl.substring(docUrl.indexOf("&vsId="), docUrl.indexOf("&objectStoreName"));
		vsId = vsId.substring(docClassName.indexOf("&vsId=")+7);
		if(vsId.contains("&")){
			vsId = vsId.substring(0, vsId.indexOf("&"));
		}

		String documentId = docUrl.substring(docUrl.indexOf("docid="), docUrl.indexOf("&template_name"));
		documentId = documentId.substring(docClassName.indexOf("&docid=")+7);
		documentId = documentId.split(",")[2];
		System.out.println("Doc ID : "+documentId);
		System.out.println("getVsId:\t" + vsId);
		System.out.println("getDocumentId:\t" + documentId);
		System.out.println("getP8ObjectStore:\t" + objectStoreName);
		System.out.println("getDocClassName:\t" + docClassName);

	}
	
}
