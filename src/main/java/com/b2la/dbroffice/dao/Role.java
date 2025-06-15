package com.b2la.dbroffice.dao;

public class Role {
    private int id;
    private String libelle;

    public Role(int id, String libelle) {
        this.id = id;
        libelle = libelle;
    }

    public Role() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        libelle = libelle;
    }


}
