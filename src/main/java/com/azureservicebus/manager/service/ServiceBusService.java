package com.azureservicebus.manager.service;

import com.azure.messaging.servicebus.*;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.*;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import com.azureservicebus.manager.model.CreateQueueResult;
import com.azureservicebus.manager.model.MessageInfo;
import com.azureservicebus.manager.model.QueueInfo;
import com.azureservicebus.manager.model.SubscriptionInfo;
import com.azureservicebus.manager.model.TopicInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Serviço para operações do Azure Service Bus
 * Centraliza todas as operações de gestão de filas e mensagens
 */
public class ServiceBusService {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceBusService.class);
    
    private String connectionString;
    private ServiceBusAdministrationClient adminClient;
    private final ExecutorService executorService;
    
    // Callbacks para notificações
    private Runnable onConnectionStatusChanged;
    private java.util.function.Consumer<String> onLogMessage;
    
    public ServiceBusService() {
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "ServiceBus-Worker");
            thread.setDaemon(true);
            return thread;
        });
    }
    
    // Configuração de callbacks
    public void setOnConnectionStatusChanged(Runnable callback) {
        this.onConnectionStatusChanged = callback;
    }
    
    public void setOnLogMessage(java.util.function.Consumer<String> callback) {
        this.onLogMessage = callback;
    }
    
    private void logMessage(String message) {
        logger.info(message);
        if (onLogMessage != null) {
            onLogMessage.accept(message);
        }
    }
    
    private void logError(String message, Exception e) {
        logger.error(message, e);
        if (onLogMessage != null) {
            onLogMessage.accept("ERRO: " + message + " - " + e.getMessage());
        }
    }
    
    /**
     * Limpa parâmetros não suportados da connection string
     */
    private String cleanConnectionString(String connectionString) {
        if (connectionString == null || connectionString.trim().isEmpty()) {
            return connectionString;
        }
        
        // Lista de parâmetros não suportados pelo SDK mais recente
        String[] unsupportedParams = {
            "TransportType",
            "RuntimePort",
            "ManagementPort"
        };
        
        String cleaned = connectionString;
        
        for (String param : unsupportedParams) {
            // Remove o parâmetro e seu valor (formato: ;ParamName=Value)
            cleaned = cleaned.replaceAll(";\\s*" + param + "\\s*=[^;]*", "");
            // Remove se estiver no início (formato: ParamName=Value;)
            cleaned = cleaned.replaceAll("^\\s*" + param + "\\s*=[^;]*;", "");
            // Remove se for o único parâmetro (formato: ParamName=Value)
            if (cleaned.matches("^\\s*" + param + "\\s*=[^;]*\\s*$")) {
                cleaned = "";
            }
        }
        
        return cleaned.trim();
    }
    
    /**
     * Conecta ao Azure Service Bus usando connection string
     */
    public CompletableFuture<Boolean> connectAsync(String connectionString) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Limpar parâmetros não suportados
                String cleanedConnectionString = cleanConnectionString(connectionString);
                
                if (cleanedConnectionString == null || cleanedConnectionString.trim().isEmpty()) {
                    throw new IllegalArgumentException("Connection string inválida ou vazia após limpeza");
                }
                
                this.connectionString = cleanedConnectionString;
                
                logMessage("Connection string limpa e validada");
                
                // Criar cliente de administração
                this.adminClient = new ServiceBusAdministrationClientBuilder()
                    .connectionString(cleanedConnectionString)
                    .buildClient();
                
                // Testar conexão listando filas
                adminClient.listQueues().stream().findFirst();
                
                logMessage("Conectado ao Azure Service Bus com sucesso");
                
                if (onConnectionStatusChanged != null) {
                    onConnectionStatusChanged.run();
                }
                
                return true;
                
            } catch (Exception e) {
                logError("Erro ao conectar ao Azure Service Bus", e);
                disconnect();
                return false;
            }
        }, executorService);
    }
    
    /**
     * Desconecta do Azure Service Bus
     */
    public void disconnect() {
        try {
            if (adminClient != null) {
                adminClient = null;
            }
            
            connectionString = null;
            
            logMessage("Desconectado do Azure Service Bus");
            
            if (onConnectionStatusChanged != null) {
                onConnectionStatusChanged.run();
            }
            
        } catch (Exception e) {
            logError("Erro ao desconectar", e);
        }
    }
    
    /**
     * Verifica se está conectado
     */
    public boolean isConnected() {
        return adminClient != null;
    }
    
    /**
     * Extrai o namespace da connection string
     */
    public String extractNamespace() {
        if (connectionString == null) return null;
        
        try {
            String[] parts = connectionString.split(";");
            for (String part : parts) {
                if (part.startsWith("Endpoint=sb://")) {
                    String endpoint = part.substring("Endpoint=sb://".length());
                    return endpoint.substring(0, endpoint.indexOf('.'));
                }
            }
        } catch (Exception e) {
            logger.warn("Erro ao extrair namespace da connection string", e);
        }
        
        return null;
    }
    
    /**
     * Lista apenas os nomes das filas (operação rápida)
     */
    public CompletableFuture<ObservableList<String>> listQueueNamesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                List<String> queueNames = new ArrayList<>();
                
                adminClient.listQueues().forEach(queueProperties -> {
                    queueNames.add(queueProperties.getName());
                });
                
                queueNames.sort(String::compareToIgnoreCase);
                
                logMessage(String.format("Carregados %d nomes de filas", queueNames.size()));
                return FXCollections.observableArrayList(queueNames);
                
            } catch (Exception e) {
                logError("Erro ao listar nomes das filas", e);
                throw new RuntimeException("Erro ao listar filas", e);
            }
        }, executorService);
    }
    
    /**
     * Obtém detalhes completos de uma fila específica
     */
    public CompletableFuture<QueueInfo> getQueueDetailsAsync(String queueName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                // Obter propriedades da fila
                QueueProperties queueProperties = adminClient.getQueue(queueName);
                
                // Obter runtime info para contagem de mensagens
                QueueRuntimeProperties runtimeProperties = adminClient.getQueueRuntimeProperties(queueName);
                
                QueueInfo queueInfo = new QueueInfo(queueName);
                
                // Propriedades básicas
                queueInfo.setStatus(queueProperties.getStatus().toString());
                queueInfo.setMaxDeliveryCount(queueProperties.getMaxDeliveryCount());
                queueInfo.setLockDuration(queueProperties.getLockDuration());
                queueInfo.setMaxSizeInMB(queueProperties.getMaxSizeInMegabytes());
                queueInfo.setPartitioningEnabled(queueProperties.isPartitioningEnabled());
                queueInfo.setSessionRequired(queueProperties.isSessionRequired());
                queueInfo.setDuplicateDetectionEnabled(queueProperties.isDuplicateDetectionRequired());
                queueInfo.setDeadLetteringOnMessageExpiration(queueProperties.isDeadLetteringOnMessageExpiration());
                queueInfo.setBatchedOperationsEnabled(queueProperties.isBatchedOperationsEnabled());
                queueInfo.setDefaultMessageTimeToLive(queueProperties.getDefaultMessageTimeToLive());
                
                // Runtime properties
                queueInfo.setTotalMessages(runtimeProperties.getTotalMessageCount());
                queueInfo.setActiveMessages(runtimeProperties.getActiveMessageCount());
                queueInfo.setDeadLetterMessages(runtimeProperties.getDeadLetterMessageCount());
                queueInfo.setScheduledMessages(runtimeProperties.getScheduledMessageCount());
                queueInfo.setSizeInKB(runtimeProperties.getSizeInBytes() / 1024.0);
                
                // Timestamps
                if (runtimeProperties.getCreatedAt() != null) {
                    queueInfo.setCreatedAt(LocalDateTime.ofInstant(
                        runtimeProperties.getCreatedAt().toInstant(), 
                        ZoneId.systemDefault()
                    ));
                }
                
                if (runtimeProperties.getUpdatedAt() != null) {
                    queueInfo.setUpdatedAt(LocalDateTime.ofInstant(
                        runtimeProperties.getUpdatedAt().toInstant(), 
                        ZoneId.systemDefault()
                    ));
                }
                
                logMessage(String.format("Detalhes carregados para fila '%s'", queueName));
                return queueInfo;
                
            } catch (Exception e) {
                logError(String.format("Erro ao obter detalhes da fila '%s'", queueName), e);
                throw new RuntimeException("Erro ao obter detalhes da fila", e);
            }
        }, executorService);
    }
    
    /**
     * Cria uma nova fila com configurações padrão
     */
    public CompletableFuture<CreateQueueResult> createQueueAsync(String queueName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                CreateQueueOptions options = new CreateQueueOptions();
                adminClient.createQueue(queueName, options);
                
                logMessage(String.format("Fila '%s' criada com sucesso", queueName));
                return CreateQueueResult.CREATED;
                
            } catch (com.azure.core.exception.ResourceExistsException e) {
                // Fila já existe - não é um erro, apenas informativo
                logMessage(String.format("Fila '%s' já existe no namespace", queueName));
                return CreateQueueResult.ALREADY_EXISTS;
                
            } catch (Exception e) {
                logError(String.format("Erro ao criar fila '%s'", queueName), e);
                return CreateQueueResult.ERROR;
            }
        }, executorService);
    }
    
    /**
     * Cria uma nova fila com configurações customizadas
     */
    public CompletableFuture<CreateQueueResult> createQueueAsync(com.azureservicebus.manager.model.QueueConfiguration config) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            if (config == null || !config.isValid()) {
                logError("Configuração de fila inválida", new IllegalArgumentException("Config inválida"));
                return CreateQueueResult.ERROR;
            }
            
            try {
                String queueName = config.getName();
                
                // Criar CreateQueueOptions com todas as configurações
                // Nota: Algumas propriedades como session, partitioning e duplicate detection só podem ser definidas na criação
                CreateQueueOptions options = new CreateQueueOptions();
                
                // Configurações que DEVEM ser definidas na criação (não podem ser alteradas depois)
                if (config.isRequiresSession()) {
                    options.setSessionRequired(true);
                }
                if (config.isPartitioningEnabled()) {
                    options.setPartitioningEnabled(true);
                }
                if (config.isDuplicateDetectionEnabled()) {
                    options.setDuplicateDetectionRequired(true);
                    options.setDuplicateDetectionHistoryTimeWindow(config.getDuplicateDetectionHistoryTimeWindow());
                }
                
                // Criar a fila com configurações iniciais
                QueueProperties queueProperties = adminClient.createQueue(queueName, options);
                
                // Aplicar configurações que podem ser modificadas após criação
                queueProperties.setMaxDeliveryCount(config.getMaxDeliveryCount());
                queueProperties.setLockDuration(config.getLockDuration());
                queueProperties.setDeadLetteringOnMessageExpiration(config.isDeadLetteringOnMessageExpiration());
                queueProperties.setBatchedOperationsEnabled(config.isBatchedOperationsEnabled());
                queueProperties.setMaxSizeInMegabytes((int) config.getMaxSizeInMB());
                queueProperties.setDefaultMessageTimeToLive(config.getDefaultMessageTimeToLive());
                
                // Atualizar a fila com as configurações
                adminClient.updateQueue(queueProperties);
                
                // Log detalhado das configurações aplicadas
                StringBuilder configLog = new StringBuilder();
                configLog.append(String.format("Fila '%s' criada com configurações customizadas: ", queueName));
                configLog.append(String.format("maxDeliveryCount=%d, ", config.getMaxDeliveryCount()));
                configLog.append(String.format("lockDuration=%d min, ", config.getLockDurationMinutes()));
                if (config.isDuplicateDetectionEnabled()) {
                    configLog.append(String.format("duplicateDetection=enabled (window=%d min), ", 
                        config.getDuplicateDetectionHistoryTimeWindowMinutes()));
                }
                if (config.isRequiresSession()) {
                    configLog.append("requiresSession=true, ");
                }
                if (config.isPartitioningEnabled()) {
                    configLog.append("partitioning=true, ");
                }
                
                logMessage(configLog.toString().replaceAll(", $", ""));
                
                return CreateQueueResult.CREATED;
                
            } catch (com.azure.core.exception.ResourceExistsException e) {
                // Fila já existe - não é um erro, apenas informativo
                logMessage(String.format("Fila '%s' já existe no namespace", config.getName()));
                return CreateQueueResult.ALREADY_EXISTS;
                
            } catch (Exception e) {
                logError(String.format("Erro ao criar fila '%s' com configurações customizadas", config.getName()), e);
                return CreateQueueResult.ERROR;
            }
        }, executorService);
    }
    
    /**
     * Remove uma fila
     */
    public CompletableFuture<Boolean> deleteQueueAsync(String queueName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                adminClient.deleteQueue(queueName);
                
                logMessage(String.format("Fila '%s' removida com sucesso", queueName));
                return true;
                
            } catch (Exception e) {
                logError(String.format("Erro ao remover fila '%s'", queueName), e);
                throw new RuntimeException("Erro ao remover fila", e);
            }
        }, executorService);
    }
    
    /**
     * Limpa todas as mensagens de uma fila
     */
    public CompletableFuture<Integer> clearQueueMessagesAsync(String queueName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                int messagesDeleted = 0;
                
                // Limpar mensagens ativas
                try (ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
                         .connectionString(connectionString)
                         .receiver()
                         .queueName(queueName)
                         .buildClient()) {
                    
                    // Receber e deletar mensagens em lotes
                    while (true) {
                        Iterable<ServiceBusReceivedMessage> receivedMessages = 
                            receiver.receiveMessages(100, java.time.Duration.ofSeconds(5));
                        
                        boolean hasMessages = false;
                        for (ServiceBusReceivedMessage message : receivedMessages) {
                            hasMessages = true;
                            receiver.complete(message);
                            messagesDeleted++;
                        }
                        
                        if (!hasMessages) {
                            break; // Não há mais mensagens
                        }
                    }
                }
                
                // Limpar mensagens dead letter
                try (ServiceBusReceiverClient dlqReceiver = new ServiceBusClientBuilder()
                         .connectionString(connectionString)
                         .receiver()
                         .queueName(queueName)
                         .subQueue(SubQueue.DEAD_LETTER_QUEUE)
                         .buildClient()) {
                    
                    // Receber e deletar mensagens dead letter em lotes
                    while (true) {
                        Iterable<ServiceBusReceivedMessage> receivedMessages = 
                            dlqReceiver.receiveMessages(100, java.time.Duration.ofSeconds(5));
                        
                        boolean hasMessages = false;
                        for (ServiceBusReceivedMessage message : receivedMessages) {
                            hasMessages = true;
                            dlqReceiver.complete(message);
                            messagesDeleted++;
                        }
                        
                        if (!hasMessages) {
                            break; // Não há mais mensagens dead letter
                        }
                    }
                }
                
                logMessage(String.format("Limpeza concluída: %d mensagens removidas da fila '%s'", messagesDeleted, queueName));
                return messagesDeleted;
                
            } catch (Exception e) {
                logError(String.format("Erro ao limpar mensagens da fila '%s'", queueName), e);
                throw new RuntimeException("Erro ao limpar mensagens", e);
            }
        }, executorService);
    }
    
    /**
     * Envia uma mensagem para uma fila
     */
    public CompletableFuture<Boolean> sendMessageAsync(String queueName, String messageBody, 
                                                      Map<String, Object> properties) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                // Criar cliente sender usando try-with-resources
                try (ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                         .connectionString(connectionString)
                         .sender()
                         .queueName(queueName)
                         .buildClient()) {
                    
                    // Criar mensagem
                    ServiceBusMessage message = new ServiceBusMessage(messageBody);
                    
                    // Adicionar propriedades customizadas se fornecidas
                    if (properties != null && !properties.isEmpty()) {
                        for (Map.Entry<String, Object> entry : properties.entrySet()) {
                            message.getApplicationProperties().put(entry.getKey(), entry.getValue());
                        }
                    }
                    
                    // Enviar mensagem
                    sender.sendMessage(message);
                    
                    logMessage(String.format("Mensagem enviada com sucesso para fila '%s'", queueName));
                    return true;
                }
                
            } catch (Exception e) {
                logError(String.format("Erro ao enviar mensagem para fila '%s'", queueName), e);
                throw new RuntimeException("Erro ao enviar mensagem", e);
            }
        }, executorService);
    }
    
    /**
     * Visualiza mensagens de uma fila sem removê-las
     */
    public CompletableFuture<ObservableList<MessageInfo>> peekMessagesAsync(String queueName, int maxMessages) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                List<MessageInfo> messages = new ArrayList<>();
                
                // Criar cliente receiver para visualizar mensagens
                try (ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
                         .connectionString(connectionString)
                         .receiver()
                         .queueName(queueName)
                         .buildClient()) {
                    
                    // Peek mensagens (visualizar sem remover)
                    Iterable<ServiceBusReceivedMessage> peekedMessages = receiver.peekMessages(maxMessages);
                    
                    for (ServiceBusReceivedMessage message : peekedMessages) {
                        MessageInfo messageInfo = new MessageInfo();
                        
                        messageInfo.setSequenceNumber(message.getSequenceNumber());
                        messageInfo.setMessageId(message.getMessageId());
                        
                        // Tratar diferentes tipos de corpo de mensagem
                        String messageBody = extractMessageBody(message);
                        messageInfo.setMessageBody(messageBody);
                        
                        messageInfo.setContentType(message.getContentType());
                        
                        if (message.getEnqueuedTime() != null) {
                            messageInfo.setEnqueuedTime(LocalDateTime.ofInstant(
                                message.getEnqueuedTime().toInstant(), 
                                ZoneId.systemDefault()
                            ));
                        }
                        
                        // Propriedades da aplicação
                        if (message.getApplicationProperties() != null && !message.getApplicationProperties().isEmpty()) {
                            messageInfo.setApplicationProperties(new HashMap<>(message.getApplicationProperties()));
                        }
                        
                        messages.add(messageInfo);
                    }
                }
                
                logMessage(String.format("Carregadas %d mensagens da fila '%s'", messages.size(), queueName));
                return FXCollections.observableArrayList(messages);
                
            } catch (Exception e) {
                logError(String.format("Erro ao visualizar mensagens da fila '%s'", queueName), e);
                throw new RuntimeException("Erro ao visualizar mensagens", e);
            }
        }, executorService);
    }
    
    /**
     * Remove uma mensagem específica de uma fila pelo sequence number
     */
    public CompletableFuture<Boolean> deleteMessageAsync(String queueName, long sequenceNumber) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                // Criar cliente receiver para receber e deletar a mensagem específica
                try (ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
                         .connectionString(connectionString)
                         .receiver()
                         .queueName(queueName)
                         .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                         .buildClient()) {
                    
                    // Receber mensagens em lotes e procurar pela mensagem específica
                    int maxAttempts = 10; // Limitar tentativas para evitar loop infinito
                    int attempts = 0;
                    
                    while (attempts < maxAttempts) {
                        Iterable<ServiceBusReceivedMessage> receivedMessages = 
                            receiver.receiveMessages(100, java.time.Duration.ofSeconds(5));
                        
                        boolean foundMessage = false;
                        boolean hasMessages = false;
                        
                        for (ServiceBusReceivedMessage message : receivedMessages) {
                            hasMessages = true;
                            
                            if (message.getSequenceNumber() == sequenceNumber) {
                                // Encontrou a mensagem, deletar
                                receiver.complete(message);
                                logMessage(String.format("Mensagem com sequence number %d removida da fila '%s'", 
                                    sequenceNumber, queueName));
                                foundMessage = true;
                            } else {
                                // Não é a mensagem que queremos, abandonar para que volte à fila
                                receiver.abandon(message);
                            }
                        }
                        
                        if (foundMessage) {
                            return true;
                        }
                        
                        if (!hasMessages) {
                            break; // Não há mais mensagens na fila
                        }
                        
                        attempts++;
                    }
                    
                    logMessage(String.format("Mensagem com sequence number %d não encontrada na fila '%s'", 
                        sequenceNumber, queueName));
                    return false;
                }
                
            } catch (Exception e) {
                logError(String.format("Erro ao remover mensagem %d da fila '%s'", sequenceNumber, queueName), e);
                throw new RuntimeException("Erro ao remover mensagem", e);
            }
        }, executorService);
    }
    
    /**
     * Extrai o corpo da mensagem tratando diferentes tipos de dados
     */
    private String extractMessageBody(ServiceBusReceivedMessage message) {
        try {
            // Primeiro, tentar acessar através do raw AMQP message
            if (message.getRawAmqpMessage() != null && message.getRawAmqpMessage().getBody() != null) {
                Object body = message.getRawAmqpMessage().getBody();
                
                // Verificar se é um AmqpMessageBody
                if (body instanceof com.azure.core.amqp.models.AmqpMessageBody) {
                    com.azure.core.amqp.models.AmqpMessageBody amqpBody = (com.azure.core.amqp.models.AmqpMessageBody) body;
                    
                    // Tentar extrair como DATA (o tipo mais comum para mensagens JSON/String)
                    try {
                        byte[] dataBytes = amqpBody.getFirstData();
                        if (dataBytes != null && dataBytes.length > 0) {
                            String result = new String(dataBytes, java.nio.charset.StandardCharsets.UTF_8);
                            logger.debug("Mensagem extraída com sucesso como tipo DATA");
                            return result;
                        }
                    } catch (UnsupportedOperationException e) {
                        // Não é tipo DATA, tentar outros tipos
                        logger.debug("Corpo não é tipo DATA, tentando VALUE: " + e.getMessage());
                    } catch (Exception e) {
                        logger.debug("Erro ao extrair como DATA: " + e.getMessage());
                    }
                    
                    // Tentar extrair como VALUE
                    try {
                        Object value = amqpBody.getValue();
                        if (value != null) {
                            // Se o valor for bytes, converter para string
                            if (value instanceof byte[]) {
                                byte[] bytes = (byte[]) value;
                                if (bytes.length > 0) {
                                    String result = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                                    logger.debug("Mensagem extraída com sucesso como tipo VALUE (bytes)");
                                    return result;
                                } else {
                                    return "[Mensagem vazia]";
                                }
                            }
                            // Caso contrário, usar toString()
                            logger.debug("Mensagem extraída com sucesso como tipo VALUE (object)");
                            return value.toString();
                        }
                    } catch (UnsupportedOperationException ex) {
                        // Não é tipo VALUE, tentar SEQUENCE
                        logger.debug("Corpo não é tipo VALUE, tentando SEQUENCE: " + ex.getMessage());
                    } catch (Exception ex) {
                        logger.debug("Erro ao extrair como VALUE: " + ex.getMessage());
                    }
                    
                    // Tentar extrair como SEQUENCE
                    try {
                        java.util.List<Object> sequence = amqpBody.getSequence();
                        if (sequence != null && !sequence.isEmpty()) {
                            // Converter a sequência para uma string legível
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < sequence.size(); i++) {
                                if (i > 0) sb.append(", ");
                                sb.append(sequence.get(i));
                            }
                            logger.debug("Mensagem extraída com sucesso como tipo SEQUENCE");
                            return sb.toString();
                        }
                    } catch (UnsupportedOperationException ex) {
                        logger.debug("Corpo não é tipo SEQUENCE: " + ex.getMessage());
                    } catch (Exception e) {
                        logger.debug("Erro ao extrair como SEQUENCE: " + e.getMessage());
                    }
                }
            }
            
            // Fallback: tentar o método padrão getBody() para STRING e BINARY
            try {
                String result = message.getBody().toString();
                logger.debug("Mensagem extraída com método padrão getBody().toString()");
                return result;
            } catch (UnsupportedOperationException e) {
                logger.debug("getBody().toString() não suportado, tentando toBytes()");
                // Se ainda falhar, tentar extrair como bytes
                try {
                    byte[] bodyBytes = message.getBody().toBytes();
                    if (bodyBytes != null && bodyBytes.length > 0) {
                        String result = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
                        logger.debug("Mensagem extraída com getBody().toBytes()");
                        return result;
                    } else {
                        return "[Mensagem vazia]";
                    }
                } catch (Exception ex) {
                    logger.warn("Não foi possível extrair corpo da mensagem usando métodos padrão: " + ex.getMessage());
                }
            } catch (Exception e) {
                logger.debug("Erro com getBody(): " + e.getMessage());
            }
            
            // Último recurso: mostrar informação sobre o tipo
            return "[Tipo de corpo não suportado - use ferramentas específicas para visualizar]";
            
        } catch (Exception e) {
            // Fallback para qualquer outro erro
            logger.error("Erro ao extrair corpo da mensagem", e);
            return String.format("[Erro ao ler mensagem: %s]", e.getMessage());
        }
    }
    
    // ===========================================================================================
    // MÉTODOS PARA TÓPICOS (TOPICS)
    // ===========================================================================================
    
    /**
     * Lista apenas os nomes dos tópicos (operação rápida)
     */
    public CompletableFuture<ObservableList<String>> listTopicNamesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                List<String> topicNames = new ArrayList<>();
                
                adminClient.listTopics().forEach(topicProperties -> {
                    topicNames.add(topicProperties.getName());
                });
                
                topicNames.sort(String::compareToIgnoreCase);
                
                logMessage(String.format("Carregados %d nomes de tópicos", topicNames.size()));
                return FXCollections.observableArrayList(topicNames);
                
            } catch (Exception e) {
                logError("Erro ao listar nomes dos tópicos", e);
                throw new RuntimeException("Erro ao listar tópicos", e);
            }
        }, executorService);
    }
    
    /**
     * Obtém detalhes completos de um tópico específico
     */
    public CompletableFuture<TopicInfo> getTopicDetailsAsync(String topicName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                // Obter propriedades do tópico
                TopicProperties topicProperties = adminClient.getTopic(topicName);
                
                // Obter runtime info
                TopicRuntimeProperties runtimeProperties = adminClient.getTopicRuntimeProperties(topicName);
                
                TopicInfo topicInfo = new TopicInfo(topicName);
                
                // Propriedades básicas
                topicInfo.setStatus(topicProperties.getStatus().toString());
                topicInfo.setMaxSizeInMB(topicProperties.getMaxSizeInMegabytes());
                topicInfo.setPartitioningEnabled(topicProperties.isPartitioningEnabled());
                topicInfo.setDuplicateDetectionEnabled(topicProperties.isDuplicateDetectionRequired());
                topicInfo.setBatchedOperationsEnabled(topicProperties.isBatchedOperationsEnabled());
                // topicInfo.setOrderingSupported(topicProperties.isSupportOrdering()); // Método não disponível nesta versão da SDK
                topicInfo.setDefaultMessageTimeToLive(topicProperties.getDefaultMessageTimeToLive());
                topicInfo.setDuplicateDetectionHistoryTimeWindow(topicProperties.getDuplicateDetectionHistoryTimeWindow());
                
                // Runtime properties
                topicInfo.setScheduledMessages(runtimeProperties.getScheduledMessageCount());
                topicInfo.setSizeInKB(runtimeProperties.getSizeInBytes() / 1024.0);
                topicInfo.setSubscriptionCount(runtimeProperties.getSubscriptionCount());
                
                // Timestamps
                if (runtimeProperties.getCreatedAt() != null) {
                    topicInfo.setCreatedAt(LocalDateTime.ofInstant(
                        runtimeProperties.getCreatedAt().toInstant(), 
                        ZoneId.systemDefault()
                    ));
                }
                
                if (runtimeProperties.getUpdatedAt() != null) {
                    topicInfo.setUpdatedAt(LocalDateTime.ofInstant(
                        runtimeProperties.getUpdatedAt().toInstant(), 
                        ZoneId.systemDefault()
                    ));
                }
                
                if (runtimeProperties.getAccessedAt() != null) {
                    topicInfo.setAccessedAt(LocalDateTime.ofInstant(
                        runtimeProperties.getAccessedAt().toInstant(), 
                        ZoneId.systemDefault()
                    ));
                }
                
                logMessage(String.format("Detalhes carregados para tópico '%s'", topicName));
                return topicInfo;
                
            } catch (Exception e) {
                logError(String.format("Erro ao obter detalhes do tópico '%s'", topicName), e);
                throw new RuntimeException("Erro ao obter detalhes do tópico", e);
            }
        }, executorService);
    }
    
    /**
     * Cria um novo tópico
     */
    public CompletableFuture<CreateQueueResult> createTopicAsync(String topicName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                CreateTopicOptions options = new CreateTopicOptions();
                adminClient.createTopic(topicName, options);
                
                logMessage(String.format("Tópico '%s' criado com sucesso", topicName));
                return CreateQueueResult.CREATED;
                
            } catch (com.azure.core.exception.ResourceExistsException e) {
                logMessage(String.format("Tópico '%s' já existe no namespace", topicName));
                return CreateQueueResult.ALREADY_EXISTS;
                
            } catch (Exception e) {
                logError(String.format("Erro ao criar tópico '%s'", topicName), e);
                return CreateQueueResult.ERROR;
            }
        }, executorService);
    }
    
    /**
     * Remove um tópico
     */
    public CompletableFuture<Boolean> deleteTopicAsync(String topicName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                adminClient.deleteTopic(topicName);
                
                logMessage(String.format("Tópico '%s' removido com sucesso", topicName));
                return true;
                
            } catch (Exception e) {
                logError(String.format("Erro ao remover tópico '%s'", topicName), e);
                throw new RuntimeException("Erro ao remover tópico", e);
            }
        }, executorService);
    }
    
    /**
     * Envia uma mensagem para um tópico
     */
    public CompletableFuture<Boolean> sendMessageToTopicAsync(String topicName, String messageBody, 
                                                             Map<String, Object> properties) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                // Criar cliente sender para tópico
                try (ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                         .connectionString(connectionString)
                         .sender()
                         .topicName(topicName)
                         .buildClient()) {
                    
                    // Criar mensagem
                    ServiceBusMessage message = new ServiceBusMessage(messageBody);
                    
                    // Adicionar propriedades customizadas se fornecidas
                    if (properties != null && !properties.isEmpty()) {
                        for (Map.Entry<String, Object> entry : properties.entrySet()) {
                            message.getApplicationProperties().put(entry.getKey(), entry.getValue());
                        }
                    }
                    
                    // Enviar mensagem
                    sender.sendMessage(message);
                    
                    logMessage(String.format("Mensagem enviada com sucesso para tópico '%s'", topicName));
                    return true;
                }
                
            } catch (Exception e) {
                logError(String.format("Erro ao enviar mensagem para tópico '%s'", topicName), e);
                throw new RuntimeException("Erro ao enviar mensagem", e);
            }
        }, executorService);
    }
    
    // ===========================================================================================
    // MÉTODOS PARA SUBSCRIPTIONS
    // ===========================================================================================
    
    /**
     * Lista apenas os nomes das subscriptions de um tópico (operação rápida)
     */
    public CompletableFuture<ObservableList<String>> listSubscriptionNamesAsync(String topicName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                List<String> subscriptionNames = new ArrayList<>();
                
                adminClient.listSubscriptions(topicName).forEach(subscriptionProperties -> {
                    subscriptionNames.add(subscriptionProperties.getSubscriptionName());
                });
                
                subscriptionNames.sort(String::compareToIgnoreCase);
                
                logMessage(String.format("Carregadas %d subscriptions do tópico '%s'", 
                    subscriptionNames.size(), topicName));
                return FXCollections.observableArrayList(subscriptionNames);
                
            } catch (Exception e) {
                logError(String.format("Erro ao listar subscriptions do tópico '%s'", topicName), e);
                throw new RuntimeException("Erro ao listar subscriptions", e);
            }
        }, executorService);
    }
    
    /**
     * Obtém detalhes completos de uma subscription específica
     */
    public CompletableFuture<SubscriptionInfo> getSubscriptionDetailsAsync(String topicName, String subscriptionName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                // Obter propriedades da subscription
                SubscriptionProperties subProperties = adminClient.getSubscription(topicName, subscriptionName);
                
                // Obter runtime info
                SubscriptionRuntimeProperties runtimeProperties = 
                    adminClient.getSubscriptionRuntimeProperties(topicName, subscriptionName);
                
                SubscriptionInfo subscriptionInfo = new SubscriptionInfo(topicName, subscriptionName);
                
                // Propriedades básicas
                subscriptionInfo.setStatus(subProperties.getStatus().toString());
                subscriptionInfo.setMaxDeliveryCount(subProperties.getMaxDeliveryCount());
                subscriptionInfo.setLockDuration(subProperties.getLockDuration());
                subscriptionInfo.setDefaultMessageTimeToLive(subProperties.getDefaultMessageTimeToLive());
                subscriptionInfo.setAutoDeleteOnIdle(subProperties.getAutoDeleteOnIdle());
                subscriptionInfo.setSessionRequired(subProperties.isSessionRequired());
                subscriptionInfo.setDeadLetteringOnMessageExpiration(subProperties.isDeadLetteringOnMessageExpiration());
                // subscriptionInfo.setDeadLetteringOnFilterEvaluationException(
                //     subProperties.isDeadLetteringOnFilterEvaluationException()); // Método não disponível nesta versão da SDK
                subscriptionInfo.setBatchedOperationsEnabled(subProperties.isBatchedOperationsEnabled());
                
                // Runtime properties
                subscriptionInfo.setTotalMessages(runtimeProperties.getTotalMessageCount());
                subscriptionInfo.setActiveMessages(runtimeProperties.getActiveMessageCount());
                subscriptionInfo.setDeadLetterMessages(runtimeProperties.getDeadLetterMessageCount());
                // subscriptionInfo.setScheduledMessages(runtimeProperties.getScheduledMessageCount()); // Método não disponível nesta versão da SDK
                subscriptionInfo.setTransferMessageCount(runtimeProperties.getTransferMessageCount());
                subscriptionInfo.setTransferDeadLetterMessageCount(runtimeProperties.getTransferDeadLetterMessageCount());
                
                // Timestamps
                if (runtimeProperties.getCreatedAt() != null) {
                    subscriptionInfo.setCreatedAt(LocalDateTime.ofInstant(
                        runtimeProperties.getCreatedAt().toInstant(), 
                        ZoneId.systemDefault()
                    ));
                }
                
                if (runtimeProperties.getUpdatedAt() != null) {
                    subscriptionInfo.setUpdatedAt(LocalDateTime.ofInstant(
                        runtimeProperties.getUpdatedAt().toInstant(), 
                        ZoneId.systemDefault()
                    ));
                }
                
                if (runtimeProperties.getAccessedAt() != null) {
                    subscriptionInfo.setAccessedAt(LocalDateTime.ofInstant(
                        runtimeProperties.getAccessedAt().toInstant(), 
                        ZoneId.systemDefault()
                    ));
                }
                
                logMessage(String.format("Detalhes carregados para subscription '%s' do tópico '%s'", 
                    subscriptionName, topicName));
                return subscriptionInfo;
                
            } catch (Exception e) {
                logError(String.format("Erro ao obter detalhes da subscription '%s' do tópico '%s'", 
                    subscriptionName, topicName), e);
                throw new RuntimeException("Erro ao obter detalhes da subscription", e);
            }
        }, executorService);
    }
    
    /**
     * Cria uma nova subscription em um tópico (com rule $Default automática)
     */
    public CompletableFuture<CreateQueueResult> createSubscriptionAsync(String topicName, String subscriptionName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                CreateSubscriptionOptions options = new CreateSubscriptionOptions();
                adminClient.createSubscription(topicName, subscriptionName, options);
                
                logMessage(String.format("Subscription '%s' criada com sucesso no tópico '%s'", 
                    subscriptionName, topicName));
                return CreateQueueResult.CREATED;
                
            } catch (com.azure.core.exception.ResourceExistsException e) {
                logMessage(String.format("Subscription '%s' já existe no tópico '%s'", 
                    subscriptionName, topicName));
                return CreateQueueResult.ALREADY_EXISTS;
                
            } catch (Exception e) {
                logError(String.format("Erro ao criar subscription '%s' no tópico '%s'", 
                    subscriptionName, topicName), e);
                return CreateQueueResult.ERROR;
            }
        }, executorService);
    }
    
    /**
     * Cria uma nova subscription com configurações avançadas
     */
    public CompletableFuture<CreateQueueResult> createSubscriptionAsync(
            com.azureservicebus.manager.model.SubscriptionConfiguration config) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            if (config == null || !config.isValid()) {
                logError("Configuração de subscription inválida", new IllegalArgumentException("Config inválida"));
                return CreateQueueResult.ERROR;
            }
            
            try {
                String topicName = config.getTopicName();
                String subscriptionName = config.getName();
                
                // Criar options básicas
                CreateSubscriptionOptions options = new CreateSubscriptionOptions();
                
                // Configurações que DEVEM ser definidas na criação
                if (config.isRequiresSession()) {
                    options.setSessionRequired(true);
                }
                
                // Aplicar configurações de entrega
                options.setMaxDeliveryCount(config.getMaxDeliveryCount());
                options.setLockDuration(java.time.Duration.ofMinutes(config.getLockDurationMinutes()));
                
                // TTL: Só setar se o usuário especificou um valor customizado
                // Se não setar, o Azure usa o padrão (TimeSpan.MaxValue = infinito)
                if (config.hasCustomMessageTimeToLive()) {
                    options.setDefaultMessageTimeToLive(java.time.Duration.ofDays(config.getDefaultMessageTimeToLiveDays()));
                }
                // Se null (vazio), não setamos - Azure usará infinito automaticamente
                
                // Configurações de dead letter
                options.setDeadLetteringOnMessageExpiration(config.isDeadLetteringOnMessageExpiration());
                
                // Configurações de performance
                options.setBatchedOperationsEnabled(config.isEnableBatchedOperations());
                
                // Auto-delete: Só aplicar se explicitamente habilitado
                // Se não habilitado, não setar (Azure usa infinito por padrão)
                if (config.isEnableAutoDeleteOnIdle()) {
                    options.setAutoDeleteOnIdle(java.time.Duration.ofHours(config.getAutoDeleteOnIdleHours()));
                }
                
                // Encaminhamento
                if (config.isEnableForwardTo() && config.getForwardTo() != null && !config.getForwardTo().isEmpty()) {
                    options.setForwardTo(config.getForwardTo());
                }
                if (config.isEnableForwardDeadLetteredMessagesTo() && 
                    config.getForwardDeadLetteredMessagesTo() != null && 
                    !config.getForwardDeadLetteredMessagesTo().isEmpty()) {
                    options.setForwardDeadLetteredMessagesTo(config.getForwardDeadLetteredMessagesTo());
                }
                
                // Metadados do usuário
                if (config.getUserMetadata() != null && !config.getUserMetadata().isEmpty()) {
                    options.setUserMetadata(config.getUserMetadata());
                }
                
                // Verificar se tem filtro customizado
                if (config.isFilterEnabled()) {
                    // Criar com rule customizada
                    CreateRuleOptions ruleOptions = new CreateRuleOptions();
                    
                    if ("SQL Filter".equals(config.getFilterType())) {
                        ruleOptions.setFilter(new SqlRuleFilter(config.getSqlExpression()));
                    } else if ("Correlation Filter".equals(config.getFilterType())) {
                        CorrelationRuleFilter correlationFilter = new CorrelationRuleFilter();
                        
                        if (config.getCorrelationId() != null && !config.getCorrelationId().isEmpty()) {
                            correlationFilter.setCorrelationId(config.getCorrelationId());
                        }
                        if (config.getMessageId() != null && !config.getMessageId().isEmpty()) {
                            correlationFilter.setMessageId(config.getMessageId());
                        }
                        if (config.getSessionId() != null && !config.getSessionId().isEmpty()) {
                            correlationFilter.setSessionId(config.getSessionId());
                        }
                        if (config.getReplyTo() != null && !config.getReplyTo().isEmpty()) {
                            correlationFilter.setReplyTo(config.getReplyTo());
                        }
                        if (config.getLabel() != null && !config.getLabel().isEmpty()) {
                            correlationFilter.setLabel(config.getLabel());
                        }
                        if (config.getContentType() != null && !config.getContentType().isEmpty()) {
                            correlationFilter.setContentType(config.getContentType());
                        }
                        
                        ruleOptions.setFilter(correlationFilter);
                    }
                    
                    // Criar com rule customizada (não cria $Default)
                    adminClient.createSubscription(topicName, subscriptionName, "CustomFilter", options, ruleOptions);
                    
                    logMessage(String.format(
                        "Subscription '%s' criada com configurações customizadas e filtro '%s' no tópico '%s'",
                        subscriptionName, config.getFilterType(), topicName));
                } else {
                    // Criar sem filtro customizado (cria com $Default)
                    adminClient.createSubscription(topicName, subscriptionName, options);
                    
                    logMessage(String.format(
                        "Subscription '%s' criada com configurações customizadas no tópico '%s'",
                        subscriptionName, topicName));
                }
                
                return CreateQueueResult.CREATED;
                
            } catch (com.azure.core.exception.ResourceExistsException e) {
                logMessage(String.format("Subscription '%s' já existe no tópico '%s'", 
                    config.getName(), config.getTopicName()));
                return CreateQueueResult.ALREADY_EXISTS;
                
            } catch (Exception e) {
                logError(String.format("Erro ao criar subscription '%s' com configurações no tópico '%s'", 
                    config.getName(), config.getTopicName()), e);
                return CreateQueueResult.ERROR;
            }
        }, executorService);
    }
    
    /**
     * Cria uma nova subscription em um tópico COM rule customizada (não cria $Default)
     */
    public CompletableFuture<CreateQueueResult> createSubscriptionWithRuleAsync(
            String topicName, String subscriptionName, String ruleName, 
            String filterType, String sqlExpression,
            String correlationId, String messageId, String sessionId,
            String replyTo, String label, String contentType) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                CreateSubscriptionOptions subscriptionOptions = new CreateSubscriptionOptions();
                CreateRuleOptions ruleOptions = new CreateRuleOptions();
                
                // Configurar filtro baseado no tipo
                if ("SQL Filter".equals(filterType)) {
                    ruleOptions.setFilter(new SqlRuleFilter(sqlExpression));
                } else if ("Correlation Filter".equals(filterType)) {
                    CorrelationRuleFilter correlationFilter = new CorrelationRuleFilter();
                    
                    if (correlationId != null && !correlationId.isEmpty()) {
                        correlationFilter.setCorrelationId(correlationId);
                    }
                    if (messageId != null && !messageId.isEmpty()) {
                        correlationFilter.setMessageId(messageId);
                    }
                    if (sessionId != null && !sessionId.isEmpty()) {
                        correlationFilter.setSessionId(sessionId);
                    }
                    if (replyTo != null && !replyTo.isEmpty()) {
                        correlationFilter.setReplyTo(replyTo);
                    }
                    if (label != null && !label.isEmpty()) {
                        correlationFilter.setLabel(label);
                    }
                    if (contentType != null && !contentType.isEmpty()) {
                        correlationFilter.setContentType(contentType);
                    }
                    
                    ruleOptions.setFilter(correlationFilter);
                } else {
                    throw new IllegalArgumentException("Tipo de filtro inválido: " + filterType);
                }
                
                // Criar subscription COM a rule customizada (não cria $Default)
                adminClient.createSubscription(topicName, subscriptionName, ruleName, subscriptionOptions, ruleOptions);
                
                logMessage(String.format(
                    "Subscription '%s' criada com sucesso no tópico '%s' com rule customizada '%s' (tipo: %s)", 
                    subscriptionName, topicName, ruleName, filterType));
                return CreateQueueResult.CREATED;
                
            } catch (com.azure.core.exception.ResourceExistsException e) {
                logMessage(String.format("Subscription '%s' já existe no tópico '%s'", 
                    subscriptionName, topicName));
                return CreateQueueResult.ALREADY_EXISTS;
                
            } catch (Exception e) {
                logError(String.format("Erro ao criar subscription '%s' com rule no tópico '%s'", 
                    subscriptionName, topicName), e);
                return CreateQueueResult.ERROR;
            }
        }, executorService);
    }
    
    /**
     * Remove uma subscription de um tópico
     */
    public CompletableFuture<Boolean> deleteSubscriptionAsync(String topicName, String subscriptionName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                adminClient.deleteSubscription(topicName, subscriptionName);
                
                logMessage(String.format("Subscription '%s' removida do tópico '%s' com sucesso", 
                    subscriptionName, topicName));
                return true;
                
            } catch (Exception e) {
                logError(String.format("Erro ao remover subscription '%s' do tópico '%s'", 
                    subscriptionName, topicName), e);
                throw new RuntimeException("Erro ao remover subscription", e);
            }
        }, executorService);
    }
    
    /**
     * Visualiza mensagens de uma subscription sem removê-las
     */
    public CompletableFuture<ObservableList<MessageInfo>> peekSubscriptionMessagesAsync(
            String topicName, String subscriptionName, int maxMessages) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                List<MessageInfo> messages = new ArrayList<>();
                
                // Criar cliente receiver para visualizar mensagens da subscription
                try (ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
                         .connectionString(connectionString)
                         .receiver()
                         .topicName(topicName)
                         .subscriptionName(subscriptionName)
                         .buildClient()) {
                    
                    // Peek mensagens (visualizar sem remover)
                    Iterable<ServiceBusReceivedMessage> peekedMessages = receiver.peekMessages(maxMessages);
                    
                    for (ServiceBusReceivedMessage message : peekedMessages) {
                        MessageInfo messageInfo = new MessageInfo();
                        
                        messageInfo.setSequenceNumber(message.getSequenceNumber());
                        messageInfo.setMessageId(message.getMessageId());
                        
                        String messageBody = extractMessageBody(message);
                        messageInfo.setMessageBody(messageBody);
                        
                        messageInfo.setContentType(message.getContentType());
                        
                        if (message.getEnqueuedTime() != null) {
                            messageInfo.setEnqueuedTime(LocalDateTime.ofInstant(
                                message.getEnqueuedTime().toInstant(), 
                                ZoneId.systemDefault()
                            ));
                        }
                        
                        if (message.getApplicationProperties() != null && !message.getApplicationProperties().isEmpty()) {
                            messageInfo.setApplicationProperties(new HashMap<>(message.getApplicationProperties()));
                        }
                        
                        messages.add(messageInfo);
                    }
                }
                
                logMessage(String.format("Carregadas %d mensagens da subscription '%s' do tópico '%s'", 
                    messages.size(), subscriptionName, topicName));
                return FXCollections.observableArrayList(messages);
                
            } catch (Exception e) {
                logError(String.format("Erro ao visualizar mensagens da subscription '%s' do tópico '%s'", 
                    subscriptionName, topicName), e);
                throw new RuntimeException("Erro ao visualizar mensagens", e);
            }
        }, executorService);
    }
    
    /**
     * Limpa todas as mensagens de uma subscription
     */
    public CompletableFuture<Integer> clearSubscriptionMessagesAsync(String topicName, String subscriptionName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                int messagesDeleted = 0;
                
                // Limpar mensagens ativas
                try (ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
                         .connectionString(connectionString)
                         .receiver()
                         .topicName(topicName)
                         .subscriptionName(subscriptionName)
                         .buildClient()) {
                    
                    while (true) {
                        Iterable<ServiceBusReceivedMessage> receivedMessages = 
                            receiver.receiveMessages(100, java.time.Duration.ofSeconds(5));
                        
                        boolean hasMessages = false;
                        for (ServiceBusReceivedMessage message : receivedMessages) {
                            hasMessages = true;
                            receiver.complete(message);
                            messagesDeleted++;
                        }
                        
                        if (!hasMessages) {
                            break;
                        }
                    }
                }
                
                // Limpar mensagens dead letter
                try (ServiceBusReceiverClient dlqReceiver = new ServiceBusClientBuilder()
                         .connectionString(connectionString)
                         .receiver()
                         .topicName(topicName)
                         .subscriptionName(subscriptionName)
                         .subQueue(SubQueue.DEAD_LETTER_QUEUE)
                         .buildClient()) {
                    
                    while (true) {
                        Iterable<ServiceBusReceivedMessage> receivedMessages = 
                            dlqReceiver.receiveMessages(100, java.time.Duration.ofSeconds(5));
                        
                        boolean hasMessages = false;
                        for (ServiceBusReceivedMessage message : receivedMessages) {
                            hasMessages = true;
                            dlqReceiver.complete(message);
                            messagesDeleted++;
                        }
                        
                        if (!hasMessages) {
                            break;
                        }
                    }
                }
                
                logMessage(String.format("Limpeza concluída: %d mensagens removidas da subscription '%s' do tópico '%s'", 
                    messagesDeleted, subscriptionName, topicName));
                return messagesDeleted;
                
            } catch (Exception e) {
                logError(String.format("Erro ao limpar mensagens da subscription '%s' do tópico '%s'", 
                    subscriptionName, topicName), e);
                throw new RuntimeException("Erro ao limpar mensagens", e);
            }
        }, executorService);
    }
    
    // ===========================================================================================
    // MÉTODOS PARA RULES (REGRAS DE SUBSCRIPTIONS)
    // ===========================================================================================
    
    /**
     * Lista as rules de uma subscription
     */
    public CompletableFuture<ObservableList<com.azureservicebus.manager.model.RuleInfo>> listRulesAsync(
            String topicName, String subscriptionName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                List<com.azureservicebus.manager.model.RuleInfo> rules = new ArrayList<>();
                
                adminClient.listRules(topicName, subscriptionName).forEach(ruleProperties -> {
                    com.azureservicebus.manager.model.RuleInfo ruleInfo = 
                        new com.azureservicebus.manager.model.RuleInfo();
                    
                    ruleInfo.setName(ruleProperties.getName());
                    
                    // Determinar tipo de filtro e expressão
                    RuleFilter filter = ruleProperties.getFilter();
                    if (filter != null) {
                        String filterType = filter.getClass().getSimpleName();
                        ruleInfo.setFilterType(filterType);
                        
                        if (filter instanceof SqlRuleFilter) {
                            SqlRuleFilter sqlFilter = (SqlRuleFilter) filter;
                            ruleInfo.setFilterExpression(sqlFilter.getSqlExpression());
                        } else if (filter instanceof CorrelationRuleFilter) {
                            CorrelationRuleFilter corrFilter = (CorrelationRuleFilter) filter;
                            
                            // Armazenar valores individuais para edição
                            if (corrFilter.getCorrelationId() != null) {
                                ruleInfo.setCorrelationId(corrFilter.getCorrelationId());
                            }
                            if (corrFilter.getMessageId() != null) {
                                ruleInfo.setMessageId(corrFilter.getMessageId());
                            }
                            if (corrFilter.getSessionId() != null) {
                                ruleInfo.setSessionId(corrFilter.getSessionId());
                            }
                            if (corrFilter.getReplyTo() != null) {
                                ruleInfo.setReplyTo(corrFilter.getReplyTo());
                            }
                            if (corrFilter.getLabel() != null) {
                                ruleInfo.setLabel(corrFilter.getLabel());
                            }
                            if (corrFilter.getContentType() != null) {
                                ruleInfo.setContentType(corrFilter.getContentType());
                            }
                            
                            // Montar descrição do correlation filter para exibição
                            StringBuilder expr = new StringBuilder();
                            if (corrFilter.getCorrelationId() != null && !corrFilter.getCorrelationId().isEmpty()) {
                                expr.append("CorrelationId='").append(corrFilter.getCorrelationId()).append("' ");
                            }
                            if (corrFilter.getMessageId() != null && !corrFilter.getMessageId().isEmpty()) {
                                expr.append("MessageId='").append(corrFilter.getMessageId()).append("' ");
                            }
                            if (corrFilter.getTo() != null && !corrFilter.getTo().isEmpty()) {
                                expr.append("To='").append(corrFilter.getTo()).append("' ");
                            }
                            if (corrFilter.getReplyTo() != null && !corrFilter.getReplyTo().isEmpty()) {
                                expr.append("ReplyTo='").append(corrFilter.getReplyTo()).append("' ");
                            }
                            if (corrFilter.getLabel() != null && !corrFilter.getLabel().isEmpty()) {
                                expr.append("Label='").append(corrFilter.getLabel()).append("' ");
                            }
                            if (corrFilter.getSessionId() != null && !corrFilter.getSessionId().isEmpty()) {
                                expr.append("SessionId='").append(corrFilter.getSessionId()).append("' ");
                            }
                            if (corrFilter.getReplyToSessionId() != null && !corrFilter.getReplyToSessionId().isEmpty()) {
                                expr.append("ReplyToSessionId='").append(corrFilter.getReplyToSessionId()).append("' ");
                            }
                            if (corrFilter.getContentType() != null && !corrFilter.getContentType().isEmpty()) {
                                expr.append("ContentType='").append(corrFilter.getContentType()).append("' ");
                            }
                            
                            String expression = expr.toString().trim();
                            ruleInfo.setFilterExpression(expression.isEmpty() ? "Correlation Filter" : expression);
                        } else if (filter instanceof TrueRuleFilter) {
                            ruleInfo.setFilterExpression("1=1 (aceita todas as mensagens)");
                        } else if (filter instanceof FalseRuleFilter) {
                            ruleInfo.setFilterExpression("1=0 (rejeita todas as mensagens)");
                        } else {
                            ruleInfo.setFilterExpression(filterType);
                        }
                    }
                    
                    // Ação da rule (opcional)
                    RuleAction action = ruleProperties.getAction();
                    if (action instanceof SqlRuleAction) {
                        SqlRuleAction sqlAction = (SqlRuleAction) action;
                        ruleInfo.setActionExpression(sqlAction.getSqlExpression());
                    }
                    
                    rules.add(ruleInfo);
                });
                
                rules.sort((r1, r2) -> {
                    // $Default sempre primeiro
                    if (r1.getName().equals("$Default")) return -1;
                    if (r2.getName().equals("$Default")) return 1;
                    return r1.getName().compareToIgnoreCase(r2.getName());
                });
                
                logMessage(String.format("Carregadas %d rules da subscription '%s/%s'", 
                    rules.size(), topicName, subscriptionName));
                return FXCollections.observableArrayList(rules);
                
            } catch (Exception e) {
                logError(String.format("Erro ao listar rules da subscription '%s/%s'", 
                    topicName, subscriptionName), e);
                throw new RuntimeException("Erro ao listar rules", e);
            }
        }, executorService);
    }
    
    /**
     * Cria uma rule com SQL Filter
     */
    public CompletableFuture<Boolean> createSqlRuleAsync(
            String topicName, String subscriptionName, String ruleName, String sqlExpression) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                CreateRuleOptions ruleOptions = new CreateRuleOptions();
                ruleOptions.setFilter(new SqlRuleFilter(sqlExpression));
                
                adminClient.createRule(topicName, subscriptionName, ruleName, ruleOptions);
                
                logMessage(String.format("Rule '%s' criada com SQL Filter na subscription '%s/%s': %s", 
                    ruleName, topicName, subscriptionName, sqlExpression));
                return true;
                
            } catch (com.azure.core.exception.ResourceExistsException e) {
                logMessage(String.format("Rule '%s' já existe na subscription '%s/%s'", 
                    ruleName, topicName, subscriptionName));
                return false;
                
            } catch (Exception e) {
                logError(String.format("Erro ao criar rule '%s' na subscription '%s/%s'", 
                    ruleName, topicName, subscriptionName), e);
                throw new RuntimeException("Erro ao criar rule", e);
            }
        }, executorService);
    }
    
    /**
     * Cria uma rule com Correlation Filter
     */
    public CompletableFuture<Boolean> createCorrelationRuleAsync(
            String topicName, String subscriptionName, String ruleName,
            String correlationId, String messageId, String sessionId,
            String replyTo, String label, String contentType) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                CorrelationRuleFilter correlationFilter = new CorrelationRuleFilter();
                
                // Definir propriedades apenas se não forem vazias
                if (correlationId != null && !correlationId.isEmpty()) {
                    correlationFilter.setCorrelationId(correlationId);
                }
                if (messageId != null && !messageId.isEmpty()) {
                    correlationFilter.setMessageId(messageId);
                }
                if (sessionId != null && !sessionId.isEmpty()) {
                    correlationFilter.setSessionId(sessionId);
                }
                if (replyTo != null && !replyTo.isEmpty()) {
                    correlationFilter.setReplyTo(replyTo);
                }
                if (label != null && !label.isEmpty()) {
                    correlationFilter.setLabel(label);
                }
                if (contentType != null && !contentType.isEmpty()) {
                    correlationFilter.setContentType(contentType);
                }
                
                CreateRuleOptions ruleOptions = new CreateRuleOptions();
                ruleOptions.setFilter(correlationFilter);
                
                adminClient.createRule(topicName, subscriptionName, ruleName, ruleOptions);
                
                logMessage(String.format("Rule '%s' criada com Correlation Filter na subscription '%s/%s'", 
                    ruleName, topicName, subscriptionName));
                return true;
                
            } catch (com.azure.core.exception.ResourceExistsException e) {
                logMessage(String.format("Rule '%s' já existe na subscription '%s/%s'", 
                    ruleName, topicName, subscriptionName));
                return false;
                
            } catch (Exception e) {
                logError(String.format("Erro ao criar rule '%s' na subscription '%s/%s'", 
                    ruleName, topicName, subscriptionName), e);
                throw new RuntimeException("Erro ao criar rule", e);
            }
        }, executorService);
    }
    
    /**
     * Remove uma rule de uma subscription
     */
    public CompletableFuture<Boolean> deleteRuleAsync(String topicName, String subscriptionName, String ruleName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("Não conectado ao Service Bus");
            }
            
            try {
                adminClient.deleteRule(topicName, subscriptionName, ruleName);
                
                logMessage(String.format("Rule '%s' removida da subscription '%s/%s' com sucesso", 
                    ruleName, topicName, subscriptionName));
                return true;
                
            } catch (Exception e) {
                logError(String.format("Erro ao remover rule '%s' da subscription '%s/%s'", 
                    ruleName, topicName, subscriptionName), e);
                throw new RuntimeException("Erro ao remover rule", e);
            }
        }, executorService);
    }
    
    /**
     * Encerra o serviço e libera recursos
     */
    public void shutdown() {
        try {
            disconnect();
            executorService.shutdown();
            logMessage("Serviço ServiceBus encerrado");
        } catch (Exception e) {
            logger.error("Erro ao encerrar serviço", e);
        }
    }
}
