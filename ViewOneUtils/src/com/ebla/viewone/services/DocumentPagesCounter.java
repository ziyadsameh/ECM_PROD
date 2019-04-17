package com.ebla.viewone.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.usermodel.XSLFSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;
import com.filenet.api.core.ContentTransfer;

public class DocumentPagesCounter {

	private ContentTransfer ct;
	private int noOfPages = 0;
	
	private static final String pdfMimeType = "application/pdf";
	
	private static final List<String> msWordMimeTypes = 
									Arrays.asList("application/vnd.ms-word","application/msword",
												  "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
	
	private static final List<String> msPowerPointMimeTypes = 
			Arrays.asList(  "application/vnd.ms-powerpoint",
							"application/vnd.openxmlformats-officedocument.presentationml.presentation",
							"application/x-mspowerpoint");

	private static final List<String> msExcelMimeTypes = 
			Arrays.asList("application/vnd.ms-excel",
						  "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
						  "application/x-msexcel");
	
	private static final String tiffMimeType = "image/tiff";
	
	public DocumentPagesCounter(ContentTransfer ct) {
		this.ct = ct;
	}
	
	public int getDocPagesCount() throws IOException {
		
		if(ct.get_ContentType().equalsIgnoreCase(pdfMimeType)) {
			PDDocument doc = PDDocument.load(ct.accessContentStream());
			noOfPages = doc.getNumberOfPages();
			doc.close();
		}
		
		else if(msWordMimeTypes.contains(ct.get_ContentType())) {
            XWPFDocument docx = new XWPFDocument(ct.accessContentStream());
            noOfPages = docx.getProperties().getExtendedProperties().getUnderlyingProperties().getPages();
            docx.close();
		}
		
		else if(msPowerPointMimeTypes.contains(ct.get_ContentType())) {
			
			//if powerpoint is (application/vnd.ms-powerpoint)/(ppt)
			if(ct.get_ContentType().equalsIgnoreCase(msPowerPointMimeTypes.get(0))) {
				HSLFSlideShow workbook = new HSLFSlideShow(ct.accessContentStream());
				noOfPages = workbook.getSlides().size();
				workbook.close();
			}
            //if powerpoint is (application/vnd.openxmlformats-officedocument.presentationml.presentation)/pptx
			if(ct.get_ContentType().equalsIgnoreCase(msPowerPointMimeTypes.get(1))) {
				OPCPackage pkg;
				try {
					pkg = OPCPackage.open(ct.accessContentStream());
					XSLFSlideShow workbook = new XSLFSlideShow(pkg);
					noOfPages = workbook.getSlideReferences().sizeOfSldIdArray();
					pkg.close();
					workbook.close();
				} catch (InvalidFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OpenXML4JException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
            }
		
		else if(msExcelMimeTypes.contains(ct.get_ContentType())) {
			
			//if powerpoint is (application/vnd.ms-powerpoint)/(ppt)
			if(ct.get_ContentType().equalsIgnoreCase(msExcelMimeTypes.get(0))) {
				HSSFWorkbook  workbook = new HSSFWorkbook (ct.accessContentStream());
				noOfPages = workbook.getNumberOfSheets();
				workbook.close();
			}
            //if powerpoint is (application/vnd.openxmlformats-officedocument.presentationml.presentation)/pptx
			if(ct.get_ContentType().equalsIgnoreCase(msExcelMimeTypes.get(1))) {
				XSSFWorkbook  workbook = new XSSFWorkbook(ct.accessContentStream());
				noOfPages = workbook.getNumberOfSheets();
				workbook.close();
			}
			
            }
		
		else if(tiffMimeType.equalsIgnoreCase(ct.get_ContentType())) {
			
			ImageIO.scanForPlugins();
			Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("TIF");
			while (readers.hasNext()) {
			    System.out.println("reader: " + readers.next());
			}
	        /*SeekableStream s = new FileSeekableStream(file);
	        TIFFDecodeParam param = null;
	        ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
	        System.out.println("Number of images in this TIFF: " +
	                           dec.getNumPages());*/
			ImageInputStream imageInput = ImageIO.createImageInputStream(ct.accessContentStream());
			//Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType("tiff");
			//ImageReader reader = ImageIO.getImageReadersByFormatName("TIF").next();
			//ImageIO.scanForPlugins();
			ImageReader reader = ImageIO.getImageReadersByFormatName("TIFF").next();
			reader.setInput(imageInput);
            noOfPages = reader.getNumImages(true);
		}
		
		else {
			//if not anything from the above use 100 times
			noOfPages = 100;
		}
		
		return noOfPages;
	}
	
	public static void main(String[] args) {
		ImageIO.scanForPlugins();
		Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("TIFF");
		while (readers.hasNext()) {
		    System.out.println("reader: " + readers.next());
		}
	}
	
}
