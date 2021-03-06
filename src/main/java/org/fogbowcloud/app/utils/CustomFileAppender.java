package org.fogbowcloud.app.utils;

import org.apache.log4j.FileAppender;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for log4j configuration.
 */
public class CustomFileAppender extends FileAppender {


    @Override
    public void setFile(String fileName) {
        if (fileName.contains("%timestamp")) {
            Date d = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSS");
            fileName = fileName.replaceAll("%timestamp", format.format(d));
        }
        super.setFile(fileName);
    }
}
