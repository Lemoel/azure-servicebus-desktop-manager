package com.azureservicebus.manager.model;

import javafx.beans.property.*;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Modelo de dados para informações de uma fila do Service Bus
 */
public class QueueInfo {
    
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final LongProperty totalMessages = new SimpleLongProperty();
    private final LongProperty activeMessages = new SimpleLongProperty();
    private final LongProperty deadLetterMessages = new SimpleLongProperty();
    private final LongProperty scheduledMessages = new SimpleLongProperty();
    private final DoubleProperty sizeInKB = new SimpleDoubleProperty();
    private final IntegerProperty maxDeliveryCount = new SimpleIntegerProperty();
    private final StringProperty lockDuration = new SimpleStringProperty();
    private final LongProperty maxSizeInMB = new SimpleLongProperty();
    private final BooleanProperty partitioningEnabled = new SimpleBooleanProperty();
    private final BooleanProperty sessionRequired = new SimpleBooleanProperty();
    private final BooleanProperty duplicateDetectionEnabled = new SimpleBooleanProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();
    
    // Construtores
    public QueueInfo() {}
    
    public QueueInfo(String name) {
        setName(name);
    }
    
    // Getters e Setters para Properties
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }
    
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }
    
    public long getTotalMessages() { return totalMessages.get(); }
    public void setTotalMessages(long totalMessages) { this.totalMessages.set(totalMessages); }
    public LongProperty totalMessagesProperty() { return totalMessages; }
    
    public long getActiveMessages() { return activeMessages.get(); }
    public void setActiveMessages(long activeMessages) { this.activeMessages.set(activeMessages); }
    public LongProperty activeMessagesProperty() { return activeMessages; }
    
    public long getDeadLetterMessages() { return deadLetterMessages.get(); }
    public void setDeadLetterMessages(long deadLetterMessages) { this.deadLetterMessages.set(deadLetterMessages); }
    public LongProperty deadLetterMessagesProperty() { return deadLetterMessages; }
    
    public long getScheduledMessages() { return scheduledMessages.get(); }
    public void setScheduledMessages(long scheduledMessages) { this.scheduledMessages.set(scheduledMessages); }
    public LongProperty scheduledMessagesProperty() { return scheduledMessages; }
    
    public double getSizeInKB() { return sizeInKB.get(); }
    public void setSizeInKB(double sizeInKB) { this.sizeInKB.set(sizeInKB); }
    public DoubleProperty sizeInKBProperty() { return sizeInKB; }
    
    public int getMaxDeliveryCount() { return maxDeliveryCount.get(); }
    public void setMaxDeliveryCount(int maxDeliveryCount) { this.maxDeliveryCount.set(maxDeliveryCount); }
    public IntegerProperty maxDeliveryCountProperty() { return maxDeliveryCount; }
    
    public String getLockDuration() { return lockDuration.get(); }
    public void setLockDuration(String lockDuration) { this.lockDuration.set(lockDuration); }
    public void setLockDuration(Duration duration) { 
        this.lockDuration.set(formatDuration(duration)); 
    }
    public StringProperty lockDurationProperty() { return lockDuration; }
    
    public long getMaxSizeInMB() { return maxSizeInMB.get(); }
    public void setMaxSizeInMB(long maxSizeInMB) { this.maxSizeInMB.set(maxSizeInMB); }
    public LongProperty maxSizeInMBProperty() { return maxSizeInMB; }
    
    public boolean isPartitioningEnabled() { return partitioningEnabled.get(); }
    public void setPartitioningEnabled(boolean partitioningEnabled) { this.partitioningEnabled.set(partitioningEnabled); }
    public BooleanProperty partitioningEnabledProperty() { return partitioningEnabled; }
    
    public boolean isSessionRequired() { return sessionRequired.get(); }
    public void setSessionRequired(boolean sessionRequired) { this.sessionRequired.set(sessionRequired); }
    public BooleanProperty sessionRequiredProperty() { return sessionRequired; }
    
    public boolean isDuplicateDetectionEnabled() { return duplicateDetectionEnabled.get(); }
    public void setDuplicateDetectionEnabled(boolean duplicateDetectionEnabled) { 
        this.duplicateDetectionEnabled.set(duplicateDetectionEnabled); 
    }
    public BooleanProperty duplicateDetectionEnabledProperty() { return duplicateDetectionEnabled; }
    
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }
    
    // Métodos utilitários
    public String getPartitioningEnabledText() {
        return isPartitioningEnabled() ? "Sim" : "Não";
    }
    
    public String getSessionRequiredText() {
        return isSessionRequired() ? "Sim" : "Não";
    }
    
    public String getDuplicateDetectionEnabledText() {
        return isDuplicateDetectionEnabled() ? "Sim" : "Não";
    }
    
    public boolean hasMessages() {
        return getTotalMessages() > 0;
    }
    
    public boolean hasDeadLetterMessages() {
        return getDeadLetterMessages() > 0;
    }
    
    private String formatDuration(Duration duration) {
        if (duration == null) return "N/A";
        
        long minutes = duration.toMinutes();
        long seconds = duration.getSeconds() % 60;
        
        if (minutes > 0) {
            return String.format("%d min %d seg", minutes, seconds);
        } else {
            return String.format("%d seg", seconds);
        }
    }
    
    @Override
    public String toString() {
        return String.format("QueueInfo{name='%s', status='%s', totalMessages=%d, activeMessages=%d, deadLetterMessages=%d}",
                getName(), getStatus(), getTotalMessages(), getActiveMessages(), getDeadLetterMessages());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        QueueInfo queueInfo = (QueueInfo) obj;
        return getName() != null ? getName().equals(queueInfo.getName()) : queueInfo.getName() == null;
    }
    
    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }
}
