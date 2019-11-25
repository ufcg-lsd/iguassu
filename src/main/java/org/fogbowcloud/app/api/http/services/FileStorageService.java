package org.fogbowcloud.app.api.http.services;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.AppConstant;
import org.fogbowcloud.app.core.exceptions.StorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
public class FileStorageService {

    private final Logger logger = Logger.getLogger(FileStorageService.class);

    public synchronized void store(MultipartFile file, Map<String, String> formFieldsToLoad) {
        final String fileName = file.getOriginalFilename();

        logger.info("Storing file of name [" + fileName + "];");

        try {
            if (file.isEmpty()) {
                final String errorMsg = "Failed to store empty file " + fileName;
                logger.error(errorMsg);
                throw new StorageException(errorMsg);
            }
            try (InputStream inputStream = file.getInputStream()) {
                if (formFieldsToLoad.containsKey(AppConstant.JDF_FILE_PATH)) {
                    final String fileContent = IOUtils.toString(inputStream);
                    final File tempFile = createTmpFile(fileContent, fileName);
                    formFieldsToLoad.put(AppConstant.JDF_FILE_PATH, tempFile.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + fileName, e);
        }
    }

    private File createTmpFile(String content, String fileName) throws IOException {
        final File tempFile = File.createTempFile(fileName, null);
        IOUtils.write(content, new FileOutputStream(tempFile));

        logger.info("Writing file named [" + fileName + "] in path" + tempFile.getAbsolutePath());
        return tempFile;
    }
}
