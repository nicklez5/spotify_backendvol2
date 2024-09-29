package com.spotify11.demo.entity;


import jakarta.persistence.*;
import lombok.*;


import java.util.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String playlistName;

    public Playlist(String playlist_name) {
        this.playlistName = playlist_name;

    }


    @OneToMany(cascade=CascadeType.ALL , fetch = FetchType.EAGER)
    private List<Song> songs = new ArrayList<Song>();


}
