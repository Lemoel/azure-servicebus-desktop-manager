package com.azureservicebus.manager.controller;

import com.azureservicebus.manager.model.SubscriptionConfiguration;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller para o diálogo de criação de subscription com configurações avançadas
 */
public class CreateSubscriptionDialogController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateSubscriptionDialogController.class);
    
    // Campos básicos
    @FXML private TextField subscriptionNameField;
    @FXML private TabPane configTabPane;
    @FXML private Label infoLabel;
    
    // Aba: Entrega
    @FXML private Spinner<Integer> maxDeliveryCountSpinner;
    @FXML private Spinner<Integer> lockDurationSpinner;
    @FXML private Spinner<Integer> messageTtlSpinner;
    @FXML private CheckBox batchedOperationsCheckBox;
    
    // Aba: Dead Letter
    @FXML private CheckBox deadLetterOnExpirationCheckBox;
    @FXML private CheckBox deadLetterOnFilterExceptionCheckBox;
    
    // Aba: Avançado
    @FXML private CheckBox requiresSessionCheckBox;
    @FXML private CheckBox autoDeleteCheckBox;
    @FXML private HBox autoDeleteBox;
    @FXML private Spinner<Integer> autoDeleteHoursSpinner;
    @FXML private TextArea userMetadataTextArea;
    
    // Aba: Encaminhamento
    @FXML private CheckBox forwardToCheckBox;
    @FXML private TextField forwardToField;
    @FXML private CheckBox forwardDeadLetterCheckBox;
    @FXML private TextField forwardDeadLetterField;
    
    // Aba: Filtros
    @FXML private CheckBox enableFilterCheckBox;
    @FXML private VBox filterSection;
    @FXML private ComboBox<String> filterTypeComboBox;
    @FXML private VBox sqlFilterSection;
    @FXML private TextArea sqlExpressionTextArea;
    @FXML private VBox correlationFilterSection;
    @FXML private TextField correlationIdField;
    @FXML private TextField messageIdField;
    @FXML private TextField sessionIdField;
    @FXML private TextField replyToField;
    @FXML private TextField labelField;
    @FXML private TextField contentTypeField;
    
    private DialogPane dialogPane;
    private String topicName;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando CreateSubscriptionDialogController");
        
        setupSpinners();
        setupFilterTypeComboBox();
        setupEventHandlers();
        
        logger.info("CreateSubscriptionDialogController inicializado com sucesso");
    }
    
    /**
     * Configura os Spinners com valores padrão
     */
    private void setupSpinners() {
        // Max Delivery Count: 1 a 1000, valor inicial 10
        maxDeliveryCountSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 10)
        );
        
        // Lock Duration: 0 a 5 minutos, valor inicial 1
        lockDurationSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 1)
        );
        
        // Message TTL: 1 a 36500 dias, SEM valor inicial (vazio = infinito)
        messageTtlSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 36500, 1)
        );
        // Limpar o valor inicial para começar vazio
        messageTtlSpinner.getValueFactory().setValue(null);
        messageTtlSpinner.setEditable(true);
        
        // Auto Delete Hours: 1 a 8760 (1 ano), valor inicial 1
        autoDeleteHoursSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8760, 1)
        );
    }
    
    /**
     * Define o DialogPane para configurar validação do botão OK
     */
    public void setDialogPane(DialogPane dialogPane) {
        this.dialogPane = dialogPane;
        setupOkButtonValidation();
    }
    
    /**
     * Define o tópico onde a subscription será criada
     */
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
    
    /**
     * Define o nome inicial da subscription (opcional)
     */
    public void setInitialSubscriptionName(String name) {
        if (name != null && !name.isEmpty()) {
            subscriptionNameField.setText(name);
        }
    }
    
    private void setupFilterTypeComboBox() {
        filterTypeComboBox.setItems(FXCollections.observableArrayList(
            "SQL Filter",
            "Correlation Filter"
        ));
        
        // Listener para mostrar/ocultar seções baseado no tipo de filtro
        filterTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                boolean isSql = newVal.equals("SQL Filter");
                
                sqlFilterSection.setVisible(isSql);
                sqlFilterSection.setManaged(isSql);
                
                correlationFilterSection.setVisible(!isSql);
                correlationFilterSection.setManaged(!isSql);
            }
        });
    }
    
    private void setupEventHandlers() {
        // Listener para checkbox de habilitar filtro
        enableFilterCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            filterSection.setVisible(newVal);
            filterSection.setManaged(newVal);
        });
        
        // Listener para auto-delete
        autoDeleteCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            autoDeleteBox.setVisible(newVal);
            autoDeleteBox.setManaged(newVal);
        });
        
        // Listener para forward to
        forwardToCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            forwardToField.setVisible(newVal);
            forwardToField.setManaged(newVal);
        });
        
        // Listener para forward dead letter
        forwardDeadLetterCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            forwardDeadLetterField.setVisible(newVal);
            forwardDeadLetterField.setManaged(newVal);
        });
    }
    
    private void setupOkButtonValidation() {
        if (dialogPane == null) {
            return;
        }
        
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if (!validateInput()) {
                    event.consume(); // Prevenir fechamento do diálogo
                }
            });
        }
    }
    
    /**
     * Valida os dados inseridos
     */
    private boolean validateInput() {
        String subscriptionName = subscriptionNameField.getText().trim();
        
        // Validar nome da subscription
        if (subscriptionName.isEmpty()) {
            showAlert("Erro de Validação", "Digite um nome para a subscription", Alert.AlertType.ERROR);
            return false;
        }
        
        // Validar limite de 50 caracteres do Azure
        if (subscriptionName.length() > 50) {
            showAlert("Erro de Validação", 
                "O nome da subscription não pode ter mais de 50 caracteres.\n" +
                "O Azure Service Bus limita o nome a 50 caracteres.\n" +
                "Tamanho atual: " + subscriptionName.length() + " caracteres", 
                Alert.AlertType.ERROR);
            return false;
        }
        
        // Validar encaminhamento
        if (forwardToCheckBox.isSelected()) {
            String forwardTo = forwardToField.getText().trim();
            if (forwardTo.isEmpty()) {
                showAlert("Erro de Validação", 
                    "Digite o destino para encaminhamento de mensagens", 
                    Alert.AlertType.ERROR);
                return false;
            }
        }
        
        if (forwardDeadLetterCheckBox.isSelected()) {
            String forwardDeadLetter = forwardDeadLetterField.getText().trim();
            if (forwardDeadLetter.isEmpty()) {
                showAlert("Erro de Validação", 
                    "Digite o destino para encaminhamento de mensagens DLQ", 
                    Alert.AlertType.ERROR);
                return false;
            }
        }
        
        // Se filtro habilitado, validar campos do filtro
        if (enableFilterCheckBox.isSelected()) {
            String filterType = filterTypeComboBox.getValue();
            
            if (filterType == null) {
                showAlert("Erro de Validação", "Selecione o tipo de filtro", Alert.AlertType.ERROR);
                return false;
            }
            
            if (filterType.equals("SQL Filter")) {
                String sqlExpression = sqlExpressionTextArea.getText().trim();
                if (sqlExpression.isEmpty()) {
                    showAlert("Erro de Validação", "Digite a expressão SQL do filtro", Alert.AlertType.ERROR);
                    return false;
                }
            } else {
                // Para Correlation Filter, pelo menos um campo deve estar preenchido
                boolean hasAtLeastOne = !correlationIdField.getText().trim().isEmpty() ||
                                       !messageIdField.getText().trim().isEmpty() ||
                                       !sessionIdField.getText().trim().isEmpty() ||
                                       !replyToField.getText().trim().isEmpty() ||
                                       !labelField.getText().trim().isEmpty() ||
                                       !contentTypeField.getText().trim().isEmpty();
                
                if (!hasAtLeastOne) {
                    showAlert("Erro de Validação", 
                        "Para Correlation Filter, preencha pelo menos um campo", 
                        Alert.AlertType.ERROR);
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Retorna a configuração completa da subscription
     */
    public SubscriptionConfiguration getConfiguration() {
        SubscriptionConfiguration config = new SubscriptionConfiguration();
        
        // Básico
        config.setName(subscriptionNameField.getText().trim());
        config.setTopicName(topicName);
        
        // Entrega
        config.setMaxDeliveryCount(maxDeliveryCountSpinner.getValue());
        config.setLockDurationMinutes(lockDurationSpinner.getValue());
        
        // TTL: Só setar se o usuário tiver preenchido algo
        Integer ttlValue = messageTtlSpinner.getValue();
        config.setDefaultMessageTimeToLiveDays(ttlValue); // null = infinito
        
        config.setEnableBatchedOperations(batchedOperationsCheckBox.isSelected());
        
        // Dead Letter
        config.setDeadLetteringOnMessageExpiration(deadLetterOnExpirationCheckBox.isSelected());
        config.setDeadLetteringOnFilterEvaluationExceptions(deadLetterOnFilterExceptionCheckBox.isSelected());
        
        // Avançado
        config.setRequiresSession(requiresSessionCheckBox.isSelected());
        config.setEnableAutoDeleteOnIdle(autoDeleteCheckBox.isSelected());
        if (autoDeleteCheckBox.isSelected()) {
            config.setAutoDeleteOnIdleHours(autoDeleteHoursSpinner.getValue());
        }
        config.setUserMetadata(userMetadataTextArea.getText().trim());
        
        // Encaminhamento
        config.setEnableForwardTo(forwardToCheckBox.isSelected());
        if (forwardToCheckBox.isSelected()) {
            config.setForwardTo(forwardToField.getText().trim());
        }
        config.setEnableForwardDeadLetteredMessagesTo(forwardDeadLetterCheckBox.isSelected());
        if (forwardDeadLetterCheckBox.isSelected()) {
            config.setForwardDeadLetteredMessagesTo(forwardDeadLetterField.getText().trim());
        }
        
        // Filtros
        config.setFilterEnabled(enableFilterCheckBox.isSelected());
        if (enableFilterCheckBox.isSelected()) {
            config.setFilterType(filterTypeComboBox.getValue());
            
            if ("SQL Filter".equals(filterTypeComboBox.getValue())) {
                config.setSqlExpression(sqlExpressionTextArea.getText().trim());
            } else {
                config.setCorrelationId(correlationIdField.getText().trim());
                config.setMessageId(messageIdField.getText().trim());
                config.setSessionId(sessionIdField.getText().trim());
                config.setReplyTo(replyToField.getText().trim());
                config.setLabel(labelField.getText().trim());
                config.setContentType(contentTypeField.getText().trim());
            }
        }
        
        return config;
    }
    
    // ===== Métodos de compatibilidade com código existente =====
    
    /**
     * Retorna o nome da subscription
     */
    public String getSubscriptionName() {
        return subscriptionNameField.getText().trim();
    }
    
    /**
     * Verifica se o filtro está habilitado
     */
    public boolean isFilterEnabled() {
        return enableFilterCheckBox.isSelected();
    }
    
    /**
     * Retorna o tipo de filtro selecionado
     */
    public String getFilterType() {
        return filterTypeComboBox.getValue();
    }
    
    /**
     * Retorna a expressão SQL (se SQL Filter)
     */
    public String getSqlExpression() {
        return sqlExpressionTextArea.getText().trim();
    }
    
    /**
     * Retorna o Correlation ID
     */
    public String getCorrelationId() {
        return correlationIdField.getText().trim();
    }
    
    /**
     * Retorna o Message ID
     */
    public String getMessageId() {
        return messageIdField.getText().trim();
    }
    
    /**
     * Retorna o Session ID
     */
    public String getSessionId() {
        return sessionIdField.getText().trim();
    }
    
    /**
     * Retorna o Reply To
     */
    public String getReplyTo() {
        return replyToField.getText().trim();
    }
    
    /**
     * Retorna o Label
     */
    public String getLabel() {
        return labelField.getText().trim();
    }
    
    /**
     * Retorna o Content Type
     */
    public String getContentType() {
        return contentTypeField.getText().trim();
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        // Garantir que alert abra no mesmo monitor do dialog pai
        if (dialogPane != null && dialogPane.getScene() != null && dialogPane.getScene().getWindow() != null) {
            alert.initOwner(dialogPane.getScene().getWindow());
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
