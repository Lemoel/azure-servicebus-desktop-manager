package com.azureservicebus.manager.controller;

import com.azureservicebus.manager.model.RuleInfo;
import com.azureservicebus.manager.model.SubscriptionInfo;
import com.azureservicebus.manager.service.ServiceBusService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

/**
 * Controller para o diálogo de detalhes da subscription
 */
public class SubscriptionDetailsDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionDetailsDialogController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    // Referências
    private ServiceBusService serviceBusService;
    private String topicName;
    private String subscriptionName;
    
    // FXML Components - Header
    @FXML private Label subscriptionNameLabel;
    
    // FXML Components - Tab Propriedades
    @FXML private Label nameLabel;
    @FXML private Label topicLabel;
    @FXML private Label statusLabel;
    @FXML private Label maxDeliveryLabel;
    @FXML private Label lockDurationLabel;
    @FXML private Label defaultTTLLabel;
    @FXML private Label autoDeleteLabel;
    @FXML private Label sessionLabel;
    @FXML private Label deadLetterExpirationLabel;
    @FXML private Label deadLetterFilterLabel;
    @FXML private Label batchedOpsLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label updatedAtLabel;
    @FXML private Label accessedAtLabel;
    
    // FXML Components - Tab Métricas
    @FXML private Label totalMessagesLabel;
    @FXML private Label activeMessagesLabel;
    @FXML private Label deadLetterMessagesLabel;
    @FXML private Label scheduledMessagesLabel;
    @FXML private Label transferMessagesLabel;
    @FXML private Label transferDLQLabel;
    @FXML private Label summaryLabel;
    
    // FXML Components - Tab Rules
    @FXML private Label rulesCountLabel;
    @FXML private TableView<RuleInfo> rulesTable;
    @FXML private TableColumn<RuleInfo, String> ruleNameColumn;
    @FXML private TableColumn<RuleInfo, String> ruleTypeColumn;
    @FXML private TableColumn<RuleInfo, String> ruleExpressionColumn;
    
    @FXML
    private void initialize() {
        logger.info("Inicializando SubscriptionDetailsDialogController");
        
        // Configurar colunas da tabela de rules
        ruleNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ruleTypeColumn.setCellValueFactory(new PropertyValueFactory<>("filterType"));
        ruleExpressionColumn.setCellValueFactory(new PropertyValueFactory<>("filterExpression"));
        
        // Permitir quebra de linha na coluna de expressão com altura dinâmica
        ruleExpressionColumn.setCellFactory(tc -> {
            return new TableCell<RuleInfo, String>() {
                private final javafx.scene.text.Text text = new javafx.scene.text.Text();
                
                {
                    text.wrappingWidthProperty().bind(ruleExpressionColumn.widthProperty().subtract(10));
                    setGraphic(text);
                }
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        text.setText(null);
                        setTooltip(null);
                        setGraphic(null);
                    } else {
                        text.setText(item);
                        setGraphic(text);
                        
                        // Adicionar Tooltip para visualização completa
                        Tooltip tooltip = new Tooltip(item);
                        tooltip.setWrapText(true);
                        tooltip.setMaxWidth(600);
                        setTooltip(tooltip);
                    }
                }
            };
        });
        
        logger.info("SubscriptionDetailsDialogController inicializado com sucesso");
    }
    
    /**
     * Define a subscription e carrega seus detalhes
     */
    public void setSubscription(String topicName, String subscriptionName, ServiceBusService serviceBusService) {
        this.topicName = topicName;
        this.subscriptionName = subscriptionName;
        this.serviceBusService = serviceBusService;
        
        // Atualizar label do título
        subscriptionNameLabel.setText(subscriptionName);
        
        // Carregar detalhes da subscription
        loadSubscriptionDetails();
        
        // Carregar rules
        loadRules();
    }
    
    /**
     * Carrega os detalhes completos da subscription
     */
    private void loadSubscriptionDetails() {
        serviceBusService.getSubscriptionDetailsAsync(topicName, subscriptionName)
            .thenAccept(subscriptionInfo -> {
                Platform.runLater(() -> updateUI(subscriptionInfo));
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    logger.error("Erro ao carregar detalhes da subscription", ex);
                    showError("Erro ao carregar detalhes: " + ex.getMessage());
                });
                return null;
            });
    }
    
    /**
     * Carrega as rules da subscription
     */
    private void loadRules() {
        serviceBusService.listRulesAsync(topicName, subscriptionName)
            .thenAccept(rules -> {
                Platform.runLater(() -> updateRulesTable(rules));
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    logger.error("Erro ao carregar rules", ex);
                    rulesCountLabel.setText("Erro ao carregar rules");
                });
                return null;
            });
    }
    
    /**
     * Atualiza a UI com os detalhes da subscription
     */
    private void updateUI(SubscriptionInfo info) {
        // Tab Propriedades - Informações Básicas
        nameLabel.setText(info.getName());
        topicLabel.setText(info.getTopicName());
        statusLabel.setText(formatStatus(info.getStatus()));
        
        // Tab Propriedades - Configurações de Entrega
        maxDeliveryLabel.setText(String.valueOf(info.getMaxDeliveryCount()));
        lockDurationLabel.setText(formatDuration(info.getLockDuration()));
        defaultTTLLabel.setText(formatDuration(info.getDefaultMessageTimeToLive()));
        autoDeleteLabel.setText(formatDuration(info.getAutoDeleteOnIdle()));
        
        // Tab Propriedades - Funcionalidades
        sessionLabel.setText(formatBoolean(info.isSessionRequired()));
        deadLetterExpirationLabel.setText(formatBoolean(info.isDeadLetteringOnMessageExpiration()));
        deadLetterFilterLabel.setText(formatBoolean(info.isDeadLetteringOnFilterEvaluationException()));
        batchedOpsLabel.setText(formatBoolean(info.isBatchedOperationsEnabled()));
        
        // Tab Propriedades - Timestamps
        createdAtLabel.setText(info.getCreatedAt() != null ? 
            info.getCreatedAt().format(DATE_FORMATTER) : "N/A");
        updatedAtLabel.setText(info.getUpdatedAt() != null ? 
            info.getUpdatedAt().format(DATE_FORMATTER) : "N/A");
        accessedAtLabel.setText(info.getAccessedAt() != null ? 
            info.getAccessedAt().format(DATE_FORMATTER) : "N/A");
        
        // Tab Métricas
        totalMessagesLabel.setText(String.format("%,d", info.getTotalMessages()));
        activeMessagesLabel.setText(String.format("%,d", info.getActiveMessages()));
        deadLetterMessagesLabel.setText(String.format("%,d", info.getDeadLetterMessages()));
        scheduledMessagesLabel.setText(String.format("%,d", info.getScheduledMessages()));
        transferMessagesLabel.setText(String.format("%,d", info.getTransferMessageCount()));
        transferDLQLabel.setText(String.format("%,d", info.getTransferDeadLetterMessageCount()));
        
        // Resumo do Status
        updateSummary(info);
    }
    
    /**
     * Atualiza a tabela de rules
     */
    private void updateRulesTable(ObservableList<RuleInfo> rules) {
        rulesTable.setItems(rules);
        rulesCountLabel.setText(rules.size() + (rules.size() == 1 ? " rule" : " rules"));
    }
    
    /**
     * Atualiza o resumo do status com base nas métricas
     */
    private void updateSummary(SubscriptionInfo info) {
        StringBuilder summary = new StringBuilder();
        
        long total = info.getTotalMessages();
        long active = info.getActiveMessages();
        long dead = info.getDeadLetterMessages();
        
        if (total == 0) {
            summary.append("✅ Subscription sem mensagens pendentes. Tudo limpo!");
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
            
            if (info.getTransferMessageCount() > 0) {
                summary.append(String.format("📤 Transferência: %,d\n", info.getTransferMessageCount()));
            }
        }
        
        summaryLabel.setText(summary.toString().trim());
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
     * Formata Duration para exibição legível
     */
    private String formatDuration(java.time.Duration duration) {
        if (duration == null) return "N/A";
        
        // Verificar se é "infinito" (valor muito grande)
        if (duration.toDays() > 10000000) {
            return "♾️ Infinito";
        }
        
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        
        StringBuilder sb = new StringBuilder();
        
        if (days > 0) {
            sb.append(days).append(days == 1 ? " dia" : " dias");
            if (hours > 0 || minutes > 0) sb.append(", ");
        }
        
        if (hours > 0) {
            sb.append(hours).append(hours == 1 ? " hora" : " horas");
            if (minutes > 0) sb.append(", ");
        }
        
        if (minutes > 0 || (days == 0 && hours == 0)) {
            sb.append(minutes).append(minutes == 1 ? " minuto" : " minutos");
        }
        
        if (days == 0 && hours == 0 && minutes == 0 && seconds > 0) {
            sb.append(seconds).append(seconds == 1 ? " segundo" : " segundos");
        }
        
        // Adicionar representação ISO entre parênteses
        sb.append(" (").append(duration.toString()).append(")");
        
        return sb.toString();
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
