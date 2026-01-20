package com.azureservicebus.manager.controller;

import com.azureservicebus.manager.model.RuleInfo;
import com.azureservicebus.manager.service.ServiceBusService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller para o diálogo de gerenciamento de Rules de uma Subscription
 */
public class RulesDialogController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(RulesDialogController.class);
    
    // Informações da Subscription
    @FXML private Label topicNameLabel;
    @FXML private Label subscriptionNameLabel;
    
    // Tabela de Rules
    @FXML private TableView<RuleInfo> rulesTable;
    @FXML private TableColumn<RuleInfo, String> ruleNameColumn;
    @FXML private TableColumn<RuleInfo, String> ruleFilterTypeColumn;
    @FXML private TableColumn<RuleInfo, String> ruleFilterExpressionColumn;
    @FXML private TableColumn<RuleInfo, Void> ruleActionsColumn;
    @FXML private Button refreshRulesButton;
    @FXML private Label rulesCountLabel;
    
    // Formulário de Criação de Rule
    @FXML private TextField newRuleNameField;
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
    @FXML private Button createRuleButton;
    
    // Dados
    private String topicName;
    private String subscriptionName;
    private ServiceBusService serviceBusService;
    private ObservableList<RuleInfo> rules = FXCollections.observableArrayList();
    private DialogPane dialogPane;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando RulesDialogController");
        
        setupTableColumns();
        setupFilterTypeComboBox();
        setupEventHandlers();
        
        logger.info("RulesDialogController inicializado com sucesso");
    }
    
    /**
     * Configura o controller com as informações necessárias
     */
    public void setSubscriptionInfo(String topicName, String subscriptionName, ServiceBusService serviceBusService) {
        this.topicName = topicName;
        this.subscriptionName = subscriptionName;
        this.serviceBusService = serviceBusService;
        
        // Atualizar labels
        topicNameLabel.setText(topicName);
        subscriptionNameLabel.setText(subscriptionName);
        
        // Carregar rules
        loadRules();
    }
    
    /**
     * Define o DialogPane (necessário para configurar botões)
     */
    public void setDialogPane(DialogPane dialogPane) {
        this.dialogPane = dialogPane;
    }
    
    private void setupTableColumns() {
        rulesTable.setItems(rules);
        
        ruleNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        // Coluna de tipo de filtro - usar método formatado
        ruleFilterTypeColumn.setCellValueFactory(cellData -> 
            cellData.getValue().nameProperty().map(name -> cellData.getValue().getFilterTypeDescription())
        );
        
        // Coluna de expressão - usar método formatado
        ruleFilterExpressionColumn.setCellValueFactory(cellData -> 
            cellData.getValue().nameProperty().map(name -> cellData.getValue().getFormattedExpression())
        );
        
        setupActionsColumn();
    }
    
    private void setupActionsColumn() {
        ruleActionsColumn.setCellFactory(new Callback<TableColumn<RuleInfo, Void>, TableCell<RuleInfo, Void>>() {
            @Override
            public TableCell<RuleInfo, Void> call(TableColumn<RuleInfo, Void> param) {
                return new TableCell<RuleInfo, Void>() {
                    private final Button deleteButton = new Button("✖");
                    private final HBox actionBox = new HBox(5);
                    
                    {
                        deleteButton.setStyle(
                            "-fx-background-color: #dc3545; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 12px; " +
                            "-fx-padding: 3 6 3 6; " +
                            "-fx-cursor: hand;"
                        );
                        deleteButton.setTooltip(new Tooltip("Remover rule"));
                        
                        actionBox.setAlignment(Pos.CENTER);
                        actionBox.getChildren().add(deleteButton);
                        
                        deleteButton.setOnAction(event -> {
                            RuleInfo ruleInfo = getTableView().getItems().get(getIndex());
                            handleDeleteRule(ruleInfo);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            RuleInfo ruleInfo = getTableView().getItems().get(getIndex());
                            // Não permitir deletar a rule $Default
                            if (ruleInfo.getIsDefault()) {
                                setGraphic(null);
                            } else {
                                setGraphic(actionBox);
                            }
                        }
                    }
                };
            }
        });
    }
    
    private void setupFilterTypeComboBox() {
        filterTypeComboBox.setItems(FXCollections.observableArrayList(
            "SQL Filter",
            "Correlation Filter"
        ));
        
        // Listener para mostrar/ocultar seções
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
        refreshRulesButton.setOnAction(e -> loadRules());
        createRuleButton.setOnAction(e -> handleCreateRule());
    }
    
    private void loadRules() {
        if (serviceBusService == null || topicName == null || subscriptionName == null) {
            return;
        }
        
        refreshRulesButton.setDisable(true);
        refreshRulesButton.setText("Carregando...");
        
        Task<ObservableList<RuleInfo>> loadTask = new Task<ObservableList<RuleInfo>>() {
            @Override
            protected ObservableList<RuleInfo> call() throws Exception {
                return serviceBusService.listRulesAsync(topicName, subscriptionName).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    rules.setAll(getValue());
                    refreshRulesButton.setDisable(false);
                    refreshRulesButton.setText("🔄 Atualizar");
                    
                    int count = getValue().size();
                    rulesCountLabel.setText(count + (count == 1 ? " rule" : " rules"));
                    
                    logger.info("Carregadas {} rules da subscription '{}/{}'", count, topicName, subscriptionName);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    refreshRulesButton.setDisable(false);
                    refreshRulesButton.setText("🔄 Atualizar");
                    showAlert("Erro", "Erro ao carregar rules: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(loadTask).start();
    }
    
    private void handleCreateRule() {
        String ruleName = newRuleNameField.getText().trim();
        String filterType = filterTypeComboBox.getValue();
        
        // Validações
        if (ruleName.isEmpty()) {
            showAlert("Erro", "Digite um nome para a rule", Alert.AlertType.ERROR);
            return;
        }
        
        if (filterType == null) {
            showAlert("Erro", "Selecione o tipo de filtro", Alert.AlertType.ERROR);
            return;
        }
        
        createRuleButton.setDisable(true);
        
        Task<Boolean> createTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                if (filterType.equals("SQL Filter")) {
                    String sqlExpression = sqlExpressionTextArea.getText().trim();
                    if (sqlExpression.isEmpty()) {
                        throw new IllegalArgumentException("Digite a expressão SQL");
                    }
                    return serviceBusService.createSqlRuleAsync(topicName, subscriptionName, ruleName, sqlExpression).get();
                } else {
                    // Correlation Filter
                    return serviceBusService.createCorrelationRuleAsync(
                        topicName, 
                        subscriptionName, 
                        ruleName,
                        correlationIdField.getText().trim(),
                        messageIdField.getText().trim(),
                        sessionIdField.getText().trim(),
                        replyToField.getText().trim(),
                        labelField.getText().trim(),
                        contentTypeField.getText().trim()
                    ).get();
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    createRuleButton.setDisable(false);
                    
                    if (getValue()) {
                        showAlert("Sucesso", 
                            String.format("Rule '%s' criada com sucesso!", ruleName), 
                            Alert.AlertType.INFORMATION);
                        
                        // Limpar formulário
                        clearCreateForm();
                        
                        // Recarregar lista
                        loadRules();
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    createRuleButton.setDisable(false);
                    showAlert("Erro", "Erro ao criar rule: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(createTask).start();
    }
    
    private void handleDeleteRule(RuleInfo ruleInfo) {
        if (ruleInfo.getIsDefault()) {
            showAlert("Aviso", "Não é possível remover a rule '$Default'", Alert.AlertType.WARNING);
            return;
        }
        
        Optional<ButtonType> result = showConfirmation(
            "Confirmar Remoção",
            String.format("Tem certeza que deseja remover a rule '%s'?\n\nEsta operação é irreversível!", ruleInfo.getName())
        );
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> deleteTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return serviceBusService.deleteRuleAsync(topicName, subscriptionName, ruleInfo.getName()).get();
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        if (getValue()) {
                            showAlert("Sucesso", 
                                String.format("Rule '%s' removida com sucesso!", ruleInfo.getName()), 
                                Alert.AlertType.INFORMATION);
                            loadRules();
                        }
                    });
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        showAlert("Erro", "Erro ao remover rule: " + getException().getMessage(), Alert.AlertType.ERROR);
                    });
                }
            };
            
            new Thread(deleteTask).start();
        }
    }
    
    private void clearCreateForm() {
        newRuleNameField.clear();
        filterTypeComboBox.setValue(null);
        sqlExpressionTextArea.clear();
        correlationIdField.clear();
        messageIdField.clear();
        sessionIdField.clear();
        replyToField.clear();
        labelField.clear();
        contentTypeField.clear();
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }
}
