package com.spotify11.demo.entity;


import jakarta.persistence.*;
import lombok.*;


import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spotify11.demo.enums.Visibility;


@Table(name = "playlists")
@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    @ToString.Include
    private int id;

    private String playlistName;

    private String coverUrl;
    private String coverKey;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @JsonIgnore
    private User owner;

    public Playlist(String playlist_name) {
        this.playlistName = playlist_name;

    }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility = Visibility.PUBLIC;

    private String description;

    @ManyToMany
    @JoinTable(
        name = "playlist_tracks",
        joinColumns = @JoinColumn(name = "playlist_id"),
        inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    @OrderColumn(name = "track_index")
    @ToString.Exclude
    @JsonIgnore
    private List<Song> tracks = new ArrayList<>();

    public void addTrack(Song s){
        if(tracks.add(s))
            s.getPlaylist().add(this);
    }
    public void removeTrack(Song s){
        if(tracks.remove(s))
            s.getPlaylist().remove(this);
    }

}
