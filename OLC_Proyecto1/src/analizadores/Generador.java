package analizadores;

/**
 *
 * @author RANDY
 */
public class Generador {
    public static void main(String[] args){
        try{
        String ruta = "src/analizadores/";
        String opcFlex[] = {ruta+"Analizador_lexico","-d",ruta};
        jflex.Main.generate(opcFlex);
        String opcCUP[] = {"-destdir",ruta,"-parser","Analizador_sintactico",ruta+"Analizador_sintactico"};
        java_cup.Main.main(opcCUP);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
