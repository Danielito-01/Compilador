import java.util.List;

public class GeneradorArduino {

    public String generar(List<Instruccion> instrucciones) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
                // Codigo generado por mi compilador

                const int ENA = 5;   // Velocidad motor izquierdo
                const int IN1 = 7;
                const int IN2 = 8;
                const int ENB = 6;   // Velocidad motor derecho
                const int IN3 = 9;
                const int IN4 = 10;

                void aplicarVelocidad(int pwm) {
                  analogWrite(ENA, pwm);
                  analogWrite(ENB, pwm);
                }

                void adelante() {
                  digitalWrite(IN1, HIGH); digitalWrite(IN2, LOW);
                  digitalWrite(IN3, HIGH); digitalWrite(IN4, LOW);
                }

                void atras() {
                  digitalWrite(IN1, LOW); digitalWrite(IN2, HIGH);
                  digitalWrite(IN3, LOW); digitalWrite(IN4, HIGH);
                }

                void izquierda() {
                  digitalWrite(IN1, LOW); digitalWrite(IN2, HIGH);
                  digitalWrite(IN3, HIGH); digitalWrite(IN4, LOW);
                }

                void derecha() {
                  digitalWrite(IN1, HIGH); digitalWrite(IN2, LOW);
                  digitalWrite(IN3, LOW); digitalWrite(IN4, HIGH);
                }

                void parar() {
                  digitalWrite(IN1, LOW); digitalWrite(IN2, LOW);
                  digitalWrite(IN3, LOW); digitalWrite(IN4, LOW);
                }

                void setup() {
                  pinMode(ENA, OUTPUT); pinMode(IN1, OUTPUT); pinMode(IN2, OUTPUT);
                  pinMode(ENB, OUTPUT); pinMode(IN3, OUTPUT); pinMode(IN4, OUTPUT);

                  aplicarVelocidad(204); // Velocidad inicial: 80%
                  delay(2000);           // Tiempo para poner el carro en el piso

                """);

        // Parte variable: una traducción por cada instrucción
        for (Instruccion ins : instrucciones) {
            sb.append("  // Linea ").append(ins.linea).append("\n");
            switch (ins.tipo) {
                case VELOCIDAD -> sb.append("  aplicarVelocidad(").append(aPwm(ins.valor)).append(");\n");
                case ADELANTE  -> sb.append("  adelante();\n  delay(").append(aMs(ins.valor)).append(");\n");
                case ATRAS     -> sb.append("  atras();\n  delay(").append(aMs(ins.valor)).append(");\n");
                case IZQUIERDA -> sb.append("  izquierda();\n  delay(").append(aMs(ins.valor)).append(");\n");
                case DERECHA   -> sb.append("  derecha();\n  delay(").append(aMs(ins.valor)).append(");\n");
                case PARAR     -> sb.append("  parar();\n");
                case ESPERAR   -> sb.append("  parar();\n  delay(").append(aMs(ins.valor)).append(");\n");
                default -> { }
            }
        }

        sb.append("""

                  parar(); // Parada final automatica
                }

                void loop() {
                  // El programa se ejecuta una sola vez
                }
                """);

        return sb.toString();
    }

    // Segundos → milisegundos (1.5 → 1500)
    private long aMs(double segundos) {
        return Math.round(segundos * 1000);
    }

    // Velocidad 0–100 → PWM 0–255 (80 → 204)
    private int aPwm(double velocidad) {
        return (int) Math.round(velocidad * 255 / 100);
    }
}