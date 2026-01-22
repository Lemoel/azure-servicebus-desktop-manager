package com.azureservicebus.manager.service;

import com.azureservicebus.manager.model.ConnectionProfile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerenciar perfis de conexão com persistência criptografada
 */
public class ProfileService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);
    
    private static final String CONFIG_DIR = ".azure-servicebus-manager";
    private static final String PROFILES_FILE = "profiles.json";
    private static final String VERSION = "1.0";
    
    private static ProfileService instance;
    private final EncryptionService encryptionService;
    private final Gson gson;
    private final Path profilesPath;
    
    private List<ConnectionProfile> profiles;
    private String activeProfileName;
    
    private ProfileService() {
        this.encryptionService = EncryptionService.getInstance();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.profilesPath = getProfilesFilePath();
        this.profiles = new ArrayList<>();
        
        loadProfiles();
        
        logger.info("ProfileService inicializado");
    }
    
    /**
     * Obtém a instância singleton do serviço
     */
    public static synchronized ProfileService getInstance() {
        if (instance == null) {
            instance = new ProfileService();
        }
        return instance;
    }
    
    /**
     * Obtém o caminho do arquivo de perfis
     */
    private Path getProfilesFilePath() {
        String userHome = System.getProperty("user.home");
        Path configDir = Paths.get(userHome, CONFIG_DIR);
        
        // Criar diretório se não existir
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                logger.info("Diretório de configuração criado: {}", configDir);
            }
        } catch (IOException e) {
            logger.error("Erro ao criar diretório de configuração", e);
        }
        
        return configDir.resolve(PROFILES_FILE);
    }
    
    /**
     * Verifica se existe algum perfil salvo
     */
    public boolean hasProfiles() {
        return !profiles.isEmpty();
    }
    
    /**
     * Carrega os perfis do arquivo
     */
    private void loadProfiles() {
        File file = profilesPath.toFile();
        
        if (!file.exists()) {
            logger.info("Arquivo de perfis não encontrado. Será criado no primeiro salvamento.");
            return;
        }
        
        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            
            // Carregar perfil ativo
            if (root.has("activeProfile")) {
                this.activeProfileName = root.get("activeProfile").getAsString();
            }
            
            // Carregar lista de perfis
            if (root.has("profiles")) {
                JsonArray profilesArray = root.getAsJsonArray("profiles");
                profiles.clear();
                
                for (int i = 0; i < profilesArray.size(); i++) {
                    JsonObject profileJson = profilesArray.get(i).getAsJsonObject();
                    
                    ConnectionProfile profile = new ConnectionProfile();
                    profile.setName(profileJson.get("name").getAsString());
                    
                    // Descriptografar connection string
                    String encryptedConnectionString = profileJson.get("connectionString").getAsString();
                    try {
                        String decryptedConnectionString = encryptionService.decrypt(encryptedConnectionString);
                        profile.setConnectionString(decryptedConnectionString);
                    } catch (Exception e) {
                        logger.error("Erro ao descriptografar connection string do perfil '{}'", profile.getName(), e);
                        continue; // Pular este perfil
                    }
                    
                    if (profileJson.has("createdAt")) {
                        profile.setCreatedAt(profileJson.get("createdAt").getAsString());
                    }
                    if (profileJson.has("lastUsedAt")) {
                        profile.setLastUsedAt(profileJson.get("lastUsedAt").getAsString());
                    }
                    
                    // Carregar cor do perfil
                    if (profileJson.has("color")) {
                        profile.setColor(profileJson.get("color").getAsString());
                    }
                    
                    profiles.add(profile);
                }
                
                logger.info("Carregados {} perfis", profiles.size());
            }
            
        } catch (Exception e) {
            logger.error("Erro ao carregar perfis", e);
        }
    }
    
    /**
     * Salva os perfis no arquivo
     */
    private void saveProfiles() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("version", VERSION);
            
            if (activeProfileName != null) {
                root.addProperty("activeProfile", activeProfileName);
            }
            
            JsonArray profilesArray = new JsonArray();
            for (ConnectionProfile profile : profiles) {
                JsonObject profileJson = new JsonObject();
                profileJson.addProperty("name", profile.getName());
                
                // Criptografar connection string
                String encryptedConnectionString = encryptionService.encrypt(profile.getConnectionString());
                profileJson.addProperty("connectionString", encryptedConnectionString);
                
                profileJson.addProperty("createdAt", profile.getCreatedAt());
                profileJson.addProperty("lastUsedAt", profile.getLastUsedAt());
                
                // Salvar cor do perfil
                if (profile.getColor() != null) {
                    profileJson.addProperty("color", profile.getColor());
                }
                
                profilesArray.add(profileJson);
            }
            
            root.add("profiles", profilesArray);
            
            // Salvar no arquivo
            try (FileWriter writer = new FileWriter(profilesPath.toFile())) {
                gson.toJson(root, writer);
                logger.info("Perfis salvos com sucesso");
            }
            
        } catch (Exception e) {
            logger.error("Erro ao salvar perfis", e);
            throw new RuntimeException("Erro ao salvar perfis", e);
        }
    }
    
    /**
     * Adiciona um novo perfil
     */
    public void addProfile(ConnectionProfile profile) {
        if (profile == null || profile.getName() == null || profile.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Perfil inválido");
        }
        
        // Verificar se já existe um perfil com o mesmo nome
        if (getProfile(profile.getName()).isPresent()) {
            throw new IllegalArgumentException("Já existe um perfil com o nome '" + profile.getName() + "'");
        }
        
        profiles.add(profile);
        
        // Se for o primeiro perfil, definir como ativo
        if (profiles.size() == 1) {
            activeProfileName = profile.getName();
        }
        
        saveProfiles();
        logger.info("Perfil '{}' adicionado", profile.getName());
    }
    
    /**
     * Atualiza um perfil existente
     */
    public void updateProfile(ConnectionProfile profile) {
        if (profile == null || profile.getName() == null) {
            throw new IllegalArgumentException("Perfil inválido");
        }
        
        Optional<ConnectionProfile> existing = getProfile(profile.getName());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Perfil '" + profile.getName() + "' não encontrado");
        }
        
        // Remover o antigo e adicionar o novo
        profiles.remove(existing.get());
        profiles.add(profile);
        
        saveProfiles();
        logger.info("Perfil '{}' atualizado", profile.getName());
    }
    
    /**
     * Remove um perfil
     */
    public void deleteProfile(String profileName) {
        Optional<ConnectionProfile> profile = getProfile(profileName);
        if (profile.isEmpty()) {
            throw new IllegalArgumentException("Perfil '" + profileName + "' não encontrado");
        }
        
        profiles.remove(profile.get());
        
        // Se era o perfil ativo, definir outro como ativo (se houver)
        if (profileName.equals(activeProfileName)) {
            activeProfileName = profiles.isEmpty() ? null : profiles.get(0).getName();
        }
        
        saveProfiles();
        logger.info("Perfil '{}' removido", profileName);
    }
    
    /**
     * Obtém um perfil pelo nome
     */
    public Optional<ConnectionProfile> getProfile(String name) {
        return profiles.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst();
    }
    
    /**
     * Obtém todos os perfis
     */
    public List<ConnectionProfile> getAllProfiles() {
        return new ArrayList<>(profiles);
    }
    
    /**
     * Obtém o perfil ativo
     */
    public Optional<ConnectionProfile> getActiveProfile() {
        if (activeProfileName == null) {
            return Optional.empty();
        }
        return getProfile(activeProfileName);
    }
    
    /**
     * Define o perfil ativo
     */
    public void setActiveProfile(String profileName) {
        Optional<ConnectionProfile> profile = getProfile(profileName);
        if (profile.isEmpty()) {
            throw new IllegalArgumentException("Perfil '" + profileName + "' não encontrado");
        }
        
        this.activeProfileName = profileName;
        
        // Atualizar lastUsedAt
        profile.get().updateLastUsed();
        
        saveProfiles();
        logger.info("Perfil ativo alterado para '{}'", profileName);
    }
    
    /**
     * Obtém o nome do perfil ativo
     */
    public String getActiveProfileName() {
        return activeProfileName;
    }
    
    /**
     * Remove todos os perfis (útil para reset)
     */
    public void deleteAllProfiles() {
        profiles.clear();
        activeProfileName = null;
        
        try {
            Files.deleteIfExists(profilesPath);
            logger.info("Todos os perfis removidos");
        } catch (IOException e) {
            logger.error("Erro ao deletar arquivo de perfis", e);
        }
    }
    
    /**
     * Recarrega os perfis do arquivo (útil após alterações externas)
     */
    public void reload() {
        loadProfiles();
    }
}
