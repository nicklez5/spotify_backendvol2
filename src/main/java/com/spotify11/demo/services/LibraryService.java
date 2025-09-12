package com.spotify11.demo.services;

import com.spotify11.demo.dtos.TrackDto;
import com.spotify11.demo.entity.Library;
import com.spotify11.demo.entity.Song;
import com.spotify11.demo.exception.*;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LibraryService {

    @Transactional
    void addExistingSong(Integer ownerId, Integer songId);
    Song createAndAttach(Integer ownerId, String title, String artist);

    @Transactional
    void removeSong(Integer ownerId, Integer songId);

    @Transactional
    void addExistingSong2(Integer ownerId, Integer songId);

    @Transactional(readOnly = true)
    List<TrackDto> list(Integer ownerId);
    
    void clear(Integer ownerId);

}
