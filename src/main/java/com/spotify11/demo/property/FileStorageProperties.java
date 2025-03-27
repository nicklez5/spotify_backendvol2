package com.spotify11.demo.property;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@ConfigurationProperties(prefix = "file")
@Entity
public class FileStorageProperties {
    private String uploadDir;
    @Id
    private Long id;


}