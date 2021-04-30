package com.fluidtouch.bookshelf;

import java.io.Serializable;

public class BookShelfDo implements Serializable {

    private String name = "";
    private String date = "";
    private int cover;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCover() {
        return cover;
    }

    public void setCover(int cover) {
        this.cover = cover;
    }
}
