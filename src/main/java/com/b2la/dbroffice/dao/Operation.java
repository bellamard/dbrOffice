package com.b2la.dbroffice.dao;

public class Operation {
    private int id;
    private String device;
    private float amount;
    private float commission;
    private String codereference, dateoperation;
    private CountUsers exp, ben;
    private State state;

    public Operation(int id, String device, float amount, float commission, String codereference, String dateoperation, CountUsers exp, CountUsers ben, State state) {
        this.id = id;
        this.device = device;
        this.amount = amount;
        this.commission = commission;
        this.codereference = codereference;
        this.dateoperation = dateoperation;
        this.exp = exp;
        this.ben = ben;
        this.state = state;
    }

    public Operation() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getCommission() {
        return commission;
    }

    public void setCommission(float commission) {
        this.commission = commission;
    }

    public String getCodereference() {
        return codereference;
    }

    public void setCodereference(String codereference) {
        this.codereference = codereference;
    }

    public String getDateoperation() {
        return dateoperation;
    }

    public void setDateoperation(String dateoperation) {
        this.dateoperation = dateoperation;
    }

    public CountUsers getExp() {
        return exp;
    }

    public void setExp(CountUsers exp) {
        this.exp = exp;
    }

    public CountUsers getBen() {
        return ben;
    }

    public void setBen(CountUsers ben) {
        this.ben = ben;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
