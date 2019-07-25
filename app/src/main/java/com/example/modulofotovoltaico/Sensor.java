package com.example.modulofotovoltaico;

import android.graphics.Color;

import java.io.Serializable;

public class Sensor implements Serializable {
    private String sensorName;
    private String medida;
    private int color;

    public Sensor(String sensorName)
    {
        this.sensorName = sensorName;
        this.medida = "0.00";
        this.color = Color.parseColor("#8DFF33");
    }
    public String getSensorName(){
        return sensorName;
    }

    public String getMedida(){
        return medida;
    }

    public int getColor(){
        return color;
    }

    public void setColor(String color){
        this.color = Color.parseColor(color);
    }

    public void setMedida(String medida){
        this.medida = medida;
    }
}
