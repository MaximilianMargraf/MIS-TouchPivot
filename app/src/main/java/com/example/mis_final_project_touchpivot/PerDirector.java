package com.example.mis_final_project_touchpivot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerDirector {
    String name_;
    long gross_director_;
    long budget_director_;
    int movies_director_;
    float avg_rt_rating_;
    float avg_imdb_rating_;
    String favorite_genre_;
    String best_movie_;

    public PerDirector(String director_, List<Movie> movies) {
        this.name_ = director_;

        // add all movies with specific genre to temporary list
        List<Movie> tmp = new ArrayList<>();
        for(int i = 0; i < movies.size(); i++){
            if(this.name_.equals(movies.get(i).directors_)){
                tmp.add(movies.get(i));
            }
        }

        List<String> genres = new ArrayList<>();
        String best_movie = "";
        float best_rating = 0;

        for(int i = 0; i <tmp.size(); i++){
            this.gross_director_+= tmp.get(i).worldwide_gross_;
            this.budget_director_ += tmp.get(i).production_budget_;
            this.movies_director_ += tmp.size();
            this.avg_rt_rating_ += tmp.get(i).rotten_tomatoes_rating_;
            this.avg_imdb_rating_ += tmp.get(i).imdb_rating_;
            // add all genres to list
            genres.add(tmp.get(i).genre_);
            // scan for best movie
            if(tmp.get(i).imdb_rating_ > best_rating){
                best_rating = tmp.get(i).imdb_rating_;
                best_movie = tmp.get(i).title_;
            }
        }

        this.avg_rt_rating_ = this.avg_rt_rating_ / this.movies_director_;
        this.avg_rt_rating_= (float) ((int) (this.avg_rt_rating_ * 100f)) / 100f;

        this.avg_imdb_rating_ = avg_imdb_rating_ / this.movies_director_;
        this.avg_imdb_rating_ = (float)((int)(this.avg_imdb_rating_ * 100f)) / 100f;

        Map<String, Integer> hm = new HashMap<String, Integer>();
        for (String i : genres) {
            Integer j = hm.get(i);
            hm.put(i, (j == null) ? 1 : j + 1);
        }
        String genre = "Unknown";
        int most_common = 0;
        for(Map.Entry<String, Integer> pair : hm.entrySet()){
            if(pair.getValue() > most_common){
                genre = pair.getKey();
                most_common = pair.getValue();
            }
        }

        this.favorite_genre_ = genre;
        this.best_movie_ = best_movie;
    }
}
