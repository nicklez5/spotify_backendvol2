package com.spotify11.demo.dtos;

import com.spotify11.demo.entity.Song;
import com.spotify11.demo.entity.UploadStatus;

public record CreateSongDto(String title, String artist) {}

