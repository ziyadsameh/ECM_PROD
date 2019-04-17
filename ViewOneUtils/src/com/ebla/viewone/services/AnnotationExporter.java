package com.ebla.viewone.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.ServletContext;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import com.ibm.ecm.serviceability.Logger;
import com.ibm.ecm.serviceability.Logger.LoggingContext;
import com.ibm.ecm.util.p8.P8Annotation;

public class AnnotationExporter {

	private static TransformerFactory transformerFactory = TransformerFactory.newInstance();
	private static Templates templates = null;
	private static Object templatesLock = new Object();
	private Logger.LoggingContext loggingContext;
	private Map<String, List<P8Annotation>> pageMap = new TreeMap<String, List<P8Annotation>>();
	private static final String ReadOnlyAnnotationPermission = "Read";
	private static final String ReadWriteAnnotationPermission = "Read/Write";

	public AnnotationExporter(List<P8Annotation> p8Annotations, LoggingContext loggingContext) {
		this.loggingContext = loggingContext;
		addP8Annotations(p8Annotations);
	}

	public static void initialize(ServletContext servletContext) throws TransformerConfigurationException {
		if (templates == null) {
			synchronized (templatesLock) {
				InputStream is = servletContext.getResourceAsStream("/WEB-INF/xsl/Annot.xsl");
				templates = transformerFactory.newTemplates(new StreamSource(is));
			}
		}
	}

	public void export(OutputStream os, Set<String> roID,String watermarksPermission) throws Exception {

		String methodName = "export";
		Logger.logEntry(this, methodName, loggingContext);
    	System.out.println("export watermarksPermission is --->> "+watermarksPermission);

		StringBuffer annotationXml = getAnnotationXml(roID,watermarksPermission);
		try {
			applyXslTransform(os, annotationXml);
		} catch (TransformerException tce) {
			throw new IOException(tce);
		}
		Logger.logExit(this, methodName, loggingContext);
	}

	public StringBuffer getAnnotationXml(Set<String> roID,String watermarksPermission) throws Exception {
		// if annotation is one of ours delete the delete tag and replace with view
		StringBuffer annotationXml = new StringBuffer();
		appendDaejaHeader(annotationXml, null, null, null);

		Iterator<Map.Entry<String, List<P8Annotation>>> i = pageMap.entrySet().iterator();

		while (i.hasNext()) {
			Map.Entry<String, List<P8Annotation>> entry = (Map.Entry) i.next();

			String page = (String) entry.getKey();
			List<P8Annotation> p8Annotations = (List) entry.getValue();

			appendDaejaPageHeader(annotationXml, page);
			Iterator<P8Annotation> j = p8Annotations.iterator();
			while (j.hasNext()) {
				P8Annotation p8Annotation = (P8Annotation) j.next();
				if (roID.contains(p8Annotation.getAnnotId())) {

					String xml = p8Annotation.getXml();
					String annoXml = null;
					if(watermarksPermission.equalsIgnoreCase(ReadOnlyAnnotationPermission)) {
						annoXml = xml.replaceAll("write", "view").replaceAll("append", "view")
								.replaceAll("edit", "view").replaceAll("delete", "view");
					}
					else if(watermarksPermission.equalsIgnoreCase(ReadWriteAnnotationPermission)) {
						annoXml = xml.replaceAll("write", "edit").replaceAll("append", "edit")
								.replaceAll("edit", "edit").replaceAll("delete", "edit");
					}
					else {
						throw new Exception("No WaterMark Permission Provided");
					}
					System.out.println("Annotations payload : "+annoXml);
					annotationXml.append(annoXml);
				} else {
					annotationXml.append(p8Annotation.getXml());
				}
			}
			appendDaejaPageFooter(annotationXml);
		}

		appendDaejaFooters(annotationXml);
		return annotationXml;

	}

	private void addP8Annotations(List<P8Annotation> p8Annotations) {
		for (Iterator<P8Annotation> i = p8Annotations.iterator(); i.hasNext();) {
			addP8Annotation((P8Annotation) i.next());
		}
	}

	private void addP8Annotation(P8Annotation p8Annotation) {
		String page = p8Annotation.getPage();

		List<P8Annotation> pageList = (List) pageMap.get(page);
		if (pageList == null) {
			pageList = new ArrayList();
			pageMap.put(page, pageList);
		}
		pageList.add(p8Annotation);
	}

	private void applyXslTransform(OutputStream os, StringBuffer annotationXml) throws TransformerException {
		Transformer transformer = templates.newTransformer();
		Reader xmlReader = new StringReader(annotationXml.toString());
		Source in = new StreamSource(xmlReader);
		Result out = new StreamResult(os);

		transformer.setOutputProperty("encoding", "UTF-8");
		transformer.transform(in, out);
	}

	private void appendDaejaHeader(StringBuffer annotationXml, String docID, String libName, String systemType) {

		annotationXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<FnDocAnnoList DocID=\"");
		if (docID != null) {
			annotationXml.append(docID);
		}
		annotationXml.append("\" LibName=\"");
		if (libName != null) {
			annotationXml.append(libName);
		}
		annotationXml.append("\" SystemType=\"");
		annotationXml.append(systemType != null ? systemType : "0");

		annotationXml.append(
				"\" xmlns:xsd=\"http:www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
	}

	private void appendDaejaPageHeader(StringBuffer annotationXml, String page) {

		annotationXml.append("    <FnPageAnnoList Page=\"");
		annotationXml.append(page);
		annotationXml.append("\">\n");
	}

	private void appendDaejaPageFooter(StringBuffer annotationXml) {

		annotationXml.append("    </FnPageAnnoList>\n");
	}

	public void appendDaejaFooters(StringBuffer annotationXml) {

		annotationXml.append("</FnDocAnnoList>\n");
	}

}
