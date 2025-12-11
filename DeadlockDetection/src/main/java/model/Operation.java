/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author calbe
 */
public class Operation {
    public enum Type { READ, WRITE, END }
    
    private Type type;
    private String transactionId;
    private String recordId;
    
    public Operation(Type type, String transactionId, String recordId) {
        this.type = type;
        this.transactionId = transactionId;
        this.recordId = recordId;
    }
    
    public static Operation parseOperation(String input) {
        // Primero, limpiar espacios y convertir a minúsculas para comparación
        String cleanInput = input.trim();
        
        // Verificar si es una operación de END primero
        if (cleanInput.toLowerCase().startsWith("end(")) {
            String content = cleanInput.substring(4, cleanInput.length() - 1).trim();
            return new Operation(Type.END, content, null);
        }
        
        // Verificar si es READ o WRITE
        if (cleanInput.toLowerCase().startsWith("read(")) {
            String content = cleanInput.substring(5, cleanInput.length() - 1).trim();
            String[] parts = content.split(",");
            if (parts.length == 2) {
                return new Operation(Type.READ, parts[0].trim(), parts[1].trim());
            }
        } else if (cleanInput.toLowerCase().startsWith("write(")) {
            String content = cleanInput.substring(6, cleanInput.length() - 1).trim();
            String[] parts = content.split(",");
            if (parts.length == 2) {
                return new Operation(Type.WRITE, parts[0].trim(), parts[1].trim());
            }
        }
        
        throw new IllegalArgumentException("Invalid operation format: " + cleanInput);
    }
    
    // Getters
    public Type getType() { return type; }
    public String getTransactionId() { return transactionId; }
    public String getRecordId() { return recordId; }
    
    @Override
    public String toString() {
        switch(type) {
            case READ: return "read(" + transactionId + "," + recordId + ")";
            case WRITE: return "write(" + transactionId + "," + recordId + ")";
            case END: return "end(" + transactionId + ")";
            default: return "";
        }
    }
}