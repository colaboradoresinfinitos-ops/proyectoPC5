/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package PC5;

/**
 *
 * @author calbe
 */
public class Operation {
    private static enum OperationType {READ, WRITE, END};
    
    private OperationType   type;
    private Transaction     transaction;
    private Record          record;

    public Operation(String id, Transaction transaction, Record record) {
        this.type = OperationType.valueOf(id);
        this.transaction = transaction;
        this.record = record;
    }

    public OperationType getType() {
        return type;
    }
    
    public Transaction getTransaction() {
        return transaction;
    }

    public Record getRecord() {
        return record;
    }

    public void execute() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(type.toString())
            .append("(")
            .append(transaction.getId())
            .append(", ")
            .append(record.getId())
            .append(")");
        return str.toString();
    }
}