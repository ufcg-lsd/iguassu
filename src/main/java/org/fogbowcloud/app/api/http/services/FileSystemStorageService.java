package org.fogbowcloud.app.api.http.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.http.controllers.JobController;
import org.fogbowcloud.app.api.http.exceptions.StorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.filechooser.FileSystemView;

@Service
public class FileSystemStorageService {

    private final Logger LOGGER = Logger.getLogger(FileSystemStorageService.class);
    private final File FILES_DEFAULT_DIRECTORY = FileSystemView.getFileSystemView().getHomeDirectory();

    public FileSystemStorageService() {}

    public void store(MultipartFile file, Map<String, String> formFieldsToLoad) {
        String fileName = file.getOriginalFilename();

        LOGGER.info("Storing file of name [" + fileName + "];");

        try {
            if (file.isEmpty()) {
                String errorMsg = "Failed to store empty file " + fileName;
                LOGGER.error(errorMsg);
                throw new StorageException(errorMsg);
            }
            try (InputStream inputStream = file.getInputStream()) {
                if (formFieldsToLoad.containsKey(JobController.JDF_FILE_PATH)) {
                    String fileContent = IOUtils.toString(inputStream);
                    File tempFile = createTmpFile(fileContent, fileName);
                    formFieldsToLoad.put(JobController.JDF_FILE_PATH, tempFile.getAbsolutePath());
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

        LOGGER.info("Writing file of name [" + fileName + "] in " + tempFile.getAbsolutePath());
        return tempFile;
    }

}