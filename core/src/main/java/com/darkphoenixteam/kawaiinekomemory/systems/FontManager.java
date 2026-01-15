package com.darkphoenixteam.kawaiinekomemory.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;

/**
 * Gestor de fuentes usando FreeType
 * Genera fuentes dinámicas desde archivos TTF/OTF
 * 
 * @author DarkphoenixTeam
 */
public class FontManager implements Disposable {
    
    private FreeTypeFontGenerator generator;
    
    // Fuentes generadas (diferentes tamaños)
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont normalFont;
    private BitmapFont smallFont;
    
    /**
     * Constructor
     * @param fontPath Ruta del archivo .ttf (ej: "fonts/game_font.ttf")
     */
    public FontManager(String fontPath) {
        try {
            generator = new FreeTypeFontGenerator(Gdx.files.internal(fontPath));
            Gdx.app.log("FontManager", "Fuente cargada: " + fontPath);
            generateFonts();
        } catch (Exception e) {
            Gdx.app.log("FontManager", "Error cargando fuente, usando default");
            createDefaultFonts();
        }
    }
    
    /**
     * Constructor con fuente default del sistema
     */
    public FontManager() {
        Gdx.app.log("FontManager", "Usando fuente bitmap default");
        createDefaultFonts();
    }
    
    private void generateFonts() {
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        
        // Caracteres a incluir (Latín + Cirílico + algunos CJK básicos)
        param.characters = FreeTypeFontGenerator.DEFAULT_CHARS + 
                          "ÁÉÍÓÚáéíóúÑñ¿¡" + // Español
                          "ÀÂÆÇÈÉÊËÎÏÔŒÙÛÜŸàâæçèéêëîïôœùûüÿ" + // Francés
                          "ÄÖÜẞäöüß" + // Alemán
                          "ÀÈÉÌÒÙàèéìòù" + // Italiano
                          "ÃÕãõ" + // Portugués
                          "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" + // Ruso
                          "абвгдеёжзийклмнопрстуфхцчшщъыьэюя" +
                          "一二三四五六七八九十"; // CJK básico (números)
        
        param.borderWidth = 1;
        param.borderColor = Color.valueOf("00000080");
        param.shadowOffsetX = 2;
        param.shadowOffsetY = 2;
        param.shadowColor = Color.valueOf("00000040");
        
        // Título grande
        param.size = 48;
        param.color = Color.valueOf("FF69B4");
        titleFont = generator.generateFont(param);
        
        // Botones
        param.size = 32;
        param.color = Color.WHITE;
        buttonFont = generator.generateFont(param);
        
        // Texto normal
        param.size = 24;
        param.color = Color.DARK_GRAY;
        normalFont = generator.generateFont(param);
        
        // Texto pequeño
        param.size = 18;
        param.color = Color.GRAY;
        smallFont = generator.generateFont(param);
        
        Gdx.app.log("FontManager", "Fuentes generadas exitosamente");
    }
    
    private void createDefaultFonts() {
        // Fallback a fuentes bitmap básicas
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);
        titleFont.setColor(1f, 0.4f, 0.7f, 1f);
        
        buttonFont = new BitmapFont();
        buttonFont.getData().setScale(2f);
        buttonFont.setColor(1f, 1f, 1f, 1f);
        
        normalFont = new BitmapFont();
        normalFont.getData().setScale(1.5f);
        normalFont.setColor(0.3f, 0.3f, 0.3f, 1f);
        
        smallFont = new BitmapFont();
        smallFont.getData().setScale(1f);
        smallFont.setColor(0.5f, 0.5f, 0.5f, 1f);
    }
    
    // Getters
    public BitmapFont getTitleFont() {
        return titleFont;
    }
    
    public BitmapFont getButtonFont() {
        return buttonFont;
    }
    
    public BitmapFont getNormalFont() {
        return normalFont;
    }
    
    public BitmapFont getSmallFont() {
        return smallFont;
    }
    
    @Override
    public void dispose() {
        if (generator != null) {
            generator.dispose();
        }
        if (titleFont != null) titleFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        if (normalFont != null) normalFont.dispose();
        if (smallFont != null) smallFont.dispose();
    }
}