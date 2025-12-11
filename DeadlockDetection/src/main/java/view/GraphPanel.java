/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import model.WaitForGraph;
import uni.aed.tda.graphTDA.Graph;
import uni.aed.tda.graphTDA.Vertex;
import uni.aed.tda.graphTDA.Edge;
import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 *
 * @author calbe
 */
public class GraphPanel extends JPanel {
    private WaitForGraph waitForGraph;
    private Map<String, Point> vertexPositions;
    
    public GraphPanel() {
        this.vertexPositions = new HashMap<>();
        setPreferredSize(new Dimension(600, 400));
        setBackground(Color.WHITE);
    }
    
    public void setWaitForGraph(WaitForGraph waitForGraph) {
        this.waitForGraph = waitForGraph;
        calculateVertexPositions();
        repaint();
    }
    
    // Método para limpiar completamente el panel
    public void clear() {
        this.waitForGraph = null;
        this.vertexPositions.clear();
        repaint();
    }
    
    private void calculateVertexPositions() {
        if (waitForGraph == null || waitForGraph.getVertexMap().isEmpty()) {
            vertexPositions.clear(); // Asegurar que esté vacío
            return;
        }
        
        vertexPositions.clear();
        int width = getWidth();
        int height = getHeight();
        
        // Si no hay vértices, salir
        if (waitForGraph.getVertexMap().isEmpty()) {
            return;
        }
        
        java.util.List<String> transactionIds = new ArrayList<>(waitForGraph.getVertexMap().keySet());
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 3;
        
        // Asegurar un radio mínimo
        radius = Math.max(radius, 100);
        
        double angleStep = 2 * Math.PI / transactionIds.size();
        
        for (int i = 0; i < transactionIds.size(); i++) {
            String transactionId = transactionIds.get(i);
            double angle = i * angleStep;
            int x = (int)(centerX + radius * Math.cos(angle));
            int y = (int)(centerY + radius * Math.sin(angle));
            vertexPositions.put(transactionId, new Point(x, y));
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Si no hay grafo o está vacío, mostrar mensaje
        if (waitForGraph == null || waitForGraph.getVertexMap().isEmpty()) {
            g.setColor(Color.GRAY);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            String message = "No graph to display";
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g.drawString(message, x, y);
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Dibujar aristas
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2));
        
        Graph<String> graph = waitForGraph.getGraph();
        for (Vertex<String> vertex : graph.getAllVertices()) {
            String from = vertex.getValue();
            Point fromPoint = vertexPositions.get(from);
            
            if (fromPoint != null) {
                for (Edge<String> edge : vertex.getEdges()) {
                    String to = edge.getToVertex().getValue();
                    Point toPoint = vertexPositions.get(to);
                    if (toPoint != null) {
                        drawArrow(g2d, fromPoint, toPoint);
                    }
                }
            }
        }
        
        // Dibujar vértices
        for (Map.Entry<String, Point> entry : vertexPositions.entrySet()) {
            String transactionId = entry.getKey();
            Point point = entry.getValue();
            
            // Dibujar círculo
            g2d.setColor(Color.BLUE);
            g2d.fillOval(point.x - 20, point.y - 20, 40, 40);
            
            // Dibujar borde
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(point.x - 20, point.y - 20, 40, 40);
            
            // Dibujar texto
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(transactionId);
            int textHeight = fm.getHeight();
            g2d.drawString(transactionId, point.x - textWidth/2, point.y + textHeight/4);
        }
        
        // Si hay ciclo, mostrar advertencia
        if (waitForGraph.hasCycle()) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            String warning = "DEADLOCK DETECTED!";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(warning)) / 2;
            int y = 30;
            g2d.drawString(warning, x, y);
        }
    }
    
    private void drawArrow(Graphics2D g2d, Point from, Point to) {
        // Calcular ángulo
        double angle = Math.atan2(to.y - from.y, to.x - from.x);
        
        // Ajustar puntos para que la flecha empiece y termine en el borde del círculo
        int radius = 20;
        int startX = from.x + (int)(radius * Math.cos(angle));
        int startY = from.y + (int)(radius * Math.sin(angle));
        int endX = to.x - (int)(radius * Math.cos(angle));
        int endY = to.y - (int)(radius * Math.sin(angle));
        
        // Dibujar línea
        g2d.drawLine(startX, startY, endX, endY);
        
        // Dibujar punta de flecha
        int arrowSize = 10;
        double arrowAngle = Math.PI / 6;
        
        int x1 = endX - (int)(arrowSize * Math.cos(angle - arrowAngle));
        int y1 = endY - (int)(arrowSize * Math.sin(angle - arrowAngle));
        int x2 = endX - (int)(arrowSize * Math.cos(angle + arrowAngle));
        int y2 = endY - (int)(arrowSize * Math.sin(angle + arrowAngle));
        
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(endX, endY);
        arrowHead.addPoint(x1, y1);
        arrowHead.addPoint(x2, y2);
        
        g2d.fill(arrowHead);
    }
    
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        calculateVertexPositions();
    }
}