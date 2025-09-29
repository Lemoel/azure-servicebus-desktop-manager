# 🤖 AI Context - Azure Service Bus Manager

Este ficheiro fornece contexto completo do projeto para agentes de IA. Leia este ficheiro quando precisar de entender rapidamente a estrutura e funcionalidades do projeto.

## 📋 Resumo do Projeto

**Nome**: Azure Service Bus Manager  
**Tipo**: Aplicação Desktop Java com JavaFX  
**Objetivo**: Gerenciar filas do Azure Service Bus através de interface gráfica moderna  
**Origem**: Reprodução de aplicação Python/Streamlit original em Java Desktop  

## 🏗️ Arquitetura e Estrutura

### **Padrão Arquitetural**
- **MVC (Model-View-Controller)**: Separação clara de responsabilidades
- **Observer Pattern**: Callbacks para atualizações de estado
- **Async Pattern**: Operações assíncronas com CompletableFuture
- **Properties Pattern**: Data binding com JavaFX Properties

### **Estrutura de Pacotes**
```
com.azureservicebus.manager/
├── AzureServiceBusManagerApp.java    # 🚀 Classe principal (JavaFX Application)
├── controller/
│   └── MainController.java           # 🎮 Controller principal da interface
├── model/
│   ├── QueueInfo.java               # 📊 Modelo de dados das filas
│   └── MessageInfo.java             # 💬 Modelo de dados das mensagens
└── service/
    └── ServiceBusService.java       # ⚙️ Serviço de integração com Azure
```

### **Recursos (src/main/resources/)**
```
├── fxml/main-view.fxml              # 🎨 Layout da interface (FXML)
├── css/styles.css                   # 💅 Estilos da aplicação
└── logback.xml                      # 📝 Configuração de logging
```

## 🔧 Tecnologias e Dependências

### **Core Technologies**
- **Java 21**: Linguagem base (LTS)
- **JavaFX 21**: Framework de interface gráfica
- **Maven**: Build e gestão de dependências

### **Azure Integration**
- **azure-messaging-servicebus 7.15.0**: SDK oficial do Azure Service Bus
- **azure-identity 1.11.1**: Gestão de autenticação

### **Utilities**
- **Jackson 2.16.0**: Processamento JSON
- **SLF4J + Logback**: Sistema de logging
- **Apache Commons Lang**: Utilitários gerais

## 🎯 Funcionalidades Principais

### **1. Gestão de Conexão**
- **Classe**: `ServiceBusService`
- **Método**: `connectAsync(String connectionString)`
- **Funcionalidade**: Conecta ao Azure Service Bus via connection string
- **Validação**: Formato da connection string e teste de conectividade

### **2. Gestão de Filas**
- **Listar Filas**: `listQueueNamesAsync()` - Carregamento otimizado em duas fases
- **Detalhes de Fila**: `getQueueDetailsAsync(String queueName)` - Informações completas
- **Criar Fila**: `createQueueAsync(String queueName)` - Criação com configurações padrão
- **Remover Fila**: `deleteQueueAsync(String queueName)` - Remoção com confirmação
- **Limpar Mensagens**: `clearQueueMessagesAsync(String queueName)` - Remove todas as mensagens

### **3. Gestão de Mensagens**
- **Visualizar**: `peekMessagesAsync(String queueName, int maxMessages)` - Preview sem remoção
- **Enviar**: `sendMessageAsync(String queueName, String messageBody, Map<String, Object> properties)`
- **Detalhes**: Visualização completa de metadados e conteúdo

### **4. Interface Gráfica**
- **Layout**: Definido em `main-view.fxml`
- **Controller**: `MainController.java` gerencia toda a lógica da UI
- **Estilos**: `styles.css` com tema moderno do Azure
- **Componentes**: TabPane com 3 abas (Filas, Mensagens, Envio)

## 📊 Modelos de Dados

### **QueueInfo.java**
```java
// Propriedades principais
- String name                    // Nome da fila
- String status                  // Status (Active, Disabled, etc.)
- long totalMessages            // Total de mensagens
- long activeMessages           // Mensagens ativas
- long deadLetterMessages       // Mensagens mortas
- long scheduledMessages        // Mensagens agendadas
- double sizeInKB              // Tamanho em KB
- int maxDeliveryCount         // Máximo de tentativas
- Duration lockDuration        // Duração do lock
- long maxSizeInMB            // Tamanho máximo
- boolean partitioningEnabled  // Particionamento habilitado
- boolean sessionRequired      // Sessões obrigatórias
- boolean duplicateDetectionEnabled // Detecção de duplicatas
```

### **MessageInfo.java**
```java
// Propriedades principais
- long sequenceNumber          // Número de sequência
- String messageId            // ID da mensagem
- String messageBody          // Corpo da mensagem
- String contentType          // Tipo de conteúdo
- String correlationId        // ID de correlação
- LocalDateTime enqueuedTime  // Hora de enfileiramento
- int deliveryCount          // Contador de entregas
- Map<String, Object> applicationProperties // Propriedades customizadas
```

## 🎮 Controller Principal (MainController.java)

### **Inicialização**
- `initialize()`: Configura interface, callbacks e event handlers
- `setupServiceCallbacks()`: Configura callbacks do ServiceBusService
- `setupInitialUI()`: Estado inicial da interface
- `setupTableColumns()`: Configuração das tabelas
- `setupEventHandlers()`: Event handlers dos componentes

### **Handlers Principais**
- `handleConnect()`: Processo de conexão assíncrona
- `handleLoadQueues()`: Carregamento de filas
- `handleQueueSelection()`: Seleção de fila para detalhes
- `handleCreateQueue()`: Criação de nova fila
- `handleDeleteQueue()`: Remoção de fila com confirmação
- `handleSendMessage()`: Envio de mensagem
- `handleLoadMessages()`: Carregamento de mensagens

### **Gestão de Estado**
- `updateConnectionStatus()`: Atualiza UI baseado no status de conexão
- `addLogMessage()`: Adiciona mensagens ao log
- `showAlert()`: Exibe alertas para o utilizador

## 🔄 Fluxo de Operações

### **1. Conexão**
```
User Input (Connection String) → Validation → ServiceBusService.connectAsync() 
→ Test Connection → Update UI Status → Enable Tabs
```

### **2. Listar Filas**
```
Load Button → ServiceBusService.listQueueNamesAsync() → Update ListView 
→ User Selection → ServiceBusService.getQueueDetailsAsync() → Update TableView
```

### **3. Enviar Mensagem**
```
User Input (Queue + Message + Properties) → Validation → ServiceBusService.sendMessageAsync() 
→ Success/Error Feedback → Clear Form
```

## 🎨 Interface e Estilos

### **Layout (main-view.fxml)**
- **BorderPane**: Layout principal
- **Top**: Header com status de conexão
- **Center**: Connection pane + TabPane principal
- **Bottom**: Log de atividades

### **Abas Principais**
1. **📋 Gerenciar Filas**: Lista, detalhes, criar, remover, limpar
2. **📥 Ver Mensagens**: Visualização e detalhes de mensagens
3. **📤 Enviar Mensagem**: Interface de envio com propriedades

### **Estilos (styles.css)**
- **Cores**: Tema Azure (azul #0078d4)
- **Botões**: Primary, Success, Warning, Danger
- **Componentes**: Tabelas, listas, campos de entrada
- **Responsividade**: Media queries para diferentes tamanhos

## 🚀 Execução e Build

### **Scripts de Execução**
- **Windows**: `run.bat` - Menu interativo
- **Linux/macOS**: `run.sh` - Menu interativo

### **Comandos Maven**
```bash
mvn clean compile          # Compilar
mvn javafx:run            # Executar em desenvolvimento
mvn clean package         # Criar JAR executável
```

**Nota**: Requer Java 21+ para compilação e execução.

### **JAR Executável**
- **Localização**: `target/azure-servicebus-manager-1.0.0-shaded.jar`
- **Execução**: `java -jar target/azure-servicebus-manager-1.0.0-shaded.jar`

## 🔧 Configurações

### **Logging (logback.xml)**
- **Console**: Logs em tempo real
- **File**: `logs/azure-servicebus-manager.log`
- **Rotação**: 10MB por arquivo, 30 dias de histórico

### **Maven (pom.xml)**
- **Java Version**: 21
- **JavaFX Version**: 21.0.1
- **Azure SDK Version**: 7.15.0
- **Plugins**: JavaFX Maven Plugin, Shade Plugin

## 🐛 Tratamento de Erros

### **Tipos de Erro**
- **Connection Errors**: Connection string inválida, rede, permissões
- **Operation Errors**: Fila não existe, operação não permitida
- **UI Errors**: Validação de entrada, estados inválidos

### **Estratégias**
- **Async Error Handling**: Try-catch em CompletableFuture
- **User Feedback**: Alerts e mensagens no log
- **Graceful Degradation**: Funcionalidades limitadas quando offline

## 📝 Logging e Debugging

### **Níveis de Log**
- **INFO**: Operações normais
- **WARN**: Situações de atenção
- **ERROR**: Erros que impedem operações

### **Localizações**
- **Console**: Output em tempo real
- **File**: `logs/azure-servicebus-manager.log`
- **UI**: TextArea de log na interface

## 🔒 Segurança

### **Connection String**
- **Armazenamento**: Apenas em memória durante a sessão
- **Validação**: Formato e conectividade
- **Limpeza**: Removida ao desconectar

### **Operações Destrutivas**
- **Confirmação Dupla**: Delete e clear operations
- **Validação**: Verificação de entrada
- **Logging**: Registro de todas as operações

## 🚧 Limitações Atuais

### **Funcionalidades Limitadas**
- **Clear Messages**: Implementação básica (placeholder)
- **Send Messages**: Implementação básica (placeholder)
- **Peek Messages**: Implementação básica (placeholder)

### **Melhorias Futuras**
- Implementação completa do cliente de mensagens
- Suporte a Topics e Subscriptions
- Exportação/importação de mensagens
- Monitoramento em tempo real

## 🎯 Pontos de Extensão

### **Para Adicionar Funcionalidades**
1. **Novos Modelos**: Adicionar em `model/`
2. **Novos Serviços**: Adicionar em `service/`
3. **Nova UI**: Modificar `main-view.fxml` e `MainController.java`
4. **Novos Estilos**: Adicionar em `styles.css`

### **Para Debugging**
1. **Logs**: Verificar `logs/azure-servicebus-manager.log`
2. **Console**: Output em tempo real
3. **UI Log**: TextArea na interface
4. **Breakpoints**: Usar IDE para debug

## 📚 Documentação Adicional

- **README-JAVA.md**: Documentação completa do utilizador
- **connection-string-example.txt**: Exemplo de configuração
- **pom.xml**: Configuração de dependências e build
- **.gitignore-java**: Controle de versão

---

**💡 Dica para IA**: Este projeto segue padrões enterprise Java com JavaFX. Para modificações, sempre considere o padrão MVC, operações assíncronas e feedback ao utilizador. A estrutura é modular e extensível.
