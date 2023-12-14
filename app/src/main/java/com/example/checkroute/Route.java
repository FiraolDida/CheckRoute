package com.example.checkroute;

public class Route {
    private String name;
    private double type;
    private String sLongitude;
    private String sLatitude;
    private String dLongitude;
    private String dLatitude;
    private String mLongitude;
    private String mLatitude;

    public Route(String name, String sLongitude, String sLatitude, String dLongitude, String dLatitude) {
        this.name = name;
        this.sLongitude = sLongitude;
        this.sLatitude = sLatitude;
        this.dLongitude = dLongitude;
        this.dLatitude = dLatitude;
    }

    public Route(String sLongitude, String sLatitude, String dLongitude, String dLatitude) {
        this.sLongitude = sLongitude;
        this.sLatitude = sLatitude;
        this.dLongitude = dLongitude;
        this.dLatitude = dLatitude;
    }

    public Route(String sLongitude, String sLatitude, String dLongitude, String dLatitude,
                 String mLongitude, String mLatitude) {
        this.sLongitude = sLongitude;
        this.sLatitude = sLatitude;
        this.dLongitude = dLongitude;
        this.dLatitude = dLatitude;
        this.mLongitude = mLongitude;
        this.mLatitude = mLatitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getType() {
        return type;
    }

    public void setType(double type) {
        this.type = type;
    }

    public String getsLongitude() {
        return sLongitude;
    }

    public void setsLongitude(String sLongitude) {
        this.sLongitude = sLongitude;
    }

    public String getsLatitude() {
        return sLatitude;
    }

    public void setsLatitude(String sLatitude) {
        this.sLatitude = sLatitude;
    }

    public String getdLongitude() {
        return dLongitude;
    }

    public void setdLongitude(String dLongitude) {
        this.dLongitude = dLongitude;
    }

    public String getdLatitude() {
        return dLatitude;
    }

    public void setdLatitude(String dLatitude) {
        this.dLatitude = dLatitude;
    }

    public String getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(String mLongitude) {
        this.mLongitude = mLongitude;
    }

    public String getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(String mLatitude) {
        this.mLatitude = mLatitude;
    }
}
