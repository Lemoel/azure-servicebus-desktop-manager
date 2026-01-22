package com.azureservicebus.manager.model;

/**
 * Modelo para configuração de uma Subscription no Azure Service Bus
 */
public class SubscriptionConfiguration {
    
    private String name;
    private String topicName;
    
    // Configurações de entrega
    private int maxDeliveryCount = 10;
    private int lockDurationMinutes = 1; // 1 minuto padrão
    private int defaultMessageTimeToLiveDays = 14; // 14 dias padrão
    
    // Configurações de dead-lettering
    private boolean deadLetteringOnMessageExpiration = false;
    private boolean deadLetteringOnFilterEvaluationExceptions = false;
    
    // Configurações de performance
    private boolean enableBatchedOperations = true;
    
    // Configurações de auto-delete
    private boolean enableAutoDeleteOnIdle = false;
    private int autoDeleteOnIdleHours = 1;
    
    // Configurações de sessão
    private boolean requiresSession = false;
    
    // Configurações de encaminhamento
    private boolean enableForwardTo = false;
    private String forwardTo = "";
    private boolean enableForwardDeadLetteredMessagesTo = false;
    private String forwardDeadLetteredMessagesTo = "";
    
    // Metadados
    private String userMetadata = "";
    
    // Configurações de filtro
    private boolean filterEnabled = false;
    private String filterType = "SQL Filter";
    private String sqlExpression = "";
    private String correlationId = "";
    private String messageId = "";
    private String sessionId = "";
    private String replyTo = "";
    private String label = "";
    private String contentType = "";
    
    public SubscriptionConfiguration() {
    }
    
    public SubscriptionConfiguration(String name, String topicName) {
        this.name = name;
        this.topicName = topicName;
    }
    
    public boolean isValid() {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (topicName == null || topicName.trim().isEmpty()) {
            return false;
        }
        if (maxDeliveryCount < 1 || maxDeliveryCount > 1000) {
            return false;
        }
        if (lockDurationMinutes < 0 || lockDurationMinutes > 5) {
            return false;
        }
        if (defaultMessageTimeToLiveDays < 0 || defaultMessageTimeToLiveDays > 365) {
            return false;
        }
        if (enableAutoDeleteOnIdle && autoDeleteOnIdleHours < 0) {
            return false;
        }
        if (enableForwardTo && (forwardTo == null || forwardTo.trim().isEmpty())) {
            return false;
        }
        if (enableForwardDeadLetteredMessagesTo && 
            (forwardDeadLetteredMessagesTo == null || forwardDeadLetteredMessagesTo.trim().isEmpty())) {
            return false;
        }
        return true;
    }
    
    // Getters and Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTopicName() {
        return topicName;
    }
    
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
    
    public int getMaxDeliveryCount() {
        return maxDeliveryCount;
    }
    
    public void setMaxDeliveryCount(int maxDeliveryCount) {
        this.maxDeliveryCount = maxDeliveryCount;
    }
    
    public int getLockDurationMinutes() {
        return lockDurationMinutes;
    }
    
    public void setLockDurationMinutes(int lockDurationMinutes) {
        this.lockDurationMinutes = lockDurationMinutes;
    }
    
    public int getDefaultMessageTimeToLiveDays() {
        return defaultMessageTimeToLiveDays;
    }
    
    public void setDefaultMessageTimeToLiveDays(int defaultMessageTimeToLiveDays) {
        this.defaultMessageTimeToLiveDays = defaultMessageTimeToLiveDays;
    }
    
    public boolean isDeadLetteringOnMessageExpiration() {
        return deadLetteringOnMessageExpiration;
    }
    
    public void setDeadLetteringOnMessageExpiration(boolean deadLetteringOnMessageExpiration) {
        this.deadLetteringOnMessageExpiration = deadLetteringOnMessageExpiration;
    }
    
    public boolean isDeadLetteringOnFilterEvaluationExceptions() {
        return deadLetteringOnFilterEvaluationExceptions;
    }
    
    public void setDeadLetteringOnFilterEvaluationExceptions(boolean deadLetteringOnFilterEvaluationExceptions) {
        this.deadLetteringOnFilterEvaluationExceptions = deadLetteringOnFilterEvaluationExceptions;
    }
    
    public boolean isEnableBatchedOperations() {
        return enableBatchedOperations;
    }
    
    public void setEnableBatchedOperations(boolean enableBatchedOperations) {
        this.enableBatchedOperations = enableBatchedOperations;
    }
    
    public boolean isEnableAutoDeleteOnIdle() {
        return enableAutoDeleteOnIdle;
    }
    
    public void setEnableAutoDeleteOnIdle(boolean enableAutoDeleteOnIdle) {
        this.enableAutoDeleteOnIdle = enableAutoDeleteOnIdle;
    }
    
    public int getAutoDeleteOnIdleHours() {
        return autoDeleteOnIdleHours;
    }
    
    public void setAutoDeleteOnIdleHours(int autoDeleteOnIdleHours) {
        this.autoDeleteOnIdleHours = autoDeleteOnIdleHours;
    }
    
    public boolean isRequiresSession() {
        return requiresSession;
    }
    
    public void setRequiresSession(boolean requiresSession) {
        this.requiresSession = requiresSession;
    }
    
    public boolean isEnableForwardTo() {
        return enableForwardTo;
    }
    
    public void setEnableForwardTo(boolean enableForwardTo) {
        this.enableForwardTo = enableForwardTo;
    }
    
    public String getForwardTo() {
        return forwardTo;
    }
    
    public void setForwardTo(String forwardTo) {
        this.forwardTo = forwardTo;
    }
    
    public boolean isEnableForwardDeadLetteredMessagesTo() {
        return enableForwardDeadLetteredMessagesTo;
    }
    
    public void setEnableForwardDeadLetteredMessagesTo(boolean enableForwardDeadLetteredMessagesTo) {
        this.enableForwardDeadLetteredMessagesTo = enableForwardDeadLetteredMessagesTo;
    }
    
    public String getForwardDeadLetteredMessagesTo() {
        return forwardDeadLetteredMessagesTo;
    }
    
    public void setForwardDeadLetteredMessagesTo(String forwardDeadLetteredMessagesTo) {
        this.forwardDeadLetteredMessagesTo = forwardDeadLetteredMessagesTo;
    }
    
    public String getUserMetadata() {
        return userMetadata;
    }
    
    public void setUserMetadata(String userMetadata) {
        this.userMetadata = userMetadata;
    }
    
    public boolean isFilterEnabled() {
        return filterEnabled;
    }
    
    public void setFilterEnabled(boolean filterEnabled) {
        this.filterEnabled = filterEnabled;
    }
    
    public String getFilterType() {
        return filterType;
    }
    
    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }
    
    public String getSqlExpression() {
        return sqlExpression;
    }
    
    public void setSqlExpression(String sqlExpression) {
        this.sqlExpression = sqlExpression;
    }
    
    public String getCorrelationId() {
        return correlationId;
    }
    
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getReplyTo() {
        return replyTo;
    }
    
    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
