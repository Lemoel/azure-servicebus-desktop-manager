@echo off
echo ========================================
echo Azure Service Bus Manager - Java/JavaFX
echo ========================================
echo.

REM Verificar se Java está instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERRO: Java não encontrado!
    echo Por favor, instale Java 21 ou superior.
    echo Download: https://adoptium.net/
    pause
    exit /b 1
)

REM Verificar se Maven está instalado
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERRO: Maven não encontrado!
    echo Por favor, instale Apache Maven.
    echo Download: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo Java e Maven encontrados!
echo.

REM Menu de opções
:menu
echo Escolha uma opção:
echo 1. Compilar projeto
echo 2. Executar em modo desenvolvimento
echo 3. Criar JAR executável
echo 4. Executar JAR (se existir)
echo 5. Limpar projeto
echo 6. Sair
echo.
set /p choice="Digite sua escolha (1-6): "

if "%choice%"=="1" goto compile
if "%choice%"=="2" goto run_dev
if "%choice%"=="3" goto package
if "%choice%"=="4" goto run_jar
if "%choice%"=="5" goto clean
if "%choice%"=="6" goto exit
echo Opção inválida!
goto menu

:compile
echo.
echo Compilando projeto...
mvn clean compile
if %errorlevel% neq 0 (
    echo ERRO na compilação!
    pause
    goto menu
)
echo Compilação concluída com sucesso!
pause
goto menu

:run_dev
echo.
echo Executando em modo desenvolvimento...
echo (Pressione Ctrl+C para parar)
mvn javafx:run
pause
goto menu

:package
echo.
echo Criando JAR executável...
mvn clean package
if %errorlevel% neq 0 (
    echo ERRO na criação do JAR!
    pause
    goto menu
)
echo JAR criado com sucesso em target/
pause
goto menu

:run_jar
echo.
if not exist "target\azure-servicebus-manager-1.0.0.jar" (
    echo JAR não encontrado!
    echo Execute a opção 3 primeiro para criar o JAR.
    pause
    goto menu
)
echo Executando JAR...
java -jar target\azure-servicebus-manager-1.0.0.jar
pause
goto menu

:clean
echo.
echo Limpando projeto...
mvn clean
echo Projeto limpo!
pause
goto menu

:exit
echo.
echo Saindo...
exit /b 0
