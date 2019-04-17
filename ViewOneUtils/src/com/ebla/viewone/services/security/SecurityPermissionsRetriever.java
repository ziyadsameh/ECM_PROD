package com.ebla.viewone.services.security;
import java.util.List;
import java.util.Map;

import com.filenet.api.core.Document;
import com.filenet.api.core.ObjectStore;

public interface SecurityPermissionsRetriever {

public Map<Integer,String> getDocSecPermission(Document doc);
	
}
