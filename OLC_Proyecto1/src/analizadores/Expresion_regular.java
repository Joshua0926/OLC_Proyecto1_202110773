/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package analizadores;


import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author RANDY
 */
public class Expresion_regular {
    private Nodo arbol_expresion;
    private String nombre;
    private ArrayList<Siguientes> tabla_siguientes = new ArrayList<>();
    private ArrayList<String> terminales = new ArrayList<>();
    private ArrayList<ArrayList> transiciones = new ArrayList<>();
    private ArrayList<Integer> estados_aceptacion = new ArrayList<>();
    private String grafica_thompson = "";
    private int num_nodo = 0;
    private int nodos_thompson = 1;
    private int num_hoja = 0;

    public Nodo getArbol_expresion() {
        return arbol_expresion;
    }

    public Expresion_regular( String nombre, Nodo arbol_expresion) {
        Nodo raiz = new Nodo(".");
        Nodo aceptacion = new Nodo("#");
        aceptacion.setHoja(true);
        raiz.setDer(aceptacion);
        raiz.setIzq(arbol_expresion);
        
        this.arbol_expresion = raiz;
        this.nombre = nombre;
    }

    public void proceso() throws IOException, InterruptedException {
        numerar_nodos(arbol_expresion);
        System.out.println("nodos numerados");
        metodo_arbol(arbol_expresion);
        System.out.println("metodo del arbol");
        crear_archivo("src/carpeta_reporte/ARBOLES_202110773/", "Arbol " + nombre, "digraph{label = \"Arbol de expresión\"\n" + g_arbol(arbol_expresion, 0) + "}");
        System.out.println("se ha creado el arbol");
        grafica_thompson += "digraph {label = \"AFND " + nombre
                + "\";\nrankdir=\"LR\";\nnode [shape=\"circle\"];"
                + "\nS0;\n"
                + "\nS1[shape = doublecircle];\n" + g_thompson(0, 1, arbol_expresion.getIzq()) + "}";
        crear_archivo("src/carpeta_reporte/AFND_202110773/", "AFND " + nombre, grafica_thompson);
        System.out.println("se ha creado el afn");
        crear_archivo("src/carpeta_reporte/SIGUIENTES_202110773/", "Siguientes " + nombre, "graph{"+g_tabla_siguientes()+"}");
        System.out.println("se han creado los siguientes");
        crear_tabla_transiciones();
        System.out.println("se han creado las transiciones ");
        crear_archivo("src/carpeta_reporte/TRANSICIONES_202110773/", "Transiciones " + nombre, "graph{"+g_tabla_transiciones()+"}");
        crear_archivo("src/carpeta_reporte/AFD_202110773/", "AFD " + nombre, "digraph {label = \"AFD " + nombre + "\"\n" + g_afd()+"}");
    }

    public void numerar_nodos(Nodo actual) {
        if (actual == null) {
            return;
        }
        numerar_nodos(actual.getIzq());
        numerar_nodos(actual.getDer());
        if (actual.isHoja()) {
            actual.setNumero(num_hoja);
            num_hoja++;
            tabla_siguientes.add(new Siguientes(actual.getDato(), actual.getNumero()));
            if (!terminales.contains(actual.getDato()) && !actual.getDato().equals("#")) {
                terminales.add(actual.getDato());
            }
        }
    }

    public void metodo_arbol(Nodo actual) {
        if (actual == null) {
            return;
        }

        metodo_arbol(actual.getIzq());
        metodo_arbol(actual.getDer());

        if (actual.isHoja()) {
            actual.setAnulable(false);
            actual.getPrimeros().add(actual.getNumero());
            actual.getUltimos().add(actual.getNumero());
        } else {
            switch (actual.getDato()) {
                case "*" -> {
                    actual.setAnulable(true);
                    actual.getPrimeros().addAll(actual.getIzq().getPrimeros());
                    actual.getUltimos().addAll(actual.getIzq().getPrimeros());
                    for (int est : actual.getIzq().getUltimos()) {
                        tabla_siguientes.get(est).getSiguientes().addAll(actual.getIzq().getPrimeros());
                    }
                }
                case "?" -> {
                    actual.setAnulable(true);
                    actual.getPrimeros().addAll(actual.getIzq().getPrimeros());
                    actual.getUltimos().addAll(actual.getIzq().getPrimeros());
                }
                case "+" -> {
                    actual.setAnulable(actual.getIzq().isAnulable());
                    actual.getPrimeros().addAll(actual.getIzq().getPrimeros());
                    actual.getUltimos().addAll(actual.getIzq().getPrimeros());
                    for (int est : actual.getIzq().getUltimos()) {
                        tabla_siguientes.get(est).getSiguientes().addAll(actual.getIzq().getPrimeros());
                    }
                }
                case "|" -> {
                    actual.setAnulable(actual.getIzq().isAnulable() || actual.getDer().isAnulable());
                    actual.getPrimeros().addAll(actual.getIzq().getPrimeros());
                    actual.getPrimeros().addAll(actual.getDer().getPrimeros());
                    actual.getUltimos().addAll(actual.getIzq().getUltimos());
                    actual.getUltimos().addAll(actual.getDer().getUltimos());
                }
                case "." -> {
                    actual.setAnulable(actual.getIzq().isAnulable() && actual.getDer().isAnulable());
                    if (actual.getIzq().isAnulable()) {
                        actual.getPrimeros().addAll(actual.getIzq().getPrimeros());
                        actual.getPrimeros().addAll(actual.getDer().getPrimeros());
                    } else {
                        actual.getPrimeros().addAll(actual.getIzq().getPrimeros());
                    }
                    if (actual.getDer().isAnulable()) {
                        actual.getUltimos().addAll(actual.getIzq().getUltimos());
                        actual.getUltimos().addAll(actual.getDer().getUltimos());
                    } else {
                        actual.getUltimos().addAll(actual.getDer().getUltimos());
                    }
                    for (int est : actual.getIzq().getUltimos()) {
                        tabla_siguientes.get(est).getSiguientes().addAll(actual.getDer().getPrimeros());
                    }
                }
            }
        }
    }
    
    public void crear_tabla_transiciones() {
        int indice = 0;
        transiciones.add(new ArrayList<>());
        ArrayList fila = transiciones.get(0);
        fila.add(arbol_expresion.getPrimeros());
        while (indice < transiciones.size()) {
            fila = transiciones.get(indice);
            for (String s : terminales) {
                fila.add(new ArrayList<>());
            }
            for (int siguiente : (ArrayList<Integer>) fila.get(0)) {
                String simbolo = tabla_siguientes.get(siguiente).getSimbolo();
                if (simbolo.equals("#")) {
                    continue;
                }
                int columna = terminales.indexOf(simbolo) + 1;
                ArrayList<Integer> col_terminal = (ArrayList<Integer>) fila.get(columna);
                for (int i : tabla_siguientes.get(siguiente).getSiguientes()) {
                    if (!col_terminal.contains(i)) {
                        col_terminal.add(i);
                    }
                }
                Collections.sort(col_terminal);
            }
            boolean encontrado;
            for (int i = 1; i < fila.size(); i++) {
                encontrado = false;
                ArrayList<Integer> estado = (ArrayList<Integer>) fila.get(i);
                for (ArrayList<ArrayList> filas : transiciones) {
                    if (filas.get(0).equals(estado)) {
                        encontrado = true;
                        break;
                    }
                }
                if (!encontrado && !estado.isEmpty()) {
                    ArrayList<ArrayList> nueva_fila = new ArrayList<>();
                    nueva_fila.add(estado);
                    transiciones.add(nueva_fila);
                }
            }
            indice++;
        }
    }
    
     public String g_thompson(int primero, int ultimo, Nodo actual) {
        String codigo_dot = "";

        if (actual.isHoja()) {
            codigo_dot += "S" + primero + " -> S" + ultimo + "[label=\"" + actual.getDato().replaceAll("\"", "") + "\"];\n";
            return codigo_dot;
        }

        switch (actual.getDato()) {
            case "." -> {
                nodos_thompson += 1;
                int mitad = nodos_thompson;
                codigo_dot += "S" + nodos_thompson + ";\n";
                codigo_dot += g_thompson(primero, mitad, actual.getIzq());
                codigo_dot += g_thompson(mitad, ultimo, actual.getDer());
            }
            case "|" -> {
                nodos_thompson += 1;
                int pos_izq = nodos_thompson;
                codigo_dot += "S" + pos_izq + ";\n";
                codigo_dot += "S" + primero + " -> S" + pos_izq + "[label=\"ε\"];\n";
                nodos_thompson += 1;
                int pos_der = nodos_thompson;
                codigo_dot += "S" + pos_der + ";\n";
                codigo_dot += "S" + pos_der + " -> S" + ultimo + "[label=\"ε\"];\n";
                codigo_dot += g_thompson(pos_izq, pos_der, actual.getIzq());
                nodos_thompson += 1;
                pos_izq = nodos_thompson;
                codigo_dot += "S" + pos_izq + ";\n";
                codigo_dot += "S" + primero + " -> S" + pos_izq + "[label=\"ε\"];\n";
                nodos_thompson += 1;
                pos_der = nodos_thompson;
                codigo_dot += "S" + pos_der + ";\n";
                codigo_dot += "S" + pos_der + " -> S" + ultimo + "[label=\"ε\"];\n";
                codigo_dot += g_thompson(pos_izq, pos_der, actual.getDer());
            }
            case "+" -> {
                nodos_thompson += 1;
                int pos_izq = nodos_thompson;
                codigo_dot += "S" + pos_izq + ";\n";
                codigo_dot += "S" + primero + " -> S" + pos_izq + "[label=\"ε\"];\n";
                nodos_thompson += 1;
                int pos_der = nodos_thompson;
                codigo_dot += "S" + pos_der + ";\n";
                codigo_dot += "S" + pos_der + " -> S" + ultimo + "[label=\"ε\"];\n";
                codigo_dot += "S" + pos_der + " -> S" + pos_izq + "[label=\"ε\"];\n";
                codigo_dot += g_thompson(pos_izq, pos_der, actual.getIzq());
            }
            case "*" -> {
                nodos_thompson += 1;
                int pos_izq = nodos_thompson;
                codigo_dot += "S" + pos_izq + ";\n";
                codigo_dot += "S" + primero + " -> S" + pos_izq + "[label=\"ε\"];\n";
                codigo_dot += "S" + primero + " -> S" + ultimo + "[label=\"ε\"];\n";
                nodos_thompson += 1;
                int pos_der = nodos_thompson;
                codigo_dot += "S" + pos_der + ";\n";
                codigo_dot += "S" + pos_der + " -> S" + ultimo + "[label=\"ε\"];\n";
                codigo_dot += "S" + pos_der + " -> S" + pos_izq + "[label=\"ε\"];\n";
                codigo_dot += g_thompson(pos_izq, pos_der, actual.getIzq());
            }
            case "?" -> {
                nodos_thompson += 1;
                int pos_izq = nodos_thompson;
                codigo_dot += "S" + pos_izq + ";\n";
                codigo_dot += "S" + primero + " -> S" + pos_izq + "[label=\"ε\"];\n";
                codigo_dot += "S" + primero + " -> S" + ultimo + "[label=\"ε\"];\n";
                nodos_thompson += 1;
                int pos_der = nodos_thompson;
                codigo_dot += "S" + pos_der + ";\n";
                codigo_dot += "S" + pos_der + " -> S" + ultimo + "[label=\"ε\"];\n";
                codigo_dot += g_thompson(pos_izq, pos_der, actual.getIzq());
            }
        }
        return codigo_dot;
    }
    
    public String g_arbol(Nodo nodo, int padre) {
        String s = "";
        num_nodo += 1;

        int actual = num_nodo;
        if (nodo == null) {
            num_nodo -= 1;
            return s;
        }

        if (nodo.isHoja()) {
            s += "S" + actual + "[shape = none label=<\n"
                    + " <TABLE border=\"0\" cellspacing=\"2\" cellpadding=\"15\"  >\n"
                    + "  <TR>\n"
                    + "  <TD colspan=\"3\"> Anulable: " + nodo.isAnulable() + "</TD>\n"
                    + "  </TR>\n"
                    + "  <TR>\n"
                    + "  <TD > Primeros: " + nodo.getPrimeros() + "</TD>\n"
                    + "  <TD border=\"1\" style=\"rounded\">" + nodo.getDato() + "</TD>\n"
                    + "  <TD > Ultimos: " + nodo.getUltimos() + "</TD>\n"
                    + "  </TR>\n"
                    + "  <TR>\n"
                    + "  <TD colspan=\"3\"> Numero: " + nodo.getNumero() + "</TD>\n"
                    + "  </TR>\n"
                    + " </TABLE>>];";
        } else {
            s += "S" + actual + "[shape = none label=<\n"
                    + " <TABLE border=\"0\" cellspacing=\"2\" cellpadding=\"10\"  >\n"
                    + "  <TR>\n"
                    + "  <TD colspan=\"3\">  Anulable: " + nodo.isAnulable() + "</TD>\n"
                    + "  </TR>\n"
                    + "  <TR>\n"
                    + "  <TD > Primeros: " + nodo.getPrimeros() + "</TD>\n"
                    + "  <TD border=\"1\" style=\"rounded\">" + nodo.getDato() + "</TD>\n"
                    + "  <TD > Ultimos: " + nodo.getUltimos() + "</TD>\n"
                    + "  </TR>\n"
                    + " </TABLE>>];";
        }

        if (padre != 0) {
            s += "S" + padre + "-> S" + actual + "[dir=none]; \n";
        }

        s += g_arbol(nodo.getIzq(), actual);

        s += g_arbol(nodo.getDer(), actual);

        return s;
    }
    
    public String g_tabla_siguientes() {
        String s = "label=<\n"
                + " <TABLE border=\"1\" cellspacing=\"0\" cellpadding=\"10\"  >\n"
                + "  <TR>\n"
                + "  <TD bgcolor=\"#A897BC\"> <font color=\"white\"><b> Simbolo </b></font></TD>\n"
                + "  <TD bgcolor=\"#A897BC\"> <font color=\"white\"><b> Hoja </b></font></TD>\n"
                + "  <TD bgcolor=\"#A897BC\"> <font color=\"white\"><b> Siguientes </b></font></TD>\n"
                + "  </TR>\n";
        for (Siguientes t : tabla_siguientes) {
            s += " <TR>\n"
                    + "  <TD>" + t.getSimbolo() + "</TD>\n"
                    + "  <TD>" + t.getHoja() + "</TD>\n"
                    + "  <TD> " + t.getSiguientes() + "</TD>\n"
                    + "  </TR>\n";
        }
        s += " </TABLE>>";
        return s;
    }
    
        public String g_tabla_transiciones() {
        String s = "label=<\n"
                + " <TABLE border=\"1\" cellspacing=\"2\" cellpadding=\"10\"  >\n"
                + "  <TR>\n"
                + "  <TD rowspan=\"2\" bgcolor=\"#A897BC\"> <font color=\"white\"><b> Estado </b></font></TD>\n"
                + "  <TD colspan=\"" + terminales.size() + "\" bgcolor=\"#A897BC\"> <font color=\"white\"><b> Terminales </b></font></TD>\n"
                + "  </TR>\n"
                + "  <TR>\n";

        for (String terminal : terminales) {
            s += "  <TD bgcolor=\"#A897BC\"> <font color=\"white\"><b> " + terminal + " </b></font></TD>\n";
        }
        s += "  </TR>\n"
                + "  \n";
        for (ArrayList<ArrayList> fila : transiciones) {
            s += "  <TR>\n"
                    + "  <TD>S" + transiciones.indexOf(fila) + " " + fila.get(0) + "</TD>\n";
            for (int i = 1; i < fila.size(); i++) {
                ArrayList<Integer> actual = fila.get(i);
                if (actual.isEmpty()) {
                    s += "  <TD> [] </TD>\n";
                }
                for (ArrayList<ArrayList> estados : transiciones) {
                    if (estados.get(0).equals(actual)) {
                        s += "  <TD>S" + transiciones.indexOf(estados) + "</TD>\n";
                        break;
                    }
                }
            }
            s += "  </TR>\n";
        }
        s += " </TABLE>>";
        return s;
    }
        
        public String g_afd() {
        String s = "rankdir=\"LR\";\n"
                + "node [shape=\"circle\"];\n"
                + "SI[shape = none];";
        for (int i = 0; i < transiciones.size(); i++) {
            if (((ArrayList<ArrayList>) transiciones.get(i)).get(0).contains(tabla_siguientes.size() - 1)) {
                s += "S" + i + "[shape=\"doublecircle\"];\n";
                estados_aceptacion.add(i);
            } else {
                s += "S" + i + ";\n";
            }
        }
        s += "SI->S0[label=\"Inicio\"];\n";
        for (ArrayList<ArrayList> f : transiciones) {
            for (int indice = 1; indice < f.size(); indice++) {
                ArrayList<Integer> actual = f.get(indice);
                for (ArrayList<ArrayList> estados : transiciones) {
                    if (estados.get(0).equals(actual)) {
                        s += "  S" + transiciones.indexOf(f) + "->S" + transiciones.indexOf(estados) + "[label=\"" + terminales.get(indice - 1).replaceAll("\"", "") + "\"]";
                        break;
                    }
                }
            }
        }
        return s;
    }
    
    public boolean analizar_cadena(ArrayList<Conjunto> conjuntos, String cadena) {
        int num_estado = 0;
        int caracter;
        int num_col;
        ArrayList<Integer> transicion;
        boolean encontrado = false;

        for (int indice = 1; indice < cadena.length() - 1; indice++) {
            ArrayList<ArrayList> estado = transiciones.get(num_estado);
            caracter = (int) cadena.charAt(indice);
            encontrado = false;
            for(int siguiente: (ArrayList<Integer>)estado.get(0)){
                String t = tabla_siguientes.get(siguiente).getSimbolo();
                if (t.startsWith("\"") && t.endsWith("\"")) {
                    if ((int) t.charAt(1) == caracter ) {
                        encontrado = true;
                        num_col = terminales.indexOf(t) + 1;
                        transicion = estado.get(num_col);
                        for (ArrayList<ArrayList> fila : transiciones) {
                            if (fila.get(0).equals(transicion)) {
                                num_estado = transiciones.indexOf(fila);
                                break;
                            }
                        }
                        break;
                    }
                } else if (t.startsWith("\\")) {
                    if ((int) cadena.charAt(indice + 1) == (int) t.charAt(1)) {
                        encontrado = true;
                        num_col = terminales.indexOf(t) + 1;
                        transicion = estado.get(num_col);
                        indice++;
                        for (ArrayList<ArrayList> fila : transiciones) {
                            if (fila.get(0).equals(transicion)) {
                                num_estado = transiciones.indexOf(fila);
                                break;
                            }
                        }
                        break;
                    }
                } else {
                    ArrayList<Integer> evaluado = new ArrayList<>();
                    for (Conjunto c : conjuntos) {
                        if (c.getNombre().equals(t)) {
                            evaluado = c.getCaracteres();
                            break;
                        }
                    }
                    if (evaluado.contains(caracter)) {
                        encontrado = true;
                        num_col = terminales.indexOf(t) + 1;
                        transicion = estado.get(num_col);
                        for (ArrayList<ArrayList> fila : transiciones) {
                            if (fila.get(0).equals(transicion)) {
                                num_estado = transiciones.indexOf(fila);
                                break;
                            }
                        }
                        break;
                    }
                }

            }
            if (!encontrado) {
                return false;
            }
        }

        return estados_aceptacion.contains(num_estado);
    }

    public void crear_archivo(String dir, String nombre, String texto) throws IOException, InterruptedException {
        File file = new File(dir, nombre);

        if (!file.exists()) {
            file.createNewFile();
        }
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
        } catch (FileNotFoundException ex) {

        }

        pw.write(texto);
        pw.flush();
        pw.close();

        String[] c = {"dot", "-Tpng", file.getAbsolutePath(), "-O"};
        Process p = Runtime.getRuntime().exec(c);
        int err = p.waitFor();
        file.delete();
    }

    public String getNombre() {
        return nombre;
    }

}
