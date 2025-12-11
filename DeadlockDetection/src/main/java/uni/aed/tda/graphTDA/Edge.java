package uni.aed.tda.graphTDA;
public class Edge<T extends Comparable<T>> implements Comparable<Edge<T>> {
    private Vertex<T> from=null; //vertice origen
    private Vertex<T> to=null; //vertice destino
    private int cost=0;
    //1° constructor
    public Edge(int cost,Vertex<T> from,Vertex<T> to) {
        if(from==null || to==null)
            throw new NullPointerException("Vertices no deben ser nulo");
        this.cost=cost;
        this.from=from;
        this.to=to;
    }
    //2° constructor
    public Edge(Edge<T> e) {
        this(e.cost,e.from,e.to);        
    }

    public Vertex<T> getFromVertex() {
        return from;
    }

    public Vertex<T> getToVertex() {
        return to;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }    

    @Override
    public boolean equals(Object e1) {
        if(!(e1 instanceof Edge))
            return false;
        Edge<T> e= (Edge<T>)e1;
        //compara costos
        boolean costs=this.cost==e.cost;
        //si los costos difieren
        if(!costs)
            return false;
        //compara los vertice origen
        boolean from=this.from==e.from;
        if(!from)
            return false;
        boolean to=this.to==e.to;
        if(!to)
            return false;
        return true;
    }
    
    
    @Override
    public int compareTo(Edge<T> o) {
        //comparando costos del objeto actual con el objeto parametro
        if(this.cost < o.cost)
            return -1;
        if(this.cost > o.cost)
            return 1;
        //comparando vertices origen del objeto actual con el objeto parametro
        int result = this.from.compareTo(o.from);
        if(result!=0)
            return result;
        //comparando vertices destino del objeto actual con el objeto parametro
        result = this.to.compareTo(o.to);
        if(result!=0)
            return result;
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append("[")      
                .append(from.getValue())    //vertice origen
                .append("(")      
                .append(from.getWeigth())   //peso del vertice origen
                .append(")")      
                .append("]")      
                .append("->")      
                .append(to.getValue())      //vertice destino
                .append("(")      
                .append(to.getWeigth())     //peso del vertice destino
                .append(")")      
                .append("]")      
                .append("=")      
                .append(cost)               //costo de la arista
                .append("\n");
        return builder.toString();                
    }
    
    
    
}
