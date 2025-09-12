package com.spotify11.demo.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer id;


    @Column(name= "fullName", nullable = false, unique = true)
    private String fullName;


    @Column(name = "email", nullable = false, unique = true)
    private String email;

    private String password;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Column(nullable = true)
    private String profileImageUrl;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "library_id",  nullable = false, unique = true)
    @JsonIgnore
    private Library library = new Library();

    @OneToMany(mappedBy = "owner",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    @JsonIgnore
    private List<Playlist> playlists = new ArrayList<>();

    public void addPlaylist(Playlist p){
        if (p == null) return;
        if (!playlists.contains(p)) playlists.add(p);
        if (p.getOwner() != this) p.setOwner(this);
    }
    public void removePlaylist(Playlist p){
        if (p == null) return;
        playlists.remove(p);
        if (p.getOwner() == this) p.setOwner(null);
    }
    public void attachLibrary(Library lib){
        if (this.library == lib) return;
        this.library = lib;
        if (lib != null && lib.getOwner() != this) lib.setOwner(this);
    }
}
