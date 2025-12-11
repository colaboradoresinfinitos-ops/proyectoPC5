/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package PC5;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author calbe
 */
public class Transaction implements Comparable<Transaction> {
    private static enum TransactionState {ACTIVE, WAITING, COMMITTED, ABORTED};

    private String              id;
    private TransactionState    state;              // indica el estado de la transacción
    private long                timestamp;          // atributo para la priorizacion de las transacciones
    private List<Operation>     executedOperations; // lista de operaciones ejecutadas por la transacción
    private Set<Record>         lockedRecords;      // conjunto de registros bloqueados por la transacción
    
    public Transaction(String id) {
        this.id = id;
        this.state = TransactionState.ACTIVE;
        this.timestamp = System.nanoTime();
        this.executedOperations = new LinkedList<>();
        this.lockedRecords = new HashSet<>();
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getId() { 
        return id; 
    }

    public void setState(String state) {
        setState(TransactionState.valueOf(state.toUpperCase()));
    }
    
    public void setState(TransactionState state) {
        this.state = state;
    }

    public TransactionState getState() {
        return state;
    }

    public long getTimestamp() { 
        return timestamp; 
    }

    public List<Operation> getExecutedOperations() {
        return executedOperations;
    }
    
    public void addExecutedOperation(Operation operation) {
        executedOperations.add(operation);
    }

    public Set<Record> getLockedRecords() {
        return lockedRecords;
    }

    public void addLockedRecord(Record record) {
        lockedRecords.add(record);
    }

    public void removeLockedRecord(Record record) {
        lockedRecords.remove(record);
    }
    
    public void removeLocks() {
        lockedRecords.clear();
    }

    public void abort() {
        setState(TransactionState.ABORTED);
        removeLocks();
    }
    
    @Override
    public int compareTo(Transaction o) {
        return Long.compare(this.timestamp, o.timestamp);
    }
}
