package com.example.mis_final_project_touchpivot;

public class Movie {
    String title_;
    int worldwide_gross_;
    int production_budget_;
    int year;
    String genre_;
    String directors_;
    int rotten_tomatoes_rating_;
    float imdb_rating_;

    public Movie(String title_, int worldwide_gross_, int production_budget_, int year, String genre_, String directors_, int rotten_tomatoes_rating_, float imdb_rating_) {
        this.title_ = title_;
        this.worldwide_gross_ = worldwide_gross_;
        this.production_budget_ = production_budget_;
        this.year = year;
        this.genre_ = genre_;
        this.directors_ = directors_;
        this.rotten_tomatoes_rating_ = rotten_tomatoes_rating_;
        this.imdb_rating_ = imdb_rating_;
    }

    public void print(){
        System.out.println("Title: "+title_+", IMDB rating: "+imdb_rating_);
    }
}
