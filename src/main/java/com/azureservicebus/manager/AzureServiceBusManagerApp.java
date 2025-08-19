package com.azureservicebus.manager;

import com.azureservicebus.manager.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * Aplicação principal do Azure Service Bus Manager
 * Aplicação desktop moderna para gerenciar filas do Azure Service Bus
 */
public class AzureServiceBusManagerApp extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureServiceBusManagerApp.class);
    private static final String APP_TITLE = "Azure Service Bus Manager";
    private static final String APP_VERSION = "1.0.0";
    
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Iniciando Azure Service Bus Manager v{}", APP_VERSION);
            
            // Carregar FXML
            FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/fxml/main-view.fxml")
            );
            
            Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
            
            // Aplicar CSS
            scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm()
            );
            
            // Configurar stage principal
            primaryStage.setTitle(APP_TITLE + " v" + APP_VERSION);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            
            // Definir ícone da aplicação
            try {
                primaryStage.getIcons().add(
                    new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app-icon.png")))
                );
            } catch (Exception e) {
                logger.warn("Não foi possível carregar o ícone da aplicação: {}", e.getMessage());
            }
            
            // Obter controller e configurar stage
            MainController controller = fxmlLoader.getController();
            controller.setPrimaryStage(primaryStage);
            
            // Configurar evento de fechamento
            primaryStage.setOnCloseRequest(event -> {
                logger.info("Encerrando aplicação...");
                controller.shutdown();
            });
            
            primaryStage.show();
            logger.info("Aplicação iniciada com sucesso");
            
        } catch (IOException e) {
            logger.error("Erro ao iniciar aplicação: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao inicializar a aplicação", e);
        }
    }
    
    @Override
    public void stop() {
        logger.info("Aplicação encerrada");
    }
    
    public static void main(String[] args) {
        // Configurar propriedades do sistema para JavaFX
        System.setProperty("javafx.preloader", "com.azureservicebus.manager.preloader.AppPreloader");
        
        logger.info("Iniciando Azure Service Bus Manager...");
        launch(args);
    }
}
