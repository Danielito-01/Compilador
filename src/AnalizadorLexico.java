import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnalizadorLexico {
    String codigo;
    int indice = 0;
    int linea = 1;
    List<Token> tokens = new ArrayList<>();
    List<String> errores = new ArrayList<>();
    static final HashMap<String, TipoToken> palabrasReservadas = new HashMap<>();

    static {
        palabrasReservadas.put("Inicio", TipoToken.INICIO);
        palabrasReservadas.put("Fin", TipoToken.FIN);
        palabrasReservadas.put("Avanzar", TipoToken.ADELANTE);
        palabrasReservadas.put("Retroceder", TipoToken.ATRAS);
        palabrasReservadas.put("Izquierda", TipoToken.IZQUIERDA);
        palabrasReservadas.put("Derecha", TipoToken.DERECHA);
        palabrasReservadas.put("Parar", TipoToken.PARAR);
        palabrasReservadas.put("Esperar", TipoToken.ESPERAR);
        palabrasReservadas.put("Velocidad", TipoToken.VELOCIDAD);
    }

    public AnalizadorLexico(String codigo) {
        this.codigo = codigo;
    }


    public List<Token> obtenerTokens() {
        while (indice < codigo.length()) {
            char actual = codigo.charAt(indice);
            if (actual == '\n' ) {
                linea++;
                indice++;
            } else if (Character.isWhitespace(actual)) {
                indice++;
            } else if (Character.isLetter(actual)) {
                leerPalabra();
            } else if (Character.isDigit(actual)) {
                leerNumero();
            } else {
                leerSimbolo(actual);
            }
        }
        return tokens;
    }

    private void leerPalabra() {
        int inicio = indice;

        while (indice < codigo.length() && Character.isLetterOrDigit(codigo.charAt(indice))) {
            indice++;
        }

        String palabra = codigo.substring(inicio, indice);
        TipoToken tipo = palabrasReservadas.get(palabra);
        if (tipo != null) {
            tokens.add(new Token(tipo, palabra, linea));
        } else {
            tokens.add(new Token(TipoToken.ERROR, palabra, linea));
            errores.add("Error léxico en línea " + linea + ": palabra no reconocida '" + palabra + "'");
        }
    }

    private void leerNumero() {
        int inicio = indice;

        while (indice < codigo.length() && Character.isDigit(codigo.charAt(indice))) {
            indice++;
        }

        if (indice < codigo.length() && codigo.charAt(indice) == '.') {
            indice++;
            if (indice < codigo.length() && Character.isDigit(codigo.charAt(indice))) {
                while(indice < codigo.length() && Character.isDigit(codigo.charAt(indice))){
                    indice++;
                }
            } else {
                String numeroMal = codigo.substring(inicio, indice);
                tokens.add(new Token(TipoToken.ERROR, numeroMal, linea));
                errores.add("Error léxico en línea " + linea + ": numero mal escrito '" + numeroMal + "'");
                return;
            }
        }

        String numero = codigo.substring(inicio, indice);
        tokens.add(new Token(TipoToken.NUMERO, numero, linea));
    }

    private void leerSimbolo(char actual) {
        TipoToken tipo;
        switch (actual) {
            case '(' -> tipo = TipoToken.PARENTESIS_ABRE;
            case ')' -> tipo = TipoToken.PARENTESIS_CIERRA;
            case '{' -> tipo = TipoToken.LLAVE_ABRE;
            case '}' -> tipo = TipoToken.LLAVE_CIERRA;
            case ';' -> tipo = TipoToken.PUNTO_COMA;
            default -> tipo = null;
        }

        String lexema = String.valueOf(actual);
        if (tipo != null) {
            tokens.add(new Token(tipo, lexema, linea));
        } else {
            tokens.add(new Token(TipoToken.ERROR, lexema, linea));
            errores.add("Error léxico en línea " + linea + ": simbolo no valido '" + lexema+ "'");
        }

        indice++;
    }

    public List<String> getErrores() {
        return errores;
    }
}
