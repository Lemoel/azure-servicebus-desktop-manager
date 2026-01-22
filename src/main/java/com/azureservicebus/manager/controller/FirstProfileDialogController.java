package com.azureservicebus.manager.controller;

import com.azureservicebus.manager.model.ConnectionProfile;
import com.azureservicebus.manager.service.ProfileService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller para o diálogo de configuração do primeiro perfil
 */
public class FirstProfileDialogController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(FirstProfileDialogController.class);
    
    @FXML private TextField profileNameField;
    @FXML private TextArea connectionStringTextArea;
    @FXML private Label statusLabel;
    
    private DialogPane dialogPane;
    private ProfileService profileService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando FirstProfileDialogController");
        
        profileService = ProfileService.getInstance();
        
        // Focar no campo de nome ao abrir
        profileNameField.requestFocus();
        
        logger.info("FirstProfileDialogController inicializado com sucesso");
    }
    
    /**
     * Define o DialogPane (necessário para configurar botões)
     */
    public void setDialogPane(DialogPane dialogPane) {
        this.dialogPane = dialogPane;
        
        // Configurar validação ao clicar no botão Salvar
        Button saveButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (saveButton != null) {
            saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if (!validateAndSave()) {
                    event.consume(); // Impedir fechamento do diálogo
                }
            });
        }
    }
    
    /**
     * Valida e salva o perfil
     */
    private boolean validateAndSave() {
        // Limpar mensagem de status anterior
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        
        // Validar nome do perfil
        String profileName = profileNameField.getText().trim();
        if (profileName.isEmpty()) {
            showError("Por favor, informe um nome para o perfil.");
            profileNameField.requestFocus();
            return false;
        }
        
        // Validar connection string
        String connectionString = connectionStringTextArea.getText().trim();
        if (connectionString.isEmpty()) {
            showError("Por favor, informe a connection string.");
            connectionStringTextArea.requestFocus();
            return false;
        }
        
        // Validar formato básico da connection string
        if (!connectionString.contains("Endpoint=sb://") || 
            !connectionString.contains("SharedAccessKeyName=") || 
            !connectionString.contains("SharedAccessKey=")) {
            showError("Connection string inválida. Verifique se copiou a string completa do Azure Portal.");
            connectionStringTextArea.requestFocus();
            return false;
        }
        
        // Criar e salvar o perfil
        try {
            ConnectionProfile profile = new ConnectionProfile(profileName, connectionString);
            profileService.addProfile(profile);
            
            logger.info("Primeiro perfil '{}' criado com sucesso", profileName);
            return true;
            
        } catch (Exception e) {
            logger.error("Erro ao criar primeiro perfil", e);
            showError("Erro ao salvar o perfil: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Exibe uma mensagem de erro
     */
    private void showError(String message) {
        statusLabel.setText("❌ " + message);
        statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
    
    /**
     * Obtém o nome do perfil criado (para uso após fechar o diálogo)
     */
    public String getCreatedProfileName() {
        return profileNameField.getText().trim();
    }
}
