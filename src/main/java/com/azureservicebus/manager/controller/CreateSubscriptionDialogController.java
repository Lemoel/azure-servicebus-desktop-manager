package com.azureservicebus.manager.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller para o diálogo de criação de subscription com filtro
 */
public class CreateSubscriptionDialogController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateSubscriptionDialogController.class);
    
    @FXML private TextField subscriptionNameField;
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
    @FXML private Label infoLabel;
    
    private DialogPane dialogPane;
    private String topicName;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando CreateSubscriptionDialogController");
        
        setupFilterTypeComboBox();
        setupEventHandlers();
        
        logger.info("CreateSubscriptionDialogController inicializado com sucesso");
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
            
            // Atualizar mensagem informativa
            if (newVal) {
                infoLabel.setText("Subscription será criada com rule '$Default' customizada com o filtro configurado");
            } else {
                infoLabel.setText("Subscription será criada com rule padrão '$Default' (aceita todas as mensagens)");
            }
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
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
