package com.example.mis_final_project_touchpivot;

public class KSI {
    public KSI(String year_, int long_term_trend, int pedestrian_, int pedal_cycle_, int powered_two_wheeler_, int car_taxi_, int bus_coach_, int goods_other_) {
        this.year_ = year_;
        this.long_term_trend_ = long_term_trend;
        this.pedestrian_ = pedestrian_;
        this.pedal_cycle_ = pedal_cycle_;
        this.powered_two_wheeler_ = powered_two_wheeler_;
        this.car_taxi_ = car_taxi_;
        this.bus_coach_ = bus_coach_;
        this.goods_other_ = goods_other_;
    }

    public String getYear_() {
        return year_;
    }

    public int getLong_term_trend() {
        return long_term_trend_;
    }

    public int getPedestrian_() {
        return pedestrian_;
    }

    public int getPedal_cycle_() {
        return pedal_cycle_;
    }

    public int getPowered_two_wheeler_() {
        return powered_two_wheeler_;
    }

    public int getCar_taxi_() {
        return car_taxi_;
    }

    public int getBus_coach_() {
        return bus_coach_;
    }

    public int getGoods_other_() {
        return goods_other_;
    }

    public void print(){
        System.out.print("Year: "+year_+", long term trend: "+long_term_trend_+"\n" +
                "KSI pedestrian: "+pedestrian_+", KSI pedal: "+pedal_cycle_+", KSI two wheeler: "+powered_two_wheeler_+", KSI car/taxi: "+car_taxi_+"\n KSI bus/coach: "+bus_coach_+", KSI goods/other: "+goods_other_+"\n\n");
    }

    // page 1, row 12 to 21, column 0 and 1
    String year_;
    int long_term_trend_;

    // page 2, row 2 to 11, column 1 2 3 4 5 6
    int pedestrian_;
    int pedal_cycle_;
    int powered_two_wheeler_;
    int car_taxi_;
    int bus_coach_;
    int goods_other_;
}
