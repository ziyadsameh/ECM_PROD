/**
 * Licensed Materials - Property of IBM (C) Copyright IBM Corp. 2014 US Government Users Restricted Rights - Use,
 * duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.ibm.ecm.extension.embeddedViewerPlugin;

import java.util.Locale;

import com.ibm.ecm.extension.Plugin;
import com.ibm.ecm.extension.PluginFeature;
import com.ibm.ecm.extension.PluginService;

public class EmbeddedViewerPlugin extends Plugin {

	public String getId() {
		return "EmbeddedViewerPlugin";
	}

	public String getName(Locale locale) {
		return "IBM ICN Embedded Viewer Plugin";
	}

	public String getVersion() {
		return "2.0.2.20150318.1";
	}

	public String getDojoModule() {
		return "embeddedContentViewerDojo";
	}
	
	public PluginFeature[] getFeatures() {
		return new PluginFeature[] { new ContentViewerFeature() };
	}
}
