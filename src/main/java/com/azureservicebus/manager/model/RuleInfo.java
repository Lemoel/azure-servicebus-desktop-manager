package com.azureservicebus.manager.model;

import javafx.beans.property.*;

/**
 * Modelo de dados para informações de uma Rule (regra) de uma Subscription
 */
public class RuleInfo {
    
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty filterType = new SimpleStringProperty();
    private final StringProperty filterExpression = new SimpleStringProperty();
    private final StringProperty actionExpression = new SimpleStringProperty();
    private final BooleanProperty isDefault = new SimpleBooleanProperty();
    
    // Campos para Correlation Filter (necessários para edição)
    private final StringProperty correlationId = new SimpleStringProperty();
    private final StringProperty messageId = new SimpleStringProperty();
    private final StringProperty sessionId = new SimpleStringProperty();
    private final StringProperty replyTo = new SimpleStringProperty();
    private final StringProperty label = new SimpleStringProperty();
    private final StringProperty contentType = new SimpleStringProperty();
    
    // Construtores
    public RuleInfo() {}
    
    public RuleInfo(String name, String filterType, String filterExpression) {
        setName(name);
        setFilterType(filterType);
        setFilterExpression(filterExpression);
        setIsDefault("$Default".equals(name));
    }
    
    // Getters e Setters para Properties
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }
    
    public String getFilterType() { return filterType.get(); }
    public void setFilterType(String filterType) { this.filterType.set(filterType); }
    public StringProperty filterTypeProperty() { return filterType; }
    
    public String getFilterExpression() { return filterExpression.get(); }
    public void setFilterExpression(String filterExpression) { this.filterExpression.set(filterExpression); }
    public StringProperty filterExpressionProperty() { return filterExpression; }
    
    public String getActionExpression() { return actionExpression.get(); }
    public void setActionExpression(String actionExpression) { this.actionExpression.set(actionExpression); }
    public StringProperty actionExpressionProperty() { return actionExpression; }
    
    public boolean getIsDefault() { return isDefault.get(); }
    public void setIsDefault(boolean isDefault) { this.isDefault.set(isDefault); }
    public BooleanProperty isDefaultProperty() { return isDefault; }
    
    // Getters e Setters para Correlation Filter
    public String getCorrelationId() { return correlationId.get(); }
    public void setCorrelationId(String correlationId) { this.correlationId.set(correlationId); }
    public StringProperty correlationIdProperty() { return correlationId; }
    
    public String getMessageId() { return messageId.get(); }
    public void setMessageId(String messageId) { this.messageId.set(messageId); }
    public StringProperty messageIdProperty() { return messageId; }
    
    public String getSessionId() { return sessionId.get(); }
    public void setSessionId(String sessionId) { this.sessionId.set(sessionId); }
    public StringProperty sessionIdProperty() { return sessionId; }
    
    public String getReplyTo() { return replyTo.get(); }
    public void setReplyTo(String replyTo) { this.replyTo.set(replyTo); }
    public StringProperty replyToProperty() { return replyTo; }
    
    public String getLabel() { return label.get(); }
    public void setLabel(String label) { this.label.set(label); }
    public StringProperty labelProperty() { return label; }
    
    public String getContentType() { return contentType.get(); }
    public void setContentType(String contentType) { this.contentType.set(contentType); }
    public StringProperty contentTypeProperty() { return contentType; }
    
    /**
     * Retorna descrição formatada do tipo de filtro
     */
    public String getFilterTypeDescription() {
        if (filterType.get() == null) return "N/A";
        
        String type = filterType.get();
        if (type.contains("SqlFilter") || type.contains("SQL")) {
            return "SQL";
        } else if (type.contains("CorrelationFilter") || type.contains("Correlation")) {
            return "Correlation";
        } else if (type.contains("TrueFilter") || type.contains("True")) {
            return "True (Todas)";
        } else if (type.contains("FalseFilter") || type.contains("False")) {
            return "False (Nenhuma)";
        }
        return type;
    }
    
    /**
     * Retorna expressão formatada para exibição
     */
    public String getFormattedExpression() {
        String expr = filterExpression.get();
        if (expr == null || expr.isEmpty()) {
            return getFilterTypeDescription();
        }
        
        // Limitar tamanho para exibição em tabela
        if (expr.length() > 50) {
            return expr.substring(0, 47) + "...";
        }
        return expr;
    }
    
    @Override
    public String toString() {
        return String.format("RuleInfo{name='%s', filterType='%s', expression='%s'}",
                getName(), getFilterType(), getFilterExpression());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        RuleInfo ruleInfo = (RuleInfo) obj;
        return getName() != null ? getName().equals(ruleInfo.getName()) : ruleInfo.getName() == null;
    }
    
    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }
}
