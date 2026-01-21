package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla de tienda (Bazaar)
 * Permite comprar/mejorar powers y desbloquear cartas con Gachapon
 * 
 * @author DarkphoenixTeam
 */
public class BazaarScreen extends BaseScreen {
    
    private static final String TAG = "BazaarScreen";
    
    // ==================== COSTOS ====================
    
    private static final int GACHA_COST = 50;
    private static final int HINT_BASE_COST = 20;
    private static final int TIMEFREEZE_BASE_COST = 10;
    private static final int UPGRADE_BASE_COST = 100;
    private static final float UPGRADE_MULTIPLIER = 1.5f;
    private static final int MAX_POWER_LEVEL = 5;
    
    // ==================== FONTS ====================
    
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    // ==================== TEXTURAS ====================
    
    private Texture patternTexture;
    private Texture nekoinIconTexture;
    private Texture hintIconTexture;
    private Texture timefreezeIconTexture;
    private Texture gachaIconTexture;
    private Texture upgradeIconTexture;
    private Texture cardBackTexture;
    
    // Texturas de cartas para mostrar resultado gacha
    private Array<Texture> allCardTextures;
    
    // ==================== BOTONES ====================
    
    private SimpleButton backButton;
    private SimpleButton hintBuyButton;
    private SimpleButton hintUpgradeButton;
    private SimpleButton timefreezeBuyButton;
    private SimpleButton timefreezeUpgradeButton;
    private SimpleButton gachaButton;
    
    // ==================== ESTADO ====================
    
    private boolean showingGachaResult = false;
    private int lastUnlockedCardId = -1;
    private float gachaResultTimer = 0f;
    private static final float GACHA_RESULT_DURATION = 2.5f;
    
    // ==================== MANAGERS ====================
    
    private AudioManager audioManager;
    private SaveManager saveManager;
    
    // ==================== CONSTRUCTOR ====================
    
    public BazaarScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(0.95f, 0.9f, 1f);  // Lavanda claro
        
        titleFont = game.getFontManager().getTitleFont();
        buttonFont = game.getFontManager().getButtonFont();
        smallFont = game.getFontManager().getSmallFont();
        layout = new GlyphLayout();
        
        audioManager = AudioManager.getInstance();
        saveManager = SaveManager.getInstance();
        
        allCardTextures = new Array<>();
        
        loadAssets();
        createButtons();
        
        Gdx.app.log(TAG, "Inicializado - Nekoins: " + saveManager.getNekoins());
    }
    
    // ==================== CARGA DE ASSETS ====================
    
    private void loadAssets() {
        try {
            patternTexture = new Texture(Gdx.files.internal(AssetPaths.PATTERN_BAZAAR));
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando pattern bazaar");
        }
        
        try {
            nekoinIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_NEKOIN));
            hintIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_HINT_HERO));
            timefreezeIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_TIMEFREEZE_HERO));
            gachaIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_GACHA));
            upgradeIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_UPGRADE));
            cardBackTexture = new Texture(Gdx.files.internal(AssetPaths.CARD_BACK));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando iconos: " + e.getMessage());
        }
        
        // Cargar todas las cartas para mostrar resultado gacha
        for (int deck = 0; deck < AssetPaths.TOTAL_DECKS; deck++) {
            for (int card = 0; card < AssetPaths.CARDS_PER_DECK; card++) {
                String path = AssetPaths.getCardPath(deck, card);
                try {
                    Texture tex = new Texture(Gdx.files.internal(path));
                    allCardTextures.add(tex);
                } catch (Exception e) {
                    allCardTextures.add(null);
                }
            }
        }
    }
    
    // ==================== CREACIÓN DE BOTONES ====================
    
    private void createButtons() {
        float centerX = Constants.VIRTUAL_WIDTH / 2f;
        float buttonWidth = Constants.VIRTUAL_WIDTH * 0.4f;
        float buttonHeight = 45f;
        float smallButtonWidth = buttonWidth * 0.45f;
        
        // === SECCIÓN HINT ===
        float hintY = Constants.VIRTUAL_HEIGHT - 200f;
        
        try {
            Texture btnTex = new Texture(Gdx.files.internal(AssetPaths.BTN_PLAY));
            
            // Botón comprar Hint
            hintBuyButton = new SimpleButton(btnTex, "COMPRAR", 
                centerX - buttonWidth - 10f, hintY, 
                buttonWidth, buttonHeight);
            hintBuyButton.setOnClick(this::buyHint);
            
            // Botón mejorar Hint
            hintUpgradeButton = new SimpleButton(btnTex, "MEJORAR",
                centerX + 10f, hintY,
                buttonWidth, buttonHeight);
            hintUpgradeButton.setOnClick(this::upgradeHint);
            
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error creando botones Hint");
        }
        
        // === SECCIÓN TIMEFREEZE ===
        float freezeY = Constants.VIRTUAL_HEIGHT - 370f;
        
        try {
            Texture btnTex = new Texture(Gdx.files.internal(AssetPaths.BTN_PLAY));
            
            // Botón comprar TimeFreeze
            timefreezeBuyButton = new SimpleButton(btnTex, "COMPRAR",
                centerX - buttonWidth - 10f, freezeY,
                buttonWidth, buttonHeight);
            timefreezeBuyButton.setOnClick(this::buyTimeFreeze);
            
            // Botón mejorar TimeFreeze
            timefreezeUpgradeButton = new SimpleButton(btnTex, "MEJORAR",
                centerX + 10f, freezeY,
                buttonWidth, buttonHeight);
            timefreezeUpgradeButton.setOnClick(this::upgradeTimeFreeze);
            
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error creando botones TimeFreeze");
        }
        
        // === SECCIÓN GACHA ===
        float gachaY = Constants.VIRTUAL_HEIGHT - 540f;
        
        try {
            Texture btnTex = new Texture(Gdx.files.internal(AssetPaths.BTN_BAZAAR));
            
            gachaButton = new SimpleButton(btnTex, "ABRIR x" + GACHA_COST,
                centerX - buttonWidth / 2f, gachaY,
                buttonWidth, buttonHeight);
            gachaButton.setOnClick(this::openGacha);
            
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error creando botón Gacha");
        }
        
        // === BOTÓN VOLVER ===
        try {
            Texture backTex = new Texture(Gdx.files.internal(AssetPaths.BTN_BACK));
            float backWidth = Constants.VIRTUAL_WIDTH * 0.4f;
            float backHeight = backWidth * 0.35f;
            
            backButton = new SimpleButton(backTex, "VOLVER",
                (Constants.VIRTUAL_WIDTH - backWidth) / 2f, 20f,
                backWidth, backHeight);
            backButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new HomeScreen(game));
            });
            
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error creando botón back");
        }
    }
    
    // ==================== LÓGICA DE COMPRAS ====================
    
    private void buyHint() {
        int cost = getHintCost();
        
        if (saveManager.spendNekoins(cost)) {
            saveManager.upgradeHint();
            audioManager.playSound(AssetPaths.SFX_COIN);
            Gdx.app.log(TAG, "Hint comprado! Nivel: " + saveManager.getHintLevel());
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "No hay suficientes nekoins para Hint");
        }
    }
    
    private void upgradeHint() {
        int level = saveManager.getHintLevel();
        if (level <= 0) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Primero debes comprar Hint");
            return;
        }
        
        if (level >= MAX_POWER_LEVEL) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Hint ya está al máximo nivel");
            return;
        }
        
        int cost = getUpgradeCost(level);
        
        if (saveManager.spendNekoins(cost)) {
            saveManager.upgradeHint();
            audioManager.playSound(AssetPaths.SFX_MATCH);
            Gdx.app.log(TAG, "Hint mejorado! Nivel: " + saveManager.getHintLevel());
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
        }
    }
    
    private void buyTimeFreeze() {
        int cost = getTimeFreezeCost();
        
        if (saveManager.spendNekoins(cost)) {
            saveManager.upgradeTimeFreeze();
            audioManager.playSound(AssetPaths.SFX_COIN);
            Gdx.app.log(TAG, "TimeFreeze comprado! Nivel: " + saveManager.getTimeFreezeLevel());
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "No hay suficientes nekoins para TimeFreeze");
        }
    }
    
    private void upgradeTimeFreeze() {
        int level = saveManager.getTimeFreezeLevel();
        if (level <= 0) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Primero debes comprar TimeFreeze");
            return;
        }
        
        if (level >= MAX_POWER_LEVEL) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "TimeFreeze ya está al máximo nivel");
            return;
        }
        
        int cost = getUpgradeCost(level);
        
        if (saveManager.spendNekoins(cost)) {
            saveManager.upgradeTimeFreeze();
            audioManager.playSound(AssetPaths.SFX_MATCH);
            Gdx.app.log(TAG, "TimeFreeze mejorado! Nivel: " + saveManager.getTimeFreezeLevel());
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
        }
    }
    
    private void openGacha() {
        // Verificar si hay cartas por desbloquear
        Array<Integer> locked = getLockedCards();
        
        if (locked.size == 0) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Todas las cartas están desbloqueadas!");
            return;
        }
        
        if (saveManager.spendNekoins(GACHA_COST)) {
            // Seleccionar carta aleatoria
            int randomIndex = MathUtils.random(0, locked.size - 1);
            int cardId = locked.get(randomIndex);
            
            saveManager.unlockCard(cardId);
            audioManager.playSound(AssetPaths.SFX_VICTORY);
            
            // Mostrar resultado
            lastUnlockedCardId = cardId;
            showingGachaResult = true;
            gachaResultTimer = GACHA_RESULT_DURATION;
            
            int deck = SaveManager.getDeckFromCardId(cardId);
            int card = SaveManager.getCardIndexFromCardId(cardId);
            Gdx.app.log(TAG, "Gacha! Desbloqueada carta " + cardId + " (Deck " + deck + ", Card " + card + ")");
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "No hay suficientes nekoins para Gacha");
        }
    }
    
    // ==================== CÁLCULOS DE COSTOS ====================
    
    private int getHintCost() {
        int level = saveManager.getHintLevel();
        if (level == 0) {
            return HINT_BASE_COST;
        }
        // Ya comprado, mostrar costo de mejora
        return getUpgradeCost(level);
    }
    
    private int getTimeFreezeCost() {
        int level = saveManager.getTimeFreezeLevel();
        if (level == 0) {
            return TIMEFREEZE_BASE_COST;
        }
        return getUpgradeCost(level);
    }
    
    private int getUpgradeCost(int currentLevel) {
        // 100 * 1.5^level (redondeado hacia abajo)
        float cost = UPGRADE_BASE_COST * (float) Math.pow(UPGRADE_MULTIPLIER, currentLevel - 1);
        return (int) cost;
    }
    
    private Array<Integer> getLockedCards() {
        Array<Integer> locked = new Array<>();
        for (int i = 0; i < 35; i++) {
            if (!saveManager.isCardUnlocked(i)) {
                locked.add(i);
            }
        }
        return locked;
    }
    
    // ==================== UPDATE ====================
    
    @Override
    protected void update(float delta) {
        // Actualizar timer de resultado gacha
        if (showingGachaResult) {
            gachaResultTimer -= delta;
            if (gachaResultTimer <= 0) {
                showingGachaResult = false;
                lastUnlockedCardId = -1;
            }
            return;  // No procesar otros inputs durante resultado
        }
        
        if (!isInputEnabled()) return;
        
        // Actualizar botones
        if (hintBuyButton != null) hintBuyButton.update(viewport);
        if (hintUpgradeButton != null) hintUpgradeButton.update(viewport);
        if (timefreezeBuyButton != null) timefreezeBuyButton.update(viewport);
        if (timefreezeUpgradeButton != null) timefreezeUpgradeButton.update(viewport);
        if (gachaButton != null) gachaButton.update(viewport);
        if (backButton != null) backButton.update(viewport);
    }
    
    // ==================== DRAW ====================
    
    @Override
    protected void draw() {
        game.getBatch().begin();
        
        drawBackground();
        drawHeader();
        drawHintSection();
        drawTimeFreezeSection();
        drawGachaSection();
        
        // Botones
        if (hintBuyButton != null) {
            int hintLevel = saveManager.getHintLevel();
            if (hintLevel == 0) {
                hintBuyButton.draw(game.getBatch(), buttonFont);
            }
        }
        if (hintUpgradeButton != null) {
            int hintLevel = saveManager.getHintLevel();
            if (hintLevel > 0 && hintLevel < MAX_POWER_LEVEL) {
                hintUpgradeButton.draw(game.getBatch(), buttonFont);
            }
        }
        
        if (timefreezeBuyButton != null) {
            int freezeLevel = saveManager.getTimeFreezeLevel();
            if (freezeLevel == 0) {
                timefreezeBuyButton.draw(game.getBatch(), buttonFont);
            }
        }
        if (timefreezeUpgradeButton != null) {
            int freezeLevel = saveManager.getTimeFreezeLevel();
            if (freezeLevel > 0 && freezeLevel < MAX_POWER_LEVEL) {
                timefreezeUpgradeButton.draw(game.getBatch(), buttonFont);
            }
        }
        
        if (gachaButton != null) {
            Array<Integer> locked = getLockedCards();
            if (locked.size > 0) {
                gachaButton.draw(game.getBatch(), buttonFont);
            }
        }
        
        if (backButton != null) backButton.draw(game.getBatch(), buttonFont);
        
        game.getBatch().end();
        
        // Resultado Gacha (overlay)
        if (showingGachaResult) {
            drawGachaResult();
        }
    }
    
    private void drawBackground() {
        if (patternTexture != null) {
            game.getBatch().setColor(1f, 1f, 1f, 0.3f);
            int tileSize = 512;
            for (int x = 0; x < Constants.VIRTUAL_WIDTH; x += tileSize) {
                for (int y = 0; y < Constants.VIRTUAL_HEIGHT; y += tileSize) {
                    game.getBatch().draw(patternTexture, x, y, tileSize, tileSize);
                }
            }
            game.getBatch().setColor(1f, 1f, 1f, 1f);
        }
    }
    
    private void drawHeader() {
        // Título
        String title = "BAZAAR";
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title,
            (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
            Constants.VIRTUAL_HEIGHT - 30f);
        
        // Nekoins
        if (nekoinIconTexture != null) {
            float iconSize = 35f;
            float iconX = Constants.VIRTUAL_WIDTH / 2f - 50f;
            float iconY = Constants.VIRTUAL_HEIGHT - 80f;
            
            game.getBatch().draw(nekoinIconTexture, iconX, iconY, iconSize, iconSize);
            
            String nekoins = String.valueOf(saveManager.getNekoins());
            layout.setText(titleFont, nekoins);
            titleFont.draw(game.getBatch(), nekoins, iconX + iconSize + 10f, iconY + iconSize - 5f);
        }
    }
    
    private void drawHintSection() {
        float sectionY = Constants.VIRTUAL_HEIGHT - 130f;
        float centerX = Constants.VIRTUAL_WIDTH / 2f;
        
        // Icono
        if (hintIconTexture != null) {
            float iconSize = 50f;
            game.getBatch().draw(hintIconTexture, 20f, sectionY - iconSize - 10f, iconSize, iconSize);
        }
        
        // Título
        String title = "PISTA";
        layout.setText(buttonFont, title);
        buttonFont.draw(game.getBatch(), title, 80f, sectionY);
        
        // Estado y costo
        int level = saveManager.getHintLevel();
        String statusText;
        String costText;
        
        if (level == 0) {
            statusText = "No comprado";
            costText = "Costo: " + HINT_BASE_COST;
            smallFont.setColor(Color.GRAY);
        } else if (level >= MAX_POWER_LEVEL) {
            statusText = "Nivel MAX (" + level + ")";
            costText = "Completado!";
            smallFont.setColor(Color.GREEN);
        } else {
            statusText = "Nivel " + level + "/" + MAX_POWER_LEVEL;
            costText = "Mejora: " + getUpgradeCost(level);
            smallFont.setColor(Color.WHITE);
        }
        
        layout.setText(smallFont, statusText);
        smallFont.draw(game.getBatch(), statusText, 80f, sectionY - 25f);
        
        layout.setText(smallFont, costText);
        smallFont.draw(game.getBatch(), costText, 80f, sectionY - 45f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawTimeFreezeSection() {
        float sectionY = Constants.VIRTUAL_HEIGHT - 300f;
        
        // Icono
        if (timefreezeIconTexture != null) {
            float iconSize = 50f;
            game.getBatch().draw(timefreezeIconTexture, 20f, sectionY - iconSize - 10f, iconSize, iconSize);
        }
        
        // Título
        String title = "CONGELAR TIEMPO";
        layout.setText(buttonFont, title);
        buttonFont.draw(game.getBatch(), title, 80f, sectionY);
        
        // Estado y costo
        int level = saveManager.getTimeFreezeLevel();
        String statusText;
        String costText;
        
        if (level == 0) {
            statusText = "No comprado";
            costText = "Costo: " + TIMEFREEZE_BASE_COST;
            smallFont.setColor(Color.GRAY);
        } else if (level >= MAX_POWER_LEVEL) {
            statusText = "Nivel MAX (" + level + ")";
            costText = "Completado!";
            smallFont.setColor(Color.CYAN);
        } else {
            statusText = "Nivel " + level + "/" + MAX_POWER_LEVEL;
            costText = "Mejora: " + getUpgradeCost(level);
            smallFont.setColor(Color.WHITE);
        }
        
        layout.setText(smallFont, statusText);
        smallFont.draw(game.getBatch(), statusText, 80f, sectionY - 25f);
        
        layout.setText(smallFont, costText);
        smallFont.draw(game.getBatch(), costText, 80f, sectionY - 45f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawGachaSection() {
        float sectionY = Constants.VIRTUAL_HEIGHT - 470f;
        float centerX = Constants.VIRTUAL_WIDTH / 2f;
        
        // Icono centrado
        if (gachaIconTexture != null) {
            float iconSize = 60f;
            game.getBatch().draw(gachaIconTexture, centerX - iconSize / 2f, sectionY - 10f, iconSize, iconSize);
        }
        
        // Título
        String title = "COFRE GACHAPON";
        layout.setText(buttonFont, title);
        buttonFont.draw(game.getBatch(), title,
            (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
            sectionY + 70f);
        
        // Estado
        Array<Integer> locked = getLockedCards();
        int unlocked = 35 - locked.size;
        String statusText = "Cartas: " + unlocked + "/35";
        
        if (locked.size == 0) {
            smallFont.setColor(Color.GREEN);
            statusText = "Todas desbloqueadas!";
        } else {
            smallFont.setColor(Color.WHITE);
        }
        
        layout.setText(smallFont, statusText);
        smallFont.draw(game.getBatch(), statusText,
            (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
            sectionY - 70f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawGachaResult() {
        game.getBatch().begin();
        
        // Overlay oscuro
        game.getBatch().setColor(0, 0, 0, 0.8f);
        if (cardBackTexture != null) {
            game.getBatch().draw(cardBackTexture, 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        }
        game.getBatch().setColor(1, 1, 1, 1);
        
        // Título
        String title = "NUEVA CARTA!";
        layout.setText(titleFont, title);
        titleFont.setColor(Color.GOLD);
        titleFont.draw(game.getBatch(), title,
            (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
            Constants.VIRTUAL_HEIGHT * 0.75f);
        titleFont.setColor(Color.WHITE);
        
        // Carta desbloqueada
        if (lastUnlockedCardId >= 0 && lastUnlockedCardId < allCardTextures.size) {
            Texture cardTex = allCardTextures.get(lastUnlockedCardId);
            if (cardTex != null) {
                float cardWidth = 150f;
                float cardHeight = cardWidth * 1.4f;
                float cardX = (Constants.VIRTUAL_WIDTH - cardWidth) / 2f;
                float cardY = (Constants.VIRTUAL_HEIGHT - cardHeight) / 2f;
                
                game.getBatch().draw(cardTex, cardX, cardY, cardWidth, cardHeight);
            }
        }
        
        // Info de la carta
        int deck = SaveManager.getDeckFromCardId(lastUnlockedCardId);
        String[] deckNames = {"Base", "☆", "☆☆", "☆☆☆", "♡"};
        int[] values = {1, 2, 3, 5, 7};
        
        String deckName = (deck >= 0 && deck < deckNames.length) ? deckNames[deck] : "???";
        int value = (deck >= 0 && deck < values.length) ? values[deck] : 1;
        
        String infoText = "Deck " + deckName + " | +" + value + " Nekoin/par";
        layout.setText(buttonFont, infoText);
        buttonFont.draw(game.getBatch(), infoText,
            (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
            Constants.VIRTUAL_HEIGHT * 0.28f);
        
        // Instrucción
        String tapText = "Toca para continuar";
        layout.setText(smallFont, tapText);
        smallFont.setColor(Color.GRAY);
        smallFont.draw(game.getBatch(), tapText,
            (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
            Constants.VIRTUAL_HEIGHT * 0.15f);
        smallFont.setColor(Color.WHITE);
        
        game.getBatch().end();
        
        // Detectar toque para cerrar
        if (Gdx.input.justTouched()) {
            showingGachaResult = false;
            lastUnlockedCardId = -1;
        }
    }
    
    // ==================== DISPOSE ====================
    
    @Override
    public void dispose() {
        Gdx.app.log(TAG, "Liberando recursos...");
        
        if (patternTexture != null) patternTexture.dispose();
        if (nekoinIconTexture != null) nekoinIconTexture.dispose();
        if (hintIconTexture != null) hintIconTexture.dispose();
        if (timefreezeIconTexture != null) timefreezeIconTexture.dispose();
        if (gachaIconTexture != null) gachaIconTexture.dispose();
        if (upgradeIconTexture != null) upgradeIconTexture.dispose();
        if (cardBackTexture != null) cardBackTexture.dispose();
        
        for (Texture tex : allCardTextures) {
            if (tex != null) tex.dispose();
        }
        allCardTextures.clear();
        
        if (backButton != null) backButton.dispose();
        if (hintBuyButton != null) hintBuyButton.dispose();
        if (hintUpgradeButton != null) hintUpgradeButton.dispose();
        if (timefreezeBuyButton != null) timefreezeBuyButton.dispose();
        if (timefreezeUpgradeButton != null) timefreezeUpgradeButton.dispose();
        if (gachaButton != null) gachaButton.dispose();
    }
}