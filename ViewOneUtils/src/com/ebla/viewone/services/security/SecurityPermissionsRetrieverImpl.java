package com.ebla.viewone.services.security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.core.Document;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.security.AccessPermission;

public class SecurityPermissionsRetrieverImpl implements SecurityPermissionsRetriever{
	
public Map<Integer,String> getDocSecPermission(Document doc){
	
    List<String> secPermissions = new ArrayList<String>();
    Map<Integer,String> secPermissionsMap = new HashMap<Integer,String>();
	PropertyFilter docPF = new PropertyFilter();    
	docPF.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.PERMISSIONS, null) );
	doc.fetchProperties(docPF);
	AccessPermissionList accessPermissionList = doc.get_Permissions();
	Iterator accessPermissionListIter = accessPermissionList.iterator();

	
	while(accessPermissionListIter.hasNext()) {
		AccessPermission ap = (AccessPermission)accessPermissionListIter.next();
		if(ap.get_GranteeName().contains("@")){
			secPermissions.add(ap.get_GranteeName().substring(0, ap.get_GranteeName().indexOf("@")));
		}
		else{
			secPermissions.add(ap.get_GranteeName());
		}
	}
	
	for(String per : secPermissions) {
		if(per.contains(DocSecurityPermissionsEnum.VIEW_SEC_PERMISSION.secPermission)) {
			secPermissionsMap.put(DocSecurityPermissionsEnum.VIEW_SEC_PERMISSION.value, per);
		}
		else if(per.contains(DocSecurityPermissionsEnum.PRINTW_SEC_PERMISSION.secPermission)) {
			secPermissionsMap.put(DocSecurityPermissionsEnum.PRINTW_SEC_PERMISSION.value, per);
		}
		else if(per.contains(DocSecurityPermissionsEnum.PRINT_SEC_PERMISSION.secPermission)) {
			secPermissionsMap.put(DocSecurityPermissionsEnum.PRINT_SEC_PERMISSION.value, per);
		}
		else if(per.contains(DocSecurityPermissionsEnum.MODIFY_SEC_PERMISSION.secPermission)) {
			secPermissionsMap.put(DocSecurityPermissionsEnum.MODIFY_SEC_PERMISSION.value, per);
		}

		System.out.println("Sec Per: "+per);
	}
	
	Iterator itr = secPermissionsMap.values().iterator();
	Iterator itr2 = secPermissionsMap.keySet().iterator();
	while(itr.hasNext()&&itr2.hasNext()){
		System.out.println("Sec Per Map: "+itr.next() + "key : "+itr2.next());

	}
	
	return secPermissionsMap;
}

}
