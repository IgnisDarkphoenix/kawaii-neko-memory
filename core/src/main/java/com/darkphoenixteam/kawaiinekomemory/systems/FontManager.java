package com.darkphoenixteam.kawaiinekomemory.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;

/**
 * Gestor de fuentes con soporte para idiomas CJK
 * Usa FreeType para generar fuentes dinámicas desde TTF/OTF
 * 
 * Fuentes:
 * - Idiomas latinos: fonts/game_font.ttf (si existe) o default
 * - Idiomas CJK: fonts/NotoSansCJKjp-Regular.otf
 * 
 * @author DarkphoenixTeam
 * @version 2.0 - Soporte CJK
 */
public class FontManager implements Disposable {
    
    private static final String TAG = "FontManager";
    
    // Rutas de fuentes
    private static final String FONT_LATIN = "fonts/game_font.ttf";
    private static final String FONT_CJK = "fonts/NotoSansCJKjp-Regular.otf";
    
    // Generadores
    private FreeTypeFontGenerator latinGenerator;
    private FreeTypeFontGenerator cjkGenerator;
    
    // Fuentes generadas
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont normalFont;
    private BitmapFont smallFont;
    
    // Estado
    private boolean isCJK = false;
    private boolean initialized = false;
    
    // Caracteres por idioma
    private static final String LATIN_CHARS = 
        FreeTypeFontGenerator.DEFAULT_CHARS +
        "ÁÉÍÓÚáéíóúÑñ¿¡" +                    // Español
        "ÀÂÆÇÈÉÊËÎÏÔŒÙÛÜŸàâæçèéêëîïôœùûüÿ" + // Francés
        "ÄÖÜẞäöüß" +                           // Alemán
        "ÀÈÉÌÒÙàèéìòù" +                       // Italiano
        "ÃÕãõ" +                               // Portugués
        "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" + // Ruso mayúsculas
        "абвгдеёжзийклмнопрстуфхцчшщъыьэюя" + // Ruso minúsculas
        "★☆●○♥♦←→↑↓";                         // Símbolos
    
    // Para CJK incluimos caracteres comunes japoneses/chinos/coreanos
    private static final String CJK_CHARS = 
        FreeTypeFontGenerator.DEFAULT_CHARS +
        "★☆●○♥♦←→↑↓" +
        // Hiragana
        "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよらりるれろわをん" +
        "がぎぐげござじずぜぞだぢづでどばびぶべぼぱぴぷぺぽ" +
        // Katakana
        "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン" +
        "ガギグゲゴザジズゼゾダヂヅデドバビブベボパピプペポ" +
        // Kanji comunes del juego
        "一二三四五六七八九十百千万" +
        "時間秒分開始終了勝敗" +
        "設定音楽効果言語戻確認" +
        "簡単普通難中級上級" +
        "遊再挑戦次続終了" +
        "枚組合記録新最高" +
        "猫可愛思出集完成" +
        // Coreano básico (Hangul)
        "가나다라마바사아자차카타파하" +
        "게임시작설정음악효과언어" +
        // Chino simplificado básico
        "游戏开始设置音乐效果语言返回确认";
    
    /**
     * Constructor con detección automática de idioma
     */
    public FontManager() {
        this(LocaleManager.getInstance().isCJK());
    }
    
    /**
     * Constructor con especificación de modo CJK
     * @param useCJK true para usar fuente CJK
     */
    public FontManager(boolean useCJK) {
        this.isCJK = useCJK;
        initialize();
    }
    
    /**
     * Inicializa las fuentes según el modo actual
     */
    private void initialize() {
        Gdx.app.log(TAG, "Inicializando fuentes - Modo CJK: " + isCJK);
        
        // Limpiar fuentes anteriores si existen
        disposeFonts();
        
        if (isCJK) {
            initializeCJKFonts();
        } else {
            initializeLatinFonts();
        }
        
        initialized = true;
    }
    
    /**
     * Inicializa fuentes para idiomas latinos
     */
    private void initializeLatinFonts() {
        try {
            // Intentar cargar fuente personalizada
            if (Gdx.files.internal(FONT_LATIN).exists()) {
                latinGenerator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_LATIN));
                Gdx.app.log(TAG, "Fuente latina cargada: " + FONT_LATIN);
            } else {
                // Fallback a fuente CJK que también soporta latín
                if (Gdx.files.internal(FONT_CJK).exists()) {
                    latinGenerator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_CJK));
                    Gdx.app.log(TAG, "Usando fuente CJK como fallback para latín");
                } else {
                    Gdx.app.log(TAG, "No se encontró fuente, usando bitmap default");
                    createDefaultFonts();
                    return;
                }
            }
            
            generateFonts(latinGenerator, LATIN_CHARS);
            
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando fuentes latinas: " + e.getMessage());
            createDefaultFonts();
        }
    }
    
    /**
     * Inicializa fuentes para idiomas CJK (Chino, Japonés, Coreano)
     */
    private void initializeCJKFonts() {
        try {
            if (Gdx.files.internal(FONT_CJK).exists()) {
                cjkGenerator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_CJK));
                Gdx.app.log(TAG, "Fuente CJK cargada: " + FONT_CJK);
                generateFonts(cjkGenerator, CJK_CHARS);
            } else {
                Gdx.app.error(TAG, "Fuente CJK no encontrada: " + FONT_CJK);
                // Intentar con fuente latina
                isCJK = false;
                initializeLatinFonts();
            }
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando fuentes CJK: " + e.getMessage());
            createDefaultFonts();
        }
    }
    
    /**
     * Genera las 4 fuentes con diferentes tamaños
     */
    private void generateFonts(FreeTypeFontGenerator generator, String characters) {
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.characters = characters;
        param.borderWidth = 1;
        param.borderColor = Color.valueOf("00000080");
        param.shadowOffsetX = 2;
        param.shadowOffsetY = 2;
        param.shadowColor = Color.valueOf("00000040");
        
        // Título grande
        param.size = isCJK ? 40 : 48;  // CJK necesita menos tamaño
        param.color = Color.valueOf("FF69B4");
        titleFont = generator.generateFont(param);
        
        // Botones
        param.size = isCJK ? 26 : 32;
        param.color = Color.WHITE;
        buttonFont = generator.generateFont(param);
        
        // Texto normal
        param.size = isCJK ? 20 : 24;
        param.color = Color.DARK_GRAY;
        normalFont = generator.generateFont(param);
        
        // Texto pequeño
        param.size = isCJK ? 16 : 18;
        param.color = Color.GRAY;
        smallFont = generator.generateFont(param);
        
        Gdx.app.log(TAG, "Fuentes generadas exitosamente");
    }
    
    /**
     * Crea fuentes bitmap por defecto (fallback)
     */
    private void createDefaultFonts() {
        Gdx.app.log(TAG, "Usando fuentes bitmap default");
        
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
    
    /**
     * Regenera las fuentes para un nuevo idioma
     * @param useCJK true si el nuevo idioma es CJK
     * @return true si se regeneraron las fuentes
     */
    public boolean regenerateForLanguage(boolean useCJK) {
        if (this.isCJK == useCJK && initialized) {
            Gdx.app.log(TAG, "No es necesario regenerar fuentes");
            return false;
        }
        
        Gdx.app.log(TAG, "Regenerando fuentes para " + (useCJK ? "CJK" : "Latín"));
        this.isCJK = useCJK;
        initialize();
        return true;
    }
    
    /**
     * Libera solo las fuentes (no los generadores)
     */
    private void disposeFonts() {
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
        if (buttonFont != null) {
            buttonFont.dispose();
            buttonFont = null;
        }
        if (normalFont != null) {
            normalFont.dispose();
            normalFont = null;
        }
        if (smallFont != null) {
            smallFont.dispose();
            smallFont = null;
        }
    }
    
    // === GETTERS ===
    
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
    
    public boolean isCJKMode() {
        return isCJK;
    }
    
    @Override
    public void dispose() {
        Gdx.app.log(TAG, "Liberando recursos de fuentes...");
        
        disposeFonts();
        
        if (latinGenerator != null) {
            latinGenerator.dispose();
            latinGenerator = null;
        }
        if (cjkGenerator != null) {
            cjkGenerator.dispose();
            cjkGenerator = null;
        }
    }
}
