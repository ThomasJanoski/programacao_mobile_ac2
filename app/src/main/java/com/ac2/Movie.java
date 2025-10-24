package com.ac2;

public class Movie {
    private String id;
    private String title;
    private String director;
    private int year;
    private int rating;
    private String genre;
    private boolean seenInCinema;

    public Movie() {
    }

    public Movie(String id, String title, String director, int year, int rating, String genre, boolean seenInCinema) {
        this.id = id;
        this.title = title;
        this.director = director;
        this.year = year;
        this.rating = rating;
        this.genre = genre;
        this.seenInCinema = seenInCinema;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public boolean isSeenInCinema() {
        return seenInCinema;
    }

    public void setSeenInCinema(boolean seenInCinema) {
        this.seenInCinema = seenInCinema;
    }
}