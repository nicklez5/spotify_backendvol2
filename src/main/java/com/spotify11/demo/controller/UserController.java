package com.spotify11.demo.controller;

import com.spotify11.demo.dtos.AuthResponse;
import com.spotify11.demo.dtos.CreatePlaylistDto;
import com.spotify11.demo.dtos.LoginUserDto;
import com.spotify11.demo.dtos.MeDto;
import com.spotify11.demo.dtos.PlaylistDto;
import com.spotify11.demo.dtos.RegisterUserDto;
import com.spotify11.demo.dtos.TrackDto;
import com.spotify11.demo.dtos.UserDto;
import com.spotify11.demo.dtos.UserPublicDto;
import com.spotify11.demo.entity.User;
import com.spotify11.demo.exception.UserException;
import com.spotify11.demo.repo.PlaylistRepo;
import com.spotify11.demo.repo.UserRepository;

import com.spotify11.demo.response.LoginResponse;
import com.spotify11.demo.security.CustomUserPrincipal;
import com.spotify11.demo.services.AuthenticationService;
import com.spotify11.demo.services.JwtService;
import com.spotify11.demo.services.PlaylistService;
import com.spotify11.demo.services.SongService;
import com.spotify11.demo.services.UserService;
import jakarta.transaction.Transactional;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PlaylistService playlistService;
    private final PlaylistRepo playlistRepo;
    private final SongService songService;
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    public UserController(UserService userService, SongService songService,PlaylistService playlistService, PlaylistRepo playlistRepo, UserRepository userRepository, AuthenticationService authenticationService, JwtService jwtService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.playlistService = playlistService;
        this.playlistRepo = playlistRepo;
        this.songService = songService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterUserDto registerUserDto) {
        User user = authenticationService.signup(registerUserDto);
        var body = new AuthResponse(user.getId(), user.getFullName(), user.getEmail(), null );
        return ResponseEntity.ok().body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);
        String token = jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());
        return ResponseEntity.ok(loginResponse);
    }
    @DeleteMapping("/logout")
    public void logout() {
        SecurityContextHolder.clearContext();
        this.authenticationService.logout();

    }
    @GetMapping("/info")
    public ResponseEntity<MeDto> me(
        @AuthenticationPrincipal CustomUserPrincipal me) {
    var u = userRepository.findById(me.getId()).orElseThrow();
    return ResponseEntity.ok(new MeDto(u.getId(), u.getFullName(), u.getEmail(), u.getProfileImageUrl()));
    }
    @GetMapping("/all")
    public List<UserPublicDto> getUsers() {
        return userService.getAllUsers();
    }


    @Transactional
    @PutMapping("/update")
    public ResponseEntity<UserDto> updateUser(@RequestParam("fullName") String fullName,@RequestParam("password") String user_password, @RequestParam("email") String user_email) throws UserException {
        User user1 =  userService.updateUser(fullName,user_password,user_email);
        return ResponseEntity.ok(new UserDto(user1.getId(), user1.getFullName(), user1.getEmail()));
    }


    @GetMapping("/profileinfo")
    public ResponseEntity<UserPublicDto> me2(
        @RequestParam("fullName") String fullName
    ) throws UserException{
        UserPublicDto xyz123 = userService.getUser(fullName);
        return ResponseEntity.ok(xyz123);
    }
    @Transactional
    @DeleteMapping("/delete")
    public ResponseEntity<User> deleteUser(@RequestParam("email") String email) throws UserException {
        User user1  = userService.deleteUser(email);
        return ResponseEntity.ok(user1);
    }
    @Transactional
    @GetMapping("/read")
    public ResponseEntity<User> readUser(@RequestParam("email") String email) throws UserException{
        User user1 = userService.readUser(email);
        return ResponseEntity.ok(user1);
    }
    @GetMapping("/tracks")
    public List<TrackDto> tracks(@AuthenticationPrincipal CustomUserPrincipal me
                             ) throws UserException {
    return songService.getAllSongsFromUser(me.getEmail());
    }
    @PostMapping("/playlists")
    public ResponseEntity<PlaylistDto> create(@AuthenticationPrincipal CustomUserPrincipal me, @RequestBody CreatePlaylistDto dto){
        PlaylistDto dto2 = playlistService.addPlaylist(me.getId(), dto);
        return ResponseEntity.created(URI.create("/api/playlists/" + dto2.id())).body(dto2);
    }

    @DeleteMapping("/playlists/{playlistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal CustomUserPrincipal me, @PathVariable int playlistId) throws UserException{
        playlistService.RemovePlaylist(me.getId(), playlistId);
    }
    @GetMapping("/list_of_playlists")
    public ResponseEntity<List<PlaylistDto>> list(@AuthenticationPrincipal CustomUserPrincipal me){
        List<PlaylistDto> dto2 = playlistService.listUserPlaylists(me.getId());
        return ResponseEntity.ok().body(dto2);
    }
    @GetMapping("/{id}/list_of_playlists")
    public ResponseEntity<List<PlaylistDto>> list_from_users(@PathVariable Integer id){
        List<PlaylistDto> dto3 = playlistService.listUserPlaylists(id);
        return ResponseEntity.ok().body(dto3);
    }

    @PatchMapping("/playlists/{playlistId}/name")
    public PlaylistDto rename(@AuthenticationPrincipal CustomUserPrincipal me, @PathVariable int playlistId, @RequestBody Map<String, String> body) throws UserException{
        return playlistService.rename(me.getId(), playlistId, body.get("name"));
    }


}

