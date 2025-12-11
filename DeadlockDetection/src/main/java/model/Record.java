/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author calbe
 */
public class Record {
    private String id;
    private Transaction lockedBy;
    private boolean isLocked;
    
    public Record(String id) {
        this.id = id;
        this.isLocked = false;
        this.lockedBy = null;
    }
    
    public boolean lock(Transaction transaction) {
        if (!isLocked) {
            this.lockedBy = transaction;
            this.isLocked = true;
            return true;
        }
        return false;
    }
    
    public void unlock() {
        this.isLocked = false;
        this.lockedBy = null;
    }
    
    public boolean isLockedBy(Transaction transaction) {
        return isLocked && lockedBy != null && lockedBy.getId().equals(transaction.getId());
    }
    
    // Getters
    public String getId() { return id; }
    public Transaction getLockedBy() { return lockedBy; }
    public boolean isLocked() { return isLocked; }
}
