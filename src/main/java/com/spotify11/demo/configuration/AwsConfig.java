package com.spotify11.demo.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsConfig {

  @Bean
  S3Client s3Client(
      @Value("${app.aws.region}") String region,
      @Value("${app.aws.profile:}") String profile) {

    var creds = (profile == null || profile.isBlank())
        ? DefaultCredentialsProvider.builder().build()
        : software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
              .builder().profileName(profile).build();

    return S3Client.builder()
        .region(software.amazon.awssdk.regions.Region.of(region))
        .credentialsProvider(creds)
        .build();
  }

  @Bean
  S3Presigner s3Presigner(
    @Value("${app.aws.region}") String region,
    @Value("${app.aws.profile:}") String profile) {

    var creds = (profile == null || profile.isBlank())
        ? DefaultCredentialsProvider.builder().build()
        : software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
              .builder().profileName(profile).build();

    return software.amazon.awssdk.services.s3.presigner.S3Presigner.builder()
        .region(software.amazon.awssdk.regions.Region.of(region))
        .credentialsProvider(creds)
        .build();
  }
}

