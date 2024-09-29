package com.spotify11.demo.entity;


import jakarta.persistence.*;
import lombok.*;


import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Library {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;


    @OneToMany(cascade=CascadeType.ALL , fetch = FetchType.EAGER)
    private List<Song> songs = new ArrayList<>();




    public void addSong(Song song, String email) {
        this.songs.add(song);
    }
    public void removeSong(Song song) {
        this.songs.remove(song);
    }
    public String toString(){
        return "Library Id: " + id + " Songs: " + songs;
    }

}
