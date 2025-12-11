/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pc5;

import java.util.*;

/**
 * Clase que extiende Graph para implementar un grafo de espera (Wait-For Graph).
 * Este grafo dirigido representa relaciones de espera entre transacciones:
 * un arco T1 → T2 significa que T1 está esperando por un recurso bloqueado por T2.
 * 
 * @param <T> Tipo de los vértices (normalmente String con IDs de transacciones)
 * @author calbe
 */
public class WaitForGraph<T extends Comparable<T>> extends Graph<T> {
    
    /**
     * Constructor que crea un grafo dirigido (DIRECTED).
     * En un grafo de espera, la dirección de las aristas es importante:
     * T1 → T2 significa que T1 espera a T2, no al revés.
     */
    public WaitForGraph() {
        super(TYPE.DIRECTED); // Llama al constructor de Graph especificando que es dirigido
    }
    
    /**
     * Agrega un vértice al grafo si no existe.
     * @param transaction Valor del vértice a agregar
     */
    public void addVertex(T transaction) {
        // Verifica si ya existe un vértice con este valor
        for (Vertex<T> v : getAllVertices()) {
            if (v.getValue().equals(transaction)) {
                return; // Ya existe, no hace nada
            }
        }
        // Crea nuevo vértice y lo agrega
        Vertex<T> v = new Vertex<>(transaction);
        getAllVertices().add(v);
    }

    /**
     * Elimina un vértice del grafo.
     * @param transaction Valor del vértice a eliminar
     */
    public void removeVertex(T transaction) {
        Vertex<T> toRemove = null;
        // Busca el vértice correspondiente
        for (Vertex<T> v : getAllVertices()) {
            if (v.getValue().equals(transaction)) {
                toRemove = v;
                break;
            }
        }
        // Si lo encontró, usa el método removeVertex de la clase base
        if (toRemove != null) {
            removeVertex(toRemove);
        }
    }

    /**
     * Agrega una arista (relación de espera) entre dos transacciones.
     * @param from Transacción que espera
     * @param to Transacción por la que se espera
     * @throws IllegalArgumentException si from == to (no puede esperarse a sí misma)
     */
    public void addEdge(T from, T to) {
        // Validación: una transacción no puede esperarse a sí misma
        if (from.equals(to)) {
            throw new IllegalArgumentException("No puede haber una arista entre un vertice y si mismo");
        }
        
        Vertex<T> fromVertex = null;
        Vertex<T> toVertex = null;
        
        // Busca los vértices existentes
        for (Vertex<T> v : getAllVertices()) {
            if (v.getValue().equals(from)) {
                fromVertex = v;
            } else if (v.getValue().equals(to)) {
                toVertex = v;
            }
        }
        
        // Si no existen los vértices, los crea
        if (fromVertex == null) {
            fromVertex = new Vertex<>(from);
            addVertex(fromVertex);
        }
        
        if (toVertex == null) {
            toVertex = new Vertex<>(to);
            addVertex(toVertex);
        }
        
        // Crea la arista 
        Edge<T> e = new Edge<>(0, fromVertex, toVertex);
        addEdge(e); // Usa el método de la clase base
    }

    /**
     * Elimina una arista del grafo.
     * @param from Transacción que dejó de esperar
     * @param to Transacción por la que ya no se espera
     */
    public void removeEdge(T from, T to) {
        Vertex<T> fromVertex = null;
        Vertex<T> toVertex = null;
        
        // Busca los vértices
        for (Vertex<T> v : getAllVertices()) {
            if (v.getValue().equals(from)) {
                fromVertex = v;
            } else if (v.getValue().equals(to)) {
                toVertex = v;
            }
        }
        
        // Si ambos vértices existen, busca y elimina la arista
        if (fromVertex != null && toVertex != null) {
            Edge<T> edgeToRemove = null;
            for (Edge<T> edge : getAllEdges()) {
                if (edge.getFromVertex().equals(fromVertex) && 
                    edge.getToVertex().equals(toVertex)) {
                    edgeToRemove = edge;
                    break;
                }
            }
            
            if (edgeToRemove != null) {
                removeEdge(edgeToRemove); // Método de la clase base
            }
        }
    }

    /**
     * Verifica si el grafo contiene algún ciclo.
     * Un ciclo indica un deadlock: T1 espera a T2, T2 espera a T3, ..., Tn espera a T1.
     * @return true si hay al menos un ciclo, false en caso contrario
     */
    public boolean hasCycle() {
        Set<T> visited = new HashSet<>();           // Vértices ya visitados
        Set<T> recursionStack = new HashSet<>();    // Vértices en la pila de recursión actual
        
        // Realiza DFS desde cada vértice no visitado
        for (Vertex<T> vertex : getAllVertices()) {
            T transaction = vertex.getValue();
            if (hasCycleDFS(transaction, visited, recursionStack)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * DFS recursivo para detectar ciclos.
     * @param current Vértice actual
     * @param visited Conjunto de vértices ya visitados
     * @param recursionStack Conjunto de vértices en la ruta actual
     * @return true si se encuentra un ciclo
     */
    private boolean hasCycleDFS(T current, Set<T> visited, Set<T> recursionStack) {
        // Si ya está en la pila de recursión, hay un ciclo
        if (recursionStack.contains(current)) {
            return true;
        }
        
        // Si ya fue visitado (pero no en esta rama), no hay ciclo
        if (visited.contains(current)) {
            return false;
        }
        
        // Marca como visitado y agrega a la pila
        visited.add(current);
        recursionStack.add(current);
        
        // Obtiene el vértice correspondiente
        Vertex<T> currentVertex = getVertex(current);
        if (currentVertex != null) {
            // Recorre todas las aristas salientes
            for (Edge<T> edge : getAllEdges()) {
                if (edge.getFromVertex().equals(currentVertex)) {
                    T neighbor = edge.getToVertex().getValue();
                    // DFS recursivo en el vecino
                    if (hasCycleDFS(neighbor, visited, recursionStack)) {
                        return true;
                    }
                }
            }
        }
        
        // remueve de la pila
        recursionStack.remove(current);
        return false;
    }
    
    /**
     * Obtiene un ciclo del grafo si existe.
     * @return Lista de vértices que forman el ciclo, o lista vacía si no hay ciclo
     */
    public List<T> getCycle() {
        Set<T> visited = new HashSet<>();
        Set<T> recursionStack = new HashSet<>();
        Map<T, T> parent = new HashMap<>(); // Para reconstruir el camino
        
        // Busca ciclos desde cada vértice no visitado
        for (Vertex<T> vertex : getAllVertices()) {
            T transaction = vertex.getValue();
            if (!visited.contains(transaction)) {
                T cycleStart = findCycleDFS(transaction, visited, recursionStack, parent);
                if (cycleStart != null) {
                    return reconstructCycle(cycleStart, parent);
                }
            }
        }
        return new ArrayList<>(); // No hay ciclo
    }
    
    /**
     * DFS para encontrar un ciclo y guardar información del camino.
     * @param current Vértice actual
     * @param visited Vértices visitados
     * @param recursionStack Pila de recursión
     * @param parent Mapa padre-hijo para reconstrucción
     * @return Vértice donde comienza el ciclo, o null si no hay
     */
    private T findCycleDFS(T current, Set<T> visited, Set<T> recursionStack, Map<T, T> parent) {
        visited.add(current);
        recursionStack.add(current);
        
        Vertex<T> currentVertex = getVertex(current);
        if (currentVertex != null) {
            for (Edge<T> edge : getAllEdges()) {
                if (edge.getFromVertex().equals(currentVertex)) {
                    T neighbor = edge.getToVertex().getValue();
                    
                    if (!visited.contains(neighbor)) {
                        parent.put(neighbor, current);
                        T result = findCycleDFS(neighbor, visited, recursionStack, parent);
                        if (result != null) {
                            return result;
                        }
                    } else if (recursionStack.contains(neighbor)) {
                        // ¡Ciclo encontrado!
                        parent.put(neighbor, current);
                        return neighbor;
                    }
                }
            }
        }
        
        recursionStack.remove(current);
        return null;
    }
    
    /**
     * Reconstruye el ciclo a partir del mapa de padres.
     * @param cycleStart Vértice donde comienza/termina el ciclo
     * @param parent Mapa de padres
     * @return Lista ordenada de vértices en el ciclo
     */
    private List<T> reconstructCycle(T cycleStart, Map<T, T> parent) {
        List<T> cycle = new ArrayList<>();
        T current = cycleStart;
        
        // Retrocede por el mapa de padres hasta volver al inicio
        do {
            cycle.add(current);
            current = parent.get(current);
        } while (current != null && !current.equals(cycleStart));
        
        cycle.add(cycleStart);      // Cierra el ciclo
        Collections.reverse(cycle); // Invierte para tener orden correcto
        return cycle;
    }
    
    /**
     * Busca un vértice por su valor.
     * @param value Valor a buscar
     * @return Vértice correspondiente, o null si no existe
     */
    private Vertex<T> getVertex(T value) {
        for (Vertex<T> v : getAllVertices()) {
            if (v.getValue().equals(value)) {
                return v;
            }
        }
        return null;
    }
    
    /**
     * Verifica si existe un vértice con el valor especificado.
     * @param transaction Valor a verificar
     * @return true si existe, false en caso contrario
     */
    public boolean hasVertex(T transaction) {
        return getVertex(transaction) != null;
    }
}