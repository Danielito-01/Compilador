public class Instruccion {
    TipoToken tipo;
    double valor;
    int linea;

    public Instruccion(TipoToken tipo, double valor, int linea) {
        this.tipo = tipo;
        this.valor = valor;
        this.linea = linea;
    }

    @Override
    public String toString() {
        if (tipo == TipoToken.PARAR) {
            return "Linea " + linea + ": " + tipo;
        } else {
            return "Linea " + linea + ": " + tipo + " (" + valor + ")";
        }
    }
}
