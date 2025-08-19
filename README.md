# 🚌 Azure Service Bus Manager - JavaFX Desktop Application

Uma aplicação desktop moderna em Java usando JavaFX para gerenciar filas do Azure Service Bus de forma intuitiva e eficiente.

## ✨ Funcionalidades

- ✅ **Interface Desktop Moderna**: Aplicação nativa JavaFX com design profissional
- ✅ **Gestão Completa de Filas**: Listar, criar, deletar e visualizar detalhes das filas
- ✅ **Operações de Mensagens**: Enviar, visualizar e limpar mensagens das filas
- ✅ **Filtro Inteligente**: Busca em tempo real nas listas de filas
- ✅ **Operações Assíncronas**: Interface responsiva que não trava durante operações
- ✅ **Log em Tempo Real**: Acompanhe todas as operações com logs detalhados
- ✅ **Sem Dependências Externas**: Não precisa de Azure CLI - usa SDK Java puro
- ✅ **Multiplataforma**: Funciona em Windows, macOS e Linux

## 🎯 Capturas de Tela

### Interface Principal
- **Painel de Conexão**: Conecte-se facilmente usando connection string
- **Lista de Filas**: Visualize todas as filas com filtro de busca
- **Detalhes da Fila**: Informações completas sobre mensagens, configurações e status
- **Operações**: Botões para criar, deletar, enviar mensagens e limpar filas

### Funcionalidades Avançadas
- **Gestão de Mensagens**: Visualize mensagens sem removê-las (peek)
- **Limpeza de Filas**: Remove todas as mensagens (ativas e dead letter)
- **Propriedades Customizadas**: Envie mensagens com metadados personalizados
- **Dead Letter Queue**: Suporte completo para mensagens mortas

## 🔧 Pré-requisitos

- **Java 17 ou superior**
- **Maven 3.6+** (para compilação)
- **Connection String** válida do Azure Service Bus

> ⚠️ **Importante**: Esta aplicação NÃO precisa do Azure CLI instalado. Usa apenas bibliotecas Java para comunicação direta com o Azure.

## 🚀 Instalação e Execução

### Opção 1: Executar com Maven (Desenvolvimento)

1. **Clone o repositório**:
```bash
git clone https://github.com/seu-usuario/azure-servicebus-manager-javafx.git
cd azure-servicebus-manager-javafx
```

2. **Compile e execute**:
```bash
mvn clean compile
mvn javafx:run
```

### Opção 2: Gerar JAR Executável

1. **Gerar o JAR**:
```bash
mvn clean package
```

2. **Executar o JAR**:
```bash
java -jar target/azure-servicebus-manager-1.0.0.jar
```

## 📖 Como Usar

### 1. **Conectar ao Azure Service Bus**

1. Abra a aplicação
2. Cole sua **Connection String** no campo de texto
3. Clique em **"Conectar"**
4. Aguarde a confirmação de conexão bem-sucedida

**Exemplo de Connection String**:
```
Endpoint=sb://seu-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=sua-chave-aqui
```

### 2. **Gerenciar Filas**

#### **Listar Filas**
- Clique em **"Carregar Filas"** para listar todas as filas
- Use o **campo de filtro** para buscar filas específicas
- A lista atualiza em tempo real conforme você digita

#### **Visualizar Detalhes**
- Selecione uma fila da lista
- Clique em **"Carregar Detalhes"**
- Visualize informações completas:
  - Número de mensagens (ativas, mortas, agendadas)
  - Configurações da fila (tamanho máximo, lock duration, etc.)
  - Timestamps de criação e atualização

#### **Criar Nova Fila**
- Digite o nome da nova fila
- Clique em **"Criar Fila"**
- A fila será criada com configurações padrão

#### **Deletar Fila**
- Selecione a fila a ser removida
- Clique em **"Deletar Fila"**
- Confirme a operação (irreversível)

### 3. **Operações com Mensagens**

#### **Enviar Mensagem**
1. Selecione a fila de destino
2. Digite o corpo da mensagem (JSON, texto, etc.)
3. Adicione propriedades customizadas (opcional)
4. Clique em **"Enviar Mensagem"**

#### **Visualizar Mensagens**
1. Selecione a fila
2. Clique em **"Ver Mensagens"**
3. As mensagens são exibidas sem serem removidas da fila
4. Visualize detalhes como sequence number, timestamp, propriedades

#### **Limpar Fila**
1. Selecione a fila com mensagens
2. Clique em **"Limpar Mensagens"**
3. Confirme a operação
4. Todas as mensagens (ativas e dead letter) serão removidas permanentemente

## 🏗️ Arquitetura Técnica

### **Tecnologias Utilizadas**
- **Java 17**: Linguagem base
- **JavaFX 21**: Interface gráfica moderna
- **Azure SDK 7.15.0**: Comunicação com Azure Service Bus
- **Maven**: Gerenciamento de dependências
- **SLF4J + Logback**: Sistema de logs

### **Padrões Implementados**
- **MVC (Model-View-Controller)**: Separação clara de responsabilidades
- **Async Programming**: Operações não-bloqueantes com CompletableFuture
- **Observer Pattern**: Callbacks para atualizações de status
- **Try-with-resources**: Gestão automática de recursos Azure

### **Estrutura do Projeto**
```
src/main/java/com/azureservicebus/manager/
├── AzureServiceBusManagerApp.java     # Classe principal
├── controller/
│   └── MainController.java            # Controlador da interface
├── service/
│   └── ServiceBusService.java         # Lógica de negócio Azure
├── model/
│   ├── QueueInfo.java                 # Modelo de dados da fila
│   └── MessageInfo.java               # Modelo de dados da mensagem
└── util/
    └── FXMLUtils.java                 # Utilitários JavaFX

src/main/resources/
├── fxml/
│   └── main.fxml                      # Layout da interface
├── css/
│   └── styles.css                     # Estilos da aplicação
└── logback.xml                        # Configuração de logs
```

## 🔍 Funcionalidades Técnicas Avançadas

### **Comunicação com Azure**
- **SDK Oficial**: Usa `azure-messaging-servicebus` da Microsoft
- **Protocolo AMQP 1.0**: Comunicação eficiente e confiável
- **Connection String**: Autenticação via Shared Access Key
- **Operações Batch**: Processa mensagens em lotes de 100 para eficiência

### **Interface Responsiva**
- **Async Operations**: Todas as operações Azure são assíncronas
- **Progress Indicators**: Feedback visual durante operações longas
- **Error Handling**: Tratamento robusto de erros com mensagens claras
- **Auto-refresh**: Atualização automática de dados quando necessário

### **Gestão de Recursos**
- **Connection Pooling**: Reutilização eficiente de conexões
- **Memory Management**: Limpeza automática de recursos
- **Thread Safety**: Operações thread-safe com ExecutorService
- **Graceful Shutdown**: Encerramento limpo da aplicação

## 🛠️ Desenvolvimento

### **Compilar o Projeto**
```bash
mvn clean compile
```

### **Executar Testes**
```bash
mvn test
```

### **Gerar Documentação**
```bash
mvn javadoc:javadoc
```

### **Análise de Código**
```bash
mvn spotbugs:check
mvn checkstyle:check
```

## 🐛 Solução de Problemas

### **Erro de Conexão**
```
Erro ao conectar ao Azure Service Bus
```
**Soluções**:
- Verifique se a connection string está correta
- Confirme que o namespace existe e está ativo
- Verifique conectividade de rede

### **Erro de Permissões**
```
The client does not have authorization to perform action
```
**Soluções**:
- Verifique se a chave de acesso tem permissões adequadas
- Confirme que a policy inclui `Manage`, `Send` e `Listen`

### **Erro de JavaFX**
```
JavaFX runtime components are missing
```
**Soluções**:
- Use Java 17+ que inclui JavaFX
- Ou execute com: `mvn javafx:run`

### **Erro de Memória**
```
OutOfMemoryError
```
**Soluções**:
- Execute com mais memória: `java -Xmx2g -jar app.jar`
- Evite carregar muitas mensagens simultaneamente

## 🤝 Contribuição

1. **Fork** o projeto
2. Crie uma **branch** para sua feature (`git checkout -b feature/nova-feature`)
3. **Commit** suas mudanças (`git commit -am 'Adiciona nova feature'`)
4. **Push** para a branch (`git push origin feature/nova-feature`)
5. Abra um **Pull Request**

### **Padrões de Código**
- Use **Java 17** features quando apropriado
- Siga **convenções JavaFX** para UI
- Mantenha **cobertura de testes** acima de 80%
- Documente **métodos públicos** com Javadoc

## 📄 Licença

Este projeto está sob a licença **MIT**. Veja o arquivo [LICENSE](LICENSE) para detalhes.

## 🆘 Suporte

Para suporte ou dúvidas:

1. **Issues**: Abra uma issue no GitHub
2. **Logs**: Verifique os logs em `logs/azure-servicebus-manager.log`
3. **Debug**: Execute com `-Dlogback.configurationFile=logback-debug.xml`

**⭐ Se este projeto foi útil, considere dar uma estrela no GitHub!**
