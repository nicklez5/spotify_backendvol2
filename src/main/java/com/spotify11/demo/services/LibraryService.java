package com.spotify11.demo.services;

import com.spotify11.demo.dtos.TrackDto;
import com.spotify11.demo.entity.Library;
import com.spotify11.demo.entity.Song;
import com.spotify11.demo.exception.*;

import java.util.List;

public interface LibraryService {
    void addExistingSong(Integer ownerId, Integer songId);
    Song createAndAttach(Integer ownerId, String title, String artist);
    void removeSong(Integer ownerId, Integer songId);

    List<TrackDto> list(Integer ownerId);
    


}
