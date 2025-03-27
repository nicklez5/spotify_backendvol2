package com.spotify11.demo.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class LoginResponse {

    private String token;

    private long expiresIn;

}
