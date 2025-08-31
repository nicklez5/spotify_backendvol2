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
    private Integer id;

    @OneToOne(mappedBy = "library", optional = false)
    private User owner;

    @OneToMany(cascade=CascadeType.ALL , mappedBy = "library", orphanRemoval = true)
    @OrderBy("id desc")
    private List<Song> songs = new ArrayList<>();



    public void addSong(Song s) {
        songs.add(s);
        s.setLibrary(this);
    }
    public void removeSong(Song s) {
        songs.remove(s);
        s.setLibrary(null);
    }
    public String toString(){
        return "Library Id: " + id + " Songs: " + songs;
    }

}
