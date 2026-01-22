package com.azureservicebus.manager.util;

/**
 * Utilitário para manipulação de cores
 */
public class ColorUtil {
    
    /**
     * Calcula a cor de texto apropriada (preto ou branco) baseado no contraste com a cor de fundo
     * 
     * @param backgroundColor Cor de fundo em formato hexadecimal (#RRGGBB)
     * @return Cor de texto em formato hexadecimal (#000000 para preto ou #FFFFFF para branco)
     */
    public static String getContrastColor(String backgroundColor) {
        if (backgroundColor == null || backgroundColor.isEmpty()) {
            return "#000000"; // Preto como padrão
        }
        
        // Remover o # se presente
        String hex = backgroundColor.startsWith("#") ? backgroundColor.substring(1) : backgroundColor;
        
        // Garantir que temos exatamente 6 caracteres
        if (hex.length() != 6) {
            return "#000000"; // Preto como padrão para formato inválido
        }
        
        try {
            // Converter para RGB
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            
            // Calcular luminância usando a fórmula WCAG
            // Fórmula: (0.299 * R + 0.587 * G + 0.114 * B)
            double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
            
            // Se a luminância for maior que 0.5, usar preto, senão usar branco
            return luminance > 0.5 ? "#000000" : "#FFFFFF";
            
        } catch (NumberFormatException e) {
            return "#000000"; // Preto como padrão para formato inválido
        }
    }
}
