package com.b2la.dbroffice.dao;

import com.b2la.dbroffice.preference.RoleType;

public class Role {
    private int id;
    private String libelle;

    public Role() {
    }

    public Role(int id, String libelle) {
        this.id = id;
        this.libelle = libelle;
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
        this.libelle = libelle;
    }
}
