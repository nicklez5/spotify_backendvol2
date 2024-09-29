package com.spotify11.demo.response;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.net.URI;

@Data
public class UploadFileResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String fileName;
    private long size;
    private String uri;


    public UploadFileResponse(Integer song_id2, String fileName, String filedownloaduri, long size) {
        this.id = song_id2;
        this.fileName = fileName;
        this.size = size;
        this.uri = filedownloaduri;

    }


}