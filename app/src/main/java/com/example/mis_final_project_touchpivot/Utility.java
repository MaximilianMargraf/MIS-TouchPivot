package com.example.mis_final_project_touchpivot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utility {
    public String[][] initMovieArray(List<Movie> l){
        int length = l.size();
        String[][] strings = new String[length+1][8];
        strings[0][0] = "Title";
        strings[0][1] = "Ww. Gross";
        strings[0][2] = "Budget";
        strings[0][3] = "Year";
        strings[0][4] = "Genre";
        strings[0][5] = "Director";
        strings[0][6] = "RT";
        strings[0][7] = "Imdb";
        for(int i = 1; i <l.size()+1;i++){
            for(int j = 0; j < 8; j++){
                switch (j) {
                    case 0:
                        strings[i][j] = "" + l.get(i-1).title_;
                        break;
                    case 1:
                        strings[i][j] = "" + l.get(i-1).worldwide_gross_;
                        break;
                    case 2:
                        strings[i][j] = "" + l.get(i-1).production_budget_;
                        break;
                    case 3:
                        strings[i][j] = "" + l.get(i-1).year;
                        break;
                    case 4:
                        strings[i][j] = "" + l.get(i-1).genre_;
                        break;
                    case 5:
                        strings[i][j] = "" + l.get(i-1).directors_;
                        break;
                    case 6:
                        strings[i][j] = "" + l.get(i-1).rotten_tomatoes_rating_;
                        break;
                    case 7:
                        strings[i][j] = "" + l.get(i-1).imdb_rating_;
                        break;
                }
            }
        }
        return strings;
    }

    public String[][] initPerYearArray(List<PerYear> l){
        int length = l.size();
        String[][] strings = new String[length+1][8];
        strings[0][0] = "Year";
        strings[0][1] = "Gross";
        strings[0][2] = "Budget";
        strings[0][3] = "Movies";
        strings[0][4] = "Dom. genre";
        strings[0][5] = "Avg. RT";
        strings[0][6] = "Avg. Imdb";
        strings[0][7] = "B. movie";
        for(int i = 1; i <l.size()+1;i++){
            for(int j = 0; j < 8; j++){
                switch (j) {
                    case 0:
                        strings[i][j] = "" + l.get(i-1).year_;
                        break;
                    case 1:
                        strings[i][j] = "" + l.get(i-1).grossedPerYear_;
                        break;
                    case 2:
                        strings[i][j] = "" + l.get(i-1).budgetPerYear_;
                        break;
                    case 3:
                        strings[i][j] = "" + l.get(i-1).moviesPerYear_;
                        break;
                    case 4:
                        strings[i][j] = "" + l.get(i-1).mostCommonGenre_;
                        break;
                    case 5:
                        strings[i][j] = "" + l.get(i-1).averageTomatoeScore_;
                        break;
                    case 6:
                        strings[i][j] = "" + l.get(i-1).averageIMDBScore_;
                        break;
                    case 7:
                        strings[i][j] = "" + l.get(i-1).IMDBbestMovie_;
                        break;
                }
            }
        }
        print(strings);
        return strings;
    }

    public String[][] initPerGenreArray(List<PerGenre> l){
        int length = l.size();
        String[][] strings = new String[length+1][7];
        strings[0][0] = "Genre";
        strings[0][1] = "Movies";
        strings[0][2] = "Gross";
        strings[0][3] = "Budget";
        strings[0][4] = "Avg. RT";
        strings[0][5] = "Avg. Imdb";
        strings[0][6] = "Best movie";
        for(int i = 1; i <l.size()+1;i++){
            for(int j = 0; j < 8; j++){
                switch (j) {
                    case 0:
                        strings[i][j] = "" + l.get(i-1).genre_;
                        break;
                    case 1:
                        strings[i][j] = "" + l.get(i-1).amount_movies_;
                        break;
                    case 2:
                        strings[i][j] = "" + l.get(i-1).gross_genre_;
                        break;
                    case 3:
                        strings[i][j] = "" + l.get(i-1).budget_genre_;
                        break;
                    case 4:
                        strings[i][j] = "" + l.get(i-1).avg_rt_score_genre_;
                        break;
                    case 5:
                        strings[i][j] = "" + l.get(i-1).avg_imdb_score_genre_;
                        break;
                    case 6:
                        strings[i][j] = "" + l.get(i-1).top_rated_movie_genre_;
                        break;
                }
            }
        }
        return strings;
    }

    public String[][] initPerDirectorArray(List<PerDirector> l){
        int length = l.size();
        String[][] strings = new String[length+1][8];
        strings[0][0] = "Director";
        strings[0][1] = "Gross";
        strings[0][2] = "Budget";
        strings[0][3] = "Movies";
        strings[0][4] = "Avg. RT";
        strings[0][5] = "Avg. Imdb";
        strings[0][6] = "Fav. genre";
        strings[0][7] = "Best movie";
        for(int i = 1; i <l.size()+1;i++){
            for(int j = 0; j < 8; j++){
                switch (j) {
                    case 0:
                        strings[i][j] = "" + l.get(i-1).name_;
                        break;
                    case 1:
                        strings[i][j] = "" + l.get(i-1).gross_director_;
                        break;
                    case 2:
                        strings[i][j] = "" + l.get(i-1).budget_director_;
                        break;
                    case 3:
                        strings[i][j] = "" + l.get(i-1).movies_director_;
                        break;
                    case 4:
                        strings[i][j] = "" + l.get(i-1).avg_rt_rating_;
                        break;
                    case 5:
                        strings[i][j] = "" + l.get(i-1).avg_imdb_rating_;
                        break;
                    case 6:
                        strings[i][j] = "" + l.get(i-1).favorite_genre_;
                        break;
                    case 7:
                        strings[i][j] = "" + l.get(i-1).best_movie_;
                        break;
                }
            }
        }
        return strings;
    }

    public void print(String[][] s){
        for(int i = 0; i < s.length; i++){
            for(int j = 0; j < s[0].length; j++){
                System.out.println("i.j: "+s[i][j]);
            }
        }
    }

    public List<String> initGenres(){
        List<String> l = new ArrayList<>();
        l.add("Action");
        l.add("Adventure");
        l.add("Black Comedy");
        l.add("Comedy");
        l.add("Concert/Performance");
        l.add("Crime");
        l.add("Documentary");
        l.add("Drama");
        l.add("Horror");
        l.add("Musical");
        l.add("Romantic Comedy");
        l.add("Romantic Drama");
        l.add("Thriller/Suspense");
        l.add("Western");
        return l;
    }

    public List<String> initDirectors(List<Movie> s){
        List<String> directors = new ArrayList<>();
        for(Movie m : s){
            directors.add(m.directors_);
        }

        Map<String, Integer> hm = new HashMap<String, Integer>();
        for (String i : directors) {
            Integer j = hm.get(i);
            hm.put(i, (j == null) ? 1 : j + 1);
        }

        List<String> l = new ArrayList<>();
        for(Map.Entry<String, Integer> pair : hm.entrySet()){
            l.add(pair.getKey());
        }
        return l;
    }
}
