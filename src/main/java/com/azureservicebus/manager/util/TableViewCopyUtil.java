package com.azureservicebus.manager.util;

import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Utilitário para adicionar funcionalidade de cópia em TableViews
 */
public class TableViewCopyUtil {
    
    /**
     * Adiciona Context Menu (clique direito) e atalho Ctrl+C para copiar dados de uma TableView
     * 
     * @param tableView A TableView onde adicionar a funcionalidade de cópia
     */
    public static <T> void addCopyToClipboardSupport(TableView<T> tableView) {
        // Criar Context Menu
        ContextMenu contextMenu = new ContextMenu();
        
        // Item "Copiar Célula"
        MenuItem copyCell = new MenuItem("📋 Copiar Célula");
        copyCell.setOnAction(event -> {
            TablePosition<T, ?> focusedCell = tableView.getFocusModel().getFocusedCell();
            if (focusedCell != null) {
                Object cellData = focusedCell.getTableColumn().getCellData(focusedCell.getRow());
                if (cellData != null) {
                    copyToClipboard(cellData.toString());
                }
            }
        });
        
        // Item "Copiar Linha"
        MenuItem copyRow = new MenuItem("📑 Copiar Linha");
        copyRow.setOnAction(event -> {
            T selectedItem = tableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                String rowData = extractRowData(tableView, selectedItem);
                copyToClipboard(rowData);
            }
        });
        
        // Item "Copiar Tudo"
        MenuItem copyAll = new MenuItem("📊 Copiar Tabela Completa");
        copyAll.setOnAction(event -> {
            StringBuilder allData = new StringBuilder();
            
            // Adicionar cabeçalhos
            for (int i = 0; i < tableView.getColumns().size(); i++) {
                TableColumn<T, ?> column = tableView.getColumns().get(i);
                allData.append(column.getText());
                if (i < tableView.getColumns().size() - 1) {
                    allData.append("\t");
                }
            }
            allData.append("\n");
            
            // Adicionar todas as linhas
            for (T item : tableView.getItems()) {
                allData.append(extractRowData(tableView, item));
                allData.append("\n");
            }
            
            copyToClipboard(allData.toString());
        });
        
        contextMenu.getItems().addAll(copyCell, copyRow, new SeparatorMenuItem(), copyAll);
        
        // Adicionar Context Menu à TableView
        tableView.setContextMenu(contextMenu);
        
        // Adicionar atalho Ctrl+C para copiar linha selecionada
        tableView.setOnKeyPressed(event -> {
            KeyCombination ctrlC = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
            KeyCombination cmdC = new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN);
            
            if (ctrlC.match(event) || cmdC.match(event)) {
                T selectedItem = tableView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    String rowData = extractRowData(tableView, selectedItem);
                    copyToClipboard(rowData);
                    event.consume();
                }
            }
        });
        
        // Habilitar/desabilitar itens do menu baseado no contexto
        contextMenu.setOnShowing(event -> {
            TablePosition<T, ?> focusedCell = tableView.getFocusModel().getFocusedCell();
            T selectedItem = tableView.getSelectionModel().getSelectedItem();
            
            copyCell.setDisable(focusedCell == null || focusedCell.getTableColumn() == null);
            copyRow.setDisable(selectedItem == null);
            copyAll.setDisable(tableView.getItems().isEmpty());
        });
    }
    
    /**
     * Extrai os dados de uma linha da TableView como string separada por tabs
     */
    private static <T> String extractRowData(TableView<T> tableView, T item) {
        StringBuilder rowData = new StringBuilder();
        
        for (int i = 0; i < tableView.getColumns().size(); i++) {
            TableColumn<T, ?> column = tableView.getColumns().get(i);
            Object cellData = column.getCellData(item);
            
            if (cellData != null) {
                rowData.append(cellData.toString());
            }
            
            // Adicionar tab entre colunas (formato compatível com Excel)
            if (i < tableView.getColumns().size() - 1) {
                rowData.append("\t");
            }
        }
        
        return rowData.toString();
    }
    
    /**
     * Copia texto para o clipboard
     */
    private static void copyToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }
}
