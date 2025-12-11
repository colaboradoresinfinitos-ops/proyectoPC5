/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package PC5;

/**
 *
 * @author calbe
 */
public class Record {
    private String      id;
    private Transaction lock;
    private Object      value;

    public Record(String id) {
        this.id = id;
        this.lock = null;
        this.value = null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id; 
    }

    public void setLock(Transaction lock) {
        this.lock = lock;
    }

    public Transaction getLock() {
        return lock;
    }

    public boolean isLocked() {
        return lock != null;
    }

    public void removeLock() {
        this.lock = null;
    }
}