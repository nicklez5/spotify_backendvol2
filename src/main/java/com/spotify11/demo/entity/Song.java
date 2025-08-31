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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Table(name = "songs")
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Library library;
    
    @Column(nullable = false)
    private Integer ownerId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    private String s3Key;
    private String contentType;
    private Long sizeBytes;


    @ManyToMany(mappedBy = "tracks")
    @ToString.Exclude
    private Set<Playlist> playlist  = new HashSet<>();

    



    private static String extractFileName(String uri){
        int q = uri.indexOf('?');
        String path = (q >= 0) ? uri.substring(0, q) : uri;
        int slash = path.lastIndexOf('/');
        return (slash >= 0 ) ? path.substring(slash + 1) : path;
    }

    @Override public String toString() {
        // Avoid reflection toString to prevent deep graphs / lazy hits
        return "Song{id=%s, title='%s', artist='%s'}".formatted(id, title, artist);
    }

    @Enumerated(EnumType.STRING)
    private UploadStatus status = UploadStatus.PENDING;

}