package com.azureservicebus.manager.controller;

import com.azureservicebus.manager.model.QueueConfiguration;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller para o diálogo de criação avançada de filas.
 * Gerencia a entrada de configurações customizadas do usuário.
 */
public class CreateQueueDialogController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateQueueDialogController.class);
    
    // Campos básicos
    @FXML private TextField queueNameField;
    @FXML private Label nameErrorLabel;
    
    // Configurações de entrega
    @FXML private Spinner<Integer> maxDeliveryCountSpinner;
    @FXML private Spinner<Integer> lockDurationSpinner;
    
    // Checkboxes
    @FXML private CheckBox deadLetterCheckBox;
    @FXML private CheckBox batchedOperationsCheckBox;
    @FXML private CheckBox requiresSessionCheckBox;
    @FXML private CheckBox partitioningCheckBox;
    @FXML private CheckBox duplicateDetectionCheckBox;
    
    // Duplicate Detection
    @FXML private javafx.scene.layout.VBox duplicateDetectionSection;
    @FXML private Spinner<Integer> duplicateDetectionWindowSpinner;
    @FXML private Separator duplicateDetectionSeparator;
    
    // Configurações adicionais
    @FXML private ComboBox<Integer> maxSizeComboBox;
    @FXML private Spinner<Integer> ttlSpinner;
    
    private QueueConfiguration configuration;
    private DialogPane dialogPane;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando CreateQueueDialogController");
        
        // Inicializar configuração com valores padrão
        configuration = new QueueConfiguration();
        
        // Configurar spinners
        setupSpinners();
        
        // Configurar ComboBox de tamanho
        setupMaxSizeComboBox();
        
        // Configurar Duplicate Detection
        setupDuplicateDetection();
        
        // Configurar validação
        setupValidation();
        
        logger.info("CreateQueueDialogController inicializado com sucesso");
    }
    
    /**
     * Configura os spinners com valores padrão e limites
     */
    private void setupSpinners() {
        // Max Delivery Count: 1 a 2000, padrão 10
        maxDeliveryCountSpinner.setValueFactory(
            new IntegerSpinnerValueFactory(1, 2000, 10, 1)
        );
        maxDeliveryCountSpinner.setEditable(true);
        
        // Lock Duration: 0 a 5 minutos, padrão 1
        lockDurationSpinner.setValueFactory(
            new IntegerSpinnerValueFactory(0, 5, 1, 1)
        );
        lockDurationSpinner.setEditable(true);
        
        // Default TTL: 1 a 36500 dias (100 anos), SEM valor inicial (null = infinito)
        IntegerSpinnerValueFactory ttlValueFactory = new IntegerSpinnerValueFactory(1, 36500, 1, 1);
        ttlValueFactory.setValue(null); // null = infinito (padrão Azure)
        ttlSpinner.setValueFactory(ttlValueFactory);
        ttlSpinner.setEditable(true);
    }
    
    /**
     * Configura a seção de Duplicate Detection
     */
    private void setupDuplicateDetection() {
        // Duplicate Detection Window: 1 a 10080 minutos (7 dias), padrão 10
        duplicateDetectionWindowSpinner.setValueFactory(
            new IntegerSpinnerValueFactory(1, 10080, 10, 1)
        );
        duplicateDetectionWindowSpinner.setEditable(true);
        
        // Listener para mostrar/ocultar seção baseado no checkbox
        duplicateDetectionCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            duplicateDetectionSection.setVisible(newVal);
            duplicateDetectionSection.setManaged(newVal);
            duplicateDetectionSeparator.setVisible(newVal);
            duplicateDetectionSeparator.setManaged(newVal);
            
            // Redimensionar o diálogo para acomodar o conteúdo
            if (dialogPane != null && dialogPane.getScene() != null && dialogPane.getScene().getWindow() != null) {
                javafx.application.Platform.runLater(() -> {
                    dialogPane.getScene().getWindow().sizeToScene();
                });
            }
        });
    }
    
    /**
     * Configura o ComboBox de tamanho máximo
     */
    private void setupMaxSizeComboBox() {
        maxSizeComboBox.setItems(FXCollections.observableArrayList(
            1024, 2048, 3072, 4096, 5120
        ));
        maxSizeComboBox.setValue(1024); // Padrão: 1GB
        
        // Formatar exibição
        maxSizeComboBox.setButtonCell(new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item + " MB (" + (item / 1024.0) + " GB)");
                }
            }
        });
        
        maxSizeComboBox.setCellFactory(param -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item + " MB (" + (item / 1024.0) + " GB)");
                }
            }
        });
    }
    
    /**
     * Configura validação em tempo real
     */
    private void setupValidation() {
        // Validação do nome da fila
        queueNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateQueueName(newVal);
        });
    }
    
    /**
     * Valida o nome da fila
     */
    private void validateQueueName(String name) {
        if (name == null || name.trim().isEmpty()) {
            nameErrorLabel.setText("Nome da fila é obrigatório");
            nameErrorLabel.setVisible(true);
            return;
        }
        
        // Validações do Azure Service Bus para nomes de fila
        if (name.length() > 260) {
            nameErrorLabel.setText("Nome não pode exceder 260 caracteres");
            nameErrorLabel.setVisible(true);
            return;
        }
        
        if (!name.matches("^[a-zA-Z0-9][a-zA-Z0-9._-]*$")) {
            nameErrorLabel.setText("Nome deve começar com letra/número e conter apenas letras, números, pontos, hífens ou underscores");
            nameErrorLabel.setVisible(true);
            return;
        }
        
        // Nome válido
        nameErrorLabel.setVisible(false);
    }
    
    /**
     * Define o DialogPane pai para controle dos botões
     */
    public void setDialogPane(DialogPane dialogPane) {
        this.dialogPane = dialogPane;
        
        // Adicionar validação ao botão OK
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if (!validateAndBuildConfiguration()) {
                    event.consume(); // Prevenir fechamento do diálogo
                }
            });
        }
    }
    
    /**
     * Valida todos os campos e constrói a configuração
     * @return true se válido, false caso contrário
     */
    private boolean validateAndBuildConfiguration() {
        String name = queueNameField.getText();
        
        // Validar nome
        if (name == null || name.trim().isEmpty()) {
            showError("Nome da fila é obrigatório");
            return false;
        }
        
        validateQueueName(name);
        if (nameErrorLabel.isVisible()) {
            showError("Nome da fila inválido: " + nameErrorLabel.getText());
            return false;
        }
        
        try {
            // Construir configuração
            configuration = new QueueConfiguration(name.trim());
            
            // Configurações de entrega
            configuration.setMaxDeliveryCount(maxDeliveryCountSpinner.getValue());
            configuration.setLockDurationMinutes(lockDurationSpinner.getValue());
            
            // Checkboxes
            configuration.setDeadLetteringOnMessageExpiration(deadLetterCheckBox.isSelected());
            configuration.setBatchedOperationsEnabled(batchedOperationsCheckBox.isSelected());
            configuration.setRequiresSession(requiresSessionCheckBox.isSelected());
            configuration.setPartitioningEnabled(partitioningCheckBox.isSelected());
            
            // Duplicate Detection
            configuration.setDuplicateDetectionEnabled(duplicateDetectionCheckBox.isSelected());
            if (duplicateDetectionCheckBox.isSelected()) {
                configuration.setDuplicateDetectionHistoryTimeWindowMinutes(duplicateDetectionWindowSpinner.getValue());
            }
            
            // Configurações adicionais
            configuration.setMaxSizeInMB(maxSizeComboBox.getValue());
            
            // TTL: Se null (vazio), deixar como null (infinito)
            // Se preenchido, converter horas para Duration
            Integer ttlValue = ttlSpinner.getValue();
            if (ttlValue != null) {
                configuration.setDefaultMessageTimeToLiveHours(ttlValue);
            } else {
                configuration.setDefaultMessageTimeToLive(null); // Infinito
            }
            
            // Validar configuração completa
            if (!configuration.isValid()) {
                String error = configuration.getValidationError();
                showError(error != null ? error : "Configuração inválida");
                return false;
            }
            
            logger.info("Configuração validada: {}", configuration);
            return true;
            
        } catch (Exception e) {
            logger.error("Erro ao validar configuração", e);
            showError("Erro ao validar configuração: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Exibe mensagem de erro
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro de Validação");
        alert.setHeaderText("Configuração Inválida");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Retorna a configuração construída (válida apenas se o diálogo foi confirmado)
     */
    public QueueConfiguration getConfiguration() {
        return configuration;
    }
    
    /**
     * Define o nome inicial da fila (opcional)
     */
    public void setInitialQueueName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            queueNameField.setText(name.trim());
        }
    }
    
    /**
     * Define valores iniciais da configuração (para edição)
     */
    public void setInitialConfiguration(QueueConfiguration config) {
        if (config == null) {
            return;
        }
        
        queueNameField.setText(config.getName());
        maxDeliveryCountSpinner.getValueFactory().setValue(config.getMaxDeliveryCount());
        lockDurationSpinner.getValueFactory().setValue(config.getLockDurationMinutes());
        deadLetterCheckBox.setSelected(config.isDeadLetteringOnMessageExpiration());
        batchedOperationsCheckBox.setSelected(config.isBatchedOperationsEnabled());
        requiresSessionCheckBox.setSelected(config.isRequiresSession());
        partitioningCheckBox.setSelected(config.isPartitioningEnabled());
        duplicateDetectionCheckBox.setSelected(config.isDuplicateDetectionEnabled());
        duplicateDetectionWindowSpinner.getValueFactory().setValue(config.getDuplicateDetectionHistoryTimeWindowMinutes());
        maxSizeComboBox.setValue((int) config.getMaxSizeInMB());
        
        // TTL: Se null, deixar vazio (infinito). Se definido, mostrar em horas
        if (config.hasCustomMessageTimeToLive()) {
            ttlSpinner.getValueFactory().setValue(config.getDefaultMessageTimeToLiveHours());
        } else {
            ttlSpinner.getValueFactory().setValue(null); // Infinito
        }
    }
}
