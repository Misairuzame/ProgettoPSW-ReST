package com.gb.modelObject;

public class SearchFilter {

    private String title;
    private String author;
    private String album;
    private String year;
    private String genre;

    public SearchFilter() {
        this.title = null;
        this.author = null;
        this.album = null;
        this.year = null;
        this.genre = null;
    }

    public SearchFilter(String title, String author, String album, String year, String genre) {
        this.title = title;
        this.author = author;
        this.album = album;
        this.year = year;
        this.genre = genre;
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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
