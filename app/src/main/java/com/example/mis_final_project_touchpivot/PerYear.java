package com.example.mis_final_project_touchpivot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerYear {
    int year_ = 0;
    long grossedPerYear_ = 0;
    long budgetPerYear_ = 0;
    int moviesPerYear_ = 0;
    String mostCommonGenre_ = "Unknown";
    float averageTomatoeScore_ = 0;
    float averageIMDBScore_ = 0;
    String IMDBbestMovie_ = "Unknown";
    Map<String, Integer> genres_amount = new HashMap<>();

    public PerYear(int year_, List<Movie> movies) {
        this.year_ = year_;

        // add all movies with specific year to temporary list
        List<Movie> tmp = new ArrayList<>();
        for(int i = 0; i < movies.size(); i++){
            if(this.year_ == movies.get(i).year){
                tmp.add(movies.get(i));
            }
        }

        List<String> genres = new ArrayList<>();

        Movie top_rated = tmp.get(0);
        // derive all
        for(int i = 0; i <tmp.size(); i++){
            this.grossedPerYear_ += tmp.get(i).worldwide_gross_;
            this.budgetPerYear_ += tmp.get(i).production_budget_;
            this.averageTomatoeScore_ += tmp.get(i).rotten_tomatoes_rating_;
            this.averageIMDBScore_ += tmp.get(i).imdb_rating_;
            genres.add(tmp.get(i).genre_);
            if(tmp.get(i).imdb_rating_ > top_rated.imdb_rating_){
                top_rated = tmp.get(i);
            }
        }
        this.moviesPerYear_ = tmp.size();
        this.averageTomatoeScore_ = this.averageTomatoeScore_/this.moviesPerYear_;
        this.averageTomatoeScore_ = (float)((int)(this.averageTomatoeScore_*100f))/100f;

        this.averageIMDBScore_ = this.averageIMDBScore_/this.moviesPerYear_;
        this.averageIMDBScore_ = (float)((int)(this.averageIMDBScore_*100f))/100f;

        // https://www.geeksforgeeks.org/count-occurrences-elements-list-java/
        // find most common genre
        Map<String, Integer> hm = new HashMap<String, Integer>();
        for (String i : genres) {
            Integer j = hm.get(i);
            hm.put(i, (j == null) ? 1 : j + 1);
        }
        this.genres_amount = hm;

        String genre = "Unknown";
        int most_common = 0;
        for(Map.Entry<String, Integer> pair : hm.entrySet()){
            if(pair.getValue() > most_common){
                genre = pair.getKey();
                most_common = pair.getValue();
            }
        }

        this.mostCommonGenre_ = genre;
        this.IMDBbestMovie_ = top_rated.title_;

        /*
        for(Map.Entry<String, Integer> pair : hm.entrySet()){
            System.out.println("Genre: "+pair.getKey()+", Amount per Year: "+pair.getValue());
        }
        */
    }
}
