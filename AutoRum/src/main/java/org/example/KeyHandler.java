package org.example;

import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent;
import javax.swing.*;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

public class KeyHandler implements HotkeyListener {
    private Robot robot;
    private volatile int key1 = KeyEvent.VK_A;
    private volatile int key2 = KeyEvent.VK_B;
    private volatile int toggleKey = KeyEvent.VK_ESCAPE;
    private volatile boolean isRunning = false;
    private final KeyPresserGUI gui;
    private static final int TOGGLE_ID = 1;
    private boolean isConfiguring = false;
    private Thread pressThread;

    public KeyHandler(KeyPresserGUI gui) {
        try {
            this.gui = gui;
            this.robot = new Robot();
            setupHotkeys();
            setupPressThread();
        } catch (AWTException e) {
            throw new RuntimeException("Falha ao inicializar Robot: " + e.getMessage());
        }
    }

    private void setupHotkeys() {
        try {
            JIntellitype.getInstance().registerHotKey(TOGGLE_ID, 0, toggleKey);
            JIntellitype.getInstance().addHotKeyListener(this);
            System.out.println("Hotkeys configurados com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao configurar hotkeys: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupPressThread() {
        pressThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (isRunning) {
                        robot.keyPress(key1);
                        robot.keyPress(key2);
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        pressThread.setDaemon(true);
        pressThread.start();
    }

    private void pressKeys() {
        try {
            System.out.println("Pressionando teclas...");
            robot.keyPress(key1);
            robot.keyPress(key2);
        } catch (Exception e) {
            System.err.println("Erro ao pressionar teclas: " + e.getMessage());
        }
    }

    private void releaseKeys() {
        try {
            System.out.println("Soltando teclas...");
            robot.keyRelease(key2);
            robot.keyRelease(key1);
        } catch (Exception e) {
            System.err.println("Erro ao soltar teclas: " + e.getMessage());
        }
    }

    private volatile long startTime = 0;

    private void forceStop() {
        isRunning = false;
        releaseKeys();
        System.out.println("Forçando parada...");
        gui.updateStatus(false);
    }

    @Override
    public void onHotKey(int identifier) {
        if (identifier == TOGGLE_ID && !isConfiguring) {
            if (!isRunning) {
                startTime = System.currentTimeMillis();
            }
            togglePressing();
        } else if (isRunning && System.currentTimeMillis() - startTime > 1000) {
            forceStop();
        }
    }

    public void handleKeyPress(int keyCode) {
        isConfiguring = true;
        if (gui.isConfiguringKey1()) {
            key1 = keyCode;
            SwingUtilities.invokeLater(() -> gui.setKey1Text(KeyEvent.getKeyText(keyCode)));
        } else if (gui.isConfiguringKey2()) {
            key2 = keyCode;
            SwingUtilities.invokeLater(() -> gui.setKey2Text(KeyEvent.getKeyText(keyCode)));
        } else if (gui.isConfiguringToggle()) {
            updateToggleHotkey(keyCode);
            SwingUtilities.invokeLater(() -> gui.setToggleKeyText(KeyEvent.getKeyText(keyCode)));
        }
        SwingUtilities.invokeLater(() -> gui.resetKeyConfiguration());
        isConfiguring = false;
    }

    private void updateToggleHotkey(int newKey) {
        try {
            JIntellitype.getInstance().unregisterHotKey(TOGGLE_ID);
            toggleKey = newKey;
            JIntellitype.getInstance().registerHotKey(TOGGLE_ID, 0, newKey);
        } catch (Exception e) {
            System.err.println("Erro ao atualizar hotkey: " + e.getMessage());
        }
    }

    public void togglePressing() {
        isRunning = !isRunning;
        if (isRunning) {
            pressKeys();
        } else {
            releaseKeys();
        }
        System.out.println("Estado alterado para: " + (isRunning ? "LIGADO" : "DESLIGADO"));
        gui.updateStatus(isRunning);
    }

    public void cleanup() {
        if (isRunning) {
            releaseKeys();
        }
        isRunning = false;
        isConfiguring = false;
        if (pressThread != null) {
            pressThread.interrupt();
        }
        try {
            JIntellitype.getInstance().unregisterHotKey(TOGGLE_ID);
            JIntellitype.getInstance().cleanUp();
        } catch (Exception e) {
            System.err.println("Erro ao limpar recursos: " + e.getMessage());
        }
    }
}