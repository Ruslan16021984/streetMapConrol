package com.gmail3333333.opensreetmap.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Basslocation {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("lotittude")
    @Expose
    private double lotittude;
    @SerializedName("longittude")
    @Expose
    private double longittude;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLotittude() {
        return lotittude;
    }

    public void setLotittude(double lotittude) {
        this.lotittude = lotittude;
    }

    public double getLongittude() {
        return longittude;
    }

    public void setLongittude(double longittude) {
        this.longittude = longittude;
    }

    public Basslocation(Integer id, String name, double lotittude, double longittude) {
        this.id = id;
        this.name = name;
        this.lotittude = lotittude;
        this.longittude = longittude;
    }

    public Basslocation() {
    }

    @Override
    public String toString() {
        return "Basslocation{" +
                "id=" + id +
                ", lotittude=" + lotittude +
                ", longittude=" + longittude +
                '}';
    }
}
