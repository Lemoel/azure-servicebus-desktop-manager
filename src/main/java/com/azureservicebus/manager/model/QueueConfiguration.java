package com.azureservicebus.manager.model;

import java.time.Duration;

/**
 * Modelo de configuração para criação de filas com parâmetros customizados.
 * Permite configurar todas as propriedades importantes de uma fila do Azure Service Bus.
 */
public class QueueConfiguration {
    
    // Propriedades obrigatórias
    private String name;
    private int maxDeliveryCount;
    private Duration lockDuration;
    
    // Propriedades opcionais com valores padrão
    private boolean deadLetteringOnMessageExpiration;
    private boolean batchedOperationsEnabled;
    private boolean requiresSession;
    private boolean partitioningEnabled;
    private boolean duplicateDetectionEnabled;
    private Duration duplicateDetectionHistoryTimeWindow;
    private long maxSizeInMB;
    private Duration defaultMessageTimeToLive;
    
    /**
     * Construtor com valores padrão recomendados
     */
    public QueueConfiguration() {
        // Valores padrão baseados no TestCaseDocGen
        this.maxDeliveryCount = 10;
        this.lockDuration = Duration.ofMinutes(1);
        this.deadLetteringOnMessageExpiration = true;
        this.batchedOperationsEnabled = true;
        this.requiresSession = false;
        this.partitioningEnabled = false;
        this.duplicateDetectionEnabled = false;
        this.duplicateDetectionHistoryTimeWindow = Duration.ofMinutes(10); // Padrão: 10 minutos
        this.maxSizeInMB = 1024;
        this.defaultMessageTimeToLive = Duration.ofHours(336); // 14 dias
    }
    
    /**
     * Construtor com nome da fila
     */
    public QueueConfiguration(String name) {
        this();
        this.name = name;
    }
    
    // Getters e Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getMaxDeliveryCount() {
        return maxDeliveryCount;
    }
    
    public void setMaxDeliveryCount(int maxDeliveryCount) {
        if (maxDeliveryCount < 1 || maxDeliveryCount > 2000) {
            throw new IllegalArgumentException("Max Delivery Count deve estar entre 1 e 2000");
        }
        this.maxDeliveryCount = maxDeliveryCount;
    }
    
    public Duration getLockDuration() {
        return lockDuration;
    }
    
    public void setLockDuration(Duration lockDuration) {
        this.lockDuration = lockDuration;
    }
    
    public void setLockDurationMinutes(int minutes) {
        if (minutes < 0 || minutes > 5) {
            throw new IllegalArgumentException("Lock Duration deve estar entre 0 e 5 minutos");
        }
        this.lockDuration = Duration.ofMinutes(minutes);
    }
    
    public int getLockDurationMinutes() {
        return (int) lockDuration.toMinutes();
    }
    
    public boolean isDeadLetteringOnMessageExpiration() {
        return deadLetteringOnMessageExpiration;
    }
    
    public void setDeadLetteringOnMessageExpiration(boolean deadLetteringOnMessageExpiration) {
        this.deadLetteringOnMessageExpiration = deadLetteringOnMessageExpiration;
    }
    
    public boolean isBatchedOperationsEnabled() {
        return batchedOperationsEnabled;
    }
    
    public void setBatchedOperationsEnabled(boolean batchedOperationsEnabled) {
        this.batchedOperationsEnabled = batchedOperationsEnabled;
    }
    
    public boolean isRequiresSession() {
        return requiresSession;
    }
    
    public void setRequiresSession(boolean requiresSession) {
        this.requiresSession = requiresSession;
    }
    
    public boolean isPartitioningEnabled() {
        return partitioningEnabled;
    }
    
    public void setPartitioningEnabled(boolean partitioningEnabled) {
        this.partitioningEnabled = partitioningEnabled;
    }
    
    public boolean isDuplicateDetectionEnabled() {
        return duplicateDetectionEnabled;
    }
    
    public void setDuplicateDetectionEnabled(boolean duplicateDetectionEnabled) {
        this.duplicateDetectionEnabled = duplicateDetectionEnabled;
    }
    
    public Duration getDuplicateDetectionHistoryTimeWindow() {
        return duplicateDetectionHistoryTimeWindow;
    }
    
    public void setDuplicateDetectionHistoryTimeWindow(Duration duplicateDetectionHistoryTimeWindow) {
        this.duplicateDetectionHistoryTimeWindow = duplicateDetectionHistoryTimeWindow;
    }
    
    public void setDuplicateDetectionHistoryTimeWindowMinutes(int minutes) {
        if (minutes < 1 || minutes > 10080) {
            throw new IllegalArgumentException("Duplicate Detection Window deve estar entre 1 minuto e 10080 minutos (7 dias)");
        }
        this.duplicateDetectionHistoryTimeWindow = Duration.ofMinutes(minutes);
    }
    
    public int getDuplicateDetectionHistoryTimeWindowMinutes() {
        return (int) duplicateDetectionHistoryTimeWindow.toMinutes();
    }
    
    public long getMaxSizeInMB() {
        return maxSizeInMB;
    }
    
    public void setMaxSizeInMB(long maxSizeInMB) {
        this.maxSizeInMB = maxSizeInMB;
    }
    
    public Duration getDefaultMessageTimeToLive() {
        return defaultMessageTimeToLive;
    }
    
    public void setDefaultMessageTimeToLive(Duration defaultMessageTimeToLive) {
        this.defaultMessageTimeToLive = defaultMessageTimeToLive;
    }
    
    public void setDefaultMessageTimeToLiveHours(int hours) {
        this.defaultMessageTimeToLive = Duration.ofHours(hours);
    }
    
    public int getDefaultMessageTimeToLiveHours() {
        return (int) defaultMessageTimeToLive.toHours();
    }
    
    /**
     * Valida a configuração antes de criar a fila
     */
    public boolean isValid() {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (maxDeliveryCount < 1 || maxDeliveryCount > 2000) {
            return false;
        }
        if (lockDuration == null || lockDuration.isNegative() || lockDuration.toMinutes() > 5) {
            return false;
        }
        return true;
    }
    
    /**
     * Retorna mensagem de erro de validação
     */
    public String getValidationError() {
        if (name == null || name.trim().isEmpty()) {
            return "Nome da fila é obrigatório";
        }
        if (maxDeliveryCount < 1 || maxDeliveryCount > 2000) {
            return "Max Delivery Count deve estar entre 1 e 2000";
        }
        if (lockDuration == null || lockDuration.isNegative()) {
            return "Lock Duration deve ser positivo";
        }
        if (lockDuration.toMinutes() > 5) {
            return "Lock Duration não pode exceder 5 minutos";
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "QueueConfiguration{" +
                "name='" + name + '\'' +
                ", maxDeliveryCount=" + maxDeliveryCount +
                ", lockDuration=" + lockDuration.toMinutes() + " min" +
                ", deadLetteringOnExpiration=" + deadLetteringOnMessageExpiration +
                ", batchedOperations=" + batchedOperationsEnabled +
                ", requiresSession=" + requiresSession +
                ", partitioning=" + partitioningEnabled +
                ", duplicateDetection=" + duplicateDetectionEnabled +
                ", duplicateDetectionWindow=" + (duplicateDetectionEnabled ? duplicateDetectionHistoryTimeWindow.toMinutes() + " min" : "N/A") +
                ", maxSize=" + maxSizeInMB + " MB" +
                ", ttl=" + defaultMessageTimeToLive.toHours() + " hours" +
                '}';
    }
}
