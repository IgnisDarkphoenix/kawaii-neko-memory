package com.darkphoenixteam.kawaiinekomemory.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;
import java.util.Locale;

/**
 * Gestor de idiomas del juego
 * - Detecta automáticamente el idioma del dispositivo en primera ejecución
 * - Guarda preferencia del usuario
 * - Fallback a inglés si no hay soporte
 * 
 * Soporta: EN, ES, RU, PT, ZH, JA, KO, FR, DE, IT
 * 
 * @author DarkphoenixTeam
 * @version 2.0 - Detección automática mejorada
 */
public class LocaleManager {
    
    private static final String TAG = "LocaleManager";
    private static final String PREFS_NAME = "KawaiiNekoSettings";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_FIRST_RUN = "first_run_language";
    
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
        
        public Language next() {
            Language[] values = values();
            int nextIndex = (ordinal() + 1) % values.length;
            return values[nextIndex];
        }
        
        public Language previous() {
            Language[] values = values();
            int prevIndex = (ordinal() - 1 + values.length) % values.length;
            return values[prevIndex];
        }
    }
    
    private static LocaleManager instance;
    private Preferences prefs;
    private I18NBundle bundle;
    private Language currentLanguage;
    private boolean isFirstRun;
    
    private LocaleManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        
        // Verificar si es primera ejecución
        isFirstRun = !prefs.getBoolean(KEY_FIRST_RUN, false);
        
        String savedCode = prefs.getString(KEY_LANGUAGE, "");
        
        if (savedCode.isEmpty()) {
            // Primera ejecución o no guardado: detectar del sistema
            currentLanguage = detectDeviceLanguage();
            Gdx.app.log(TAG, "Idioma detectado automáticamente: " + currentLanguage.displayName);
            
            // Guardar para futuras ejecuciones
            prefs.putString(KEY_LANGUAGE, currentLanguage.code);
            prefs.putBoolean(KEY_FIRST_RUN, true);
            prefs.flush();
        } else {
            currentLanguage = Language.fromCode(savedCode);
            Gdx.app.log(TAG, "Idioma cargado de preferencias: " + currentLanguage.displayName);
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
            Gdx.app.error(TAG, "Error detectando idioma del dispositivo: " + e.getMessage());
        }
        
        return Language.EN; // Default a inglés
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
                          ": " + e.getMessage());
            
            // Fallback a inglés
            try {
                FileHandle baseFileHandle = Gdx.files.internal("i18n/strings");
                bundle = I18NBundle.createBundle(baseFileHandle, new Locale("en"), "UTF-8");
                currentLanguage = Language.EN;
                Gdx.app.log(TAG, "Fallback a inglés");
            } catch (Exception e2) {
                Gdx.app.error(TAG, "Error fatal cargando bundle de inglés: " + e2.getMessage());
                // Crear bundle vacío para evitar crashes
                bundle = null;
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
        if (bundle == null) {
            return "[" + key + "]";
        }
        try {
            return bundle.get(key);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Key no encontrada: " + key);
            return "[" + key + "]";
        }
    }
    
    /**
     * Obtiene un string localizado con formato (parámetros)
     * Ejemplo: format("game.pairs", 5) → "5 pairs" o "5 pares"
     */
    public String format(String key, Object... args) {
        if (bundle == null) {
            return "[" + key + "]";
        }
        try {
            return bundle.format(key, args);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error formateando key: " + key);
            return "[" + key + "]";
        }
    }
    
    /**
     * Cambia el idioma del juego
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
    
    /**
     * Retrocede al idioma anterior en la lista
     * @return true si necesita regenerar fuentes
     */
    public boolean cyclePreviousLanguage() {
        return setLanguage(currentLanguage.previous());
    }
    
    public Language getCurrentLanguage() {
        return currentLanguage;
    }
    
    public boolean isCJK() {
        return currentLanguage.isCJK;
    }
    
    public boolean isFirstRun() {
        return isFirstRun;
    }
    
    public void markFirstRunComplete() {
        isFirstRun = false;
        prefs.putBoolean(KEY_FIRST_RUN, true);
        prefs.flush();
    }
    
    /**
     * Obtiene todos los idiomas disponibles
     */
    public Language[] getAvailableLanguages() {
        return Language.values();
    }
    
    /**
     * Fuerza recarga del singleton
     */
    public static void reload() {
        instance = null;
    }
}
