package com.spotify11.demo.exception;

public class SongException extends Exception{
    private static final long serialVersionUID = 1L;
    public SongException(String message) {
        super(message);
    }
    public SongException(){
        super();
    }
}
