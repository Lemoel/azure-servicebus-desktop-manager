package com.azureservicebus.manager.controller;

import com.azureservicebus.manager.model.QueueInfo;
import com.azureservicebus.manager.model.MessageInfo;
import com.azureservicebus.manager.service.ServiceBusService;
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
import java.util.HashMap;
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
    @FXML private VBox connectionPane;
    @FXML private TextArea connectionStringTextArea;
    @FXML private Button connectButton;
    @FXML private Button disconnectButton;
    @FXML private Label connectionStatusLabel;
    @FXML private Label namespaceLabel;
    
    // Componentes da interface - Filas
    @FXML private TabPane mainTabPane;
    @FXML private Tab queuesTab;
    @FXML private Tab messagesTab;
    @FXML private Tab sendMessageTab;
    
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
    
    // Aba de Mensagens
    @FXML private ComboBox<String> viewQueueComboBox;
    @FXML private Button loadMessagesButton;
    @FXML private TableView<MessageInfo> messagesTable;
    @FXML private TableColumn<MessageInfo, Long> sequenceNumberColumn;
    @FXML private TableColumn<MessageInfo, String> messageIdColumn;
    @FXML private TableColumn<MessageInfo, String> messageBodyColumn;
    @FXML private TableColumn<MessageInfo, String> enqueuedTimeColumn;
    @FXML private TextArea messageDetailsTextArea;
    
    // Aba de Envio de Mensagens
    @FXML private ComboBox<String> sendQueueComboBox;
    @FXML private TextArea messageBodyTextArea;
    @FXML private TextField property1KeyField;
    @FXML private TextField property1ValueField;
    @FXML private TextField property2KeyField;
    @FXML private TextField property2ValueField;
    @FXML private Button sendMessageButton;
    
    // Log
    @FXML private TextArea logTextArea;
    @FXML private Button clearLogButton;
    
    // Serviços e dados
    private ServiceBusService serviceBusService;
    private Stage primaryStage;
    private ObservableList<String> queueNames = FXCollections.observableArrayList();
    private ObservableList<QueueInfo> queueDetails = FXCollections.observableArrayList();
    private ObservableList<MessageInfo> messages = FXCollections.observableArrayList();
    
    // Flags para prevenir loops infinitos nas ComboBoxes
    private boolean updatingViewQueueComboBox = false;
    private boolean updatingSendQueueComboBox = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando MainController");
        
        // Inicializar serviço
        serviceBusService = new ServiceBusService();
        setupServiceCallbacks();
        
        // Configurar interface inicial
        setupInitialUI();
        setupTableColumns();
        setupEventHandlers();
        
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
        
        // Configurar listas
        queueListView.setItems(queueNames);
        queueDetailsTable.setItems(queueDetails);
        messagesTable.setItems(messages);
        
        // Configurar ComboBoxes
        viewQueueComboBox.setItems(queueNames);
        sendQueueComboBox.setItems(queueNames);
        
        // Placeholder para connection string
        connectionStringTextArea.setPromptText(
            "Cole aqui a connection string do Azure Service Bus:\n" +
            "Endpoint=sb://your-namespace.servicebus.windows.net/;SharedAccessKeyName=...;SharedAccessKey=..."
        );
        
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
    }
    
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(new Callback<TableColumn<QueueInfo, Void>, TableCell<QueueInfo, Void>>() {
            @Override
            public TableCell<QueueInfo, Void> call(TableColumn<QueueInfo, Void> param) {
                return new TableCell<QueueInfo, Void>() {
                    private final Button deleteButton = new Button("🗑️");
                    private final Button clearButton = new Button("🧹");
                    private final HBox actionBox = new HBox(5);
                    
                    {
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
                        
                        // Configurar container
                        actionBox.setAlignment(Pos.CENTER);
                        actionBox.getChildren().addAll(deleteButton, clearButton);
                        
                        // Event handlers
                        deleteButton.setOnAction(event -> {
                            QueueInfo queueInfo = getTableView().getItems().get(getIndex());
                            handleDeleteQueueFromTable(queueInfo.getName());
                        });
                        
                        clearButton.setOnAction(event -> {
                            QueueInfo queueInfo = getTableView().getItems().get(getIndex());
                            handleClearMessagesFromTable(queueInfo.getName());
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
    
    private void setupEventHandlers() {
        // Conexão
        connectButton.setOnAction(e -> handleConnect());
        disconnectButton.setOnAction(e -> handleDisconnect());
        
        // Filas
        loadQueuesButton.setOnAction(e -> handleLoadQueues());
        queueFilterField.textProperty().addListener((obs, oldVal, newVal) -> filterQueues(newVal));
        queueListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> handleQueueSelection(newVal)
        );
        
        createQueueButton.setOnAction(e -> handleCreateQueue());
        
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
        
        // Log
        clearLogButton.setOnAction(e -> logTextArea.clear());
    }
    
    private void setupViewQueueComboBoxFilter() {
        // Tornar a ComboBox editável e configurar filtro
        viewQueueComboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingViewQueueComboBox) {
                return; // Prevenir loops infinitos
            }
            
            updatingViewQueueComboBox = true;
            try {
                if (newVal == null || newVal.isEmpty()) {
                    viewQueueComboBox.setItems(queueNames);
                } else {
                    ObservableList<String> filteredQueues = queueNames.filtered(
                        queueName -> queueName.toLowerCase().contains(newVal.toLowerCase())
                    );
                    viewQueueComboBox.setItems(filteredQueues);
                    
                    // Manter o dropdown aberto durante a digitação
                    if (!viewQueueComboBox.isShowing()) {
                        viewQueueComboBox.show();
                    }
                }
            } finally {
                updatingViewQueueComboBox = false;
            }
        });
        
        // Quando o usuário seleciona um item da lista
        viewQueueComboBox.setOnAction(e -> {
            String selectedQueue = viewQueueComboBox.getValue();
            if (selectedQueue != null && !selectedQueue.isEmpty()) {
                viewQueueComboBox.getEditor().setText(selectedQueue);
            }
        });
        
        // Quando o campo perde o foco, validar se o texto digitado corresponde a uma fila válida
        viewQueueComboBox.getEditor().focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !updatingViewQueueComboBox) { // Perdeu o foco e não está em atualização
                updatingViewQueueComboBox = true;
                try {
                    String typedText = viewQueueComboBox.getEditor().getText();
                    String currentValue = viewQueueComboBox.getValue();
                    
                    // Restaurar a lista completa quando perder o foco
                    viewQueueComboBox.setItems(queueNames);
                    
                    if (typedText != null && !typedText.isEmpty()) {
                        // Verificar se o texto digitado corresponde exatamente a uma fila
                        boolean found = queueNames.stream().anyMatch(queue -> queue.equals(typedText));
                        if (found) {
                            viewQueueComboBox.setValue(typedText);
                        } else {
                            // Se não encontrou correspondência exata, limpar a seleção
                            viewQueueComboBox.setValue(null);
                            viewQueueComboBox.getEditor().clear();
                        }
                    } else if (currentValue != null) {
                        // Se o campo está vazio mas há um valor selecionado, manter o valor
                        viewQueueComboBox.setValue(currentValue);
                    }
                } finally {
                    updatingViewQueueComboBox = false;
                }
            }
        });
    }
    
    private void setupSendQueueComboBoxFilter() {
        // Tornar a ComboBox editável e configurar filtro
        sendQueueComboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingSendQueueComboBox) {
                return; // Prevenir loops infinitos
            }
            
            updatingSendQueueComboBox = true;
            try {
                if (newVal == null || newVal.isEmpty()) {
                    sendQueueComboBox.setItems(queueNames);
                } else {
                    ObservableList<String> filteredQueues = queueNames.filtered(
                        queueName -> queueName.toLowerCase().contains(newVal.toLowerCase())
                    );
                    sendQueueComboBox.setItems(filteredQueues);
                    
                    // Manter o dropdown aberto durante a digitação
                    if (!sendQueueComboBox.isShowing()) {
                        sendQueueComboBox.show();
                    }
                }
            } finally {
                updatingSendQueueComboBox = false;
            }
        });
        
        // Quando o usuário seleciona um item da lista
        sendQueueComboBox.setOnAction(e -> {
            String selectedQueue = sendQueueComboBox.getValue();
            if (selectedQueue != null && !selectedQueue.isEmpty()) {
                sendQueueComboBox.getEditor().setText(selectedQueue);
            }
        });
        
        // Quando o campo perde o foco, validar se o texto digitado corresponde a uma fila válida
        sendQueueComboBox.getEditor().focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !updatingSendQueueComboBox) { // Perdeu o foco e não está em atualização
                updatingSendQueueComboBox = true;
                try {
                    String typedText = sendQueueComboBox.getEditor().getText();
                    String currentValue = sendQueueComboBox.getValue();
                    
                    // Restaurar a lista completa quando perder o foco
                    sendQueueComboBox.setItems(queueNames);
                    
                    if (typedText != null && !typedText.isEmpty()) {
                        // Verificar se o texto digitado corresponde exatamente a uma fila
                        boolean found = queueNames.stream().anyMatch(queue -> queue.equals(typedText));
                        if (found) {
                            sendQueueComboBox.setValue(typedText);
                        } else {
                            // Se não encontrou correspondência exata, limpar a seleção
                            sendQueueComboBox.setValue(null);
                            sendQueueComboBox.getEditor().clear();
                        }
                    } else if (currentValue != null) {
                        // Se o campo está vazio mas há um valor selecionado, manter o valor
                        sendQueueComboBox.setValue(currentValue);
                    }
                } finally {
                    updatingSendQueueComboBox = false;
                }
            }
        });
    }
    
    private void updateConnectionStatus() {
        boolean connected = serviceBusService.isConnected();
        
        connectionPane.setVisible(!connected);
        connectionPane.setManaged(!connected);
        
        connectButton.setDisable(connected);
        disconnectButton.setDisable(!connected);
        
        if (connected) {
            connectionStatusLabel.setText("✅ Conectado");
            connectionStatusLabel.setStyle("-fx-text-fill: green;");
            
            String namespace = serviceBusService.extractNamespace();
            namespaceLabel.setText("Namespace: " + (namespace != null ? namespace : "N/A"));
            
            // Habilitar abas
            queuesTab.setDisable(false);
            messagesTab.setDisable(false);
            sendMessageTab.setDisable(false);
            
        } else {
            connectionStatusLabel.setText("❌ Desconectado");
            connectionStatusLabel.setStyle("-fx-text-fill: red;");
            namespaceLabel.setText("Namespace: N/A");
            
            // Desabilitar abas
            queuesTab.setDisable(true);
            messagesTab.setDisable(true);
            sendMessageTab.setDisable(true);
            
            // Limpar seleções primeiro para evitar IndexOutOfBoundsException
            try {
                queueListView.getSelectionModel().clearSelection();
                queueDetailsTable.getSelectionModel().clearSelection();
                messagesTable.getSelectionModel().clearSelection();
                viewQueueComboBox.getSelectionModel().clearSelection();
                sendQueueComboBox.getSelectionModel().clearSelection();
            } catch (Exception e) {
                logger.warn("Erro ao limpar seleções: " + e.getMessage());
            }
            
            // Limpar dados de forma segura
            try {
                queueNames.clear();
            } catch (Exception e) {
                logger.warn("Erro ao limpar queueNames: " + e.getMessage());
            }
            
            try {
                queueDetails.clear();
            } catch (Exception e) {
                logger.warn("Erro ao limpar queueDetails: " + e.getMessage());
            }
            
            try {
                messages.clear();
            } catch (Exception e) {
                logger.warn("Erro ao limpar messages: " + e.getMessage());
            }
        }
    }
    
    private void handleConnect() {
        String connectionString = connectionStringTextArea.getText().trim();
        
        if (connectionString.isEmpty()) {
            showAlert("Erro", "Por favor, insira a connection string.", Alert.AlertType.ERROR);
            return;
        }
        
        if (!connectionString.contains("Endpoint=sb://") || !connectionString.contains("SharedAccessKey")) {
            showAlert("Erro", "Formato de connection string inválido.", Alert.AlertType.ERROR);
            return;
        }
        
        connectButton.setDisable(true);
        connectButton.setText("Conectando...");
        
        Task<Boolean> connectTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return serviceBusService.connectAsync(connectionString).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    connectButton.setDisable(false);
                    connectButton.setText("Conectar");
                    
                    if (getValue()) {
                        addLogMessage("Conexão estabelecida com sucesso!");
                    } else {
                        showAlert("Erro", "Falha na conexão. Verifique a connection string.", Alert.AlertType.ERROR);
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    connectButton.setDisable(false);
                    connectButton.setText("Conectar");
                    showAlert("Erro", "Erro ao conectar: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(connectTask).start();
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
        
        Task<Boolean> createTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return serviceBusService.createQueueAsync(queueName).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    createQueueButton.setDisable(false);
                    newQueueNameField.clear();
                    
                    if (getValue()) {
                        addLogMessage(String.format("Fila '%s' criada com sucesso!", queueName));
                        handleLoadQueues(); // Recarregar lista
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
    
    
    private void handleLoadMessages() {
        String selectedQueue = viewQueueComboBox.getValue();
        
        if (selectedQueue == null) {
            showAlert("Erro", "Selecione uma fila", Alert.AlertType.ERROR);
            return;
        }
        
        loadMessagesButton.setDisable(true);
        loadMessagesButton.setText("Carregando...");
        
        Task<ObservableList<MessageInfo>> loadTask = new Task<ObservableList<MessageInfo>>() {
            @Override
            protected ObservableList<MessageInfo> call() throws Exception {
                return serviceBusService.peekMessagesAsync(selectedQueue, 20).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    messages.setAll(getValue());
                    loadMessagesButton.setDisable(false);
                    loadMessagesButton.setText("Carregar Mensagens");
                    addLogMessage(String.format("Carregadas %d mensagens da fila '%s'", getValue().size(), selectedQueue));
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
        details.append(selectedMessage.getMessageBody()).append("\n");
        details.append("\n=== PROPRIEDADES ===\n");
        details.append(selectedMessage.getApplicationPropertiesAsString());
        
        messageDetailsTextArea.setText(details.toString());
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
                        messageBodyTextArea.clear();
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
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    public void shutdown() {
        if (serviceBusService != null) {
            serviceBusService.shutdown();
        }
    }
}
