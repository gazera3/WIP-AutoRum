package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class KeyPresserGUI extends JFrame {
    private JButton btnKey1;
    private JButton btnKey2;
    private JButton btnToggleKey;
    private JPanel statusIndicator;
    private JLabel statusLabel;
    private JLabel creditsLabel;
    private boolean configuringKey1 = false;
    private boolean configuringKey2 = false;
    private boolean configuringToggle = false;
    private KeyHandler handler;
    private KeyEventDispatcher currentDispatcher;

    public KeyPresserGUI() {
        try {
            setupGUI();
            handler = new KeyHandler(this);
        } catch (Exception e) {
            handleInitializationError(e);
        }
    }

    private void handleInitializationError(Exception e) {
        String message = "Erro ao inicializar o programa:\n" + e.getMessage();
        JOptionPane.showMessageDialog(this, message, "Erro de Inicialização",
                JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    private void setupGUI() {
        setTitle("Auto Presser");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Painel de botões
        JPanel buttonsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        btnKey1 = createStyledButton("Tecla 1: A");
        btnKey2 = createStyledButton("Tecla 2: B");
        btnToggleKey = createStyledButton("Tecla Liga/Desliga: ESC");

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout(5, 0));
        statusPanel.setOpaque(false);

        // Indicador de status
        statusIndicator = new JPanel();
        statusIndicator.setPreferredSize(new Dimension(15, 15));
        statusIndicator.setBackground(Color.RED);
        statusIndicator.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        // Label de status
        statusLabel = new JLabel("DESLIGADO", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));

        // Adiciona componentes ao painel de status
        JPanel indicatorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        indicatorPanel.setOpaque(false);
        indicatorPanel.add(statusIndicator);
        indicatorPanel.add(statusLabel);

        buttonsPanel.add(btnKey1);
        buttonsPanel.add(btnKey2);
        buttonsPanel.add(btnToggleKey);
        buttonsPanel.add(statusPanel);

        creditsLabel = new JLabel("por Sophie Alonso", SwingConstants.RIGHT);
        creditsLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        creditsLabel.setForeground(new Color(100, 100, 100));

        mainPanel.add(buttonsPanel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(creditsLabel);

        setupButtonListeners();

        add(mainPanel);
        pack();
        setSize(200, 180);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        setupWindowDragging(mainPanel);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(240, 240, 240));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return button;
    }

    private void setupButtonListeners() {
        btnKey1.addActionListener(e -> {
            configuringKey1 = true;
            configuringKey2 = false;
            configuringToggle = false;
            btnKey1.setText("Pressione uma tecla...");
            statusLabel.setText("Aguardando tecla...");
            startKeyMonitoring();
        });

        btnKey2.addActionListener(e -> {
            configuringKey2 = true;
            configuringKey1 = false;
            configuringToggle = false;
            btnKey2.setText("Pressione uma tecla...");
            statusLabel.setText("Aguardando tecla...");
            startKeyMonitoring();
        });

        btnToggleKey.addActionListener(e -> {
            configuringToggle = true;
            configuringKey1 = false;
            configuringKey2 = false;
            btnToggleKey.setText("Pressione uma tecla...");
            statusLabel.setText("Aguardando tecla...");
            startKeyMonitoring();
        });
    }

    private void startKeyMonitoring() {
        // Remove o dispatcher anterior se existir
        if (currentDispatcher != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .removeKeyEventDispatcher(currentDispatcher);
        }

        // Cria e registra o novo dispatcher
        currentDispatcher = e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && isConfiguringKeys()) {
                handler.handleKeyPress(e.getKeyCode());

                // Remove o dispatcher após configurar
                KeyboardFocusManager.getCurrentKeyboardFocusManager()
                        .removeKeyEventDispatcher(currentDispatcher);
                currentDispatcher = null;

                return true;
            }
            return false;
        };

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(currentDispatcher);
    }

    private void setupWindowDragging(JPanel panel) {
        Point offset = new Point();
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                offset.setLocation(e.getX(), e.getY());
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point p = getLocation();
                setLocation(p.x + e.getX() - offset.x, p.y + e.getY() - offset.y);
            }
        });
    }

    public boolean isConfiguringKeys() {
        return configuringKey1 || configuringKey2 || configuringToggle;
    }

    public boolean isConfiguringKey1() {
        return configuringKey1;
    }

    public boolean isConfiguringKey2() {
        return configuringKey2;
    }

    public boolean isConfiguringToggle() {
        return configuringToggle;
    }

    public void setKey1Text(String text) {
        btnKey1.setText("Tecla 1: " + text);
        statusLabel.setText("DESLIGADO");
    }

    public void setKey2Text(String text) {
        btnKey2.setText("Tecla 2: " + text);
        statusLabel.setText("DESLIGADO");
    }

    public void setToggleKeyText(String text) {
        btnToggleKey.setText("Tecla Liga/Desliga: " + text);
        statusLabel.setText("DESLIGADO");
    }

    public void resetKeyConfiguration() {
        configuringKey1 = false;
        configuringKey2 = false;
        configuringToggle = false;
    }

    public void updateStatus(boolean isRunning) {
        SwingUtilities.invokeLater(() -> {
            if (isRunning) {
                statusIndicator.setBackground(Color.GREEN);
                statusLabel.setText("LIGADO");
                statusLabel.setForeground(new Color(0, 100, 0));
            } else {
                statusIndicator.setBackground(Color.RED);
                statusLabel.setText("DESLIGADO");
                statusLabel.setForeground(Color.BLACK);
            }
        });
    }

    @Override
    public void dispose() {
        if (handler != null) {
            handler.cleanup();
        }
        if (currentDispatcher != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .removeKeyEventDispatcher(currentDispatcher);
        }
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                KeyPresserGUI frame = new KeyPresserGUI();
                frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Erro ao iniciar o programa: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}