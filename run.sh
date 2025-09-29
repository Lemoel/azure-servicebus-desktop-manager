#!/bin/bash

echo "========================================"
echo "Azure Service Bus Manager - Java/JavaFX"
echo "========================================"
echo

# Verificar se Java está instalado
if ! command -v java &> /dev/null; then
    echo "ERRO: Java não encontrado!"
    echo "Por favor, instale Java 21 ou superior."
    echo "Download: https://adoptium.net/"
    exit 1
fi

# Verificar se Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "ERRO: Maven não encontrado!"
    echo "Por favor, instale Apache Maven."
    echo "Download: https://maven.apache.org/download.cgi"
    exit 1
fi

echo "Java e Maven encontrados!"
echo

# Função para mostrar o menu
show_menu() {
    echo "Escolha uma opção:"
    echo "1. Compilar projeto"
    echo "2. Executar em modo desenvolvimento"
    echo "3. Criar JAR executável"
    echo "4. Executar JAR (se existir)"
    echo "5. Limpar projeto"
    echo "6. Sair"
    echo
}

# Função para compilar
compile_project() {
    echo
    echo "Compilando projeto..."
    mvn clean compile
    if [ $? -eq 0 ]; then
        echo "Compilação concluída com sucesso!"
    else
        echo "ERRO na compilação!"
    fi
    echo
    read -p "Pressione Enter para continuar..."
}

# Função para executar em desenvolvimento
run_dev() {
    echo
    echo "Executando em modo desenvolvimento..."
    echo "(Pressione Ctrl+C para parar)"
    mvn javafx:run
    echo
    read -p "Pressione Enter para continuar..."
}

# Função para criar JAR
package_jar() {
    echo
    echo "Criando JAR executável..."
    mvn clean package
    if [ $? -eq 0 ]; then
        echo "JAR criado com sucesso em target/"
    else
        echo "ERRO na criação do JAR!"
    fi
    echo
    read -p "Pressione Enter para continuar..."
}

# Função para executar JAR
run_jar() {
    echo
    if [ ! -f "target/azure-servicebus-manager-1.0.0.jar" ]; then
        echo "JAR não encontrado!"
        echo "Execute a opção 3 primeiro para criar o JAR."
    else
        echo "Executando JAR..."
        java --module-path /usr/local/lib/javafx-21.0.1/lib --add-modules javafx.controls,javafx.fxml -jar target/azure-servicebus-manager-1.0.0.jar 2>/dev/null || java -jar target/azure-servicebus-manager-1.0.0.jar
    fi
    echo
    read -p "Pressione Enter para continuar..."
}

# Função para limpar projeto
clean_project() {
    echo
    echo "Limpando projeto..."
    mvn clean
    echo "Projeto limpo!"
    echo
    read -p "Pressione Enter para continuar..."
}

# Loop principal do menu
while true; do
    show_menu
    read -p "Digite sua escolha (1-6): " choice
    
    case $choice in
        1)
            compile_project
            ;;
        2)
            run_dev
            ;;
        3)
            package_jar
            ;;
        4)
            run_jar
            ;;
        5)
            clean_project
            ;;
        6)
            echo
            echo "Saindo..."
            exit 0
            ;;
        *)
            echo "Opção inválida!"
            echo
            ;;
    esac
done
