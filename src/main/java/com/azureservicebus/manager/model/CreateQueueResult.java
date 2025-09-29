package com.azureservicebus.manager.model;

/**
 * Enum para representar os possíveis resultados da operação de criação de fila
 */
public enum CreateQueueResult {
    /**
     * Fila criada com sucesso
     */
    CREATED,
    
    /**
     * Fila já existe no namespace
     */
    ALREADY_EXISTS,
    
    /**
     * Erro durante a criação da fila
     */
    ERROR
}
