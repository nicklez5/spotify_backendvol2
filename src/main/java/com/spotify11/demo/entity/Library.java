package com.spotify11.demo.entity;


import jakarta.persistence.*;
import lombok.*;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
    @JsonIgnore
    private User owner;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
    name = "library_songs",
    joinColumns = @JoinColumn(name = "library_id"),
    inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    @OrderBy("id desc")
    private Set<Song> songs = new LinkedHashSet<>();



    public void addSong(Song s) {
        songs.add(s);
    }
    public void removeSong(Song s) {
        songs.remove(s);
    }
    @Override public String toString() {
        return "Library{id=" + id + "}";
        }

}
