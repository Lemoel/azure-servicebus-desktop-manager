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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
     * Cria uma nova fila
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
            // Tentar obter o corpo como string primeiro (funciona para STRING e BINARY)
            return message.getBody().toString();
            
        } catch (UnsupportedOperationException e) {
            // Se falhar, é provavelmente um tipo VALUE
            try {
                // Tentar obter como bytes e converter para string
                byte[] bodyBytes = message.getBody().toBytes();
                if (bodyBytes != null && bodyBytes.length > 0) {
                    return new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
                } else {
                    return "[Mensagem vazia]";
                }
                
            } catch (Exception ex) {
                // Se ainda assim falhar, mostrar informação sobre o tipo
                String bodyType = "DESCONHECIDO";
                try {
                    // Tentar determinar o tipo através de reflexão ou outras formas
                    bodyType = "VALUE (tipo serializado)";
                } catch (Exception ignored) {
                    // Ignorar erros na determinação do tipo
                }
                
                return String.format("[Tipo de corpo não suportado: %s - Tamanho: %d bytes]", 
                    bodyType, 
                    message.getBody() != null ? message.getBody().toBytes().length : 0);
            }
        } catch (Exception e) {
            // Fallback para qualquer outro erro
            logger.warn("Erro ao extrair corpo da mensagem: " + e.getMessage());
            return String.format("[Erro ao ler mensagem: %s]", e.getMessage());
        }
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
