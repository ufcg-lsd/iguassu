package org.fogbowcloud.app.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;

public class ServerResourceUtils {
	
	public static void loadFields(Representation entity,
                                  Map<String, String> formFieldstoLoad,
                                  Map<String, File> filesToLoad)
            throws IOException, FileUploadException {
		DiskFileItemFactory factory = new DiskFileItemFactory();
    	factory.setSizeThreshold(1000240);
    	RestletFileUpload upload = new RestletFileUpload(factory);
    	FileItemIterator fileIterator = upload.getItemIterator(entity);

    	while (fileIterator.hasNext()) {
    		FileItemStream fi = fileIterator.next();
    		String fieldName = fi.getFieldName();
    		if (fi.isFormField()) {
    			if (formFieldstoLoad.containsKey(fieldName)) {
    				formFieldstoLoad.put(fieldName, IOUtils.toString(fi.openStream()));
    			}
    		} else {
    			if (filesToLoad.containsKey(fieldName)) {
        			String fileContent = IOUtils.toString(fi.openStream());
        			String fileName = fi.getName();
        			File file = createTmpFile(fileContent, fileName);
    				filesToLoad.put(fieldName, file);
        		}
    		}
    	}
    }
	
	private static File createTmpFile(String content, String fileName) throws IOException {
		File tempFile = File.createTempFile(fileName, null);
		IOUtils.write(content, new FileOutputStream(tempFile));
		return tempFile;
	}

}
