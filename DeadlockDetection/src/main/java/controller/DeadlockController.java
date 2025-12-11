/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import model.Record;
import model.Operation;
import model.WaitForGraph;
import model.Transaction;
import java.util.*;

/**
 *
 * @author calbe
 */
public class DeadlockController {
    private WaitForGraph graph;
    private Queue<Operation> operationQueue;
    private List<String> log;
    private boolean processing;
    
    public DeadlockController() {
        this.graph = new WaitForGraph();
        this.operationQueue = new LinkedList<>();
        this.log = new ArrayList<>();
        this.processing = false;
    }
    
    public void addOperation(String operationString) {
        try {
            // Limpiar y validar la entrada
            String cleanInput = operationString.trim();
            if (cleanInput.isEmpty()) {
                log.add("Error: Empty input");
                return;
            }
            
            Operation operation = Operation.parseOperation(cleanInput);
            operationQueue.add(operation);
            log.add("Added operation: " + cleanInput);
        } catch (IllegalArgumentException e) {
            log.add("Error: Invalid operation format - " + operationString);
        } catch (StringIndexOutOfBoundsException e) {
            log.add("Error: Malformed operation - " + operationString);
        }
    }
    
    public void addMultipleOperations(String input) {
        if (input == null || input.trim().isEmpty()) {
            log.add("Error: Empty input for multiple operations");
            return;
        }
        
        // Separar por comas, pero teniendo cuidado con paréntesis
        String[] operations = splitOperations(input);
        
        for (String op : operations) {
            if (!op.trim().isEmpty()) {
                addOperation(op.trim());
            }
        }
    }
    
    private String[] splitOperations(String input) {
        // Dividir por comas que no estén dentro de paréntesis
        List<String> operations = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenCount = 0;
        
        for (char c : input.toCharArray()) {
            if (c == '(') {
                parenCount++;
            } else if (c == ')') {
                parenCount--;
            }
            
            if (c == ',' && parenCount == 0) {
                operations.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        // Agregar la última operación
        if (current.length() > 0) {
            operations.add(current.toString());
        }
        
        return operations.toArray(new String[0]);
    }
    
    public void processNextOperation() {
        if (operationQueue.isEmpty() || processing) {
            return;
        }
        
        processing = true;
        Operation operation = operationQueue.poll();
        
        switch(operation.getType()) {
            case READ:
            case WRITE:
                processAccessOperation(operation);
                break;
            case END:
                processEndOperation(operation);
                break;
        }
        
        // Verificar deadlock después de cada operación
        checkForDeadlock();
        processing = false;
    }
    
    private void processAccessOperation(Operation operation) {
        String transactionId = operation.getTransactionId();
        String recordId = operation.getRecordId();
        
        Transaction transaction = graph.getTransaction(transactionId);
        if (transaction == null) {
            transaction = new Transaction(transactionId);
            graph.addTransaction(transaction);
            log.add("Created new transaction: " + transactionId);
        }
        
        transaction.addOperation(operation);
        
        Record record = graph.getOrCreateRecord(recordId);
        
        if (record.isLocked()) {
            Transaction lockingTransaction = record.getLockedBy();
            if (!lockingTransaction.getId().equals(transactionId)) {
                // La transacción debe esperar
                transaction.suspend();
                graph.addEdge(transactionId, lockingTransaction.getId());
                log.add("Transaction " + transactionId + " waits for " + 
                        lockingTransaction.getId() + " (record: " + recordId + ")");
                log.add("Added edge: " + transactionId + " -> " + lockingTransaction.getId());
            } else {
                // La misma transacción ya tiene el lock
                transaction.resume();
            }
        } else {
            // Obtener el lock
            record.lock(transaction);
            transaction.resume();
            log.add("Transaction " + transactionId + " locked record " + recordId);
        }
    }
    
    private void processEndOperation(Operation operation) {
        String transactionId = operation.getTransactionId();
        Transaction transaction = graph.getTransaction(transactionId);
        
        if (transaction != null) {
            transaction.finish();
            graph.removeTransaction(transactionId);
            log.add("Transaction " + transactionId + " finished and removed from graph");
            
            // Reanudar transacciones que estaban esperando por esta
            resumeWaitingTransactions();
        }
    }
    
    private void checkForDeadlock() {
        if (graph.hasCycle()) {
            log.add("DEADLOCK DETECTED!");
            
            List<String> cycle = graph.findCycle();
            if (cycle != null) {
                log.add("Cycle found: " + String.join(" -> ", cycle));
                
                String youngestTransactionId = graph.getYoungestTransactionInCycle(cycle);
                if (youngestTransactionId != null) {
                    log.add("Interrupting youngest transaction: " + youngestTransactionId);
                    
                    // Reinsertar operaciones de la transacción interrumpida al final de la cola
                    Transaction youngestTransaction = graph.getTransaction(youngestTransactionId);
                    if (youngestTransaction != null) {
                        List<Operation> operationsToReadd = new ArrayList<>();
                        for (Operation op : youngestTransaction.getOperations()) {
                            operationsToReadd.add(op);
                        }
                        
                        // Reinsertar en la cola
                        for (Operation op : operationsToReadd) {
                            operationQueue.add(op);
                        }
                        
                        // Remover la transacción del grafo
                        graph.removeTransaction(youngestTransactionId);
                        log.add("Transaction " + youngestTransactionId + " interrupted and operations re-queued");
                    }
                }
            }
        }
    }
    
    private void resumeWaitingTransactions() {
        // Esta función podría implementar la reanudación de transacciones
        // que estaban esperando por transacciones que han terminado
    }
    
    public void processAllOperations() {
        while (!operationQueue.isEmpty()) {
            processNextOperation();
            try {
                Thread.sleep(1000); // Pausa para visualización
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public void reset() {
        // Limpiar completamente el grafo
        graph.clearAll();
        this.operationQueue.clear();
        this.log.clear();
        this.processing = false;
        
        // Agregar mensaje inicial
        log.add("=== System Reset ===");
        log.add("Ready for new operations.");
    }
    
    // Getters para la GUI
    public WaitForGraph getGraph() { return graph; }
    public Queue<Operation> getOperationQueue() { return operationQueue; }
    public List<String> getLog() { return log; }
    public boolean hasOperations() { return !operationQueue.isEmpty(); }
}