package org.fogbowcloud.app.api.http.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.fogbowcloud.app.api.http.controllers.JobController;
import org.fogbowcloud.app.api.http.exceptions.StorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService {


    public FileSystemStorageService() {

    }

    public void store(MultipartFile file, Map<String, String> formFieldsToLoad) {
        String fileName = file.getOriginalFilename();
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + fileName);
            }
            try (InputStream inputStream = file.getInputStream()) {
                if (formFieldsToLoad.containsKey(JobController.JDF_FILE_PATH)) {
                    String fileContent = IOUtils.toString(inputStream);
                    formFieldsToLoad.put(JobController.JDF_FILE_PATH, fileName);
                    File tempFile = createTmpFile(fileContent, fileName);
                }
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file " + fileName, e);
        }
    }

    private File createTmpFile(String content, String fileName) throws IOException {
        File tempFile = File.createTempFile(fileName, null);
        IOUtils.write(content, new FileOutputStream(tempFile));
        return tempFile;
    }

}