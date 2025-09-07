package com.spotify11.demo.entity;


import jakarta.persistence.*;
import lombok.*;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Library {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer id;

    @OneToOne(mappedBy = "library", optional = false)
    @JsonIgnore @ToString.Exclude
    private User owner;

    @OneToMany(cascade=CascadeType.ALL , mappedBy = "library", orphanRemoval = true)
    @OrderBy("id desc")
    @JsonIgnore
    @ToString.Exclude
    private Set<Song> songs = new HashSet<>();



    public void addSong(Song s) {
        songs.add(s);
        s.setLibrary(this);
    }
    public void removeSong(Song s) {
        songs.remove(s);
        s.setLibrary(null);
    }
    @Override public String toString() {
        return "Library{id=" + id + "}";
        }

}
