package com.b2la.dbroffice.dao;

import com.b2la.dbroffice.preference.StateOper;

public class State {
    private int id;
    private StateOper libelle;

    public State(int id, StateOper libelle) {
        this.id = id;
        this.libelle = libelle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public StateOper getLibelle() {
        return libelle;
    }

    public void setLibelle(StateOper libelle) {
        this.libelle = libelle;
    }
}
