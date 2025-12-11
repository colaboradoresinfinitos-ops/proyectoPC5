/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author calbe
 */
public class Transaction {
    private String id;
    private List<Operation> operations;
    private int currentStep;
    private boolean active;
    private boolean suspended;
    private boolean finished;
    
    public Transaction(String id) {
        this.id = id;
        this.operations = new ArrayList<>();
        this.currentStep = 0;
        this.active = true;
        this.suspended = false;
        this.finished = false;
    }
    
    public void addOperation(Operation op) {
        operations.add(op);
    }
    
    public Operation getCurrentOperation() {
        if (currentStep < operations.size()) {
            return operations.get(currentStep);
        }
        return null;
    }
    
    public void moveToNextStep() {
        currentStep++;
    }
    
    public boolean hasMoreOperations() {
        return currentStep < operations.size();
    }
    
    public void suspend() {
        this.suspended = true;
    }
    
    public void resume() {
        this.suspended = false;
    }
    
    public void finish() {
        this.finished = true;
        this.active = false;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public boolean isSuspended() { return suspended; }
    public boolean isFinished() { return finished; }
    public boolean isActive() { return active; }
    public int getCurrentStep() { return currentStep; }
    public List<Operation> getOperations() { return operations; }
}
