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
    
    @SerializedName("color")
    private String color; // Cor hexadecimal (ex: #28a745)
    
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
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    /**
     * Calcula a cor de texto (branco ou preto) que contrasta melhor com a cor de fundo
     * @return "#FFFFFF" para texto branco ou "#000000" para texto preto
     */
    public String getContrastTextColor() {
        if (color == null || color.isEmpty()) {
            return "#000000"; // Padrão: texto preto
        }
        
        try {
            // Remove o # se presente
            String hex = color.startsWith("#") ? color.substring(1) : color;
            
            // Converte hex para RGB
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            
            // Calcula a luminância usando a fórmula WCAG
            double luminance = 0.299 * r + 0.587 * g + 0.114 * b;
            
            // Se a luminância for maior que 186, usar texto preto; caso contrário, branco
            return luminance > 186 ? "#000000" : "#FFFFFF";
        } catch (Exception e) {
            return "#000000"; // Em caso de erro, retorna texto preto
        }
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
