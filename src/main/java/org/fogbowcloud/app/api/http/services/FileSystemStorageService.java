package org.fogbowcloud.app.api.http.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.exceptions.StorageException;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService {

    private final Logger LOGGER = Logger.getLogger(FileSystemStorageService.class);

    public void store(MultipartFile file, Map<String, String> formFieldsToLoad) {
        final String fileName = file.getOriginalFilename();

        LOGGER.info("Storing file of name [" + fileName + "];");

        try {
            if (file.isEmpty()) {
                final String errorMsg = "Failed to store empty file " + fileName;
                LOGGER.error(errorMsg);
                throw new StorageException(errorMsg);
            }
            try (InputStream inputStream = file.getInputStream()) {
                if (formFieldsToLoad.containsKey(IguassuPropertiesConstants.JDF_FILE_PATH)) {
                    final String fileContent = IOUtils.toString(inputStream);
                    final File tempFile = createTmpFile(fileContent, fileName);
                    formFieldsToLoad
                        .put(IguassuPropertiesConstants.JDF_FILE_PATH, tempFile.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + fileName, e);
        }
    }

    private File createTmpFile(String content, String fileName) throws IOException {
        final File tempFile = File.createTempFile(fileName, null);
        IOUtils.write(content, new FileOutputStream(tempFile));

        LOGGER.info("Writing file of name [" + fileName + "] in " + tempFile.getAbsolutePath());
        return tempFile;
    }
}