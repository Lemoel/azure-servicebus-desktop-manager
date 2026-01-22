package com.azureservicebus.manager.controller;

import com.azureservicebus.manager.model.ConnectionProfile;
import com.azureservicebus.manager.service.ProfileService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller para o diálogo de gerenciamento de perfis
 */
public class ProfileManagerDialogController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileManagerDialogController.class);
    
    @FXML private TableView<ConnectionProfile> profilesTable;
    @FXML private TableColumn<ConnectionProfile, String> activeColumn;
    @FXML private TableColumn<ConnectionProfile, String> nameColumn;
    @FXML private TableColumn<ConnectionProfile, String> namespaceColumn;
    @FXML private TableColumn<ConnectionProfile, String> lastUsedColumn;
    @FXML private TableColumn<ConnectionProfile, Void> actionsColumn;
    @FXML private Label profileCountLabel;
    
    @FXML private Label formTitleLabel;
    @FXML private TextField profileNameField;
    @FXML private TextArea connectionStringTextArea;
    @FXML private Button saveProfileButton;
    @FXML private Button newProfileButton;
    @FXML private Button cancelEditButton;
    @FXML private Label formStatusLabel;
    @FXML private ToggleButton greenColorButton;
    @FXML private ToggleButton orangeColorButton;
    @FXML private ToggleButton redColorButton;
    
    private ProfileService profileService;
    private ObservableList<ConnectionProfile> profiles;
    private boolean isEditMode = false;
    private String editingProfileName = null;
    private ToggleGroup colorToggleGroup;
    private String selectedColor = "#28a745"; // Verde como padrão
    
    private Runnable onProfileChangedCallback;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando ProfileManagerDialogController");
        
        profileService = ProfileService.getInstance();
        profiles = FXCollections.observableArrayList();
        
        // Configurar ToggleGroup para os botões de cor
        colorToggleGroup = new ToggleGroup();
        greenColorButton.setToggleGroup(colorToggleGroup);
        orangeColorButton.setToggleGroup(colorToggleGroup);
        redColorButton.setToggleGroup(colorToggleGroup);
        
        // Selecionar verde como padrão
        greenColorButton.setSelected(true);
        
        setupTableColumns();
        setupEventHandlers();
        loadProfiles();
        
        logger.info("ProfileManagerDialogController inicializado com sucesso");
    }
    
    /**
     * Handler para seleção de cor
     */
    @FXML
    private void handleColorSelection() {
        Toggle selectedToggle = colorToggleGroup.getSelectedToggle();
        if (selectedToggle != null) {
            ToggleButton selectedButton = (ToggleButton) selectedToggle;
            selectedColor = (String) selectedButton.getUserData();
            logger.debug("Cor selecionada: {}", selectedColor);
        }
    }
    
    /**
     * Define callback para notificar mudanças de perfil
     */
    public void setOnProfileChangedCallback(Runnable callback) {
        this.onProfileChangedCallback = callback;
    }
    
    private void setupTableColumns() {
        profilesTable.setItems(profiles);
        
        // Coluna de ativo (ícone)
        activeColumn.setCellValueFactory(cellData -> {
            String activeProfileName = profileService.getActiveProfileName();
            boolean isActive = cellData.getValue().getName().equals(activeProfileName);
            return new javafx.beans.property.SimpleStringProperty(isActive ? "✅" : "");
        });
        activeColumn.setStyle("-fx-alignment: CENTER;");
        
        // Coluna de nome
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        // Coluna de namespace
        namespaceColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNamespace())
        );
        
        // Coluna de último uso
        lastUsedColumn.setCellValueFactory(cellData -> {
            String lastUsed = cellData.getValue().getLastUsedAt();
            String formatted = formatDateTime(lastUsed);
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });
        
        // Coluna de ações
        setupActionsColumn();
        
        // Permitir edição ao clicar em uma linha
        profilesTable.setRowFactory(tv -> {
            TableRow<ConnectionProfile> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    switchToEditMode(row.getItem());
                }
            });
            return row;
        });
    }
    
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<ConnectionProfile, Void>() {
            private final Button editButton = new Button("✏️");
            private final Button deleteButton = new Button("🗑️");
            private final HBox actionBox = new HBox(5);
            
            {
                editButton.setStyle("-fx-font-size: 12px; -fx-cursor: hand;");
                editButton.setTooltip(new Tooltip("Editar perfil"));
                
                deleteButton.setStyle("-fx-font-size: 12px; -fx-cursor: hand;");
                deleteButton.setTooltip(new Tooltip("Excluir perfil"));
                
                actionBox.setAlignment(Pos.CENTER);
                actionBox.getChildren().addAll(editButton, deleteButton);
                
                editButton.setOnAction(event -> {
                    ConnectionProfile profile = getTableView().getItems().get(getIndex());
                    switchToEditMode(profile);
                });
                
                deleteButton.setOnAction(event -> {
                    ConnectionProfile profile = getTableView().getItems().get(getIndex());
                    handleDeleteProfile(profile);
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
        });
    }
    
    private void setupEventHandlers() {
        saveProfileButton.setOnAction(e -> handleSaveProfile());
        newProfileButton.setOnAction(e -> switchToCreateMode());
        cancelEditButton.setOnAction(e -> switchToCreateMode());
    }
    
    private void loadProfiles() {
        profiles.clear();
        profiles.addAll(profileService.getAllProfiles());
        
        int count = profiles.size();
        profileCountLabel.setText(count + (count == 1 ? " perfil" : " perfis"));
        
        logger.info("Carregados {} perfis no gerenciador", count);
    }
    
    private void switchToCreateMode() {
        isEditMode = false;
        editingProfileName = null;
        
        formTitleLabel.setText("➕ Criar Novo Perfil");
        saveProfileButton.setText("💾 Salvar Perfil");
        newProfileButton.setVisible(false);
        newProfileButton.setManaged(false);
        cancelEditButton.setVisible(false);
        cancelEditButton.setManaged(false);
        
        profileNameField.setDisable(false);
        profileNameField.clear();
        connectionStringTextArea.clear();
        
        hideFormStatus();
        
        profilesTable.getSelectionModel().clearSelection();
    }
    
    private void switchToEditMode(ConnectionProfile profile) {
        isEditMode = true;
        editingProfileName = profile.getName();
        
        formTitleLabel.setText("✏️ Editar Perfil: " + profile.getName());
        saveProfileButton.setText("💾 Salvar Alterações");
        newProfileButton.setVisible(true);
        newProfileButton.setManaged(true);
        cancelEditButton.setVisible(true);
        cancelEditButton.setManaged(true);
        
        profileNameField.setDisable(true);
        profileNameField.setText(profile.getName());
        connectionStringTextArea.setText(profile.getConnectionString());
        
        // Selecionar a cor do perfil
        String profileColor = profile.getColor();
        if (profileColor != null) {
            selectedColor = profileColor;
            switch (profileColor) {
                case "#28a745":
                    greenColorButton.setSelected(true);
                    break;
                case "#fd7e14":
                    orangeColorButton.setSelected(true);
                    break;
                case "#dc3545":
                    redColorButton.setSelected(true);
                    break;
                default:
                    greenColorButton.setSelected(true);
            }
        } else {
            greenColorButton.setSelected(true);
            selectedColor = "#28a745";
        }
        
        hideFormStatus();
    }
    
    private void handleSaveProfile() {
        hideFormStatus();
        
        String profileName = profileNameField.getText().trim();
        String connectionString = connectionStringTextArea.getText().trim();
        
        // Validações
        if (profileName.isEmpty()) {
            showFormError("Por favor, informe um nome para o perfil.");
            profileNameField.requestFocus();
            return;
        }
        
        if (connectionString.isEmpty()) {
            showFormError("Por favor, informe a connection string.");
            connectionStringTextArea.requestFocus();
            return;
        }
        
        if (!connectionString.contains("Endpoint=sb://") || 
            !connectionString.contains("SharedAccessKeyName=") || 
            !connectionString.contains("SharedAccessKey=")) {
            showFormError("Connection string inválida. Verifique o formato.");
            connectionStringTextArea.requestFocus();
            return;
        }
        
        try {
            if (isEditMode) {
                // Atualizar perfil existente
                ConnectionProfile profile = new ConnectionProfile(editingProfileName, connectionString);
                profile.setColor(selectedColor);
                profileService.updateProfile(profile);
                showFormSuccess("Perfil atualizado com sucesso!");
                logger.info("Perfil '{}' atualizado com cor {}", editingProfileName, selectedColor);
            } else {
                // Criar novo perfil
                ConnectionProfile profile = new ConnectionProfile(profileName, connectionString);
                profile.setColor(selectedColor);
                profileService.addProfile(profile);
                showFormSuccess("Perfil criado com sucesso!");
                logger.info("Perfil '{}' criado com cor {}", profileName, selectedColor);
            }
            
            // Recarregar lista
            loadProfiles();
            
            // Voltar para modo criação
            Platform.runLater(() -> {
                try {
                    Thread.sleep(1500);
                    switchToCreateMode();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            // Notificar mudança
            if (onProfileChangedCallback != null) {
                onProfileChangedCallback.run();
            }
            
        } catch (Exception e) {
            logger.error("Erro ao salvar perfil", e);
            showFormError("Erro ao salvar perfil: " + e.getMessage());
        }
    }
    
    private void handleDeleteProfile(ConnectionProfile profile) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmar Exclusão");
        confirmation.setHeaderText("Excluir perfil '" + profile.getName() + "'?");
        confirmation.setContentText("Esta ação não pode ser desfeita.");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                profileService.deleteProfile(profile.getName());
                logger.info("Perfil '{}' excluído", profile.getName());
                
                loadProfiles();
                switchToCreateMode();
                
                // Notificar mudança
                if (onProfileChangedCallback != null) {
                    onProfileChangedCallback.run();
                }
                
                showInfo("Sucesso", "Perfil excluído com sucesso!");
                
            } catch (Exception e) {
                logger.error("Erro ao excluir perfil", e);
                showError("Erro", "Erro ao excluir perfil: " + e.getMessage());
            }
        }
    }
    
    private void showFormError(String message) {
        formStatusLabel.setText("❌ " + message);
        formStatusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        formStatusLabel.setVisible(true);
        formStatusLabel.setManaged(true);
    }
    
    private void showFormSuccess(String message) {
        formStatusLabel.setText("✅ " + message);
        formStatusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        formStatusLabel.setVisible(true);
        formStatusLabel.setManaged(true);
    }
    
    private void hideFormStatus() {
        formStatusLabel.setVisible(false);
        formStatusLabel.setManaged(false);
    }
    
    private String formatDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            return "N/A";
        }
        
        try {
            LocalDateTime dateTime = LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (DateTimeParseException e) {
            return isoDateTime;
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
