public class Token {
    TipoToken tipo;
    String lexema;
    int linea;

    public Token(TipoToken tipo, String lexema, int linea) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
    }

    @Override
    public String toString() {
        return "Linea " + this.linea + ": " + this.tipo + " -> " + this.lexema;
    }
}
