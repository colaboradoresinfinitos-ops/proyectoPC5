/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import uni.aed.tda.graphTDA.Graph;
import uni.aed.tda.graphTDA.Vertex;
import uni.aed.tda.graphTDA.Edge;
import java.util.*;

/**
 *
 * @author calbe
 */
public class WaitForGraph {
    private Graph<String> graph;
    private Map<String, Transaction> transactions;
    private Map<String, Record> records;
    private Map<String, Vertex<String>> vertexMap;
    
    public WaitForGraph() {
        this.graph = new Graph<>(Graph.TYPE.DIRECTED);
        this.transactions = new HashMap<>();
        this.records = new HashMap<>();
        this.vertexMap = new HashMap<>();
    }
    
    // Método para limpiar completamente el grafo
    public void clearAll() {
        // Crear un nuevo grafo
        this.graph = new Graph<>(Graph.TYPE.DIRECTED);
        
        // Limpiar todos los mapas
        this.transactions.clear();
        this.records.clear();
        this.vertexMap.clear();
    }
    
    public void addTransaction(Transaction transaction) {
        String id = transaction.getId();
        transactions.put(id, transaction);
        
        // Crear vértice en el grafo si no existe
        if (!vertexMap.containsKey(id)) {
            Vertex<String> vertex = new Vertex<>(id);
            graph.getAllVertices().add(vertex);
            vertexMap.put(id, vertex);
        }
    }
    
    public void removeTransaction(String transactionId) {
        // Remover transacción del mapa
        transactions.remove(transactionId);
        
        // Remover vértice del grafo
        Vertex<String> vertexToRemove = vertexMap.get(transactionId);
        if (vertexToRemove != null) {
            // Primero remover todas las aristas relacionadas
            List<Edge<String>> edgesToRemove = new ArrayList<>();
            for (Vertex<String> v : graph.getAllVertices()) {
                List<Edge<String>> edges = v.getEdges();
                for (Edge<String> e : edges) {
                    if (e.getFromVertex().getValue().equals(transactionId) || 
                        e.getToVertex().getValue().equals(transactionId)) {
                        edgesToRemove.add(e);
                    }
                }
                edges.removeAll(edgesToRemove);
            }
            
            // Remover aristas de la lista global
            graph.getAllEdges().removeAll(edgesToRemove);
            
            // Remover el vértice
            graph.getAllVertices().remove(vertexToRemove);
            vertexMap.remove(transactionId);
        }
        
        // Liberar todos los locks de esta transacción
        for (Record record : records.values()) {
            if (record.isLocked() && record.getLockedBy() != null && 
                record.getLockedBy().getId().equals(transactionId)) {
                record.unlock();
            }
        }
    }
    
    public void addEdge(String fromTransactionId, String toTransactionId) {
        Vertex<String> fromVertex = vertexMap.get(fromTransactionId);
        Vertex<String> toVertex = vertexMap.get(toTransactionId);
        
        if (fromVertex != null && toVertex != null) {
            // Verificar si la arista ya existe
            boolean edgeExists = false;
            for (Edge<String> e : fromVertex.getEdges()) {
                if (e.getToVertex().getValue().equals(toTransactionId)) {
                    edgeExists = true;
                    break;
                }
            }
            
            if (!edgeExists) {
                Edge<String> edge = new Edge<>(1, fromVertex, toVertex);
                fromVertex.addEdge(edge);
                graph.getAllEdges().add(edge);
            }
        }
    }
    
    public void removeEdge(String fromTransactionId, String toTransactionId) {
        Vertex<String> fromVertex = vertexMap.get(fromTransactionId);
        if (fromVertex != null) {
            List<Edge<String>> edgesToRemove = new ArrayList<>();
            for (Edge<String> e : fromVertex.getEdges()) {
                if (e.getToVertex().getValue().equals(toTransactionId)) {
                    edgesToRemove.add(e);
                }
            }
            fromVertex.getEdges().removeAll(edgesToRemove);
            graph.getAllEdges().removeAll(edgesToRemove);
        }
    }
    
    public Record getOrCreateRecord(String recordId) {
        records.putIfAbsent(recordId, new Record(recordId));
        return records.get(recordId);
    }
    
    public Transaction getTransaction(String transactionId) {
        return transactions.get(transactionId);
    }
    
    public boolean hasCycle() {
        Map<String, Boolean> visited = new HashMap<>();
        Map<String, Boolean> recursionStack = new HashMap<>();
        
        // Inicializar mapas
        for (Vertex<String> v : graph.getAllVertices()) {
            visited.put(v.getValue(), false);
            recursionStack.put(v.getValue(), false);
        }
        
        // Verificar ciclos para cada vértice
        for (Vertex<String> v : graph.getAllVertices()) {
            if (!visited.get(v.getValue())) {
                if (hasCycleUtil(v.getValue(), visited, recursionStack)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean hasCycleUtil(String transactionId, Map<String, Boolean> visited, 
                                  Map<String, Boolean> recursionStack) {
        if (!visited.get(transactionId)) {
            visited.put(transactionId, true);
            recursionStack.put(transactionId, true);
            
            Vertex<String> vertex = vertexMap.get(transactionId);
            if (vertex != null) {
                for (Edge<String> edge : vertex.getEdges()) {
                    String neighbor = edge.getToVertex().getValue();
                    if (!visited.get(neighbor)) {
                        if (hasCycleUtil(neighbor, visited, recursionStack)) {
                            return true;
                        }
                    } else if (recursionStack.get(neighbor)) {
                        return true;
                    }
                }
            }
        }
        
        recursionStack.put(transactionId, false);
        return false;
    }
    
    public List<String> findCycle() {
        Map<String, Boolean> visited = new HashMap<>();
        Map<String, Boolean> recursionStack = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        
        // Inicializar mapas
        for (Vertex<String> v : graph.getAllVertices()) {
            visited.put(v.getValue(), false);
            recursionStack.put(v.getValue(), false);
            parent.put(v.getValue(), null);
        }
        
        // Buscar ciclos
        for (Vertex<String> v : graph.getAllVertices()) {
            if (!visited.get(v.getValue())) {
                String cycleStart = findCycleUtil(v.getValue(), visited, recursionStack, parent);
                if (cycleStart != null) {
                    // Reconstruir el ciclo
                    List<String> cycle = new ArrayList<>();
                    String current = cycleStart;
                    do {
                        cycle.add(current);
                        current = parent.get(current);
                    } while (current != null && !current.equals(cycleStart));
                    cycle.add(cycleStart);
                    Collections.reverse(cycle);
                    return cycle;
                }
            }
        }
        
        return null;
    }
    
    private String findCycleUtil(String transactionId, Map<String, Boolean> visited,
                                  Map<String, Boolean> recursionStack, Map<String, String> parent) {
        visited.put(transactionId, true);
        recursionStack.put(transactionId, true);
        
        Vertex<String> vertex = vertexMap.get(transactionId);
        if (vertex != null) {
            for (Edge<String> edge : vertex.getEdges()) {
                String neighbor = edge.getToVertex().getValue();
                if (!visited.get(neighbor)) {
                    parent.put(neighbor, transactionId);
                    String result = findCycleUtil(neighbor, visited, recursionStack, parent);
                    if (result != null) {
                        return result;
                    }
                } else if (recursionStack.get(neighbor)) {
                    parent.put(neighbor, transactionId);
                    return neighbor;
                }
            }
        }
        
        recursionStack.put(transactionId, false);
        return null;
    }
    
    public String getYoungestTransactionInCycle(List<String> cycle) {
        if (cycle == null || cycle.isEmpty()) return null;
        
        // Asumimos que las transacciones más nuevas tienen números más altos en su ID
        return cycle.stream()
            .max(Comparator.comparingInt(this::extractTransactionNumber))
            .orElse(null);
    }
    
    private int extractTransactionNumber(String transactionId) {
        try {
            return Integer.parseInt(transactionId.substring(1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    // Getters para la GUI
    public Graph<String> getGraph() { return graph; }
    public Map<String, Transaction> getTransactions() { return transactions; }
    public Map<String, Record> getRecords() { return records; }
    public Map<String, Vertex<String>> getVertexMap() { return vertexMap; }
}