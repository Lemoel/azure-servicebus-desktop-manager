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
    
    // Formulário de Criação/Edição de Rule
    @FXML private Label formTitleLabel;
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
    @FXML private Button newRuleButton;
    @FXML private Button cancelEditButton;
    
    // Dados
    private String topicName;
    private String subscriptionName;
    private ServiceBusService serviceBusService;
    private ObservableList<RuleInfo> rules = FXCollections.observableArrayList();
    private DialogPane dialogPane;
    
    // Controle de modo de edição
    private boolean isEditMode = false;
    private RuleInfo editingRule = null;
    
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
        
        // Coluna de expressão - usar método formatado com tooltip
        ruleFilterExpressionColumn.setCellValueFactory(cellData -> 
            cellData.getValue().nameProperty().map(name -> cellData.getValue().getFormattedExpression())
        );
        
        // Adicionar tooltip para mostrar expressão completa
        ruleFilterExpressionColumn.setCellFactory(column -> {
            return new TableCell<RuleInfo, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        setText(item);
                        // Pegar a rule da linha atual
                        RuleInfo ruleInfo = getTableView().getItems().get(getIndex());
                        String fullExpression = ruleInfo.getFilterExpression();
                        
                        // Só adicionar tooltip se o texto for diferente (foi truncado)
                        if (fullExpression != null && !fullExpression.equals(item)) {
                            Tooltip tooltip = new Tooltip(fullExpression);
                            tooltip.setWrapText(true);
                            tooltip.setMaxWidth(400);
                            setTooltip(tooltip);
                        } else {
                            setTooltip(null);
                        }
                    }
                }
            };
        });
        
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
        createRuleButton.setOnAction(e -> handleCreateOrUpdateRule());
        newRuleButton.setOnAction(e -> switchToCreateMode());
        cancelEditButton.setOnAction(e -> switchToCreateMode());
        
        // Listener para seleção na tabela - prevenir seleção de $Default
        rulesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.getIsDefault()) {
                // Se for $Default, prevenir a seleção
                rulesTable.getSelectionModel().clearSelection();
            } else if (newSelection != null) {
                // Se não for $Default, entrar em modo edição
                switchToEditMode(newSelection);
            }
        });
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
    
    private void switchToCreateMode() {
        isEditMode = false;
        editingRule = null;
        
        // Atualizar UI
        formTitleLabel.setText("➕ Criar Nova Rule");
        createRuleButton.setText("✅ Criar Rule");
        newRuleButton.setVisible(false);
        newRuleButton.setManaged(false);
        cancelEditButton.setVisible(false);
        cancelEditButton.setManaged(false);
        
        // Habilitar campo nome
        newRuleNameField.setDisable(false);
        newRuleNameField.setStyle("");
        
        // Limpar formulário
        clearCreateForm();
        
        // Limpar seleção da tabela
        rulesTable.getSelectionModel().clearSelection();
    }
    
    private void switchToEditMode(RuleInfo ruleInfo) {
        isEditMode = true;
        editingRule = ruleInfo;
        
        // Atualizar UI
        formTitleLabel.setText("✏️ Editar Rule");
        createRuleButton.setText("💾 Salvar Alterações");
        newRuleButton.setVisible(true);
        newRuleButton.setManaged(true);
        cancelEditButton.setVisible(true);
        cancelEditButton.setManaged(true);
        
        // Desabilitar campo nome (não pode ser alterado)
        newRuleNameField.setDisable(true);
        newRuleNameField.setStyle("-fx-opacity: 0.6;");
        
        // Preencher formulário com dados da rule
        fillFormWithRuleData(ruleInfo);
    }
    
    private void fillFormWithRuleData(RuleInfo ruleInfo) {
        // Nome da rule
        newRuleNameField.setText(ruleInfo.getName());
        
        // Determinar tipo de filtro e preencher campos apropriados
        String filterType = ruleInfo.getFilterType();
        
        if (filterType != null && (filterType.contains("SqlFilter") || filterType.contains("SQL"))) {
            // SQL Filter
            filterTypeComboBox.setValue("SQL Filter");
            sqlExpressionTextArea.setText(ruleInfo.getFilterExpression());
            
            // Limpar campos de correlation
            correlationIdField.clear();
            messageIdField.clear();
            sessionIdField.clear();
            replyToField.clear();
            labelField.clear();
            contentTypeField.clear();
            
        } else if (filterType != null && (filterType.contains("CorrelationFilter") || filterType.contains("Correlation"))) {
            // Correlation Filter
            filterTypeComboBox.setValue("Correlation Filter");
            
            // Preencher campos individuais
            correlationIdField.setText(ruleInfo.getCorrelationId() != null ? ruleInfo.getCorrelationId() : "");
            messageIdField.setText(ruleInfo.getMessageId() != null ? ruleInfo.getMessageId() : "");
            sessionIdField.setText(ruleInfo.getSessionId() != null ? ruleInfo.getSessionId() : "");
            replyToField.setText(ruleInfo.getReplyTo() != null ? ruleInfo.getReplyTo() : "");
            labelField.setText(ruleInfo.getLabel() != null ? ruleInfo.getLabel() : "");
            contentTypeField.setText(ruleInfo.getContentType() != null ? ruleInfo.getContentType() : "");
            
            // Limpar SQL expression
            sqlExpressionTextArea.clear();
        } else {
            // True/False filter - não editável, voltar para modo criação
            showAlert("Aviso", "Este tipo de filtro não pode ser editado. Crie uma nova rule.", Alert.AlertType.WARNING);
            switchToCreateMode();
        }
    }
    
    private void handleCreateOrUpdateRule() {
        if (isEditMode) {
            handleUpdateRule();
        } else {
            handleCreateRule();
        }
    }
    
    private void handleUpdateRule() {
        if (editingRule == null) {
            showAlert("Erro", "Nenhuma rule selecionada para edição", Alert.AlertType.ERROR);
            return;
        }
        
        String ruleName = editingRule.getName();
        String filterType = filterTypeComboBox.getValue();
        
        // Validações
        if (filterType == null) {
            showAlert("Erro", "Selecione o tipo de filtro", Alert.AlertType.ERROR);
            return;
        }
        
        // Confirmar atualização
        Optional<ButtonType> result = showConfirmation(
            "Confirmar Atualização",
            String.format("Tem certeza que deseja atualizar a rule '%s'?\n\nA rule será removida e recriada com as novas configurações.", ruleName)
        );
        
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        
        createRuleButton.setDisable(true);
        
        Task<Boolean> updateTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Passo 1: Deletar rule existente
                boolean deleted = serviceBusService.deleteRuleAsync(topicName, subscriptionName, ruleName).get();
                
                if (!deleted) {
                    throw new RuntimeException("Falha ao remover rule existente");
                }
                
                // Passo 2: Criar nova rule com configurações atualizadas
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
                            String.format("Rule '%s' atualizada com sucesso!", ruleName), 
                            Alert.AlertType.INFORMATION);
                        
                        // Voltar para modo criação
                        switchToCreateMode();
                        
                        // Recarregar lista
                        loadRules();
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    createRuleButton.setDisable(false);
                    showAlert("Erro", "Erro ao atualizar rule: " + getException().getMessage(), Alert.AlertType.ERROR);
                    
                    // Recarregar lista para garantir estado consistente
                    loadRules();
                });
            }
        };
        
        new Thread(updateTask).start();
    }
    
    private void handleCreateRule() {
        String ruleName = newRuleNameField.getText().trim();
        String filterType = filterTypeComboBox.getValue();
        
        // Validações
        if (ruleName.isEmpty()) {
            showAlert("Erro", "Digite um nome para a rule", Alert.AlertType.ERROR);
            return;
        }
        
        // Prevenir criação de rule com nome reservado $Default
        if (ruleName.equalsIgnoreCase("$Default")) {
            showAlert("Erro", 
                "O nome '$Default' é reservado pelo Azure Service Bus.\n\n" +
                "Use um nome diferente para a rule.\n" +
                "A rule '$Default' é criada automaticamente com novas subscriptions.", 
                Alert.AlertType.ERROR);
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
