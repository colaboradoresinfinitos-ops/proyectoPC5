/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package PC5;

import java.util.*;

/**
 * Clase principal que gestiona todas las transacciones, bloqueos y detección de deadlocks.
 * Implementa el algoritmo de prevención de deadlocks usando un grafo de espera.
 * @author calbe
 */
public class TransactionManager {
    private WaitForGraph<String> waitForGraph;              // Grafo de espera para detección de deadlocks
    private Queue<Operation> operationQueue;                // Cola de operaciones pendientes
    private Map<String, Transaction> transactions;          // Mapa de transacciones por ID
    private Map<String, Record> records;                    // Mapa de registros por ID
    private Map<String, List<Operation>> waitingOperations; // Operaciones en espera por transacción
    
    /**
     * Constructor que inicializa todas las estructuras de datos.
     */
    public TransactionManager() {
        this.waitForGraph = new WaitForGraph<>();
        this.operationQueue = new LinkedList<>();
        this.transactions = new HashMap<>();
        this.records = new HashMap<>();
        this.waitingOperations = new HashMap<>();
    }
    
    /**
     * Parsea una cadena de entrada y la convierte en una operación.
     * Soporta formatos: read(T,A), write(T,A), end(T)
     * @param input Cadena de entrada
     * @return Operación parseada, o null si el formato es inválido
     */
    public Operation parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("La entrada no puede ser nula o vacia");
        }
    
        // Normalizar entrada: eliminar espacios y convertir a minúsculas
        String normalized = input.trim()
                                .replaceAll("\\s+", "")  // Elimina TODOS los espacios
                                .toLowerCase();

        // CASO 1: READ
        if (normalized.startsWith("read(") && normalized.endsWith(")")) {
            return parseTwoParamOperation(normalized, "read", "READ");
        }

        // CASO 2: WRITE
        else if (normalized.startsWith("write(") && normalized.endsWith(")")) {
            return parseTwoParamOperation(normalized, "write", "WRITE");
        }

        // CASO 3: END
        else if (normalized.startsWith("end(") && normalized.endsWith(")")) {
            return parseSingleParamOperation(normalized, "end", "END");
        }
        
        throw new IllegalArgumentException("Operacion no reconocida: " + input);
    }
    
    /**
     * Parsea operaciones con DOS parámetros: read(T,A), write(T,A)
     */
    private Operation parseTwoParamOperation(String input, String prefix, String opType) {
        try {
            // Extraer contenido entre paréntesis: "T,A"
            String content = input.substring(prefix.length() + 1, input.length() - 1);

            // Separar por coma
            String[] parts = content.split(",", -1); // -1 para incluir strings vacíos

            if (parts.length != 2) {
                throw new IllegalArgumentException(
                    String.format("La operacion %s requiere 2 parametros. Formato: %s(T,R)", 
                        opType, prefix)
                );
            }

            String transactionId = parts[0];
            String recordId = parts[1];

            // Validar que no estén vacíos
            if (transactionId.isEmpty() || recordId.isEmpty()) {
                throw new IllegalArgumentException(
                    String.format("Los parametros de %s no pueden estar vacios", opType)
                );
            }

            Transaction transaction = getOrCreateTransaction(transactionId);
            Record record = getOrCreateRecord(recordId);

            return new Operation(opType, transaction, record);

        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(
                String.format("Formato de %s invalido", opType), e
            );
        }
    }

    /**
     * Parsea operaciones con UN parámetro: end(T)
     */
    private Operation parseSingleParamOperation(String input, String prefix, String opType) {
        try {
            // Extraer contenido entre paréntesis: "T"
            String content = input.substring(prefix.length() + 1, input.length() - 1);

            if (content.isEmpty()) {
                throw new IllegalArgumentException(
                    String.format("La operacion %s requiere un parametro. Formato: %s(T)", 
                        opType, prefix)
                );
            }

            Transaction transaction = getOrCreateTransaction(content);

            // Para END
            return new Operation(opType, transaction, null);

        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(
                String.format("Formato de %s invalido", opType), e
            );
        }
    }

    /**
     * Parsea una operación y la agrega a la cola de procesamiento.
     * @param input Cadena de entrada con la operación
     */
    public void parseAndAddOperation(String input) {
        try {
            Operation operation = parse(input);
            if (operation != null) {
                operationQueue.add(operation);
                System.out.println("Operacion anadida: " + operation.toString());
            } else {
                System.err.println("Error: Formato invalido: " + input);
            }
        } catch (Exception e) {
            System.err.println("Error parsing operation: " + input);
            e.printStackTrace();
        }
    }
    
    /**
     * Procesa todas las operaciones en la cola hasta que esté vacía.
     */
    public void processAllOperations() {
        System.out.println("\n=== INICIANDO PROCESO DE OPERACIONES ===");
        
        int operationCount = 0;
        while (!operationQueue.isEmpty()) {
            Operation operation = operationQueue.poll();
            operationCount++;
            System.out.println("\n[" + operationCount + "] Procesando: " + operation.toString());
            execute(operation);
        }
        
        System.out.println("\n=== PROCESO COMPLETADO (" + operationCount + " operaciones) ===");
    }
    
    /**
     * Ejecuta una operación individual.
     * @param operation Operación a ejecutar
     */
    public void execute(Operation operation) {
        Transaction transaction = operation.getTransaction();
        Operation.OperationType type = operation.getType();
        
        // Si la transacción está abortada, ignora la operación
        if (transaction.getState() == Transaction.TransactionState.ABORTED) {
            System.out.println("Transaccion " + transaction.getId() + " esta ABORTADA, operacion ignorada");
            return;
        }
        
        switch (type) {
            case READ:
            case WRITE:
                handleReadWrite(operation);
                break;
                
            case END:
                handleEnd(operation);
                break;
        }
    }
    
    /**
     * Maneja operaciones READ y WRITE.
     * @param operation Operación READ o WRITE
     */
    private void handleReadWrite(Operation operation) {
        Transaction transaction = operation.getTransaction();
        Record record = operation.getRecord();
        
        // Verifica si puede adquirir el bloqueo
        if (canLock(operation)) {
            // Adquiere el bloqueo y ejecuta la operación
            addLock(operation);
            transaction.addExecutedOperation(operation);
            System.out.println("Operacion ejecutada exitosamente");
        } else {
            // No puede adquirir el bloqueo - debe esperar
            Transaction lockHolder = record.getLock();
            
            if (lockHolder != null && !lockHolder.equals(transaction)) {
                // Agrega arista al grafo de espera: transaction → lockHolder
                waitForGraph.addEdge(transaction.getId(), lockHolder.getId());
                System.out.println(transaction.getId() + " espera por " + lockHolder.getId() + 
                                 " (registro " + record.getId() + " bloqueado)");
                
                // Cambia estado a WAITING
                transaction.setState(Transaction.TransactionState.WAITING);
                
                // Guarda la operación para reintentar luego
                waitingOperations.computeIfAbsent(transaction.getId(), k -> new ArrayList<>())
                    .add(operation);
                
                // Verifica si hay deadlock
                checkForDeadlock();
            }
        }
    }
    
    /**
     * Maneja operación END (finalización de transacción).
     * @param operation Operación END
     */
    private void handleEnd(Operation operation) {
        Transaction transaction = operation.getTransaction();
        System.out.println("Finalizando transaccion " + transaction.getId());
        
        // Marca como COMMITTED
        transaction.setState(Transaction.TransactionState.COMMITTED);
        
        // Libera todos los bloqueos
        for (Record record : new HashSet<>(transaction.getLockedRecords())) {
            removeLock(transaction, record);
        }
        
        // Elimina del grafo de espera
        waitForGraph.removeVertex(transaction.getId());
        
        // Reanuda transacciones que estaban esperando por esta
        resumeWaitingTransactions(transaction);
        
        // Elimina de la lista de transacciones activas
        transactions.remove(transaction.getId());
    }
    
    /**
     * Verifica si una transacción puede adquirir un bloqueo sobre un registro.
     * @param operation Operación a verificar
     * @return true si puede bloquear, false si debe esperar
     */
    private boolean canLock(Operation operation) {
        Transaction transaction = operation.getTransaction();
        Record record = operation.getRecord();
        
        // Puede bloquear si:
        // 1. El registro no está bloqueado, o
        // 2. Ya está bloqueado por la misma transacción
        if (!record.isLocked()) {
            return true;
        }
        
        return record.isLockedBy(transaction);
    }
    
    /**
     * Adquiere un bloqueo para una operación.
     * @param operation Operación que requiere el bloqueo
     */
    private void addLock(Operation operation) {
        Transaction transaction = operation.getTransaction();
        Record record = operation.getRecord();
        
        // Solo adquiere bloqueo si el registro no está bloqueado
        if (!record.isLocked()) {
            record.setLock(transaction);
            transaction.addLockedRecord(record);
            System.out.println(transaction.getId() + " bloquea " + record.getId());
        }
    }
    
    /**
     * Libera un bloqueo.
     * @param transaction Transacción que libera el bloqueo
     * @param record Registro a liberar
     */
    private void removeLock(Transaction transaction, Record record) {
        record.removeLock();
        transaction.removeLockedRecord(record);
        System.out.println(transaction.getId() + " libera " + record.getId());
    }
    
    /**
     * Reanuda transacciones que estaban esperando por una transacción completada.
     * @param completedTransaction Transacción que acaba de finalizar
     */
    private void resumeWaitingTransactions(Transaction completedTransaction) {
        List<String> transactionsToResume = new ArrayList<>();
        
        // Encuentra todas las transacciones que esperaban por la completada
        for (Edge<String> edge : waitForGraph.getAllEdges()) {
            if (edge.getToVertex().getValue().equals(completedTransaction.getId())) {
                transactionsToResume.add(edge.getFromVertex().getValue());
            }
        }
        
        // Para cada transacción que esperaba:
        for (String transactionId : transactionsToResume) {
            // Elimina la arista de espera
            waitForGraph.removeEdge(transactionId, completedTransaction.getId());
            
            Transaction waitingTransaction = transactions.get(transactionId);
            if (waitingTransaction != null && 
                waitingTransaction.getState() == Transaction.TransactionState.WAITING) {
                
                // Cambia estado a ACTIVA
                waitingTransaction.setState(Transaction.TransactionState.ACTIVE);
                
                // Obtiene operaciones pendientes de esta transacción
                List<Operation> operations = waitingOperations.get(transactionId);
                if (operations != null && !operations.isEmpty()) {
                    // Reintenta la primera operación pendiente
                    Operation operation = operations.remove(0);
                    System.out.println("Reanudando " + transactionId + 
                                     ", reintentando: " + operation.toString());
                    operationQueue.add(operation);
                    
                    // Si no quedan más operaciones pendientes, elimina la entrada
                    if (operations.isEmpty()) {
                        waitingOperations.remove(transactionId);
                    }
                }
            }
        }
    }
    
    /**
     * Verifica si hay deadlock en el grafo de espera.
     * Si hay deadlock, lo resuelve abortando la transacción más joven.
     */
    private void checkForDeadlock() {
        if (waitForGraph.hasCycle()) {
            System.out.println("\n ¡DETECTADO DEADLOCK!");
            List<String> cycle = waitForGraph.getCycle();
            System.out.println(" Ciclo encontrado: " + cycle);
            
            // Resuelve el deadlock
            resolveDeadlock(cycle);
        }
    }
    
    /**
     * Resuelve un deadlock abortando la transacción más joven en el ciclo.
     * @param cycle Lista de IDs de transacciones en el ciclo
     */
    private void resolveDeadlock(List<String> cycle) {
        // Encuentra la transacción más joven (con timestamp más pequeño)
        String youngestTransactionId = getYoungestTransaction(cycle);
        System.out.println("Abortando transaccion mas joven del ciclo: " + youngestTransactionId);
        
        // Aborta la transacción
        abortTransaction(youngestTransactionId);
    }
    
    /**
     * Encuentra la transacción más joven en una lista.
     * @param transactionIds Lista de IDs de transacciones
     * @return ID de la transacción más joven
     */
    private String getYoungestTransaction(List<String> transactionIds) {
        String youngestId = null;
        long youngestTimestamp = Long.MAX_VALUE;
        
        // Busca la transacción con el timestamp más pequeño (más antigua)
        // Nota: "más joven" en el contexto del problema significa "más reciente"
        // pero usamos el timestamp directo de System.nanoTime()
        for (String transactionId : transactionIds) {
            Transaction transaction = transactions.get(transactionId);
            if (transaction != null && transaction.getTimestamp() < youngestTimestamp) {
                youngestTimestamp = transaction.getTimestamp();
                youngestId = transactionId;
            }
        }
        
        return youngestId;
    }
    
    /**
     * Aborta una transacción.
     * @param transactionId ID de la transacción a abortar
     */
    private void abortTransaction(String transactionId) {
        Transaction transaction = transactions.get(transactionId);
        if (transaction == null) return;
        
        // Cambia estado a ABORTED
        transaction.abort();
        System.out.println("Transaccion " + transactionId + " ABORTADA");
        
        // Libera todos sus bloqueos
        for (Record record : new HashSet<>(transaction.getLockedRecords())) {
            removeLock(transaction, record);
        }
        
        // Elimina del grafo de espera
        waitForGraph.removeVertex(transactionId);
        
        // Reinserta operaciones pendientes de esta transacción al final de la cola
        List<Operation> operations = waitingOperations.remove(transactionId);
        if (operations != null && !operations.isEmpty()) {
            System.out.println("Reinsertando " + operations.size() + 
                             " operaciones pendientes de " + transactionId + " al final de la cola");
            for (Operation op : operations) {
                operationQueue.add(op);
            }
        }
        
        // Reanuda transacciones que esperaban por esta
        resumeWaitingTransactions(transaction);
    }
    
    /**
     * Obtiene una transacción existente o crea una nueva.
     * @param transactionId ID de la transacción
     * @return Transacción correspondiente
     */
    private Transaction getOrCreateTransaction(String transactionId) {
        if (!transactions.containsKey(transactionId)) {
            // Crea nueva transacción
            Transaction transaction = new Transaction(transactionId);
            transactions.put(transactionId, transaction);
            
            // Agrega vértice al grafo de espera
            waitForGraph.addVertex(transactionId);
            
            System.out.println("Nueva transaccion creada: " + transactionId);
        }
        return transactions.get(transactionId);
    }
    
    /**
     * Obtiene un registro existente o crea uno nuevo.
     * @param recordId ID del registro
     * @return Registro correspondiente
     */
    private Record getOrCreateRecord(String recordId) {
        if (!records.containsKey(recordId)) {
            // Crea nuevo registro
            Record record = new Record(recordId);
            records.put(recordId, record);
        }
        return records.get(recordId);
    }
    
    /**
     * Muestra el estado actual del sistema.
     */
    public void printStatus() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("ESTADO ACTUAL DEL SISTEMA");
        System.out.println("=".repeat(50));
        
        // Información de transacciones
        System.out.println("\n TRANSACCIONES (" + transactions.size() + "):");
        for (Transaction t : transactions.values()) {
            System.out.println("  • " + t.getId() + 
                             " - Estado: " + t.getState() + 
                             " - Bloqueos: " + t.getLockedRecords().size() +
                             " - Timestamp: " + t.getTimestamp());
        }
        
        // Información de registros
        System.out.println("\n️ REGISTROS (" + records.size() + "):");
        for (Record r : records.values()) {
            String lockInfo = r.isLocked() ? 
                "Bloqueado por: " + r.getLock().getId() : 
                "Libre";
            System.out.println("  • " + r.getId() + " - " + lockInfo);
        }
        
        // Información de colas
        System.out.println("\n COLA DE OPERACIONES: " + operationQueue.size() + " pendientes");
        System.out.println("OPERACIONES EN ESPERA: " + 
                         waitingOperations.values().stream().mapToInt(List::size).sum() + " total");
        
        // Estado del grafo de espera
        System.out.println("\n GRAFO DE ESPERA:");
        System.out.println("  * Vértices: " + waitForGraph.getAllVertices().size());
        System.out.println("  * Aristas: " + waitForGraph.getAllEdges().size());
        System.out.println("  * ¿Tiene ciclo?: " + (waitForGraph.hasCycle() ? "SÍ" : "NO"));
        
        if (waitForGraph.hasCycle()) {
            List<String> cycle = waitForGraph.getCycle();
            System.out.println("  * Ciclo detectado: " + cycle);
        }
        
        System.out.println("=".repeat(50));
    }
}