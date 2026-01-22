package com.azureservicebus.manager.controller;

import com.azureservicebus.manager.model.CreateQueueResult;
import com.azureservicebus.manager.model.QueueInfo;
import com.azureservicebus.manager.model.MessageInfo;
import com.azureservicebus.manager.model.TopicInfo;
import com.azureservicebus.manager.model.SubscriptionInfo;
import com.azureservicebus.manager.service.ServiceBusService;
import com.azureservicebus.manager.util.TableViewCopyUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller principal da aplicação
 * Gerencia a interface e coordena as operações com o ServiceBusService
 */
public class MainController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // Componentes da interface - Conexão
    @FXML private Button disconnectButton;
    @FXML private Label connectionStatusLabel;
    @FXML private Label namespaceLabel;
    @FXML private HBox headerBox; // Container do header
    @FXML private HBox namespaceBox; // Container da área do namespace
    
    // Componentes da interface - Perfis
    @FXML private ComboBox<String> profileComboBox;
    @FXML private Button manageProfilesButton;
    
    // Componentes da interface - Abas principais
    @FXML private TabPane mainTabPane;
    @FXML private Tab queuesMainTab;
    @FXML private Tab topicsMainTab;
    
    // Aba de Filas
    @FXML private Button loadQueuesButton;
    @FXML private TextField queueFilterField;
    @FXML private ListView<String> queueListView;
    @FXML private TableView<QueueInfo> queueDetailsTable;
    @FXML private TableColumn<QueueInfo, String> queueNameColumn;
    @FXML private TableColumn<QueueInfo, String> queueStatusColumn;
    @FXML private TableColumn<QueueInfo, Long> totalMessagesColumn;
    @FXML private TableColumn<QueueInfo, Long> activeMessagesColumn;
    @FXML private TableColumn<QueueInfo, Long> deadLetterMessagesColumn;
    @FXML private TableColumn<QueueInfo, Double> sizeColumn;
    @FXML private TableColumn<QueueInfo, Void> actionsColumn;
    
    @FXML private TextField newQueueNameField;
    @FXML private Button createQueueButton;
    @FXML private Button createAdvancedQueueButton;
    
    // Aba de Tópicos
    @FXML private Button loadTopicsButton;
    @FXML private TextField topicFilterField;
    @FXML private ListView<String> topicListView;
    @FXML private TableView<TopicInfo> topicDetailsTable;
    @FXML private TableColumn<TopicInfo, String> topicNameColumn;
    @FXML private TableColumn<TopicInfo, String> topicStatusColumn;
    @FXML private TableColumn<TopicInfo, Integer> topicSubscriptionCountColumn;
    @FXML private TableColumn<TopicInfo, Double> topicSizeColumn;
    @FXML private TableColumn<TopicInfo, Void> topicActionsColumn;
    
    @FXML private TextField newTopicNameField;
    @FXML private Button createTopicButton;
    
    // Subscriptions
    @FXML private Label selectedTopicLabel;
    @FXML private TextField subscriptionFilterField;
    @FXML private Button loadSubscriptionsButton;
    @FXML private TableView<SubscriptionInfo> subscriptionsTable;
    @FXML private TableColumn<SubscriptionInfo, String> subscriptionNameColumn;
    @FXML private TableColumn<SubscriptionInfo, String> subscriptionStatusColumn;
    @FXML private TableColumn<SubscriptionInfo, Long> subscriptionTotalMessagesColumn;
    @FXML private TableColumn<SubscriptionInfo, Long> subscriptionActiveMessagesColumn;
    @FXML private TableColumn<SubscriptionInfo, Long> subscriptionDeadLetterMessagesColumn;
    @FXML private TableColumn<SubscriptionInfo, Void> subscriptionActionsColumn;
    
    @FXML private TextField newSubscriptionNameField;
    @FXML private Button createSubscriptionButton;
    @FXML private Button createAdvancedSubscriptionButton;
    
    // Aba de Mensagens
    @FXML private ComboBox<String> viewQueueComboBox;
    @FXML private Button loadMessagesButton;
    @FXML private TableView<MessageInfo> messagesTable;
    @FXML private TableColumn<MessageInfo, Long> sequenceNumberColumn;
    @FXML private TableColumn<MessageInfo, String> messageIdColumn;
    @FXML private TableColumn<MessageInfo, String> messageBodyColumn;
    @FXML private TableColumn<MessageInfo, String> enqueuedTimeColumn;
    @FXML private TableColumn<MessageInfo, Void> messageActionsColumn;
    @FXML private TextArea messageDetailsTextArea;
    
    // Aba de Envio de Mensagens (Filas)
    @FXML private ComboBox<String> sendQueueComboBox;
    @FXML private TextArea messageBodyTextArea;
    @FXML private TextField property1KeyField;
    @FXML private TextField property1ValueField;
    @FXML private TextField property2KeyField;
    @FXML private TextField property2ValueField;
    @FXML private Button sendMessageButton;
    
    // Aba de Ver Mensagens de Tópicos/Subscriptions
    @FXML private ComboBox<String> viewTopicComboBox;
    @FXML private ComboBox<String> viewSubscriptionComboBox;
    @FXML private Button loadTopicMessagesButton;
    @FXML private TableView<MessageInfo> topicMessagesTable;
    @FXML private TableColumn<MessageInfo, Long> topicSequenceNumberColumn;
    @FXML private TableColumn<MessageInfo, String> topicMessageIdColumn;
    @FXML private TableColumn<MessageInfo, String> topicMessageBodyColumn;
    @FXML private TableColumn<MessageInfo, String> topicEnqueuedTimeColumn;
    @FXML private TableColumn<MessageInfo, Void> topicMessageActionsColumn;
    @FXML private TextArea topicMessageDetailsTextArea;
    
    // Aba de Envio de Mensagens para Tópicos
    @FXML private ComboBox<String> sendTopicComboBox;
    @FXML private TextArea sendTopicMessageBodyTextArea;
    @FXML private TextField sendTopicProperty1KeyField;
    @FXML private TextField sendTopicProperty1ValueField;
    @FXML private TextField sendTopicProperty2KeyField;
    @FXML private TextField sendTopicProperty2ValueField;
    @FXML private Button sendToTopicButton;
    
    // Log
    @FXML private TextArea logTextArea;
    @FXML private Button clearLogButton;
    
    // Serviços e dados
    private ServiceBusService serviceBusService;
    private com.azureservicebus.manager.service.ProfileService profileService;
    private Stage primaryStage;
    private ObservableList<String> queueNames = FXCollections.observableArrayList();
    private ObservableList<String> profileNames = FXCollections.observableArrayList();
    private ObservableList<QueueInfo> queueDetails = FXCollections.observableArrayList();
    private ObservableList<MessageInfo> messages = FXCollections.observableArrayList();
    
    // Dados de tópicos e subscriptions
    private ObservableList<String> topicNames = FXCollections.observableArrayList();
    private ObservableList<TopicInfo> topicDetails = FXCollections.observableArrayList();
    private ObservableList<SubscriptionInfo> subscriptionDetails = FXCollections.observableArrayList();
    private ObservableList<String> subscriptionNames = FXCollections.observableArrayList();
    private String selectedTopicName = null;
    
    // Mensagens de tópicos
    private ObservableList<MessageInfo> topicMessages = FXCollections.observableArrayList();
    
    // Flags para prevenir loops infinitos nas ComboBoxes
    private boolean updatingViewQueueComboBox = false;
    private boolean updatingSendQueueComboBox = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando MainController");
        
        // Inicializar serviços
        serviceBusService = new ServiceBusService();
        profileService = com.azureservicebus.manager.service.ProfileService.getInstance();
        setupServiceCallbacks();
        
        // Configurar interface inicial
        setupInitialUI();
        setupTableColumns();
        setupEventHandlers();
        
        // Adicionar funcionalidade de cópia em todas as TableViews
        setupCopyFunctionality();
        
        // Configurar sistema de perfis
        setupProfileSystem();
        
        logger.info("MainController inicializado com sucesso");
    }
    
    private void setupServiceCallbacks() {
        serviceBusService.setOnConnectionStatusChanged(() -> {
            Platform.runLater(this::updateConnectionStatus);
        });
        
        serviceBusService.setOnLogMessage(message -> {
            Platform.runLater(() -> addLogMessage(message));
        });
    }
    
    private void setupInitialUI() {
        // Estado inicial - desconectado
        updateConnectionStatus();
        
        // Configurar listas de filas
        queueListView.setItems(queueNames);
        queueDetailsTable.setItems(queueDetails);
        messagesTable.setItems(messages);
        
        // Configurar listas de tópicos
        topicListView.setItems(topicNames);
        topicDetailsTable.setItems(topicDetails);
        subscriptionsTable.setItems(subscriptionDetails);
        
        // Configurar ComboBoxes de Filas
        viewQueueComboBox.setItems(queueNames);
        sendQueueComboBox.setItems(queueNames);
        
        // Configurar ComboBoxes de Tópicos
        viewTopicComboBox.setItems(topicNames);
        viewSubscriptionComboBox.setItems(subscriptionNames);
        sendTopicComboBox.setItems(topicNames);
        
        // Configurar tabelas de mensagens de tópicos
        topicMessagesTable.setItems(topicMessages);
        
        // Placeholder para mensagem
        messageBodyTextArea.setPromptText(
            "Digite o corpo da mensagem aqui...\n" +
            "Exemplo JSON:\n" +
            "{\n" +
            "  \"id\": 123,\n" +
            "  \"action\": \"test\",\n" +
            "  \"data\": \"exemplo\"\n" +
            "}"
        );
    }
    
    private void setupTableColumns() {
        // Colunas da tabela de filas
        queueNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        queueStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalMessagesColumn.setCellValueFactory(new PropertyValueFactory<>("totalMessages"));
        activeMessagesColumn.setCellValueFactory(new PropertyValueFactory<>("activeMessages"));
        deadLetterMessagesColumn.setCellValueFactory(new PropertyValueFactory<>("deadLetterMessages"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("sizeInKB"));
        
        // Configurar coluna de ações
        setupActionsColumn();
        
        // Colunas da tabela de mensagens
        sequenceNumberColumn.setCellValueFactory(new PropertyValueFactory<>("sequenceNumber"));
        messageIdColumn.setCellValueFactory(new PropertyValueFactory<>("messageId"));
        messageBodyColumn.setCellValueFactory(cellData -> 
            cellData.getValue().messageBodyProperty().map(body -> 
                body != null && body.length() > 50 ? body.substring(0, 50) + "..." : body
            )
        );
        enqueuedTimeColumn.setCellValueFactory(cellData -> 
            cellData.getValue().enqueuedTimeProperty().map(time -> 
                time != null ? time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "N/A"
            )
        );
        
        // Configurar coluna de ações para mensagens
        setupMessageActionsColumn();
        
        // Colunas da tabela de tópicos
        topicNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        topicStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        topicSubscriptionCountColumn.setCellValueFactory(new PropertyValueFactory<>("subscriptionCount"));
        topicSizeColumn.setCellValueFactory(new PropertyValueFactory<>("sizeInKB"));
        
        // Colunas da tabela de subscriptions
        subscriptionNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        subscriptionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        subscriptionTotalMessagesColumn.setCellValueFactory(new PropertyValueFactory<>("totalMessages"));
        subscriptionActiveMessagesColumn.setCellValueFactory(new PropertyValueFactory<>("activeMessages"));
        subscriptionDeadLetterMessagesColumn.setCellValueFactory(new PropertyValueFactory<>("deadLetterMessages"));
        
        // Configurar coluna de ações para subscriptions
        setupSubscriptionActionsColumn();
        
        // Colunas da tabela de mensagens de tópicos
        topicSequenceNumberColumn.setCellValueFactory(new PropertyValueFactory<>("sequenceNumber"));
        topicMessageIdColumn.setCellValueFactory(new PropertyValueFactory<>("messageId"));
        topicMessageBodyColumn.setCellValueFactory(cellData -> 
            cellData.getValue().messageBodyProperty().map(body -> 
                body != null && body.length() > 50 ? body.substring(0, 50) + "..." : body
            )
        );
        topicEnqueuedTimeColumn.setCellValueFactory(cellData -> 
            cellData.getValue().enqueuedTimeProperty().map(time -> 
                time != null ? time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "N/A"
            )
        );
        
        // Configurar coluna de ações para mensagens de tópicos
        setupTopicMessageActionsColumn();
    }
    
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(new Callback<TableColumn<QueueInfo, Void>, TableCell<QueueInfo, Void>>() {
            @Override
            public TableCell<QueueInfo, Void> call(TableColumn<QueueInfo, Void> param) {
                return new TableCell<QueueInfo, Void>() {
                    private final Button infoButton = new Button("ℹ️");
                    private final Button deleteButton = new Button("✖");
                    private final Button clearButton = new Button("⚠");
                    private final Button refreshButton = new Button("↻");
                    private final HBox actionBox = new HBox(5);
                    
                    {
                        // Configurar botão de informações
                        infoButton.setStyle(
                            "-fx-background-color: #007bff; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 12px; " +
                            "-fx-padding: 3 6 3 6; " +
                            "-fx-cursor: hand;"
                        );
                        infoButton.setTooltip(new Tooltip("Ver detalhes completos"));
                        
                        // Configurar botão de deletar
                        deleteButton.setStyle(
                            "-fx-background-color: #dc3545; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 12px; " +
                            "-fx-padding: 3 6 3 6; " +
                            "-fx-cursor: hand;"
                        );
                        deleteButton.setTooltip(new Tooltip("Remover fila"));
                        
                        // Configurar botão de limpar
                        clearButton.setStyle(
                            "-fx-background-color: #ffc107; " +
                            "-fx-text-fill: black; " +
                            "-fx-font-size: 12px; " +
                            "-fx-padding: 3 6 3 6; " +
                            "-fx-cursor: hand;"
                        );
                        clearButton.setTooltip(new Tooltip("Limpar mensagens da fila"));
                        
                        // Configurar botão de refresh
                        refreshButton.setStyle(
                            "-fx-background-color: #17a2b8; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 12px; " +
                            "-fx-padding: 3 6 3 6; " +
                            "-fx-cursor: hand;"
                        );
                        refreshButton.setTooltip(new Tooltip("Atualizar dados da fila"));
                        
                        // Configurar container
                        actionBox.setAlignment(Pos.CENTER);
                        actionBox.getChildren().addAll(infoButton, refreshButton, deleteButton, clearButton);
                        
                        // Event handlers
                        infoButton.setOnAction(event -> {
                            QueueInfo queueInfo = getTableView().getItems().get(getIndex());
                            handleShowQueueDetails(queueInfo);
                        });
                        
                        deleteButton.setOnAction(event -> {
                            QueueInfo queueInfo = getTableView().getItems().get(getIndex());
                            handleDeleteQueueFromTable(queueInfo.getName());
                        });
                        
                        clearButton.setOnAction(event -> {
                            QueueInfo queueInfo = getTableView().getItems().get(getIndex());
                            handleClearMessagesFromTable(queueInfo.getName());
                        });
                        
                        refreshButton.setOnAction(event -> {
                            QueueInfo queueInfo = getTableView().getItems().get(getIndex());
                            handleRefreshQueueFromTable(queueInfo.getName());
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(actionBox);
                        }
                    }
                };
            }
        });
    }
    
    private void setupMessageActionsColumn() {
        messageActionsColumn.setCellFactory(new Callback<TableColumn<MessageInfo, Void>, TableCell<MessageInfo, Void>>() {
            @Override
            public TableCell<MessageInfo, Void> call(TableColumn<MessageInfo, Void> param) {
                return new TableCell<MessageInfo, Void>() {
                    private final Button deleteButton = new Button("✖");
                    private final HBox actionBox = new HBox(5);
                    
                    {
                        // Configurar botão de deletar mensagem
                        deleteButton.setStyle(
                            "-fx-background-color: #dc3545; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 12px; " +
                            "-fx-padding: 3 6 3 6; " +
                            "-fx-cursor: hand;"
                        );
                        deleteButton.setTooltip(new Tooltip("Remover mensagem"));
                        
                        // Configurar container
                        actionBox.setAlignment(Pos.CENTER);
                        actionBox.getChildren().add(deleteButton);
                        
                        // Event handler
                        deleteButton.setOnAction(event -> {
                            MessageInfo messageInfo = getTableView().getItems().get(getIndex());
                            handleDeleteMessageFromTable(messageInfo);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(actionBox);
                        }
                    }
                };
            }
        });
    }
    
    private void setupSubscriptionActionsColumn() {
        subscriptionActionsColumn.setCellFactory(new Callback<TableColumn<SubscriptionInfo, Void>, TableCell<SubscriptionInfo, Void>>() {
            @Override
            public TableCell<SubscriptionInfo, Void> call(TableColumn<SubscriptionInfo, Void> param) {
                return new TableCell<SubscriptionInfo, Void>() {
                    private final Button infoButton = new Button("ℹ️");
                    private final Button rulesButton = new Button("📜");
                    private final Button deleteButton = new Button("✖");
                    private final Button clearButton = new Button("⚠");
                    private final HBox actionBox = new HBox(5);
                    
                    {
                        infoButton.setStyle(
                            "-fx-background-color: #007bff; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 12px; " +
                            "-fx-padding: 3 6 3 6; " +
                            "-fx-cursor: hand;"
                        );
                        infoButton.setTooltip(new Tooltip("Ver detalhes completos"));
                        
                        rulesButton.setStyle(
                            "-fx-background-color: #6f42c1; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 12px; " +
                            "-fx-padding: 3 6 3 6; " +
                            "-fx-cursor: hand;"
                        );
                        rulesButton.setTooltip(new Tooltip("Gerenciar Rules"));
                        
                        deleteButton.setStyle(
                            "-fx-background-color: #dc3545; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 12px; " +
                            "-fx-padding: 3 6 3 6; " +
                            "-fx-cursor: hand;"
                        );
                        deleteButton.setTooltip(new Tooltip("Remover subscription"));
                        
                        clearButton.setStyle(
                            "-fx-background-color: #ffc107; " +
                            "-fx-text-fill: black; " +
                            "-fx-font-size: 12px; " +
                            "-fx-padding: 3 6 3 6; " +
                            "-fx-cursor: hand;"
                        );
                        clearButton.setTooltip(new Tooltip("Limpar mensagens"));
                        
                        actionBox.setAlignment(Pos.CENTER);
                        actionBox.getChildren().addAll(infoButton, rulesButton, deleteButton, clearButton);
                        
                        infoButton.setOnAction(event -> {
                            SubscriptionInfo subInfo = getTableView().getItems().get(getIndex());
                            handleShowSubscriptionDetails(subInfo);
                        });
                        
                        rulesButton.setOnAction(event -> {
                            SubscriptionInfo subInfo = getTableView().getItems().get(getIndex());
                            handleManageRules(subInfo);
                        });
                        
                        deleteButton.setOnAction(event -> {
                            SubscriptionInfo subInfo = getTableView().getItems().get(getIndex());
                            handleDeleteSubscription(subInfo);
                        });
                        
                        clearButton.setOnAction(event -> {
                            SubscriptionInfo subInfo = getTableView().getItems().get(getIndex());
                            handleClearSubscriptionMessages(subInfo);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(actionBox);
                        }
                    }
                };
            }
        });
    }
    
    private void setupTopicMessageActionsColumn() {
        topicMessageActionsColumn.setCellFactory(new Callback<TableColumn<MessageInfo, Void>, TableCell<MessageInfo, Void>>() {
            @Override
            public TableCell<MessageInfo, Void> call(TableColumn<MessageInfo, Void> param) {
                return new TableCell<MessageInfo, Void>() {
                    private final Button deleteButton = new Button("✖");
                    private final HBox actionBox = new HBox(5);
                    
                    {
                        // Configurar botão de deletar mensagem de tópico
                        deleteButton.setStyle(
                            "-fx-background-color: #dc3545; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 12px; " +
                            "-fx-padding: 3 6 3 6; " +
                            "-fx-cursor: hand;"
                        );
                        deleteButton.setTooltip(new Tooltip("Remover mensagem"));
                        
                        // Configurar container
                        actionBox.setAlignment(Pos.CENTER);
                        actionBox.getChildren().add(deleteButton);
                        
                        // Event handler
                        deleteButton.setOnAction(event -> {
                            MessageInfo messageInfo = getTableView().getItems().get(getIndex());
                            // Por enquanto apenas mostra aviso - remoção de mensagens de tópicos requer receiver
                            showAlert("Info", "Remoção de mensagens de subscriptions não está implementada nesta versão.", Alert.AlertType.INFORMATION);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(actionBox);
                        }
                    }
                };
            }
        });
    }
    
    private void handleDeleteQueueFromTable(String queueName) {
        // Diálogo de confirmação para remoção
        Optional<ButtonType> result = showConfirmation(
            "Confirmar Remoção da Fila",
            String.format("Tem certeza que deseja remover a fila '%s'?\n\nEsta operação é irreversível e removerá permanentemente a fila e todas as suas mensagens!", queueName)
        );
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> deleteTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return serviceBusService.deleteQueueAsync(queueName).get();
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        if (getValue()) {
                            addLogMessage(String.format("Fila '%s' removida com sucesso!", queueName));
                            handleLoadQueues(); // Recarregar lista de filas
                            queueDetails.clear(); // Limpar detalhes da tabela
                        }
                    });
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        showAlert("Erro", "Erro ao remover fila: " + getException().getMessage(), Alert.AlertType.ERROR);
                    });
                }
            };
            
            new Thread(deleteTask).start();
        }
    }
    
    private void handleClearMessagesFromTable(String queueName) {
        // Diálogo de confirmação para limpeza
        Optional<ButtonType> result = showConfirmation(
            "Confirmar Limpeza de Mensagens",
            String.format("Tem certeza que deseja limpar TODAS as mensagens da fila '%s'?\n\nEsta operação é irreversível e todas as mensagens serão excluídas permanentemente!", queueName)
        );
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Integer> clearTask = new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {
                    return serviceBusService.clearQueueMessagesAsync(queueName).get();
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        addLogMessage(String.format("Limpeza da fila '%s' concluída: %d mensagens removidas", queueName, getValue()));
                        // Atualizar detalhes da fila se ela estiver selecionada
                        String selectedQueue = queueListView.getSelectionModel().getSelectedItem();
                        if (queueName.equals(selectedQueue)) {
                            handleQueueSelection(selectedQueue);
                        }
                    });
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        showAlert("Erro", "Erro ao limpar mensagens: " + getException().getMessage(), Alert.AlertType.ERROR);
                    });
                }
            };
            
            new Thread(clearTask).start();
        }
    }
    
    private void handleDeleteMessageFromTable(MessageInfo messageInfo) {
        String queueName = viewQueueComboBox.getValue();
        
        if (queueName == null) {
            showAlert("Erro", "Não foi possível determinar a fila da mensagem", Alert.AlertType.ERROR);
            return;
        }
        
        // Diálogo de confirmação para remoção da mensagem
        Optional<ButtonType> result = showConfirmation(
            "Confirmar Remoção da Mensagem",
            String.format("Tem certeza que deseja remover a mensagem com sequence number %d?\n\nEsta operação é irreversível!", 
                messageInfo.getSequenceNumber())
        );
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> deleteTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return serviceBusService.deleteMessageAsync(queueName, messageInfo.getSequenceNumber()).get();
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        if (getValue()) {
                            addLogMessage(String.format("Mensagem %d removida com sucesso da fila '%s'!", 
                                messageInfo.getSequenceNumber(), queueName));
                            
                            // Recarregar mensagens para atualizar a lista
                            handleLoadMessages();
                            
                            // Mostrar diálogo de sucesso
                            showAlert("Sucesso", 
                                String.format("Mensagem removida com sucesso da fila '%s'!", queueName), 
                                Alert.AlertType.INFORMATION);
                        } else {
                            showAlert("Aviso", 
                                String.format("Mensagem %d não foi encontrada na fila '%s'. Pode já ter sido processada.", 
                                    messageInfo.getSequenceNumber(), queueName), 
                                Alert.AlertType.WARNING);
                        }
                    });
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        showAlert("Erro", "Erro ao remover mensagem: " + getException().getMessage(), Alert.AlertType.ERROR);
                    });
                }
            };
            
            new Thread(deleteTask).start();
        }
    }
    
    private void handleRefreshQueueFromTable(String queueName) {
        if (!serviceBusService.isConnected()) {
            showAlert("Erro", "Não conectado ao Service Bus", Alert.AlertType.ERROR);
            return;
        }
        
        Task<QueueInfo> refreshTask = new Task<QueueInfo>() {
            @Override
            protected QueueInfo call() throws Exception {
                return serviceBusService.getQueueDetailsAsync(queueName).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    // Atualizar a fila específica na tabela
                    QueueInfo updatedQueue = getValue();
                    for (int i = 0; i < queueDetails.size(); i++) {
                        if (queueDetails.get(i).getName().equals(queueName)) {
                            queueDetails.set(i, updatedQueue);
                            break;
                        }
                    }
                    
                    addLogMessage(String.format("Dados da fila '%s' atualizados com sucesso!", queueName));
                    
                    // Mostrar diálogo de sucesso
                    showAlert("Sucesso", 
                        String.format("Dados da fila '%s' foram atualizados com sucesso!", queueName), 
                        Alert.AlertType.INFORMATION);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("Erro", "Erro ao atualizar dados da fila: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(refreshTask).start();
    }
    
    private void setupEventHandlers() {
        // Conexão
        disconnectButton.setOnAction(e -> handleDisconnect());
        
        // Filas
        loadQueuesButton.setOnAction(e -> handleLoadQueues());
        queueFilterField.textProperty().addListener((obs, oldVal, newVal) -> filterQueues(newVal));
        queueListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> handleQueueSelection(newVal)
        );
        
        createQueueButton.setOnAction(e -> handleCreateQueue());
        
        // Criar fila avançada - se o botão existir (pode não existir em versões antigas do FXML)
        if (createAdvancedQueueButton != null) {
            createAdvancedQueueButton.setOnAction(e -> handleCreateAdvancedQueue());
        }
        
        // Mensagens
        loadMessagesButton.setOnAction(e -> handleLoadMessages());
        messagesTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> handleMessageSelection(newVal)
        );
        
        // Configurar filtro na ComboBox de visualização de mensagens
        setupViewQueueComboBoxFilter();
        
        // Envio de mensagens
        sendMessageButton.setOnAction(e -> handleSendMessage());
        
        // Configurar filtro na ComboBox de envio de mensagens
        setupSendQueueComboBoxFilter();
        
        // Tópicos
        loadTopicsButton.setOnAction(e -> handleLoadTopics());
        topicFilterField.textProperty().addListener((obs, oldVal, newVal) -> filterTopics(newVal));
        topicListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> handleTopicSelection(newVal)
        );
        createTopicButton.setOnAction(e -> handleCreateTopic());
        
        // Subscriptions
        loadSubscriptionsButton.setOnAction(e -> handleLoadSubscriptions());
        subscriptionFilterField.textProperty().addListener((obs, oldVal, newVal) -> filterSubscriptions(newVal));
        createSubscriptionButton.setOnAction(e -> handleCreateSubscription());
        
        // Criar subscription avançada - se o botão existir
        if (createAdvancedSubscriptionButton != null) {
            createAdvancedSubscriptionButton.setOnAction(e -> handleCreateAdvancedSubscription());
        }
        
        // Ver Mensagens de Tópicos
        loadTopicMessagesButton.setOnAction(e -> handleLoadTopicMessages());
        topicMessagesTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> handleTopicMessageSelection(newVal)
        );
        
        // Envio de mensagens para tópicos
        sendToTopicButton.setOnAction(e -> handleSendMessageToTopic());
        
        // Carregar subscriptions quando um tópico for selecionado no viewTopicComboBox
        viewTopicComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                loadSubscriptionsForTopic(newVal);
            }
        });
        
        // Log
        clearLogButton.setOnAction(e -> logTextArea.clear());
    }
    
    /**
     * Adiciona funcionalidade de cópia (Context Menu + Ctrl+C) em todas as TableViews
     */
    private void setupCopyFunctionality() {
        // Tabela de filas
        TableViewCopyUtil.addCopyToClipboardSupport(queueDetailsTable);
        
        // Tabela de mensagens de filas
        TableViewCopyUtil.addCopyToClipboardSupport(messagesTable);
        
        // Tabela de tópicos
        TableViewCopyUtil.addCopyToClipboardSupport(topicDetailsTable);
        
        // Tabela de subscriptions
        TableViewCopyUtil.addCopyToClipboardSupport(subscriptionsTable);
        
        // Tabela de mensagens de tópicos
        TableViewCopyUtil.addCopyToClipboardSupport(topicMessagesTable);
        
        logger.info("Funcionalidade de cópia adicionada em todas as TableViews");
    }
    
    /**
     * Configura o sistema de perfis
     */
    private void setupProfileSystem() {
        // Configurar ComboBox de perfis
        if (profileComboBox != null) {
            profileComboBox.setItems(profileNames);
            loadProfiles();
            
            // Event handler para mudança de perfil
            profileComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.isEmpty()) {
                    handleProfileSelection(newVal);
                }
            });
        }
        
        // Event handler para gerenciar perfis
        if (manageProfilesButton != null) {
            manageProfilesButton.setOnAction(e -> handleManageProfiles());
        }
        
        logger.info("Sistema de perfis configurado");
    }
    
    private void loadProfiles() {
        profileNames.clear();
        
        // Obter todos os perfis e extrair os nomes
        List<com.azureservicebus.manager.model.ConnectionProfile> allProfiles = profileService.getAllProfiles();
        for (com.azureservicebus.manager.model.ConnectionProfile profile : allProfiles) {
            profileNames.add(profile.getName());
        }
        
        // Selecionar perfil ativo se houver
        String activeProfile = profileService.getActiveProfileName();
        if (activeProfile != null && profileNames.contains(activeProfile)) {
            profileComboBox.setValue(activeProfile);
        }
    }
    
    private void handleProfileSelection(String profileName) {
        try {
            Optional<com.azureservicebus.manager.model.ConnectionProfile> profileOpt = profileService.getProfile(profileName);
            if (profileOpt.isPresent()) {
                com.azureservicebus.manager.model.ConnectionProfile profile = profileOpt.get();
                
                // Aplicar cor do perfil no header
                applyProfileColor(profile.getColor());
                
                // Desconectar da conexão anterior se estiver conectado
                if (serviceBusService.isConnected()) {
                    serviceBusService.disconnect();
                    addLogMessage("Desconectado do perfil anterior");
                }
                
                // Definir como perfil ativo
                profileService.setActiveProfile(profileName);
                
                // Conectar automaticamente com a nova connection string
                String connectionString = profile.getConnectionString();
                addLogMessage(String.format("Conectando ao perfil '%s'...", profileName));
                
                Task<Boolean> connectTask = new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        return serviceBusService.connectAsync(connectionString).get();
                    }
                    
                    @Override
                    protected void succeeded() {
                        Platform.runLater(() -> {
                            if (getValue()) {
                                addLogMessage(String.format("Conectado ao perfil '%s' com sucesso!", profileName));
                            } else {
                                showAlert("Erro", "Falha ao conectar ao perfil. Verifique a connection string.", Alert.AlertType.ERROR);
                            }
                        });
                    }
                    
                    @Override
                    protected void failed() {
                        Platform.runLater(() -> {
                            showAlert("Erro", "Erro ao conectar: " + getException().getMessage(), Alert.AlertType.ERROR);
                        });
                    }
                };
                
                new Thread(connectTask).start();
                
            } else {
                showAlert("Erro", "Perfil não encontrado: " + profileName, Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            logger.error("Erro ao selecionar perfil", e);
            showAlert("Erro", "Erro ao carregar perfil: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void handleManageProfiles() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/profile-manager-dialog.fxml")
            );
            
            DialogPane dialogPane = loader.load();
            ProfileManagerDialogController dialogController = loader.getController();
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(primaryStage); // Garantir que dialog abra no mesmo monitor da janela principal
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Gerenciar Perfis de Conexão");
            dialog.setResizable(true);
            
            dialog.showAndWait();
            
            // Recarregar perfis após fechar o diálogo
            loadProfiles();
            
        } catch (Exception e) {
            logger.error("Erro ao abrir gerenciador de perfis", e);
            showAlert("Erro", "Erro ao abrir gerenciador: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void setupViewQueueComboBoxFilter() {
        viewQueueComboBox.getEditor().focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String typedText = viewQueueComboBox.getEditor().getText();
                
                if (typedText != null && !typedText.trim().isEmpty()) {
                    String trimmed = typedText.trim();
                    boolean found = queueNames.stream().anyMatch(queue -> queue.equals(trimmed));
                    if (found) {
                        viewQueueComboBox.setValue(trimmed);
                    } else {
                        Optional<String> partialMatch = queueNames.stream()
                            .filter(queue -> queue.toLowerCase().contains(trimmed.toLowerCase()))
                            .findFirst();
                        
                        if (partialMatch.isPresent()) {
                            viewQueueComboBox.setValue(partialMatch.get());
                            viewQueueComboBox.getEditor().setText(partialMatch.get());
                        } else {
                            viewQueueComboBox.setValue(trimmed);
                        }
                    }
                }
            }
        });
    }
    
    private void setupSendQueueComboBoxFilter() {
        sendQueueComboBox.getEditor().focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String typedText = sendQueueComboBox.getEditor().getText();
                
                if (typedText != null && !typedText.trim().isEmpty()) {
                    String trimmed = typedText.trim();
                    boolean found = queueNames.stream().anyMatch(queue -> queue.equals(trimmed));
                    if (found) {
                        sendQueueComboBox.setValue(trimmed);
                    } else {
                        Optional<String> partialMatch = queueNames.stream()
                            .filter(queue -> queue.toLowerCase().contains(trimmed.toLowerCase()))
                            .findFirst();
                        
                        if (partialMatch.isPresent()) {
                            sendQueueComboBox.setValue(partialMatch.get());
                            sendQueueComboBox.getEditor().setText(partialMatch.get());
                        } else {
                            sendQueueComboBox.setValue(null);
                            sendQueueComboBox.getEditor().clear();
                        }
                    }
                }
            }
        });
    }
    
    private void updateConnectionStatus() {
        boolean connected = serviceBusService.isConnected();
        
        disconnectButton.setDisable(!connected);
        
        if (connected) {
            connectionStatusLabel.setText("✅ Conectado");
            connectionStatusLabel.setStyle("-fx-text-fill: green;");
            
            String namespace = serviceBusService.extractNamespace();
            namespaceLabel.setText("Namespace: " + (namespace != null ? namespace : "N/A"));
            
            // Habilitar abas principais
            queuesMainTab.setDisable(false);
            topicsMainTab.setDisable(false);
            
        } else {
            connectionStatusLabel.setText("❌ Desconectado");
            connectionStatusLabel.setStyle("-fx-text-fill: red;");
            namespaceLabel.setText("Namespace: N/A");
            
            // Desabilitar abas principais
            queuesMainTab.setDisable(true);
            topicsMainTab.setDisable(true);
            
            // Limpar dados de forma segura para evitar IndexOutOfBoundsException
            // Primeiro limpar seleções
            try {
                queueListView.getSelectionModel().clearSelection();
                queueDetailsTable.getSelectionModel().clearSelection();
                messagesTable.getSelectionModel().clearSelection();
                viewQueueComboBox.getSelectionModel().clearSelection();
                sendQueueComboBox.getSelectionModel().clearSelection();
            } catch (Exception e) {
                logger.warn("Erro ao limpar seleções: " + e.getMessage());
            }
            
            // Depois substituir as listas por listas vazias (mais seguro que clear())
            try {
                queueNames.setAll(FXCollections.observableArrayList());
                queueDetails.setAll(FXCollections.observableArrayList());
                messages.setAll(FXCollections.observableArrayList());
            } catch (Exception e) {
                logger.warn("Erro ao limpar listas: " + e.getMessage());
            }
        }
    }
    
    private void handleDisconnect() {
        serviceBusService.disconnect();
        addLogMessage("Desconectado do Azure Service Bus");
    }
    
    private void handleLoadQueues() {
        if (!serviceBusService.isConnected()) {
            showAlert("Erro", "Não conectado ao Service Bus", Alert.AlertType.ERROR);
            return;
        }
        
        loadQueuesButton.setDisable(true);
        loadQueuesButton.setText("Carregando...");
        
        Task<ObservableList<String>> loadTask = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws Exception {
                return serviceBusService.listQueueNamesAsync().get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    queueNames.setAll(getValue());
                    loadQueuesButton.setDisable(false);
                    loadQueuesButton.setText("Carregar Filas");
                    addLogMessage(String.format("Carregadas %d filas", getValue().size()));
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    loadQueuesButton.setDisable(false);
                    loadQueuesButton.setText("Carregar Filas");
                    showAlert("Erro", "Erro ao carregar filas: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(loadTask).start();
    }
    
    private void filterQueues(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            queueListView.setItems(queueNames);
            return;
        }
        
        ObservableList<String> filteredQueues = queueNames.filtered(
            queueName -> queueName.toLowerCase().contains(filter.toLowerCase())
        );
        queueListView.setItems(filteredQueues);
    }
    
    private void handleQueueSelection(String selectedQueue) {
        if (selectedQueue == null || !serviceBusService.isConnected()) {
            return;
        }
        
        Task<QueueInfo> detailsTask = new Task<QueueInfo>() {
            @Override
            protected QueueInfo call() throws Exception {
                return serviceBusService.getQueueDetailsAsync(selectedQueue).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    queueDetails.setAll(getValue());
                    addLogMessage(String.format("Detalhes carregados para fila '%s'", selectedQueue));
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("Erro", "Erro ao carregar detalhes: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(detailsTask).start();
    }
    
    private void handleCreateQueue() {
        String queueName = newQueueNameField.getText().trim();
        
        if (queueName.isEmpty()) {
            showAlert("Erro", "Digite um nome para a fila", Alert.AlertType.ERROR);
            return;
        }
        
        createQueueButton.setDisable(true);
        
        Task<CreateQueueResult> createTask = new Task<CreateQueueResult>() {
            @Override
            protected CreateQueueResult call() throws Exception {
                return serviceBusService.createQueueAsync(queueName).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    createQueueButton.setDisable(false);
                    newQueueNameField.clear();
                    
                    CreateQueueResult result = getValue();
                    switch (result) {
                        case CREATED:
                            addLogMessage(String.format("Fila '%s' criada com sucesso!", queueName));
                            showAlert("Sucesso", 
                                String.format("Fila '%s' foi criada com sucesso!", queueName), 
                                Alert.AlertType.INFORMATION);
                            handleLoadQueues(); // Recarregar lista
                            break;
                            
                        case ALREADY_EXISTS:
                            addLogMessage(String.format("Fila '%s' já existe no namespace", queueName));
                            showAlert("Informação", 
                                String.format("A fila '%s' já existe no namespace.\nVocê pode utilizá-la normalmente.", queueName), 
                                Alert.AlertType.WARNING);
                            handleLoadQueues(); // Recarregar lista para mostrar a fila
                            break;
                            
                        case ERROR:
                            showAlert("Erro", 
                                String.format("Erro ao criar fila '%s'. Verifique os logs para mais detalhes.", queueName), 
                                Alert.AlertType.ERROR);
                            break;
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    createQueueButton.setDisable(false);
                    showAlert("Erro", "Erro ao criar fila: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(createTask).start();
    }
    
    /**
     * Abre diálogo para criação de fila com configurações avançadas
     */
    private void handleCreateAdvancedQueue() {
        try {
            // Carregar o FXML do diálogo
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/create-queue-dialog.fxml")
            );
            
            DialogPane dialogPane = loader.load();
            CreateQueueDialogController dialogController = loader.getController();
            
            // Pré-preencher com o nome digitado, se houver
            String initialName = newQueueNameField.getText().trim();
            if (!initialName.isEmpty()) {
                dialogController.setInitialQueueName(initialName);
            }
            
            // Criar o diálogo PRIMEIRO
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(primaryStage); // Garantir que dialog abra no mesmo monitor da janela principal
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Configurar Nova Fila");
            dialog.setResizable(true); // Permitir redimensionamento pelo mouse
            
            // Customizar textos dos botões
            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.setText("✅ Criar Fila");
            }
            
            Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
            if (cancelButton != null) {
                cancelButton.setText("❌ Cancelar");
            }
            
            // IMPORTANTE: Configurar o controller DEPOIS que o Dialog foi criado
            // e os botões já existem no dialogPane
            dialogController.setDialogPane(dialogPane);
            
            // Exibir diálogo e processar resultado
            Optional<ButtonType> result = dialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Obter configuração do diálogo
                com.azureservicebus.manager.model.QueueConfiguration config = dialogController.getConfiguration();
                
                if (config != null && config.isValid()) {
                    // Criar fila com configurações customizadas
                    createAdvancedQueueButton.setDisable(true);
                    
                    Task<CreateQueueResult> createTask = new Task<CreateQueueResult>() {
                        @Override
                        protected CreateQueueResult call() throws Exception {
                            return serviceBusService.createQueueAsync(config).get();
                        }
                        
                        @Override
                        protected void succeeded() {
                            Platform.runLater(() -> {
                                createAdvancedQueueButton.setDisable(false);
                                newQueueNameField.clear();
                                
                                CreateQueueResult createResult = getValue();
                                switch (createResult) {
                                    case CREATED:
                                        addLogMessage(String.format("Fila '%s' criada com configurações customizadas!", config.getName()));
                                        showAlert("Sucesso", 
                                            String.format("Fila '%s' foi criada com sucesso!\n\nConfigurações aplicadas:\n" +
                                                "• Max Delivery Count: %d\n" +
                                                "• Lock Duration: %d minuto(s)\n" +
                                                "• Dead Letter on Expiration: %s\n" +
                                                "• Batched Operations: %s",
                                                config.getName(),
                                                config.getMaxDeliveryCount(),
                                                config.getLockDurationMinutes(),
                                                config.isDeadLetteringOnMessageExpiration() ? "Sim" : "Não",
                                                config.isBatchedOperationsEnabled() ? "Sim" : "Não"
                                            ), 
                                            Alert.AlertType.INFORMATION);
                                        handleLoadQueues();
                                        break;
                                        
                                    case ALREADY_EXISTS:
                                        addLogMessage(String.format("Fila '%s' já existe no namespace", config.getName()));
                                        showAlert("Informação", 
                                            String.format("A fila '%s' já existe no namespace.\nVocê pode utilizá-la normalmente.", config.getName()), 
                                            Alert.AlertType.WARNING);
                                        handleLoadQueues();
                                        break;
                                        
                                    case ERROR:
                                        showAlert("Erro", 
                                            String.format("Erro ao criar fila '%s'. Verifique os logs para mais detalhes.", config.getName()), 
                                            Alert.AlertType.ERROR);
                                        break;
                                }
                            });
                        }
                        
                        @Override
                        protected void failed() {
                            Platform.runLater(() -> {
                                createAdvancedQueueButton.setDisable(false);
                                showAlert("Erro", "Erro ao criar fila: " + getException().getMessage(), Alert.AlertType.ERROR);
                            });
                        }
                    };
                    
                    new Thread(createTask).start();
                }
            }
            
        } catch (Exception e) {
            logger.error("Erro ao abrir diálogo de criação avançada", e);
            showAlert("Erro", "Erro ao abrir diálogo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    
    private void handleLoadMessages() {
        String selectedQueue = viewQueueComboBox.getValue();
        
        if (selectedQueue == null || selectedQueue.isEmpty()) {
            String editorText = viewQueueComboBox.getEditor().getText();
            if (editorText != null && !editorText.trim().isEmpty()) {
                selectedQueue = editorText.trim();
                if (!queueNames.contains(selectedQueue)) {
                    showAlert("Erro", String.format("A fila '%s' não existe ou não foi carregada.", selectedQueue), Alert.AlertType.ERROR);
                    return;
                }
            } else {
                showAlert("Erro", "Selecione uma fila", Alert.AlertType.ERROR);
                return;
            }
        }
        
        final String queueName = selectedQueue;
        
        loadMessagesButton.setDisable(true);
        loadMessagesButton.setText("Carregando...");
        
        Task<ObservableList<MessageInfo>> loadTask = new Task<ObservableList<MessageInfo>>() {
            @Override
            protected ObservableList<MessageInfo> call() throws Exception {
                return serviceBusService.peekMessagesAsync(queueName, 20).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    messages.setAll(getValue());
                    loadMessagesButton.setDisable(false);
                    loadMessagesButton.setText("Carregar Mensagens");
                    addLogMessage(String.format("Carregadas %d mensagens da fila '%s'", getValue().size(), queueName));
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    loadMessagesButton.setDisable(false);
                    loadMessagesButton.setText("Carregar Mensagens");
                    showAlert("Erro", "Erro ao carregar mensagens: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(loadTask).start();
    }
    
    private void handleMessageSelection(MessageInfo selectedMessage) {
        if (selectedMessage == null) {
            messageDetailsTextArea.clear();
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("=== DETALHES DA MENSAGEM ===\n\n");
        details.append("Sequence Number: ").append(selectedMessage.getSequenceNumber()).append("\n");
        details.append("Message ID: ").append(selectedMessage.getMessageId()).append("\n");
        details.append("Content Type: ").append(selectedMessage.getContentType()).append("\n");
        details.append("Enqueued Time: ").append(selectedMessage.getFormattedEnqueuedTime()).append("\n");
        details.append("Size: ").append(selectedMessage.getFormattedSize()).append("\n");
        details.append("\n=== CORPO DA MENSAGEM ===\n");
        
        // Formatar o corpo da mensagem como JSON se possível
        String messageBody = selectedMessage.getMessageBody();
        if (messageBody != null && !messageBody.trim().isEmpty()) {
            if (isValidJson(messageBody.trim())) {
                // Se é JSON válido, formatar com indentação
                details.append(formatJson(messageBody.trim()));
            } else {
                // Se não é JSON, mostrar como texto normal
                details.append(messageBody);
            }
        } else {
            details.append("(Mensagem vazia)");
        }
        
        details.append("\n\n=== PROPRIEDADES ===\n");
        details.append(selectedMessage.getApplicationPropertiesAsString());
        
        messageDetailsTextArea.setText(details.toString());
    }
    
    /**
     * Formata uma string JSON com indentação básica
     */
    private String formatJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return jsonString;
        }
        
        try {
            // Formatação básica de JSON com indentação
            StringBuilder formatted = new StringBuilder();
            int indentLevel = 0;
            boolean inString = false;
            boolean escapeNext = false;
            
            for (int i = 0; i < jsonString.length(); i++) {
                char c = jsonString.charAt(i);
                
                if (escapeNext) {
                    formatted.append(c);
                    escapeNext = false;
                    continue;
                }
                
                if (c == '\\') {
                    formatted.append(c);
                    escapeNext = true;
                    continue;
                }
                
                if (c == '"' && !escapeNext) {
                    inString = !inString;
                    formatted.append(c);
                    continue;
                }
                
                if (inString) {
                    formatted.append(c);
                    continue;
                }
                
                switch (c) {
                    case '{':
                    case '[':
                        formatted.append(c);
                        indentLevel++;
                        formatted.append('\n');
                        addIndentation(formatted, indentLevel);
                        break;
                    case '}':
                    case ']':
                        formatted.append('\n');
                        indentLevel--;
                        addIndentation(formatted, indentLevel);
                        formatted.append(c);
                        break;
                    case ',':
                        formatted.append(c);
                        formatted.append('\n');
                        addIndentation(formatted, indentLevel);
                        break;
                    case ':':
                        formatted.append(c);
                        formatted.append(' ');
                        break;
                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                        // Ignorar espaços em branco desnecessários
                        break;
                    default:
                        formatted.append(c);
                        break;
                }
            }
            
            return formatted.toString();
        } catch (Exception e) {
            // Se houver erro na formatação, retornar o JSON original
            return jsonString;
        }
    }
    
    /**
     * Adiciona indentação ao StringBuilder
     */
    private void addIndentation(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
    }
    
    /**
     * Escapa caracteres especiais para JSON
     */
    private String escapeJsonString(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f");
    }
    
    /**
     * Verifica se uma string é um JSON válido
     */
    private boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = jsonString.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) || 
               (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }
    
    private void handleSendMessage() {
        String selectedQueue = sendQueueComboBox.getValue();
        String messageBody = messageBodyTextArea.getText().trim();
        
        if (selectedQueue == null) {
            showAlert("Erro", "Selecione uma fila", Alert.AlertType.ERROR);
            return;
        }
        
        if (messageBody.isEmpty()) {
            showAlert("Erro", "Digite o corpo da mensagem", Alert.AlertType.ERROR);
            return;
        }
        
        // Construir propriedades
        Map<String, Object> properties = new HashMap<>();
        if (!property1KeyField.getText().trim().isEmpty() && !property1ValueField.getText().trim().isEmpty()) {
            properties.put(property1KeyField.getText().trim(), property1ValueField.getText().trim());
        }
        if (!property2KeyField.getText().trim().isEmpty() && !property2ValueField.getText().trim().isEmpty()) {
            properties.put(property2KeyField.getText().trim(), property2ValueField.getText().trim());
        }
        
        sendMessageButton.setDisable(true);
        sendMessageButton.setText("Enviando...");
        
        Task<Boolean> sendTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return serviceBusService.sendMessageAsync(selectedQueue, messageBody, properties).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    sendMessageButton.setDisable(false);
                    sendMessageButton.setText("Enviar Mensagem");
                    
                    if (getValue()) {
                        addLogMessage(String.format("Mensagem enviada para fila '%s'", selectedQueue));
                        
                        // Mostrar diálogo de sucesso
                        showAlert("Sucesso", 
                            String.format("Mensagem enviada com sucesso para a fila '%s'!", selectedQueue), 
                            Alert.AlertType.INFORMATION);
                        
                        // Limpar apenas os campos de propriedades, mantendo o corpo da mensagem
                        property1KeyField.clear();
                        property1ValueField.clear();
                        property2KeyField.clear();
                        property2ValueField.clear();
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    sendMessageButton.setDisable(false);
                    sendMessageButton.setText("Enviar Mensagem");
                    showAlert("Erro", "Erro ao enviar mensagem: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(sendTask).start();
    }
    
    // ===========================================================================================
    // MÉTODOS PARA TÓPICOS
    // ===========================================================================================
    
    private void handleLoadTopics() {
        if (!serviceBusService.isConnected()) {
            showAlert("Erro", "Não conectado ao Service Bus", Alert.AlertType.ERROR);
            return;
        }
        
        loadTopicsButton.setDisable(true);
        loadTopicsButton.setText("Carregando...");
        
        Task<ObservableList<String>> loadTask = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws Exception {
                return serviceBusService.listTopicNamesAsync().get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    topicNames.setAll(getValue());
                    loadTopicsButton.setDisable(false);
                    loadTopicsButton.setText("Carregar Tópicos");
                    addLogMessage(String.format("Carregados %d tópicos", getValue().size()));
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    loadTopicsButton.setDisable(false);
                    loadTopicsButton.setText("Carregar Tópicos");
                    showAlert("Erro", "Erro ao carregar tópicos: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(loadTask).start();
    }
    
    private void filterTopics(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            topicListView.setItems(topicNames);
            return;
        }
        
        ObservableList<String> filteredTopics = topicNames.filtered(
            topicName -> topicName.toLowerCase().contains(filter.toLowerCase())
        );
        topicListView.setItems(filteredTopics);
    }
    
    private void filterSubscriptions(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            subscriptionsTable.setItems(subscriptionDetails);
            return;
        }
        
        ObservableList<SubscriptionInfo> filteredSubscriptions = subscriptionDetails.filtered(
            sub -> sub.getName().toLowerCase().contains(filter.toLowerCase())
        );
        subscriptionsTable.setItems(filteredSubscriptions);
    }
    
    private void handleTopicSelection(String selectedTopic) {
        if (selectedTopic == null || !serviceBusService.isConnected()) {
            return;
        }
        
        selectedTopicName = selectedTopic;
        selectedTopicLabel.setText("Tópico selecionado: " + selectedTopic);
        
        // Habilitar botões de subscription
        loadSubscriptionsButton.setDisable(false);
        newSubscriptionNameField.setDisable(false);
        createSubscriptionButton.setDisable(false);
        if (createAdvancedSubscriptionButton != null) {
            createAdvancedSubscriptionButton.setDisable(false);
        }
        
        // Carregar detalhes do tópico
        Task<TopicInfo> detailsTask = new Task<TopicInfo>() {
            @Override
            protected TopicInfo call() throws Exception {
                return serviceBusService.getTopicDetailsAsync(selectedTopic).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    topicDetails.setAll(getValue());
                    addLogMessage(String.format("Detalhes carregados para tópico '%s'", selectedTopic));
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> showAlert("Erro", "Erro ao carregar detalhes: " + getException().getMessage(), Alert.AlertType.ERROR));
            }
        };
        
        new Thread(detailsTask).start();
        
        // Carregar subscriptions automaticamente
        handleLoadSubscriptions();
    }
    
    private void handleCreateTopic() {
        String topicName = newTopicNameField.getText().trim();
        
        if (topicName.isEmpty()) {
            showAlert("Erro", "Digite um nome para o tópico", Alert.AlertType.ERROR);
            return;
        }
        
        createTopicButton.setDisable(true);
        
        Task<CreateQueueResult> createTask = new Task<CreateQueueResult>() {
            @Override
            protected CreateQueueResult call() throws Exception {
                return serviceBusService.createTopicAsync(topicName).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    createTopicButton.setDisable(false);
                    newTopicNameField.clear();
                    
                    CreateQueueResult result = getValue();
                    switch (result) {
                        case CREATED:
                            addLogMessage(String.format("Tópico '%s' criado com sucesso!", topicName));
                            showAlert("Sucesso", 
                                String.format("Tópico '%s' foi criado com sucesso!", topicName), 
                                Alert.AlertType.INFORMATION);
                            handleLoadTopics();
                            break;
                            
                        case ALREADY_EXISTS:
                            addLogMessage(String.format("Tópico '%s' já existe no namespace", topicName));
                            showAlert("Informação", 
                                String.format("O tópico '%s' já existe no namespace.\nVocê pode utilizá-lo normalmente.", topicName), 
                                Alert.AlertType.WARNING);
                            handleLoadTopics();
                            break;
                            
                        case ERROR:
                            showAlert("Erro", 
                                String.format("Erro ao criar tópico '%s'. Verifique os logs para mais detalhes.", topicName), 
                                Alert.AlertType.ERROR);
                            break;
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    createTopicButton.setDisable(false);
                    showAlert("Erro", "Erro ao criar tópico: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(createTask).start();
    }
    
    private void handleLoadSubscriptions() {
        if (selectedTopicName == null) {
            showAlert("Erro", "Selecione um tópico primeiro", Alert.AlertType.ERROR);
            return;
        }
        
        loadSubscriptionsButton.setDisable(true);
        loadSubscriptionsButton.setText("Carregando...");
        
        Task<ObservableList<String>> loadTask = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws Exception {
                return serviceBusService.listSubscriptionNamesAsync(selectedTopicName).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    ObservableList<String> subscriptionNames = getValue();
                    
                    // Carregar detalhes de cada subscription
                    subscriptionDetails.clear();
                    
                    if (subscriptionNames.isEmpty()) {
                        loadSubscriptionsButton.setDisable(false);
                        loadSubscriptionsButton.setText("Carregar Subscriptions");
                        addLogMessage(String.format("Nenhuma subscription encontrada no tópico '%s'", selectedTopicName));
                        return;
                    }
                    
                    // Carregar detalhes de todas as subscriptions
                    Task<Void> detailsTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            List<SubscriptionInfo> details = new ArrayList<>();
                            for (String subName : subscriptionNames) {
                                SubscriptionInfo subInfo = serviceBusService.getSubscriptionDetailsAsync(
                                    selectedTopicName, subName).get();
                                details.add(subInfo);
                            }
                            
                            Platform.runLater(() -> subscriptionDetails.setAll(details));
                            
                            return null;
                        }
                        
                        @Override
                        protected void succeeded() {
                            Platform.runLater(() -> {
                                loadSubscriptionsButton.setDisable(false);
                                loadSubscriptionsButton.setText("Carregar Subscriptions");
                                addLogMessage(String.format("Carregadas %d subscriptions do tópico '%s'", 
                                    subscriptionNames.size(), selectedTopicName));
                            });
                        }
                        
                        @Override
                        protected void failed() {
                            Platform.runLater(() -> {
                                loadSubscriptionsButton.setDisable(false);
                                loadSubscriptionsButton.setText("Carregar Subscriptions");
                                showAlert("Erro", "Erro ao carregar detalhes das subscriptions: " + 
                                    getException().getMessage(), Alert.AlertType.ERROR);
                            });
                        }
                    };
                    
                    new Thread(detailsTask).start();
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    loadSubscriptionsButton.setDisable(false);
                    loadSubscriptionsButton.setText("Carregar Subscriptions");
                    showAlert("Erro", "Erro ao carregar subscriptions: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(loadTask).start();
    }
    
    private void handleCreateSubscription() {
        if (selectedTopicName == null) {
            showAlert("Erro", "Selecione um tópico primeiro", Alert.AlertType.ERROR);
            return;
        }
        
        String subscriptionName = newSubscriptionNameField.getText().trim();
        
        if (subscriptionName.isEmpty()) {
            showAlert("Erro", "Digite um nome para a subscription", Alert.AlertType.ERROR);
            return;
        }
        
        // Validar limite de 50 caracteres do Azure
        if (subscriptionName.length() > 50) {
            showAlert("Erro", 
                "O nome da subscription não pode ter mais de 50 caracteres.\n" +
                "O Azure Service Bus limita o nome a 50 caracteres.\n" +
                "Tamanho atual: " + subscriptionName.length() + " caracteres", 
                Alert.AlertType.ERROR);
            return;
        }
        
        createSubscriptionButton.setDisable(true);
        
        Task<CreateQueueResult> createTask = new Task<CreateQueueResult>() {
            @Override
            protected CreateQueueResult call() throws Exception {
                return serviceBusService.createSubscriptionAsync(selectedTopicName, subscriptionName).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    createSubscriptionButton.setDisable(false);
                    newSubscriptionNameField.clear();
                    
                    CreateQueueResult result = getValue();
                    switch (result) {
                        case CREATED:
                            addLogMessage(String.format("Subscription '%s' criada com sucesso no tópico '%s'!", 
                                subscriptionName, selectedTopicName));
                            showAlert("Sucesso", 
                                String.format("Subscription '%s' foi criada com sucesso!", subscriptionName), 
                                Alert.AlertType.INFORMATION);
                            handleLoadSubscriptions();
                            break;
                            
                        case ALREADY_EXISTS:
                            addLogMessage(String.format("Subscription '%s' já existe no tópico '%s'", 
                                subscriptionName, selectedTopicName));
                            showAlert("Informação", 
                                String.format("A subscription '%s' já existe no tópico '%s'.\nVocê pode utilizá-la normalmente.", 
                                    subscriptionName, selectedTopicName), 
                                Alert.AlertType.WARNING);
                            handleLoadSubscriptions();
                            break;
                            
                        case ERROR:
                            showAlert("Erro", 
                                String.format("Erro ao criar subscription '%s'. Verifique os logs para mais detalhes.", 
                                    subscriptionName), 
                                Alert.AlertType.ERROR);
                            break;
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    createSubscriptionButton.setDisable(false);
                    showAlert("Erro", "Erro ao criar subscription: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(createTask).start();
    }
    
    /**
     * Abre diálogo para criação de subscription com configurações avançadas
     */
    private void handleCreateAdvancedSubscription() {
        if (selectedTopicName == null) {
            showAlert("Erro", "Selecione um tópico primeiro", Alert.AlertType.ERROR);
            return;
        }
        
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/create-subscription-dialog.fxml")
            );
            
            DialogPane dialogPane = loader.load();
            CreateSubscriptionDialogController dialogController = loader.getController();
            
            // Pré-preencher com o nome digitado, se houver
            String initialName = newSubscriptionNameField.getText().trim();
            if (!initialName.isEmpty()) {
                dialogController.setInitialSubscriptionName(initialName);
            }
            
            // Configurar tópico
            dialogController.setTopicName(selectedTopicName);
            
            // Criar o diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(primaryStage); // Garantir que dialog abra no mesmo monitor da janela principal
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Configurar Nova Subscription - " + selectedTopicName);
            dialog.setResizable(true);
            
            // Customizar textos dos botões
            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.setText("✅ Criar Subscription");
            }
            
            Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
            if (cancelButton != null) {
                cancelButton.setText("❌ Cancelar");
            }
            
            // Configurar o controller com o dialogPane
            dialogController.setDialogPane(dialogPane);
            
            // Exibir diálogo e processar resultado
            Optional<ButtonType> result = dialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Obter configuração completa do diálogo
                com.azureservicebus.manager.model.SubscriptionConfiguration config = dialogController.getConfiguration();
                
                if (config != null && config.isValid()) {
                    // Criar subscription com configurações customizadas
                    createAdvancedSubscriptionButton.setDisable(true);
                    
                    Task<CreateQueueResult> createTask = new Task<CreateQueueResult>() {
                        @Override
                        protected CreateQueueResult call() throws Exception {
                            return serviceBusService.createSubscriptionAsync(config).get();
                        }
                        
                        @Override
                        protected void succeeded() {
                            Platform.runLater(() -> {
                                createAdvancedSubscriptionButton.setDisable(false);
                                newSubscriptionNameField.clear();
                                
                                CreateQueueResult createResult = getValue();
                                switch (createResult) {
                                    case CREATED:
                                        StringBuilder msg = new StringBuilder();
                                        msg.append(String.format("Subscription '%s' criada com sucesso!\n\n", config.getName()));
                                        msg.append("Configurações aplicadas:\n");
                                        msg.append(String.format("• Max Delivery Count: %d\n", config.getMaxDeliveryCount()));
                                        msg.append(String.format("• Lock Duration: %d minuto(s)\n", config.getLockDurationMinutes()));
                                        msg.append(String.format("• Message TTL: %d dia(s)\n", config.getDefaultMessageTimeToLiveDays()));
                                        
                                        if (config.isFilterEnabled()) {
                                            msg.append(String.format("• Filtro: %s\n", config.getFilterType()));
                                        }
                                        
                                        addLogMessage(String.format("Subscription '%s' criada com configurações customizadas no tópico '%s'!", 
                                            config.getName(), selectedTopicName));
                                        showAlert("Sucesso", msg.toString(), Alert.AlertType.INFORMATION);
                                        handleLoadSubscriptions();
                                        break;
                                        
                                    case ALREADY_EXISTS:
                                        addLogMessage(String.format("Subscription '%s' já existe no tópico '%s'", 
                                            config.getName(), selectedTopicName));
                                        showAlert("Informação", 
                                            String.format("A subscription '%s' já existe no tópico '%s'.\nVocê pode utilizá-la normalmente.", 
                                                config.getName(), selectedTopicName), 
                                            Alert.AlertType.WARNING);
                                        handleLoadSubscriptions();
                                        break;
                                        
                                    case ERROR:
                                        showAlert("Erro", 
                                            String.format("Erro ao criar subscription '%s'. Verifique os logs para mais detalhes.", 
                                                config.getName()), 
                                            Alert.AlertType.ERROR);
                                        break;
                                }
                            });
                        }
                        
                        @Override
                        protected void failed() {
                            Platform.runLater(() -> {
                                createAdvancedSubscriptionButton.setDisable(false);
                                showAlert("Erro", "Erro ao criar subscription: " + getException().getMessage(), Alert.AlertType.ERROR);
                            });
                        }
                    };
                    
                    new Thread(createTask).start();
                }
            }
            
        } catch (Exception e) {
            logger.error("Erro ao abrir diálogo de criação avançada de subscription", e);
            showAlert("Erro", "Erro ao abrir diálogo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void createSimpleSubscription(String subscriptionName) {
        createAdvancedSubscriptionButton.setDisable(true);
        
        Task<CreateQueueResult> createTask = new Task<CreateQueueResult>() {
            @Override
            protected CreateQueueResult call() throws Exception {
                return serviceBusService.createSubscriptionAsync(selectedTopicName, subscriptionName).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    createAdvancedSubscriptionButton.setDisable(false);
                    newSubscriptionNameField.clear();
                    
                    CreateQueueResult result = getValue();
                    if (result == CreateQueueResult.CREATED) {
                        addLogMessage(String.format("Subscription '%s' criada com sucesso!", subscriptionName));
                        showAlert("Sucesso", 
                            String.format("Subscription '%s' foi criada com sucesso!", subscriptionName), 
                            Alert.AlertType.INFORMATION);
                        handleLoadSubscriptions();
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    createAdvancedSubscriptionButton.setDisable(false);
                    showAlert("Erro", "Erro ao criar subscription: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(createTask).start();
    }
    
    // ===========================================================================================
    // MÉTODOS PARA MENSAGENS DE TÓPICOS
    // ===========================================================================================
    
    private void handleLoadTopicMessages() {
        String selectedTopic = viewTopicComboBox.getValue();
        String selectedSubscription = viewSubscriptionComboBox.getValue();
        
        if (selectedTopic == null || selectedTopic.isEmpty()) {
            showAlert("Erro", "Selecione um tópico", Alert.AlertType.ERROR);
            return;
        }
        
        if (selectedSubscription == null || selectedSubscription.isEmpty()) {
            showAlert("Erro", "Selecione uma subscription", Alert.AlertType.ERROR);
            return;
        }
        
        loadTopicMessagesButton.setDisable(true);
        loadTopicMessagesButton.setText("Carregando...");
        
        Task<ObservableList<MessageInfo>> loadTask = new Task<ObservableList<MessageInfo>>() {
            @Override
            protected ObservableList<MessageInfo> call() throws Exception {
                return serviceBusService.peekSubscriptionMessagesAsync(selectedTopic, selectedSubscription, 20).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    topicMessages.setAll(getValue());
                    loadTopicMessagesButton.setDisable(false);
                    loadTopicMessagesButton.setText("Carregar Mensagens");
                    addLogMessage(String.format("Carregadas %d mensagens da subscription '%s' do tópico '%s'", 
                        getValue().size(), selectedSubscription, selectedTopic));
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    loadTopicMessagesButton.setDisable(false);
                    loadTopicMessagesButton.setText("Carregar Mensagens");
                    showAlert("Erro", "Erro ao carregar mensagens: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(loadTask).start();
    }
    
    private void handleTopicMessageSelection(MessageInfo selectedMessage) {
        if (selectedMessage == null) {
            topicMessageDetailsTextArea.clear();
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("=== DETALHES DA MENSAGEM ===\n\n");
        details.append("Sequence Number: ").append(selectedMessage.getSequenceNumber()).append("\n");
        details.append("Message ID: ").append(selectedMessage.getMessageId()).append("\n");
        details.append("Content Type: ").append(selectedMessage.getContentType()).append("\n");
        details.append("Enqueued Time: ").append(selectedMessage.getFormattedEnqueuedTime()).append("\n");
        details.append("Size: ").append(selectedMessage.getFormattedSize()).append("\n");
        details.append("\n=== CORPO DA MENSAGEM ===\n");
        
        String messageBody = selectedMessage.getMessageBody();
        if (messageBody != null && !messageBody.trim().isEmpty()) {
            if (isValidJson(messageBody.trim())) {
                details.append(formatJson(messageBody.trim()));
            } else {
                details.append(messageBody);
            }
        } else {
            details.append("(Mensagem vazia)");
        }
        
        details.append("\n\n=== PROPRIEDADES ===\n");
        details.append(selectedMessage.getApplicationPropertiesAsString());
        
        topicMessageDetailsTextArea.setText(details.toString());
    }
    
    private void handleSendMessageToTopic() {
        String selectedTopic = sendTopicComboBox.getValue();
        String messageBody = sendTopicMessageBodyTextArea.getText().trim();
        
        if (selectedTopic == null) {
            showAlert("Erro", "Selecione um tópico", Alert.AlertType.ERROR);
            return;
        }
        
        if (messageBody.isEmpty()) {
            showAlert("Erro", "Digite o corpo da mensagem", Alert.AlertType.ERROR);
            return;
        }
        
        // Construir propriedades
        Map<String, Object> properties = new HashMap<>();
        if (!sendTopicProperty1KeyField.getText().trim().isEmpty() && !sendTopicProperty1ValueField.getText().trim().isEmpty()) {
            properties.put(sendTopicProperty1KeyField.getText().trim(), sendTopicProperty1ValueField.getText().trim());
        }
        if (!sendTopicProperty2KeyField.getText().trim().isEmpty() && !sendTopicProperty2ValueField.getText().trim().isEmpty()) {
            properties.put(sendTopicProperty2KeyField.getText().trim(), sendTopicProperty2ValueField.getText().trim());
        }
        
        sendToTopicButton.setDisable(true);
        sendToTopicButton.setText("Enviando...");
        
        Task<Boolean> sendTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return serviceBusService.sendMessageToTopicAsync(selectedTopic, messageBody, properties).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    sendToTopicButton.setDisable(false);
                    sendToTopicButton.setText("Publicar no Tópico");
                    
                    if (getValue()) {
                        addLogMessage(String.format("Mensagem publicada no tópico '%s'", selectedTopic));
                        
                        showAlert("Sucesso", 
                            String.format("Mensagem publicada com sucesso no tópico '%s'!\nTodas as subscriptions receberão a mensagem.", selectedTopic), 
                            Alert.AlertType.INFORMATION);
                        
                        // Limpar campos de propriedades
                        sendTopicProperty1KeyField.clear();
                        sendTopicProperty1ValueField.clear();
                        sendTopicProperty2KeyField.clear();
                        sendTopicProperty2ValueField.clear();
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    sendToTopicButton.setDisable(false);
                    sendToTopicButton.setText("Publicar no Tópico");
                    showAlert("Erro", "Erro ao publicar mensagem: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(sendTask).start();
    }
    
    private void handleShowQueueDetails(QueueInfo queueInfo) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/queue-details-dialog.fxml")
            );
            
            DialogPane dialogPane = loader.load();
            QueueDetailsDialogController dialogController = loader.getController();
            
            dialogController.setQueue(queueInfo.getName(), serviceBusService);
            dialogController.setDialogPane(dialogPane);
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(primaryStage); // Garantir que dialog abra no mesmo monitor da janela principal
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Detalhes da Fila - " + queueInfo.getName());
            dialog.setResizable(true);
            
            dialog.showAndWait();
            
        } catch (Exception e) {
            logger.error("Erro ao abrir diálogo de detalhes da fila", e);
            showAlert("Erro", "Erro ao abrir diálogo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void handleShowSubscriptionDetails(SubscriptionInfo subInfo) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/subscription-details-dialog.fxml")
            );
            
            DialogPane dialogPane = loader.load();
            SubscriptionDetailsDialogController dialogController = loader.getController();
            
            dialogController.setSubscription(subInfo.getTopicName(), subInfo.getName(), serviceBusService);
            dialogController.setDialogPane(dialogPane);
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(primaryStage); // Garantir que dialog abra no mesmo monitor da janela principal
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Detalhes da Subscription - " + subInfo.getTopicName() + "/" + subInfo.getName());
            dialog.setResizable(true);
            
            dialog.showAndWait();
            
        } catch (Exception e) {
            logger.error("Erro ao abrir diálogo de detalhes", e);
            showAlert("Erro", "Erro ao abrir diálogo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void handleManageRules(SubscriptionInfo subInfo) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/rules-dialog.fxml")
            );
            
            DialogPane dialogPane = loader.load();
            RulesDialogController dialogController = loader.getController();
            
            dialogController.setSubscriptionInfo(subInfo.getTopicName(), subInfo.getName(), serviceBusService);
            dialogController.setDialogPane(dialogPane);
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(primaryStage); // Garantir que dialog abra no mesmo monitor da janela principal
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Gerenciar Rules - " + subInfo.getTopicName() + "/" + subInfo.getName());
            
            dialogPane.getButtonTypes().setAll(ButtonType.CLOSE);
            
            dialog.showAndWait();
            
        } catch (Exception e) {
            logger.error("Erro ao abrir diálogo de rules", e);
            showAlert("Erro", "Erro ao abrir diálogo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void handleDeleteSubscription(SubscriptionInfo subInfo) {
        Optional<ButtonType> result = showConfirmation(
            "Confirmar Remoção",
            String.format("Tem certeza que deseja remover a subscription '%s' do tópico '%s'?\n\nEsta operação é irreversível!", 
                subInfo.getName(), subInfo.getTopicName())
        );
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> deleteTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return serviceBusService.deleteSubscriptionAsync(subInfo.getTopicName(), subInfo.getName()).get();
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        if (getValue()) {
                            addLogMessage(String.format("Subscription '%s' removida com sucesso!", subInfo.getName()));
                            handleLoadSubscriptions();
                        }
                    });
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        showAlert("Erro", "Erro ao remover subscription: " + getException().getMessage(), Alert.AlertType.ERROR);
                    });
                }
            };
            
            new Thread(deleteTask).start();
        }
    }
    
    private void handleClearSubscriptionMessages(SubscriptionInfo subInfo) {
        Optional<ButtonType> result = showConfirmation(
            "Confirmar Limpeza",
            String.format("Tem certeza que deseja limpar TODAS as mensagens da subscription '%s'?\n\nEsta operação é irreversível!", 
                subInfo.getName())
        );
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Integer> clearTask = new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {
                    return serviceBusService.clearSubscriptionMessagesAsync(subInfo.getTopicName(), subInfo.getName()).get();
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        addLogMessage(String.format("%d mensagens removidas da subscription '%s'", getValue(), subInfo.getName()));
                        handleLoadSubscriptions();
                    });
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        showAlert("Erro", "Erro ao limpar mensagens: " + getException().getMessage(), Alert.AlertType.ERROR);
                    });
                }
            };
            
            new Thread(clearTask).start();
        }
    }
    
    private void loadSubscriptionsForTopic(String topicName) {
        if (topicName == null || topicName.isEmpty()) {
            subscriptionNames.clear();
            return;
        }
        
        Task<ObservableList<String>> loadTask = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws Exception {
                return serviceBusService.listSubscriptionNamesAsync(topicName).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    subscriptionNames.setAll(getValue());
                    addLogMessage(String.format("Carregadas %d subscriptions do tópico '%s'", getValue().size(), topicName));
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    subscriptionNames.clear();
                    logger.warn("Erro ao carregar subscriptions: " + getException().getMessage());
                });
            }
        };
        
        new Thread(loadTask).start();
    }
    
    private void addLogMessage(String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String logEntry = String.format("[%s] %s\n", timestamp, message);
        
        logTextArea.appendText(logEntry);
        
        // Manter apenas últimas 100 linhas
        String[] lines = logTextArea.getText().split("\n");
        if (lines.length > 100) {
            StringBuilder sb = new StringBuilder();
            for (int i = lines.length - 100; i < lines.length; i++) {
                sb.append(lines[i]).append("\n");
            }
            logTextArea.setText(sb.toString());
        }
        
        // Scroll para o final
        logTextArea.setScrollTop(Double.MAX_VALUE);
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.initOwner(primaryStage); // Garantir que alert abra no mesmo monitor da janela principal
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(primaryStage); // Garantir que alert abra no mesmo monitor da janela principal
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Aplica a cor do perfil como borda na área do namespace
     */
    private void applyProfileColor(String color) {
        if (namespaceBox == null || color == null) {
            return;
        }
        
        // Aplicar apenas borda com a cor do perfil na área do namespace
        namespaceBox.setStyle(
            "-fx-border-color: " + color + "; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px;"
        );
        
        logger.info("Borda com cor do perfil aplicada na área do namespace: {}", color);
    }
    
    public void shutdown() {
        if (serviceBusService != null) {
            serviceBusService.shutdown();
        }
    }
}
