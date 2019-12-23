package com.gb.modelObject;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Music {

    private static final Logger logger = LoggerFactory.getLogger(Music.class);

    private long id;
    private String title;
    private String author;
    private String album;
    private int year;
    private String genre;
    private String url;

    public Music() { }

    public Music(long id, String title, String author, String album, int year, String genre, String url) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.album = album;
        this.year = year;
        this.genre = genre;
        this.url = url;
    }

    public Music(ResultSet rs) {
        try {
            setId(      rs.getLong(  1) );
            setTitle(   rs.getString(2) );
            setAuthor(  rs.getString(3) );
            setAlbum(   rs.getString(4) );
            setYear(    rs.getInt(   5) );
            setGenre(   rs.getString(6) );
            setUrl(     rs.getString(7) );
        } catch(SQLException e) {
            logger.error("Error creating Music object: {}", e.getMessage());
        }
    }

    public JSONObject getJson() {
        return new JSONObject()
                .put("id", getId())
                .put("title", getTitle())
                .put("author", getAuthor())
                .put("album", getAlbum())
                .put("year", getYear())
                .put("genre", getGenre())
                .put("url", getUrl());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
