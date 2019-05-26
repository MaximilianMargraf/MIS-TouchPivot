package com.example.mis_final_project_touchpivot;

// class for data points belonging to Cycle Hires
public class CycleHires {

    private String date;
    private int hires;

    public CycleHires(String date, int hires){
        this.date = date;
        this.hires = hires;
    }

    public String getDate() {
        return date;
    }

    public int getHires() {
        return hires;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setHires(int hires) {
        this.hires = hires;
    }
}
