package com.azureservicebus.manager.controller;

import com.azureservicebus.manager.model.QueueInfo;
import com.azureservicebus.manager.service.ServiceBusService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

/**
 * Controller para o diálogo de detalhes da fila
 */
public class QueueDetailsDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(QueueDetailsDialogController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    // Referências
    private ServiceBusService serviceBusService;
    private String queueName;
    
    // FXML Components - Header
    @FXML private Label queueNameLabel;
    
    // FXML Components - Tab Propriedades
    @FXML private Label nameLabel;
    @FXML private Label statusLabel;
    @FXML private Label sizeLabel;
    @FXML private Label maxDeliveryLabel;
    @FXML private Label lockDurationLabel;
    @FXML private Label defaultTTLLabel;
    @FXML private Label maxSizeLabel;
    @FXML private Label deadLetterExpirationLabel;
    @FXML private Label batchedOpsLabel;
    @FXML private Label duplicateDetectionLabel;
    @FXML private Label sessionLabel;
    @FXML private Label partitioningLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label updatedAtLabel;
    
    // FXML Components - Tab Métricas
    @FXML private Label totalMessagesLabel;
    @FXML private Label activeMessagesLabel;
    @FXML private Label deadLetterMessagesLabel;
    @FXML private Label scheduledMessagesLabel;
    @FXML private Label summaryLabel;
    
    // FXML Components - Tab Informações
    @FXML private Label queuePathLabel;
    @FXML private Label dlqPathLabel;
    @FXML private Label transferDlqPathLabel;
    @FXML private Label warningLabel;
    
    @FXML
    private void initialize() {
        logger.info("Inicializando QueueDetailsDialogController");
        logger.info("QueueDetailsDialogController inicializado com sucesso");
    }
    
    /**
     * Define a fila e carrega seus detalhes
     */
    public void setQueue(String queueName, ServiceBusService serviceBusService) {
        this.queueName = queueName;
        this.serviceBusService = serviceBusService;
        
        // Atualizar label do título
        queueNameLabel.setText(queueName);
        
        // Carregar detalhes da fila
        loadQueueDetails();
    }
    
    /**
     * Carrega os detalhes completos da fila
     */
    private void loadQueueDetails() {
        serviceBusService.getQueueDetailsAsync(queueName)
            .thenAccept(queueInfo -> {
                Platform.runLater(() -> updateUI(queueInfo));
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    logger.error("Erro ao carregar detalhes da fila", ex);
                    showError("Erro ao carregar detalhes: " + ex.getMessage());
                });
                return null;
            });
    }
    
    /**
     * Atualiza a UI com os detalhes da fila
     */
    private void updateUI(QueueInfo info) {
        // Tab Propriedades - Informações Básicas
        nameLabel.setText(info.getName());
        statusLabel.setText(formatStatus(info.getStatus()));
        sizeLabel.setText(formatSize(info.getSizeInKB()));
        
        // Tab Propriedades - Configurações de Entrega
        maxDeliveryLabel.setText(String.valueOf(info.getMaxDeliveryCount()));
        lockDurationLabel.setText(info.getLockDuration() != null ? info.getLockDuration() : "N/A");
        defaultTTLLabel.setText(info.getDefaultMessageTimeToLive() != null ? info.getDefaultMessageTimeToLive() : "N/A");
        maxSizeLabel.setText(String.format("%,d MB", info.getMaxSizeInMB()));
        
        // Tab Propriedades - Funcionalidades
        deadLetterExpirationLabel.setText(formatBoolean(info.isDeadLetteringOnMessageExpiration()));
        batchedOpsLabel.setText(formatBoolean(info.isBatchedOperationsEnabled()));
        duplicateDetectionLabel.setText(formatBoolean(info.isDuplicateDetectionEnabled()));
        sessionLabel.setText(formatBoolean(info.isSessionRequired()));
        partitioningLabel.setText(formatBoolean(info.isPartitioningEnabled()));
        
        // Tab Propriedades - Timestamps
        createdAtLabel.setText(info.getCreatedAt() != null ? 
            info.getCreatedAt().format(DATE_FORMATTER) : "N/A");
        updatedAtLabel.setText(info.getUpdatedAt() != null ? 
            info.getUpdatedAt().format(DATE_FORMATTER) : "N/A");
        
        // Tab Métricas
        totalMessagesLabel.setText(String.format("%,d", info.getTotalMessages()));
        activeMessagesLabel.setText(String.format("%,d", info.getActiveMessages()));
        deadLetterMessagesLabel.setText(String.format("%,d", info.getDeadLetterMessages()));
        scheduledMessagesLabel.setText(String.format("%,d", info.getScheduledMessages()));
        
        // Resumo do Status
        updateSummary(info);
        
        // Tab Informações - Caminhos
        queuePathLabel.setText(info.getName());
        dlqPathLabel.setText(info.getName() + "/$deadletterqueue");
        transferDlqPathLabel.setText(info.getName() + "/$transferdeadletterqueue");
        
        // Alertas
        updateWarnings(info);
    }
    
    /**
     * Atualiza o resumo do status com base nas métricas
     */
    private void updateSummary(QueueInfo info) {
        StringBuilder summary = new StringBuilder();
        
        long total = info.getTotalMessages();
        long active = info.getActiveMessages();
        long dead = info.getDeadLetterMessages();
        
        if (total == 0) {
            summary.append("✅ Fila sem mensagens pendentes. Tudo limpo!");
        } else {
            summary.append(String.format("📊 Total: %,d mensagens\n", total));
            
            if (active > 0) {
                double activePercent = (active * 100.0) / total;
                summary.append(String.format("✅ Ativas: %,d (%.1f%%)\n", active, activePercent));
            }
            
            if (dead > 0) {
                double deadPercent = (dead * 100.0) / total;
                summary.append(String.format("⚠️ Dead Letter: %,d (%.1f%%) - Requer atenção!\n", dead, deadPercent));
            }
            
            if (info.getScheduledMessages() > 0) {
                summary.append(String.format("⏰ Agendadas: %,d\n", info.getScheduledMessages()));
            }
        }
        
        summaryLabel.setText(summary.toString().trim());
    }
    
    /**
     * Atualiza alertas com base na configuração da fila
     */
    private void updateWarnings(QueueInfo info) {
        StringBuilder warnings = new StringBuilder();
        
        // Verificar dead letter messages
        if (info.getDeadLetterMessages() > 0) {
            warnings.append(String.format("⚠️ Esta fila possui %,d mensagens na Dead Letter Queue que precisam de atenção!\n\n", 
                info.getDeadLetterMessages()));
        }
        
        // Verificar Max Delivery Count baixo
        if (info.getMaxDeliveryCount() < 5) {
            warnings.append("⚠️ Max Delivery Count está configurado com valor baixo (")
                    .append(info.getMaxDeliveryCount())
                    .append("). Mensagens com erros transitórios podem ir para DLQ prematuramente.\n\n");
        }
        
        // Verificar se sessões estão habilitadas
        if (info.isSessionRequired()) {
            warnings.append("ℹ️ Esta fila requer sessões. Certifique-se de que suas mensagens incluem um SessionId.\n\n");
        }
        
        // Verificar particionamento
        if (info.isPartitioningEnabled()) {
            warnings.append("ℹ️ Particionamento está habilitado. Isso melhora throughput mas desabilita transações e ordenação garantida.\n\n");
        }
        
        // Verificar se duplicate detection está desabilitado
        if (!info.isDuplicateDetectionEnabled()) {
            warnings.append("ℹ️ Detecção de duplicatas está desabilitada. Mensagens duplicadas não serão automaticamente filtradas.\n\n");
        }
        
        if (warnings.length() == 0) {
            warningLabel.setText("✅ Nenhum alerta. A configuração da fila está ok!");
        } else {
            warningLabel.setText(warnings.toString().trim());
        }
    }
    
    /**
     * Formata status com ícone
     */
    private String formatStatus(String status) {
        if (status == null) return "N/A";
        
        switch (status.toUpperCase()) {
            case "ACTIVE":
                return "✅ Active";
            case "DISABLED":
                return "❌ Disabled";
            case "CREATING":
                return "🔄 Creating";
            case "DELETING":
                return "🗑️ Deleting";
            default:
                return status;
        }
    }
    
    /**
     * Formata booleano com ícones
     */
    private String formatBoolean(boolean value) {
        return value ? "✅ Sim" : "❌ Não";
    }
    
    /**
     * Formata tamanho em KB para exibição legível
     */
    private String formatSize(double sizeInKB) {
        if (sizeInKB < 1) {
            return String.format("%.2f KB", sizeInKB);
        } else if (sizeInKB < 1024) {
            return String.format("%.2f KB", sizeInKB);
        } else if (sizeInKB < 1024 * 1024) {
            return String.format("%.2f MB", sizeInKB / 1024);
        } else {
            return String.format("%.2f GB", sizeInKB / (1024 * 1024));
        }
    }
    
    /**
     * Exibe mensagem de erro
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText("Erro ao carregar detalhes");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
