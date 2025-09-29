# Azure Service Bus Manager - Aplicação Desktop Java

Uma aplicação desktop moderna desenvolvida em Java com JavaFX para gerenciar filas do Azure Service Bus. Esta é a versão Java equivalente à aplicação Python/Streamlit original.

## 🚀 Funcionalidades

### ✅ **Interface Desktop Moderna**
- Interface gráfica nativa com JavaFX
- Design responsivo e profissional
- Tema moderno com cores do Azure
- Suporte a dark mode (opcional)

### ✅ **Gestão Completa de Filas**
- **Listar Filas**: Visualização de todas as filas com detalhes completos
- **Filtro de Busca**: Encontre filas específicas rapidamente
- **Criar Filas**: Criação de novas filas com configurações padrão
- **Remover Filas**: Remoção segura com confirmação dupla
- **Detalhes Avançados**: Informações completas sobre cada fila

### ✅ **Gestão de Mensagens**
- **Visualizar Mensagens**: Preview de mensagens sem removê-las
- **Enviar Mensagens**: Interface para envio com propriedades customizadas
- **Limpar Filas**: Remoção de todas as mensagens (incluindo dead letter)
- **Detalhes de Mensagens**: Visualização completa do conteúdo e metadados

### ✅ **Recursos Avançados**
- **Log em Tempo Real**: Acompanhamento de todas as operações
- **Operações Assíncronas**: Interface responsiva durante operações longas
- **Validação de Dados**: Verificação de connection strings e inputs
- **Tratamento de Erros**: Mensagens de erro claras e informativas

## 🛠️ Tecnologias Utilizadas

### **Framework e UI**
- **Java 21**: Linguagem de programação moderna (LTS)
- **JavaFX 21**: Framework para interface gráfica moderna
- **FXML**: Definição declarativa da interface
- **CSS**: Estilização avançada da interface

### **Azure Integration**
- **Azure Service Bus SDK 7.15.0**: SDK oficial mais recente
- **Azure Identity 1.11.1**: Gestão de autenticação

### **Utilitários**
- **Jackson 2.16.0**: Processamento JSON
- **SLF4J + Logback**: Sistema de logging profissional
- **Apache Commons Lang**: Utilitários gerais

### **Build e Gestão**
- **Maven**: Gestão de dependências e build
- **Maven Shade Plugin**: Criação de JAR executável

## 📋 Pré-requisitos

1. **Java 21 ou superior**
2. **Maven 3.6+**
3. **Connection String** do Azure Service Bus
4. **Permissões adequadas** no Azure Service Bus

## 🚀 Instalação e Execução

### **1. Clonar o Projeto**
```bash
git clone <repository-url>
cd azure-servicebus-manager
```

### **2. Compilar o Projeto**
```bash
mvn clean compile
```

### **3. Executar em Desenvolvimento**
```bash
mvn javafx:run
```

### **4. Criar JAR Executável**
```bash
mvn clean package
```

### **5. Executar JAR**
```bash
java -jar target/azure-servicebus-manager-1.0.0-shaded.jar
```

## 🎯 Como Usar

### **1. Conectar ao Azure Service Bus**
1. Inicie a aplicação
2. Cole sua connection string na área de texto
3. Clique em "🔌 Conectar"
4. Aguarde a confirmação de conexão

### **2. Gerenciar Filas**
1. Vá para a aba "📋 Gerenciar Filas"
2. Clique em "🔄 Carregar Filas"
3. Use o filtro para encontrar filas específicas
4. Selecione uma fila para ver detalhes completos

### **3. Criar Nova Fila**
1. Na seção "➕ Criar Nova Fila"
2. Digite o nome da nova fila
3. Clique em "🚀 Criar Fila"

### **4. Enviar Mensagens**
1. Vá para a aba "📤 Enviar Mensagem"
2. Selecione a fila de destino
3. Digite o corpo da mensagem
4. Adicione propriedades customizadas (opcional)
5. Clique em "📤 Enviar Mensagem"

### **5. Visualizar Mensagens**
1. Vá para a aba "📥 Ver Mensagens"
2. Selecione a fila desejada
3. Clique em "👁️ Carregar Mensagens"
4. Selecione uma mensagem para ver detalhes completos

## 🏗️ Arquitetura do Projeto

### **Estrutura de Pacotes**
```
com.azureservicebus.manager/
├── AzureServiceBusManagerApp.java    # Classe principal
├── controller/
│   └── MainController.java           # Controller da interface
├── model/
│   ├── QueueInfo.java               # Modelo de dados das filas
│   └── MessageInfo.java             # Modelo de dados das mensagens
└── service/
    └── ServiceBusService.java       # Serviço de integração Azure
```

### **Recursos**
```
src/main/resources/
├── fxml/
│   └── main-view.fxml              # Layout da interface
├── css/
│   └── styles.css                  # Estilos da aplicação
└── logback.xml                     # Configuração de logging
```

### **Padrões Utilizados**
- **MVC (Model-View-Controller)**: Separação clara de responsabilidades
- **Observer Pattern**: Callbacks para atualizações de estado
- **Async/Await**: Operações assíncronas com CompletableFuture
- **Properties Pattern**: Binding de dados com JavaFX Properties

## 🔧 Configuração Avançada

### **Logging**
Os logs são salvos em `logs/azure-servicebus-manager.log` com rotação automática:
- Máximo 10MB por arquivo
- Histórico de 30 dias
- Limite total de 300MB

### **Personalização de Interface**
Edite `src/main/resources/css/styles.css` para personalizar:
- Cores e temas
- Tamanhos e espaçamentos
- Animações e efeitos

### **Configuração de Build**
Modifique `pom.xml` para:
- Alterar versões de dependências
- Adicionar novos plugins
- Configurar propriedades do JAR

## 🐛 Solução de Problemas

### **Erro de Connection String**
```
Erro na conexão: Unauthorized
```
**Solução**: Verifique se a connection string está correta e tem as permissões necessárias.

### **Erro de JavaFX**
```
JavaFX runtime components are missing
```
**Solução**: Certifique-se de usar Java 21+ ou adicione o JavaFX ao classpath.

### **Erro de Dependências**
```
Could not resolve dependencies
```
**Solução**: Execute `mvn clean install` para baixar todas as dependências.

### **Interface não Carrega**
```
Location is not set
```
**Solução**: Verifique se os arquivos FXML estão no classpath correto.

## 🆚 Comparação com a Versão Python

| Funcionalidade | Python/Streamlit | Java/JavaFX |
|---|---|---|
| **Performance** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Interface** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Portabilidade** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Manutenibilidade** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Recursos Nativos** | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Facilidade de Deploy** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |

### **Vantagens da Versão Java**
- ✅ Interface nativa mais responsiva
- ✅ Melhor performance para operações intensivas
- ✅ Tipagem estática e maior robustez
- ✅ Melhor integração com ferramentas enterprise
- ✅ Suporte nativo a threading

### **Vantagens da Versão Python**
- ✅ Desenvolvimento mais rápido
- ✅ Deploy mais simples (web-based)
- ✅ Menor curva de aprendizado
- ✅ Melhor para prototipagem rápida

## 🤝 Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 🆘 Suporte

Para suporte ou dúvidas:
1. Abra uma issue no GitHub
2. Verifique os logs em `logs/azure-servicebus-manager.log`
3. Confirme que a connection string está funcionando

## 🔮 Roadmap

### **Próximas Funcionalidades**
- [ ] Suporte a Topics e Subscriptions
- [ ] Exportação de mensagens para JSON/CSV
- [ ] Importação em lote de mensagens
- [ ] Monitoramento em tempo real
- [ ] Suporte a múltiplas connection strings
- [ ] Plugin system para extensões
- [ ] Integração com Azure Key Vault
- [ ] Suporte a Service Bus Premium features

### **Melhorias Técnicas**
- [ ] Testes unitários e de integração
- [ ] CI/CD pipeline
- [ ] Documentação de API
- [ ] Containerização com Docker
- [ ] Instalador nativo (MSI/DMG/DEB)
