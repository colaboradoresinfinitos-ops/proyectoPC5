/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package PC5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author calbe
 */
public class Graph<T extends Comparable<T>> {
    public enum TYPE{DIRECTED,UNDIRECTED}
    private List<Vertex<T>> allVertices=new ArrayList<>();//lista de vertices
    private List<Edge<T>> allEdges=new ArrayList<>();//lista de aristas
    private TYPE type=TYPE.UNDIRECTED;//predeterminamos el tipo de grafo
    //constructores
    public Graph() {
    }    
    public Graph(TYPE type) {
        this.type=type;
    }
    public Graph(Graph<T> g) {
        this.type=g.type;
        //descargando vertices en el grafo actual, uno a uno
        for(Vertex<T> v: g.getAllVertices() )
            this.allVertices.add(new Vertex<T>(v));            
        //descargando aristas en el grafo actual, uno a uno
        for(Vertex<T> v: g.getAllVertices() ){
            for(Edge<T> e: v.getEdges() )
                this.allEdges.add(e);
        }  
    }    
    public Graph(Collection<Vertex<T>> vertices,Collection<Edge<T>> edges) {
        this(TYPE.UNDIRECTED,vertices,edges);
    }    
    
    public Graph(TYPE type,Collection<Vertex<T>> vertices,Collection<Edge<T>> edges) {
        this(type);
        this.allVertices.addAll(vertices);
        this.allEdges.addAll(edges);
        //recorremos la coleccion de aristas
        for(Edge<T> e: edges){
            Vertex<T> from=e.getFromVertex();
            Vertex<T> to=e.getToVertex();
            //si no existen los vertices origen y destino en la lista de vertices actual, continuamos evaluando el siguiente
            //si existen entonces los vinculamos
            if(!this.allVertices.contains(from) || !this.allVertices.contains(to)) continue;
            from.addEdge(e);
            if(this.type==TYPE.UNDIRECTED){
              Edge<T> reciproca=new Edge<T>(e.getCost(),to,from); //arista inversa por ser grafo no dirigido o bidireccional
              to.addEdge(reciproca);
              this.allEdges.add(reciproca);
            }
        }
    }
    
    /**
     * Agrega un vértice al grafo.
     * @param vertex Vértice a agregar
     */
    public void addVertex(Vertex<T> vertex) {
        if (!allVertices.contains(vertex)) {
            allVertices.add(vertex);
        }
    }
    
    /**
     * Elimina un vértice del grafo.
     * @param vertex Vértice a eliminar
     */
    public void removeVertex(Vertex<T> vertex) {
        // Eliminar todas las aristas relacionadas
        List<Edge<T>> edgesToRemove = new ArrayList<>();
        for (Edge<T> edge : allEdges) {
            if (edge.getFromVertex().equals(vertex) || edge.getToVertex().equals(vertex)) {
                edgesToRemove.add(edge);
            }
        }
        allEdges.removeAll(edgesToRemove);
        
        // Eliminar el vértice
        allVertices.remove(vertex);
    }
    
    /**
     * Agrega una arista al grafo.
     * @param edge Arista a agregar
     */
    public void addEdge(Edge<T> edge) {
        if (!allEdges.contains(edge)) {
            allEdges.add(edge);
            edge.getFromVertex().addEdge(edge);
            
            // Si es no dirigido, agregar arista recíproca
            if (type == TYPE.UNDIRECTED) {
                Edge<T> reciprocal = new Edge<T>(edge.getCost(), 
                                                edge.getToVertex(), 
                                                edge.getFromVertex());
                allEdges.add(reciprocal);
                edge.getToVertex().addEdge(reciprocal);
            }
        }
    }
    
    /**
     * Elimina una arista del grafo.
     * @param edge Arista a eliminar
     */
    public void removeEdge(Edge<T> edge) {
        allEdges.remove(edge);
        edge.getFromVertex().getEdges().remove(edge);
        
        // Si es no dirigido, eliminar también la recíproca
        if (type == TYPE.UNDIRECTED) {
            Edge<T> reciprocal = null;
            for (Edge<T> e : edge.getToVertex().getEdges()) {
                if (e.getToVertex().equals(edge.getFromVertex())) {
                    reciprocal = e;
                    break;
                }
            }
            if (reciprocal != null) {
                allEdges.remove(reciprocal);
                edge.getToVertex().getEdges().remove(reciprocal);
            }
        }
    }
    
    public List<Vertex<T>> getAllVertices() {
        return allVertices;
    }

    public List<Edge<T>> getAllEdges() {
        return allEdges;
    }

    public TYPE getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        for(Vertex<T> v: allVertices )
            builder.append(v.toString());
        return builder.toString();
    }
}
