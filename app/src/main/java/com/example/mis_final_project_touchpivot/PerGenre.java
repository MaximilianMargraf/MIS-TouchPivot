package com.example.mis_final_project_touchpivot;

import java.util.ArrayList;
import java.util.List;

public class PerGenre {
    String genre_ = "";
    int amount_movies_ = 0;
    long gross_genre_ = 0;
    long budget_genre_ = 0;
    float avg_rt_score_genre_ = 0;
    float avg_imdb_score_genre_ = 0;
    String top_rated_movie_genre_ = "Unknown";

    public PerGenre(String genre_, List<Movie> movies) {
        this.genre_ = genre_;

        // add all movies with specific genre to temporary list
        List<Movie> tmp = new ArrayList<>();
        for(int i = 0; i < movies.size(); i++){
            //System.out.println("Genre: "+genre_+", Found genre: "+movies.get(i).genre_);
            if(this.genre_ .equals(movies.get(i).genre_)){
                tmp.add(movies.get(i));
                //System.out.println("Added movie "+movies.get(i).title_+" to tmp list");
            }
        }

        //check only if there actually is a movie with that genre
        if(tmp.size()>0) {
            Movie top_rated = tmp.get(0);
            // derive all
            for (int i = 0; i < tmp.size(); i++) {
                this.gross_genre_ += tmp.get(i).worldwide_gross_;
                this.budget_genre_ += tmp.get(i).production_budget_;
                this.avg_rt_score_genre_ += tmp.get(i).rotten_tomatoes_rating_;
                this.avg_imdb_score_genre_ += tmp.get(i).imdb_rating_;
                if (tmp.get(i).imdb_rating_ > top_rated.imdb_rating_) {
                    top_rated = tmp.get(i);
                }
            }
            this. amount_movies_ = tmp.size();

            this.avg_rt_score_genre_ = this.avg_rt_score_genre_ / this.amount_movies_;
            this.avg_rt_score_genre_ = (float) ((int) (this.avg_rt_score_genre_ * 100f)) / 100f;

            this.avg_imdb_score_genre_ = avg_imdb_score_genre_ / this.amount_movies_;
            this.avg_imdb_score_genre_ = (float)((int)(this.avg_imdb_score_genre_ * 100f)) / 100f;

            this.top_rated_movie_genre_ = top_rated.title_;
        }
    }
}
