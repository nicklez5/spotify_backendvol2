package com.spotify11.demo.entity;


import jakarta.persistence.*;
import lombok.*;


import java.util.*;

import com.spotify11.demo.enums.Visibility;

@Data
@Table(name = "playlists")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String playlistName;

    private String coverUrl;
    private String coverKey;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User owner;

    public Playlist(String playlist_name) {
        this.playlistName = playlist_name;

    }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility = Visibility.PUBLIC;


    @ManyToMany
    @JoinTable(
        name = "playlist_tracks",
        joinColumns = @JoinColumn(name = "playlist_id"),
        inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    @OrderColumn(name = "track_index")
    private List<Song> tracks = new ArrayList<Song>();

    public void addTrack(Song s){
        tracks.add(s);
        s.getPlaylist().add(this);
    }
    public void removeTrack(Song s){
        tracks.remove(s);
        s.getPlaylist().remove(this);
    }

}
