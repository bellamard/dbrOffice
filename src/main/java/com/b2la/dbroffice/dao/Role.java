package com.b2la.dbroffice.dao;

public class Role {
    private int id;
    private String Libelle;

    public Role(int id, String libelle) {
        this.id = id;
        Libelle = libelle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLibelle() {
        return Libelle;
    }

    public void setLibelle(String libelle) {
        Libelle = libelle;
    }


}
