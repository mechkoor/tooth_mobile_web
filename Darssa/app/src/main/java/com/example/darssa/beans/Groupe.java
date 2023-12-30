package com.example.darssa.beans;

import java.util.List;

public class Groupe {

    private long id;
    private String code;
    private String year;

    private List<PW> pws;

    public Groupe(long id, String code, String year, List<PW> pws) {
        this.id = id;
        this.code = code;
        this.year = year;
        this.pws = pws;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public List<PW> getPws() {
        return pws;
    }

    public void setPws(List<PW> pws) {
        this.pws = pws;
    }
}
