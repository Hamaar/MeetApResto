package com.hamaar.meetapresto.Model;

/*
 ***********************************************************************
 * Created by Hamaar on 1/5/2019
 * Author : Hamaar @ 2019 : MeetApResto.git
 * Jakarta, Indonesia
 ***********************************************************************
 */
public class Category {

    private int id;
    private String cat;

    public Category(int id, String cat) {
        this.id = id;
        this.cat = cat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }


}
