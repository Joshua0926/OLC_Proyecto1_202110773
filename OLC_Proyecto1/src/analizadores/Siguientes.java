/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package analizadores;

import java.util.ArrayList;

/**
 *
 * @author RANDY
 */
public class Siguientes {
     private String simbolo;
    private int hoja;
    private ArrayList<Integer> siguientes = new ArrayList<>();

    public Siguientes(String simbolo, int hoja) {
        this.simbolo = simbolo;
        this.hoja = hoja;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public int getHoja() {
        return hoja;
    }

    public ArrayList<Integer> getSiguientes() {
        return siguientes;
    }
}
