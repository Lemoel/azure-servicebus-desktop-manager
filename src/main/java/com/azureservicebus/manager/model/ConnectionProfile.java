package com.azureservicebus.manager.model;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa um perfil de conexão do Azure Service Bus
 */
public class ConnectionProfile {
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("connectionString")
    private String connectionString; // Armazenada em formato criptografado
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("lastUsedAt")
    private String lastUsedAt;
    
    public ConnectionProfile() {
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.lastUsedAt = this.createdAt;
    }
    
    public ConnectionProfile(String name, String connectionString) {
        this();
        this.name = name;
        this.connectionString = connectionString;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getConnectionString() {
        return connectionString;
    }
    
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getLastUsedAt() {
        return lastUsedAt;
    }
    
    public void setLastUsedAt(String lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
    
    /**
     * Atualiza a data/hora do último uso para agora
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    /**
     * Extrai o namespace da connection string (para exibição)
     */
    public String getNamespace() {
        if (connectionString == null || connectionString.isEmpty()) {
            return "N/A";
        }
        
        try {
            // Procurar por "Endpoint=sb://" e extrair até o próximo "/"
            int startIndex = connectionString.indexOf("Endpoint=sb://");
            if (startIndex != -1) {
                startIndex += "Endpoint=sb://".length();
                int endIndex = connectionString.indexOf("/", startIndex);
                if (endIndex != -1) {
                    String endpoint = connectionString.substring(startIndex, endIndex);
                    // Remover .servicebus.windows.net
                    return endpoint.replace(".servicebus.windows.net", "");
                }
            }
            return "N/A";
        } catch (Exception e) {
            return "N/A";
        }
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConnectionProfile that = (ConnectionProfile) obj;
        return name != null && name.equals(that.name);
    }
    
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
