/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import controller.DeadlockController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author calbe
 */
public class DeadlockGUI extends JFrame {
    private DeadlockController controller;
    private GraphPanel graphPanel;
    private JTextArea logArea;
    private JTextField inputField;
    private JButton addButton;
    private JButton processButton;
    private JButton processAllButton;
    private JButton resetButton;
    private JTextArea queueArea;
    
    public DeadlockGUI() {
        this.controller = new DeadlockController();
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Deadlock Detection System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel del grafo
        graphPanel = new GraphPanel();
        JScrollPane graphScrollPane = new JScrollPane(graphPanel);
        graphScrollPane.setPreferredSize(new Dimension(600, 400));
        
        // Panel de entrada
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputField = new JTextField();
        inputField.setToolTipText("Enter operations like: read(T1,A1) or multiple separated by commas");
        
        addButton = new JButton("Add Operation");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addOperation();
            }
        });
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        processButton = new JButton("Process Next");
        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processNext();
            }
        });
        
        processAllButton = new JButton("Process All");
        processAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processAll();
            }
        });
        
        resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });
        
        buttonPanel.add(addButton);
        buttonPanel.add(processButton);
        buttonPanel.add(processAllButton);
        buttonPanel.add(resetButton);
        
        inputPanel.add(new JLabel("Input:"), BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Panel de cola de operaciones
        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(BorderFactory.createTitledBorder("Operation Queue"));
        queueArea = new JTextArea(5, 20);
        queueArea.setEditable(false);
        queuePanel.add(new JScrollPane(queueArea), BorderLayout.CENTER);
        
        // Panel de log
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Log"));
        logArea = new JTextArea(10, 60);
        logArea.setEditable(false);
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        // Área de información
        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        infoPanel.add(queuePanel);
        infoPanel.add(logPanel);
        
        // Agregar componentes al panel principal
        mainPanel.add(graphScrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Configurar atajos de teclado
        setupKeyBindings();
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void setupKeyBindings() {
        inputField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "addOperation");
        inputField.getActionMap().put("addOperation", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addOperation();
            }
        });
    }
    
    private void addOperation() {
        String input = inputField.getText().trim();
        if (!input.isEmpty()) {
            if (input.contains(",")) {
                controller.addMultipleOperations(input);
            } else {
                controller.addOperation(input);
            }
            inputField.setText("");
            updateDisplay();
        }
    }
    
    private void processNext() {
        controller.processNextOperation();
        updateDisplay();
    }
    
    private void processAll() {
        new Thread(() -> {
            processAllButton.setEnabled(false);
            processButton.setEnabled(false);
            addButton.setEnabled(false);
            
            while (controller.hasOperations()) {
                controller.processNextOperation();
                updateDisplay();
                
                try {
                    Thread.sleep(1500); // Pausa para visualización
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            processAllButton.setEnabled(true);
            processButton.setEnabled(true);
            addButton.setEnabled(true);
        }).start();
    }
    
    private void reset() {
        controller.reset();
        graphPanel.clear();  // Limpiar el panel gráfico también
        updateDisplay();
    }
    
    private void updateDisplay() {
        // Actualizar grafo
        graphPanel.setWaitForGraph(controller.getGraph());
        
        // Actualizar log
        logArea.setText("");
        for (String logEntry : controller.getLog()) {
            logArea.append(logEntry + "\n");
        }
        logArea.setCaretPosition(logArea.getDocument().getLength());
        
        // Actualizar cola
        queueArea.setText("");
        controller.getOperationQueue().forEach(op -> 
            queueArea.append(op.toString() + "\n"));
        
        // Actualizar estado de botones
        processButton.setEnabled(controller.hasOperations());
        processAllButton.setEnabled(controller.hasOperations());
        
        // Forzar repaint
        repaint();
    }
}
