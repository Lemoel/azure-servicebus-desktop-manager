package com.azureservicebus.manager.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Modelo de dados para informações de uma mensagem do Service Bus
 */
public class MessageInfo {
    
    private final LongProperty sequenceNumber = new SimpleLongProperty();
    private final StringProperty messageId = new SimpleStringProperty();
    private final StringProperty messageBody = new SimpleStringProperty();
    private final StringProperty contentType = new SimpleStringProperty();
    private final StringProperty correlationId = new SimpleStringProperty();
    private final StringProperty sessionId = new SimpleStringProperty();
    private final StringProperty replyTo = new SimpleStringProperty();
    private final StringProperty subject = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> enqueuedTime = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> scheduledEnqueueTime = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> expiresAt = new SimpleObjectProperty<>();
    private final IntegerProperty deliveryCount = new SimpleIntegerProperty();
    private final LongProperty sizeInBytes = new SimpleLongProperty();
    private final StringProperty lockToken = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> lockedUntil = new SimpleObjectProperty<>();
    private final StringProperty deadLetterReason = new SimpleStringProperty();
    private final StringProperty deadLetterErrorDescription = new SimpleStringProperty();
    private final MapProperty<String, Object> applicationProperties = new SimpleMapProperty<>();
    
    // Construtores
    public MessageInfo() {
        this.applicationProperties.set(FXCollections.observableHashMap());
    }
    
    public MessageInfo(String messageBody) {
        this();
        setMessageBody(messageBody);
    }
    
    // Getters e Setters para Properties
    public long getSequenceNumber() { return sequenceNumber.get(); }
    public void setSequenceNumber(long sequenceNumber) { this.sequenceNumber.set(sequenceNumber); }
    public LongProperty sequenceNumberProperty() { return sequenceNumber; }
    
    public String getMessageId() { return messageId.get(); }
    public void setMessageId(String messageId) { this.messageId.set(messageId); }
    public StringProperty messageIdProperty() { return messageId; }
    
    public String getMessageBody() { return messageBody.get(); }
    public void setMessageBody(String messageBody) { this.messageBody.set(messageBody); }
    public StringProperty messageBodyProperty() { return messageBody; }
    
    public String getContentType() { return contentType.get(); }
    public void setContentType(String contentType) { this.contentType.set(contentType); }
    public StringProperty contentTypeProperty() { return contentType; }
    
    public String getCorrelationId() { return correlationId.get(); }
    public void setCorrelationId(String correlationId) { this.correlationId.set(correlationId); }
    public StringProperty correlationIdProperty() { return correlationId; }
    
    public String getSessionId() { return sessionId.get(); }
    public void setSessionId(String sessionId) { this.sessionId.set(sessionId); }
    public StringProperty sessionIdProperty() { return sessionId; }
    
    public String getReplyTo() { return replyTo.get(); }
    public void setReplyTo(String replyTo) { this.replyTo.set(replyTo); }
    public StringProperty replyToProperty() { return replyTo; }
    
    public String getSubject() { return subject.get(); }
    public void setSubject(String subject) { this.subject.set(subject); }
    public StringProperty subjectProperty() { return subject; }
    
    public LocalDateTime getEnqueuedTime() { return enqueuedTime.get(); }
    public void setEnqueuedTime(LocalDateTime enqueuedTime) { this.enqueuedTime.set(enqueuedTime); }
    public ObjectProperty<LocalDateTime> enqueuedTimeProperty() { return enqueuedTime; }
    
    public LocalDateTime getScheduledEnqueueTime() { return scheduledEnqueueTime.get(); }
    public void setScheduledEnqueueTime(LocalDateTime scheduledEnqueueTime) { this.scheduledEnqueueTime.set(scheduledEnqueueTime); }
    public ObjectProperty<LocalDateTime> scheduledEnqueueTimeProperty() { return scheduledEnqueueTime; }
    
    public LocalDateTime getExpiresAt() { return expiresAt.get(); }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt.set(expiresAt); }
    public ObjectProperty<LocalDateTime> expiresAtProperty() { return expiresAt; }
    
    public int getDeliveryCount() { return deliveryCount.get(); }
    public void setDeliveryCount(int deliveryCount) { this.deliveryCount.set(deliveryCount); }
    public IntegerProperty deliveryCountProperty() { return deliveryCount; }
    
    public long getSizeInBytes() { return sizeInBytes.get(); }
    public void setSizeInBytes(long sizeInBytes) { this.sizeInBytes.set(sizeInBytes); }
    public LongProperty sizeInBytesProperty() { return sizeInBytes; }
    
    public String getLockToken() { return lockToken.get(); }
    public void setLockToken(String lockToken) { this.lockToken.set(lockToken); }
    public StringProperty lockTokenProperty() { return lockToken; }
    
    public LocalDateTime getLockedUntil() { return lockedUntil.get(); }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil.set(lockedUntil); }
    public ObjectProperty<LocalDateTime> lockedUntilProperty() { return lockedUntil; }
    
    public String getDeadLetterReason() { return deadLetterReason.get(); }
    public void setDeadLetterReason(String deadLetterReason) { this.deadLetterReason.set(deadLetterReason); }
    public StringProperty deadLetterReasonProperty() { return deadLetterReason; }
    
    public String getDeadLetterErrorDescription() { return deadLetterErrorDescription.get(); }
    public void setDeadLetterErrorDescription(String deadLetterErrorDescription) { 
        this.deadLetterErrorDescription.set(deadLetterErrorDescription); 
    }
    public StringProperty deadLetterErrorDescriptionProperty() { return deadLetterErrorDescription; }
    
    public Map<String, Object> getApplicationProperties() { return applicationProperties.get(); }
    public void setApplicationProperties(Map<String, Object> applicationProperties) { 
        this.applicationProperties.set(FXCollections.observableMap(applicationProperties)); 
    }
    public MapProperty<String, Object> applicationPropertiesProperty() { return applicationProperties; }
    
    // Métodos utilitários
    public String getMessageBodyPreview() {
        String body = getMessageBody();
        if (body == null || body.isEmpty()) {
            return "Mensagem vazia";
        }
        
        if (body.length() <= 100) {
            return body;
        }
        
        return body.substring(0, 100) + "...";
    }
    
    public String getFormattedEnqueuedTime() {
        LocalDateTime time = getEnqueuedTime();
        if (time == null) return "N/A";
        
        return time.toString().replace("T", " ").substring(0, 19);
    }
    
    public String getFormattedSize() {
        long bytes = getSizeInBytes();
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    public String getApplicationPropertiesAsString() {
        Map<String, Object> props = getApplicationProperties();
        if (props == null || props.isEmpty()) {
            return "{}";
        }
        
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : props.entrySet()) {
            if (!first) sb.append(", ");
            sb.append("\"").append(entry.getKey()).append("\": ");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else {
                sb.append(value);
            }
            first = false;
        }
        sb.append("}");
        
        return sb.toString();
    }
    
    public boolean isDeadLetter() {
        return getDeadLetterReason() != null && !getDeadLetterReason().isEmpty();
    }
    
    public boolean isScheduled() {
        return getScheduledEnqueueTime() != null && 
               getScheduledEnqueueTime().isAfter(LocalDateTime.now());
    }
    
    public boolean isExpired() {
        return getExpiresAt() != null && 
               getExpiresAt().isBefore(LocalDateTime.now());
    }
    
    public void addApplicationProperty(String key, Object value) {
        if (applicationProperties.get() == null) {
            applicationProperties.set(FXCollections.observableHashMap());
        }
        applicationProperties.get().put(key, value);
    }
    
    public void removeApplicationProperty(String key) {
        if (applicationProperties.get() != null) {
            applicationProperties.get().remove(key);
        }
    }
    
    @Override
    public String toString() {
        return String.format("MessageInfo{sequenceNumber=%d, messageId='%s', contentType='%s', sizeInBytes=%d}",
                getSequenceNumber(), getMessageId(), getContentType(), getSizeInBytes());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MessageInfo that = (MessageInfo) obj;
        return getSequenceNumber() == that.getSequenceNumber() &&
               (getMessageId() != null ? getMessageId().equals(that.getMessageId()) : that.getMessageId() == null);
    }
    
    @Override
    public int hashCode() {
        int result = Long.hashCode(getSequenceNumber());
        result = 31 * result + (getMessageId() != null ? getMessageId().hashCode() : 0);
        return result;
    }
}
