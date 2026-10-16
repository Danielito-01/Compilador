import java.util.ArrayList;
import java.util.List;

public class AnalizadorSemantico {
    List<String> errores = new ArrayList<>();

    public void analizar(List<Instruccion> instrucciones) {
        if (instrucciones.isEmpty()) {
            errores.add("Error semantico: el programa no tiene instrucciones");
            return;
        }

        for (Instruccion ins : instrucciones) {
            switch (ins.tipo) {
                case VELOCIDAD -> {
                    if (ins.valor < 0 || ins.valor > 100) {
                        errores.add("Error semantico en linea " + ins.linea
                                + ": la velocidad debe estar entre 0 y 100");
                    }
                    if (ins.valor != Math.floor(ins.valor)) {
                        errores.add("Error semantico en linea " + ins.linea
                                + ": la velocidad debe ser un numero entero");
                    }
                }

                case ADELANTE, ATRAS, IZQUIERDA, DERECHA, ESPERAR -> {
                    if (ins.valor <= 0) {
                        errores.add("Error semantico en linea " + ins.linea
                                + ": el tiempo debe ser mayor que 0");
                    }
                }

                default -> { }
            }
        }
    }

    public List<String> getErrores() {
        return errores;
    }
}