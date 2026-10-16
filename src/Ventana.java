import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class Ventana extends JFrame {

    private static final Color VERDE = new Color(0, 130, 0);
    private static final Color ROJO = new Color(190, 0, 0);
    private static final Color GRIS_FONDO = new Color(240, 240, 240);
    private static final Font FUENTE = new Font("Consolas", Font.PLAIN, 15);

    private static final String EJEMPLO = """
            Inicio {
                Velocidad(80);
                Avanzar(1.5);
                Derecha(0.6);
                Parar();
            }
            Fin;
            """;

    private final JButton btnCompilar = new JButton("Compilar");
    private final JButton btnEjecutar = new JButton("Ejecutar");
    private final JButton btnCompilarEjecutar = new JButton("Compilar y ejecutar");
    private final JButton btnDepurar = new JButton("Depurar");
    private final JButton btnEjemplo = new JButton("Ejemplo");

    private final JTextArea editor = new JTextArea();
    private final JTextArea numerosLinea = new JTextArea();
    private final JTextArea areaArduino = new JTextArea();
    private final JTextArea areaResultado = new JTextArea();
    private final JTabbedPane pestanasResultado = new JTabbedPane();

    private final DefaultTableModel modeloTokens = new DefaultTableModel(
            new Object[]{"#", "Línea", "Tipo", "Lexema"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tablaTokens = new JTable(modeloTokens);

    private final JLabel lblPosicion = new JLabel("Línea 1, columna 1");
    private final JLabel lblEstado = new JLabel("Sin compilar");

    private List<Instruccion> instrucciones = null;
    private boolean compilado = false;

    public Ventana() {
        super("CompiladorCarro IDE");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 760);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        barra.add(btnCompilar);
        barra.addSeparator();
        barra.add(btnEjecutar);
        barra.addSeparator();
        barra.add(btnCompilarEjecutar);
        barra.addSeparator();
        barra.add(btnDepurar);
        barra.addSeparator();
        barra.add(btnEjemplo);

        btnCompilar.setToolTipText("Revisa el código (Ctrl+B)");
        btnEjecutar.setToolTipText("Genera el código Arduino y lo copia");
        btnCompilarEjecutar.setToolTipText("Compila y genera el Arduino (F5)");
        btnDepurar.setToolTipText("Muestra los tokens del código");
        btnEjemplo.setToolTipText("Carga el programa de ejemplo");

        editor.setFont(FUENTE);
        editor.setTabSize(4);
        editor.setMargin(new Insets(4, 6, 4, 6));
        editor.setText(EJEMPLO);
        editor.setCaretPosition(0);

        JScrollPane scrollEditor = new JScrollPane(editor);
        scrollEditor.setBorder(BorderFactory.createTitledBorder("Código fuente"));

        numerosLinea.setFont(FUENTE);
        numerosLinea.setEditable(false);
        numerosLinea.setBackground(GRIS_FONDO);
        numerosLinea.setForeground(Color.GRAY);
        numerosLinea.setMargin(new Insets(4, 6, 4, 6));
        scrollEditor.setRowHeaderView(numerosLinea);

        editor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { cambiarCodigo(); }
            public void removeUpdate(DocumentEvent e) { cambiarCodigo(); }
            public void changedUpdate(DocumentEvent e) { }
        });
        editor.addCaretListener(e -> actualizarPosicion());

        areaArduino.setFont(FUENTE);
        areaArduino.setEditable(false);
        areaArduino.setMargin(new Insets(4, 6, 4, 6));
        JScrollPane scrollArduino = new JScrollPane(areaArduino);
        scrollArduino.setBorder(BorderFactory.createTitledBorder("Código Arduino generado"));

        areaResultado.setFont(FUENTE);
        areaResultado.setEditable(false);
        areaResultado.setMargin(new Insets(4, 6, 4, 6));
        pestanasResultado.addTab("Mensajes", new JScrollPane(areaResultado));

        tablaTokens.setFont(FUENTE);
        pestanasResultado.addTab("Tokens", new JScrollPane(tablaTokens));

        JPanel panelResultado = new JPanel(new BorderLayout());
        panelResultado.setBorder(BorderFactory.createTitledBorder("Resultado"));
        panelResultado.add(pestanasResultado, BorderLayout.CENTER);

        JPanel barraEstado = new JPanel(new BorderLayout());
        barraEstado.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        barraEstado.add(lblPosicion, BorderLayout.WEST);
        barraEstado.add(lblEstado, BorderLayout.EAST);

        JSplitPane arriba = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollEditor, scrollArduino);
        arriba.setResizeWeight(0.5);

        JSplitPane principal = new JSplitPane(JSplitPane.VERTICAL_SPLIT, arriba, panelResultado);
        principal.setResizeWeight(0.60);
        SwingUtilities.invokeLater(() -> principal.setDividerLocation(0.60));

        add(barra, BorderLayout.NORTH);
        add(principal, BorderLayout.CENTER);
        add(barraEstado, BorderLayout.SOUTH);

        btnCompilar.addActionListener(e -> compilar());
        btnEjecutar.addActionListener(e -> ejecutar());
        btnCompilarEjecutar.addActionListener(e -> {
            if (compilar()) ejecutar();
        });
        btnDepurar.addActionListener(e -> depurar());
        btnEjemplo.addActionListener(e -> cargarEjemplo());

        registrarAtajo(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK), btnCompilar, "compilar");
        registrarAtajo(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), btnCompilarEjecutar, "compilarEjecutar");

        actualizarNumeros();
        actualizarPosicion();
    }

    private void registrarAtajo(KeyStroke tecla, JButton boton, String nombre) {
        JComponent raiz = getRootPane();
        raiz.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(tecla, nombre);
        raiz.getActionMap().put(nombre, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boton.doClick();
            }
        });
    }

    private void cambiarCodigo() {
        actualizarNumeros();
        compilado = false;
        mostrarEstado("Sin compilar", Color.DARK_GRAY);
    }

    private void actualizarNumeros() {
        StringBuilder sb = new StringBuilder();
        int total = editor.getLineCount();
        for (int i = 1; i <= total; i++) {
            sb.append(i);
            if (i < total) {
                sb.append("\n");
            }
        }
        numerosLinea.setText(sb.toString());
    }

    private void actualizarPosicion() {
        try {
            int pos = editor.getCaretPosition();
            int linea = editor.getLineOfOffset(pos);
            int columna = pos - editor.getLineStartOffset(linea) + 1;
            lblPosicion.setText("Línea " + (linea + 1) + ", columna " + columna);
        } catch (BadLocationException ex) {
            lblPosicion.setText("");
        }
    }

    private boolean compilar() {
        compilado = false;
        areaArduino.setText("");

        AnalizadorLexico lexico = new AnalizadorLexico(editor.getText());
        List<Token> tokens = lexico.obtenerTokens();
        llenarTablaTokens(tokens);
        if (!lexico.getErrores().isEmpty()) {
            mostrarErrores(lexico.getErrores());
            return false;
        }

        AnalizadorSintactico sintactico = new AnalizadorSintactico(tokens);
        instrucciones = sintactico.analizar();
        if (!sintactico.getErrores().isEmpty()) {
            mostrarErrores(sintactico.getErrores());
            return false;
        }

        AnalizadorSemantico semantico = new AnalizadorSemantico();
        semantico.analizar(instrucciones);
        if (!semantico.getErrores().isEmpty()) {
            mostrarErrores(semantico.getErrores());
            return false;
        }

        compilado = true;
        mostrarEstado("Compilado: listo para ejecutar", VERDE);
        mostrarResultado("Compilación exitosa: " + instrucciones.size()
                + " instrucciones, 0 errores.", VERDE);
        return true;
    }

    private void ejecutar() {
        if (!compilado) {
            mostrarResultado("No hay una compilación válida. Presiona 'Compilar' primero.", ROJO);
            return;
        }

        String ino = new GeneradorArduino().generar(instrucciones);
        areaArduino.setText(ino);
        areaArduino.setCaretPosition(0);

        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(ino), null);

        mostrarResultado("Código Arduino generado y copiado al portapapeles. "
                + "Pégalo en Arduino IDE con Ctrl+V.", VERDE);
    }

    private void depurar() {
        AnalizadorLexico lexico = new AnalizadorLexico(editor.getText());
        List<Token> tokens = lexico.obtenerTokens();
        llenarTablaTokens(tokens);

        StringBuilder sb = new StringBuilder();
        sb.append("Tokens encontrados: ").append(tokens.size()).append("\n");
        if (lexico.getErrores().isEmpty()) {
            sb.append("Sin errores léxicos.");
        } else {
            sb.append("Errores léxicos: ").append(lexico.getErrores().size()).append("\n");
            for (String error : lexico.getErrores()) {
                sb.append(error).append("\n");
            }
        }

        mostrarResultado(sb.toString(), lexico.getErrores().isEmpty() ? VERDE : ROJO);
        pestanasResultado.setSelectedIndex(1);
    }

    private void cargarEjemplo() {
        if (!editor.getText().equals(EJEMPLO)) {
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "Esto reemplazará lo que tienes en el editor. ¿Continuar?",
                    "Cargar ejemplo", JOptionPane.YES_NO_OPTION);
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }

        editor.setText(EJEMPLO);
        editor.setCaretPosition(0);
        areaArduino.setText("");
        mostrarResultado("Ejemplo cargado. Presiona Ctrl+B para revisarlo.", VERDE);
    }

    private void mostrarErrores(List<String> errores) {
        StringBuilder sb = new StringBuilder("Compilación fallida: " + errores.size() + " error(es)\n\n");
        for (String error : errores) {
            sb.append(error).append("\n");
        }
        mostrarEstado("Con errores", ROJO);
        mostrarResultado(sb.toString(), ROJO);
    }

    private void llenarTablaTokens(List<Token> tokens) {
        modeloTokens.setRowCount(0);
        int numero = 1;
        for (Token t : tokens) {
            modeloTokens.addRow(new Object[]{numero++, t.linea, t.tipo, t.lexema});
        }
    }

    private void mostrarResultado(String texto, Color color) {
        areaResultado.setForeground(color);
        areaResultado.setText(texto);
        pestanasResultado.setSelectedIndex(0);
    }

    private void mostrarEstado(String texto, Color color) {
        lblEstado.setText(texto);
        lblEstado.setForeground(color);
    }
}