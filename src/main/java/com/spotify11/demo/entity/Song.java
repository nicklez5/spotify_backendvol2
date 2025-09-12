package com.spotify11.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Setter
@Getter
@Table(name = "songs")
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer id;


    @ManyToMany(mappedBy = "songs")
    @JsonIgnore 
    private Set<Library> libraries = new LinkedHashSet<>();
    
    @Column(nullable = false)
    private Integer ownerId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    private String s3Key;
    private String contentType;
    private Long sizeBytes;

    @Column(name= "duration_sec")
    private Integer durationSec;


    @ManyToMany(mappedBy = "tracks")
     @JsonIgnore
    private Set<Playlist> playlist  = new HashSet<>();

    



    private static String extractFileName(String uri){
        int q = uri.indexOf('?');
        String path = (q >= 0) ? uri.substring(0, q) : uri;
        int slash = path.lastIndexOf('/');
        return (slash >= 0 ) ? path.substring(slash + 1) : path;
    }

    

    @Enumerated(EnumType.STRING)
    private UploadStatus status = UploadStatus.PENDING;

}