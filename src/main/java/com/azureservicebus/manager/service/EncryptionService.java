package com.azureservicebus.manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Serviço de criptografia para proteger connection strings.
 * Usa AES-256-GCM com chave derivada de informações da máquina.
 */
public class EncryptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int TAG_SIZE = 128;
    private static final int ITERATION_COUNT = 65536;
    
    private static EncryptionService instance;
    private SecretKey secretKey;
    
    private EncryptionService() {
        try {
            this.secretKey = generateMachineKey();
            logger.info("EncryptionService inicializado com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao inicializar EncryptionService", e);
            throw new RuntimeException("Falha ao inicializar serviço de criptografia", e);
        }
    }
    
    /**
     * Obtém a instância singleton do serviço
     */
    public static synchronized EncryptionService getInstance() {
        if (instance == null) {
            instance = new EncryptionService();
        }
        return instance;
    }
    
    /**
     * Gera uma chave derivada de informações únicas da máquina
     */
    private SecretKey generateMachineKey() throws Exception {
        // Combinar informações únicas da máquina
        StringBuilder machineInfo = new StringBuilder();
        
        // Nome do usuário
        machineInfo.append(System.getProperty("user.name"));
        
        // Sistema operacional
        machineInfo.append(System.getProperty("os.name"));
        
        // Hostname
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            machineInfo.append(hostname);
        } catch (Exception e) {
            logger.warn("Não foi possível obter hostname, usando valor padrão");
            machineInfo.append("default-host");
        }
        
        // Home directory
        machineInfo.append(System.getProperty("user.home"));
        
        // Salt fixo (para garantir que a mesma chave seja gerada sempre na mesma máquina)
        byte[] salt = "azure-servicebus-manager-v1".getBytes(StandardCharsets.UTF_8);
        
        // Derivar chave usando PBKDF2
        KeySpec spec = new PBEKeySpec(
            machineInfo.toString().toCharArray(),
            salt,
            ITERATION_COUNT,
            KEY_SIZE
        );
        
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        
        return new SecretKeySpec(keyBytes, "AES");
    }
    
    /**
     * Criptografa uma string usando AES-256-GCM
     * 
     * @param plaintext Texto em claro
     * @return String no formato: Base64(IV + EncryptedData)
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return "";
        }
        
        try {
            // Gerar IV aleatório
            byte[] iv = new byte[IV_SIZE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            // Configurar cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            // Criptografar
            byte[] encryptedData = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combinar IV + dados criptografados
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
            
            // Codificar em Base64
            return Base64.getEncoder().encodeToString(combined);
            
        } catch (Exception e) {
            logger.error("Erro ao criptografar dados", e);
            throw new RuntimeException("Falha ao criptografar dados", e);
        }
    }
    
    /**
     * Descriptografa uma string criptografada
     * 
     * @param encrypted String no formato Base64(IV + EncryptedData)
     * @return Texto em claro
     */
    public String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return "";
        }
        
        try {
            // Decodificar Base64
            byte[] combined = Base64.getDecoder().decode(encrypted);
            
            // Extrair IV e dados criptografados
            byte[] iv = new byte[IV_SIZE];
            byte[] encryptedData = new byte[combined.length - IV_SIZE];
            
            System.arraycopy(combined, 0, iv, 0, IV_SIZE);
            System.arraycopy(combined, IV_SIZE, encryptedData, 0, encryptedData.length);
            
            // Configurar cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            // Descriptografar
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            logger.error("Erro ao descriptografar dados", e);
            throw new RuntimeException("Falha ao descriptografar dados. Os dados podem estar corrompidos ou foram criados em outra máquina.", e);
        }
    }
    
    /**
     * Testa se a criptografia está funcionando corretamente
     */
    public boolean testEncryption() {
        try {
            String testString = "Test-String-123";
            String encrypted = encrypt(testString);
            String decrypted = decrypt(encrypted);
            return testString.equals(decrypted);
        } catch (Exception e) {
            logger.error("Falha no teste de criptografia", e);
            return false;
        }
    }
}
