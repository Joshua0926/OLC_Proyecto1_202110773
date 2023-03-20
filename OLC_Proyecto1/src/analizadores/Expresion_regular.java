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
    private String grafica_arbol_expresion = "";
    private String grafica_thompson = "";
    private int num_nodo = 0;
    private int nodos_thompson = 0;
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
        metodo_arbol(arbol_expresion);
        grafica_arbol_expresion += "digraph{label = \"Arbol de expresión\"\n" + g_arbol(arbol_expresion, 0) + "}";
        crear_archivo("src/carpeta_reporte/ARBOLES_202110773/", "Arbol " + nombre, grafica_arbol_expresion);
        grafica_thompson += "digraph {label = \"AFND " + nombre
                + "\";\nrankdir=\"LR\";\nnode [shape=\"circle\"];"
                + "\nN_0[label=\"\"];\n"
                + "\nN_1[shape = doublecircle, label=\"\"];\n" + g_thompson(0, 1, arbol_expresion.getIzq()) + "}";
        crear_archivo("src/carpeta_reporte/AFND_202110773/", "AFND " + nombre, grafica_thompson);
        crear_archivo("src/SIGUIENTES_202110773/", "Siguientes " + nombre, g_tabla_siguientes());
        crear_tabla_transiciones();
        
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
            codigo_dot += "N_" + primero + " -> N_" + ultimo + "[label=\"" + actual.getDato().replaceAll("\"", "") + "\"];\n";
            return codigo_dot;
        }

        switch (actual.getDato()) {
            case "." -> {
                nodos_thompson += 1;
                int mitad = nodos_thompson;
                codigo_dot += "N_" + nodos_thompson + "[label = \"\"];\n";
                codigo_dot += g_thompson(primero, mitad, actual.getIzq());
                codigo_dot += g_thompson(mitad, ultimo, actual.getDer());
            }
            case "|" -> {
                nodos_thompson += 1;
                int pos_izq = nodos_thompson;
                codigo_dot += "N_" + pos_izq + "[label = \"\"];\n";
                codigo_dot += "N_" + primero + " -> N_" + pos_izq + "[label=\"ε\"];\n";
                nodos_thompson += 1;
                int pos_der = nodos_thompson;
                codigo_dot += "N_" + pos_der + "[label = \"\"];\n";
                codigo_dot += "N_" + pos_der + " -> N_" + ultimo + "[label=\"ε\"];\n";
                codigo_dot += g_thompson(pos_izq, pos_der, actual.getIzq());
                nodos_thompson += 1;
                pos_izq = nodos_thompson;
                codigo_dot += "N_" + pos_izq + "[label = \"\"];\n";
                codigo_dot += "N_" + primero + " -> N_" + pos_izq + "[label=\"ε\"];\n";
                nodos_thompson += 1;
                pos_der = nodos_thompson;
                codigo_dot += "N_" + pos_der + "[label = \"\"];\n";
                codigo_dot += "N_" + pos_der + " -> N_" + ultimo + "[label=\"ε\"];\n";
                codigo_dot += g_thompson(pos_izq, pos_der, actual.getDer());
            }
            case "+" -> {
                nodos_thompson += 1;
                int pos_izq = nodos_thompson;
                codigo_dot += "N_" + pos_izq + "[label = \"\"];\n";
                codigo_dot += "N_" + primero + " -> N_" + pos_izq + "[label=\"ε\"];\n";
                nodos_thompson += 1;
                int pos_der = nodos_thompson;
                codigo_dot += "N_" + pos_der + "[label = \"\"];\n";
                codigo_dot += "N_" + pos_der + " -> N_" + ultimo + "[label=\"ε\"];\n";
                codigo_dot += "N_" + pos_der + " -> N_" + pos_izq + "[label=\"ε\"];\n";
                codigo_dot += g_thompson(pos_izq, pos_der, actual.getIzq());
            }
            case "*" -> {
                nodos_thompson += 1;
                int pos_izq = nodos_thompson;
                codigo_dot += "N_" + pos_izq + "[label = \"\"];\n";
                codigo_dot += "N_" + primero + " -> N_" + pos_izq + "[label=\"ε\"];\n";
                codigo_dot += "N_" + primero + " -> N_" + ultimo + "[label=\"ε\"];\n";
                nodos_thompson += 1;
                int pos_der = nodos_thompson;
                codigo_dot += "N_" + pos_der + "[label = \"\"];\n";
                codigo_dot += "N_" + pos_der + " -> N_" + ultimo + "[label=\"ε\"];\n";
                codigo_dot += "N_" + pos_der + " -> N_" + pos_izq + "[label=\"ε\"];\n";
                codigo_dot += g_thompson(pos_izq, pos_der, actual.getIzq());
            }
            case "?" -> {
                nodos_thompson += 1;
                int pos_izq = nodos_thompson;
                codigo_dot += "N_" + pos_izq + "[label = \"\"];\n";
                codigo_dot += "N_" + primero + " -> N_" + pos_izq + "[label=\"ε\"];\n";
                codigo_dot += "N_" + primero + " -> N_" + ultimo + "[label=\"ε\"];\n";
                nodos_thompson += 1;
                int pos_der = nodos_thompson;
                codigo_dot += "N_" + pos_der + "[label = \"\"];\n";
                codigo_dot += "N_" + pos_der + " -> N_" + ultimo + "[label=\"ε\"];\n";
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
            s += "N_" + actual + "[shape = none label=<\n"
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
            s += "N_" + actual + "[shape = none label=<\n"
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
            s += "N_" + padre + "-> N_" + actual + "[dir=none]; \n";
        }

        s += g_arbol(nodo.getIzq(), actual);

        s += g_arbol(nodo.getDer(), actual);

        return s;
    }
    
    public String g_tabla_siguientes() {
        String s = "label=<\n"
                + " <TABLE border=\"1\" cellspacing=\"0\" cellpadding=\"10\"  >\n"
                + "  <TR>\n"
                + "  <TD bgcolor=\"#A897BC\"> <font color=\"white\"> <b>Simbolo</b> </font></TD>\n"
                + "  <TD bgcolor=\"#A897BC\"> <font color=\"white\"> <b>Hoja</b></font></TD>\n"
                + "  <TD bgcolor=\"#A897BC\"> <font color=\"white\"> <b>Siguientes</b></font></TD>\n"
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
