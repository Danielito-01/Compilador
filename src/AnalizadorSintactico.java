import java.util.ArrayList;
import java.util.List;

public class AnalizadorSintactico {
    List<Token> tokens;
    int posicion = 0;
    List<String> errores = new ArrayList<>();
    List<Instruccion> instrucciones = new ArrayList<>();

    private static class ErrorSintactico extends RuntimeException {
    }

    public AnalizadorSintactico(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token actual() {
        return tokens.get(posicion);
    }

    private boolean fin() {
        return posicion >= tokens.size();
    }

    public List<String> getErrores() {
        return errores;
    }

    private Token consumir(TipoToken esperado, String descripcion) {
        if (!fin() && actual().tipo == esperado) {
            return tokens.get(posicion++);
        }

        String encontrado;
        if (fin()) {
            encontrado = "el final del codigo";
        } else {
            encontrado = "'" + actual().lexema + "'";
        }

        int lineaError;
        if (posicion > 0) {
            lineaError = tokens.get(posicion - 1).linea;
        } else if (!fin()) {
            lineaError = actual().linea;
        } else {
            lineaError = 1;
        }

        errores.add("Error sintactico en linea " + lineaError + ": se esperaba " + descripcion + " pero se encontro " + encontrado);
        throw new ErrorSintactico();
    }

    public List<Instruccion> analizar() {
        try {
            consumir(TipoToken.INICIO, "'Inicio'");
            consumir(TipoToken.LLAVE_ABRE, "'{'");
        } catch (ErrorSintactico e) {
            return instrucciones;
        }

        while (!fin() && actual().tipo != TipoToken.LLAVE_CIERRA && actual().tipo != TipoToken.FIN) {
            try {
                instruccion();
            } catch (ErrorSintactico e) {
                sincronizar();
            }
        }

        try {
            consumir(TipoToken.LLAVE_CIERRA, "'}'");
            consumir(TipoToken.FIN, "'Fin'");
            consumir(TipoToken.PUNTO_COMA, "';'");
            if (!fin()) {
                errores.add("Error sintactico en linea " + actual().linea + ": no puede haber nada despues de 'Fin;'");
            }
        } catch (ErrorSintactico e) {
        }
        return instrucciones;
    }

    private void instruccion() {
        Token comando = actual();

        switch (comando.tipo) {
            case ADELANTE, ATRAS, IZQUIERDA, DERECHA, ESPERAR, VELOCIDAD -> {
                posicion++;
                consumir(TipoToken.PARENTESIS_ABRE, "'('");
                Token numero = consumir(TipoToken.NUMERO, "un numero");
                consumir(TipoToken.PARENTESIS_CIERRA, "')'");
                consumir(TipoToken.PUNTO_COMA, "';'");
                instrucciones.add(new Instruccion(comando.tipo, Double.parseDouble(numero.lexema), comando.linea));
            }

            case PARAR -> {
                posicion++;
                consumir(TipoToken.PARENTESIS_ABRE, "'('");
                consumir(TipoToken.PARENTESIS_CIERRA, "')'");
                consumir(TipoToken.PUNTO_COMA, "';'");
                instrucciones.add(new Instruccion(comando.tipo, 0, comando.linea));
            }

            default -> {
                errores.add("Error sintactico en linea " + comando.linea + ": se esperaba una instruccion pero se encontro '" + comando.lexema + "'");
                throw new ErrorSintactico();
            }
        }
    }

    private void sincronizar() {
        while (!fin()) {
            TipoToken tipo = actual().tipo;
            if (tipo == TipoToken.LLAVE_CIERRA || tipo == TipoToken.FIN || esComando(tipo)) {
                return;
            }
            posicion++;
            if (tipo == TipoToken.PUNTO_COMA) {
                return;
            }
        }
    }

    private boolean esComando(TipoToken tipo) {
        return tipo == TipoToken.ADELANTE || tipo == TipoToken.ATRAS
                || tipo == TipoToken.IZQUIERDA || tipo == TipoToken.DERECHA
                || tipo == TipoToken.ESPERAR || tipo == TipoToken.VELOCIDAD
                || tipo == TipoToken.PARAR;
    }
}
