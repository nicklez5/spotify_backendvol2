package com.spotify11.demo.utilites;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import com.spotify11.demo.constants.Constants;
public class Functions {
    public static final String basePath =  Constants.UPLOAD_LOCATION;
    public static File multipartFile(MultipartFile file, String fileName) throws IOException {
        File convFile = new File(basePath + File.separator + fileName);
        file.transferTo(convFile);
        return convFile;
    }

}
