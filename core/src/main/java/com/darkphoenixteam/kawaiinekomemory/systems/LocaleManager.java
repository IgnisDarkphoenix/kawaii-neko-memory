package com.darkphoenixteam.kawaiinekomemory.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;
import java.util.Locale;

/**
 * Gestor de idiomas del juego
 * Soporta: EN, ES, RU, PT, ZH, JA, KO, FR, DE, IT
 * 
 * @author DarkphoenixTeam
 */
public class LocaleManager {
    
    private static final String TAG = "LocaleManager";
    private static final String PREFS_NAME = "KawaiiNekoSettings";
    private static final String KEY_LANGUAGE = "language";
    
    /**
     * Idiomas soportados
     */
    public enum Language {
        EN("en", "English", false),
        ES("es", "Español", false),
        FR("fr", "Français", false),
        DE("de", "Deutsch", false),
        IT("it", "Italiano", false),
        PT("pt", "Português", false),
        RU("ru", "Русский", false),
        ZH("zh", "中文", true),
        JA("ja", "日本語", true),
        KO("ko", "한국어", true);
        
        public final String code;
        public final String displayName;
        public final boolean isCJK;
        
        Language(String code, String displayName, boolean isCJK) {
            this.code = code;
            this.displayName = displayName;
            this.isCJK = isCJK;
        }
        
        public static Language fromCode(String code) {
            for (Language lang : values()) {
                if (lang.code.equals(code)) {
                    return lang;
                }
            }
            return EN; // Default
        }
        
        /**
         * Siguiente idioma en la lista (para cycling)
         */
        public Language next() {
            Language[] values = values();
            int nextIndex = (ordinal() + 1) % values.length;
            return values[nextIndex];
        }
    }
    
    private static LocaleManager instance;
    private Preferences prefs;
    private I18NBundle bundle;
    private Language currentLanguage;
    
    private LocaleManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        
        // Cargar idioma guardado o detectar del sistema
        String savedCode = prefs.getString(KEY_LANGUAGE, "");
        
        if (savedCode.isEmpty()) {
            // Detectar idioma del dispositivo
            currentLanguage = detectDeviceLanguage();
            Gdx.app.log(TAG, "Idioma detectado del dispositivo: " + currentLanguage.displayName);
        } else {
            currentLanguage = Language.fromCode(savedCode);
            Gdx.app.log(TAG, "Idioma cargado de prefs: " + currentLanguage.displayName);
        }
        
        loadBundle();
    }
    
    /**
     * Detecta el idioma del dispositivo
     */
    private Language detectDeviceLanguage() {
        try {
            String systemLang = Locale.getDefault().getLanguage();
            Gdx.app.log(TAG, "Sistema reporta idioma: " + systemLang);
            
            for (Language lang : Language.values()) {
                if (lang.code.equals(systemLang)) {
                    return lang;
                }
            }
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error detectando idioma del dispositivo");
        }
        
        return Language.EN; // Default
    }
    
    /**
     * Carga el bundle de strings para el idioma actual
     */
    private void loadBundle() {
        try {
            FileHandle baseFileHandle = Gdx.files.internal("i18n/strings");
            Locale locale = new Locale(currentLanguage.code);
            bundle = I18NBundle.createBundle(baseFileHandle, locale, "UTF-8");
            
            Gdx.app.log(TAG, "Bundle cargado: " + currentLanguage.code + 
                        " (" + currentLanguage.displayName + ")");
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando bundle para " + currentLanguage.code + 
                          ". Usando inglés por defecto.");
            
            // Fallback a inglés
            try {
                FileHandle baseFileHandle = Gdx.files.internal("i18n/strings");
                bundle = I18NBundle.createBundle(baseFileHandle, new Locale("en"), "UTF-8");
            } catch (Exception e2) {
                Gdx.app.error(TAG, "Error fatal cargando bundle de inglés");
            }
        }
    }
    
    public static LocaleManager getInstance() {
        if (instance == null) {
            instance = new LocaleManager();
        }
        return instance;
    }
    
    /**
     * Obtiene un string localizado por su key
     */
    public String get(String key) {
        try {
            return bundle.get(key);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Key no encontrada: " + key);
            return "[" + key + "]";
        }
    }
    
    /**
     * Obtiene un string localizado con formato (parámetros)
     * Ejemplo: get("game.pairs.format", 5, 10) → "Pares: 5/10"
     */
    public String format(String key, Object... args) {
        try {
            return bundle.format(key, args);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error formateando key: " + key);
            return "[" + key + "]";
        }
    }
    
    /**
     * Cambia el idioma del juego
     * NOTA: Requiere regenerar las fuentes si cambia entre CJK y no-CJK
     * 
     * @return true si cambió de grupo de fuentes (requiere regenerar FontManager)
     */
    public boolean setLanguage(Language language) {
        boolean wasCJK = currentLanguage.isCJK;
        
        currentLanguage = language;
        prefs.putString(KEY_LANGUAGE, language.code);
        prefs.flush();
        
        loadBundle();
        
        boolean fontChangeNeeded = (wasCJK != language.isCJK);
        
        Gdx.app.log(TAG, "Idioma cambiado a: " + language.displayName + 
                    " | Regenerar fuentes: " + fontChangeNeeded);
        
        return fontChangeNeeded;
    }
    
    /**
     * Avanza al siguiente idioma en la lista
     * @return true si necesita regenerar fuentes
     */
    public boolean cycleLanguage() {
        return setLanguage(currentLanguage.next());
    }
    
    public Language getCurrentLanguage() {
        return currentLanguage;
    }
    
    public boolean isCJK() {
        return currentLanguage.isCJK;
    }
    
    /**
     * Obtiene todos los idiomas disponibles
     */
    public Language[] getAvailableLanguages() {
        return Language.values();
    }
    
    /**
     * Fuerza recarga (útil después de cambiar idioma)
     */
    public static void reload() {
        instance = null;
    }
}