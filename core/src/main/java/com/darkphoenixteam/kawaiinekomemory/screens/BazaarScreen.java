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
import com.darkphoenixteam.kawaiinekomemory.systems.LocaleManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SharedAssetManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla del Bazaar OPTIMIZADA
 * - Usa SharedAssetManager para texturas comunes
 * - Lazy loading de cartas (solo carga la carta ganada en gacha)
 * - Memoria reducida de ~7MB a ~200KB
 * 
 * @author DarkphoenixTeam
 * @version 4.0 - Optimización de memoria
 */
public class BazaarScreen extends BaseScreen {
    
    private static final String TAG = "BazaarScreen";
    
    // === TAP DELAY ===
    private static final float TAP_COOLDOWN = 0.3f;
    private float tapTimer = 0f;
    
    // === FONTS ===
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    // === TEXTURAS (via SharedAssetManager) ===
    private SharedAssetManager assets;
    
    // Paths que usamos (para release en dispose)
    private static final String[] TEXTURE_PATHS = {
        AssetPaths.PATTERN_BAZAAR,
        AssetPaths.ICON_NEKOIN,
        AssetPaths.ICON_HINT_HERO,
        AssetPaths.ICON_TIMEFREEZE_HERO,
        AssetPaths.ICON_GACHA,
        AssetPaths.ICON_UPGRADE,
        AssetPaths.CARD_BACK,
        AssetPaths.BTN_EMPTY,
        AssetPaths.BTN_BACK
    };
    
    // Referencias locales (no poseemos estas texturas)
    private Texture patternTexture;
    private Texture nekoinIconTexture;
    private Texture hintIconTexture;
    private Texture timefreezeIconTexture;
    private Texture gachaIconTexture;
    private Texture upgradeIconTexture;
    private Texture cardBackTexture;
    
    // === GACHA RESULT ===
    // OPTIMIZACIÓN: Solo cargamos UNA carta cuando se gana
    private Texture gachaResultTexture;
    private int lastUnlockedCardId = -1;
    private boolean showingGachaResult = false;
    private float gachaResultTimer = 0f;
    private static final float GACHA_RESULT_DURATION = 2.5f;
    
    // === BOTONES ===
    private SimpleButton backButton;
    private SimpleButton hintBuyButton;
    private SimpleButton timefreezeBuyButton;
    private SimpleButton gachaButton;
    private SimpleButton timeAttackUpgradeButton;
    
    private Texture buttonTexture;
    
    // === SISTEMAS ===
    private AudioManager audioManager;
    private SaveManager saveManager;
    private LocaleManager locale;
    
    // === LAYOUT ===
    private static final float SECTION_START_Y = 130f;
    private static final float SECTION_HEIGHT = 160f;
    private static final float BUTTON_WIDTH_PERCENT = 0.45f;
    private static final float BUTTON_HEIGHT = 45f;
    
    public BazaarScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(0.95f, 0.9f, 1f);
        
        titleFont = game.getFontManager().getTitleFont();
        buttonFont = game.getFontManager().getButtonFont();
        smallFont = game.getFontManager().getSmallFont();
        layout = new GlyphLayout();
        
        audioManager = AudioManager.getInstance();
        saveManager = SaveManager.getInstance();
        locale = LocaleManager.getInstance();
        assets = SharedAssetManager.getInstance();
        
        audioManager.playMusic(AssetPaths.MUSIC_BAZAAR, true);
        
        loadAssets();
        createButtons();
        
        Gdx.app.log(TAG, "Bazaar inicializado (optimizado)");
        Gdx.app.log(TAG, "Memoria: " + assets.getMemoryUsage());
    }
    
    // ==================== ASSETS (OPTIMIZADO) ====================
    
    private void loadAssets() {
        // Usar SharedAssetManager para todas las texturas
        patternTexture = assets.get(AssetPaths.PATTERN_BAZAAR);
        if (patternTexture != null) {
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        }
        
        nekoinIconTexture = assets.get(AssetPaths.ICON_NEKOIN);
        hintIconTexture = assets.get(AssetPaths.ICON_HINT_HERO);
        timefreezeIconTexture = assets.get(AssetPaths.ICON_TIMEFREEZE_HERO);
        gachaIconTexture = assets.get(AssetPaths.ICON_GACHA);
        upgradeIconTexture = assets.get(AssetPaths.ICON_UPGRADE);
        cardBackTexture = assets.get(AssetPaths.CARD_BACK);
        
        // Textura de botón
        buttonTexture = assets.get(AssetPaths.BTN_EMPTY);
        if (buttonTexture == null) {
            buttonTexture = assets.get(AssetPaths.BTN_PLAY);
        }
        
        // ¡NO cargamos las 35 cartas aquí!
        // Solo cargaremos la carta cuando se gane en el gacha
    }
    
    // ==================== BOTONES ====================
    
    private void createButtons() {
        float centerX = Constants.VIRTUAL_WIDTH / 2f;
        float buttonWidth = Constants.VIRTUAL_WIDTH * BUTTON_WIDTH_PERCENT;
        
        if (buttonTexture != null) {
            float hintY = Constants.VIRTUAL_HEIGHT - SECTION_START_Y - BUTTON_HEIGHT;
            hintBuyButton = new SimpleButton(buttonTexture, locale.get("bazaar.buy"),
                centerX - buttonWidth / 2f, hintY, buttonWidth, BUTTON_HEIGHT);
            hintBuyButton.setOnClick(this::buyHint);
            
            float freezeY = hintY - SECTION_HEIGHT;
            timefreezeBuyButton = new SimpleButton(buttonTexture, locale.get("bazaar.buy"),
                centerX - buttonWidth / 2f, freezeY, buttonWidth, BUTTON_HEIGHT);
            timefreezeBuyButton.setOnClick(this::buyTimeFreeze);
            
            float upgradeY = freezeY - SECTION_HEIGHT;
            timeAttackUpgradeButton = new SimpleButton(buttonTexture, locale.get("bazaar.upgrade"),
                centerX - buttonWidth / 2f, upgradeY, buttonWidth, BUTTON_HEIGHT);
            timeAttackUpgradeButton.setOnClick(this::buyTimeAttackUpgrade);
            
            float gachaY = upgradeY - SECTION_HEIGHT;
            gachaButton = new SimpleButton(buttonTexture, locale.get("bazaar.open"),
                centerX - buttonWidth / 2f, gachaY, buttonWidth, BUTTON_HEIGHT);
            gachaButton.setOnClick(this::openGacha);
        }
        
        Texture backTex = assets.get(AssetPaths.BTN_BACK);
        if (backTex != null) {
            float backWidth = Constants.VIRTUAL_WIDTH * 0.4f;
            float backHeight = backWidth * 0.35f;
            backButton = new SimpleButton(backTex, locale.get("common.back"),
                (Constants.VIRTUAL_WIDTH - backWidth) / 2f, 15f, backWidth, backHeight);
            backButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new HomeScreen(game));
            });
        }
    }
    
    // ==================== COMPRAS ====================
    
    private void buyHint() {
        if (tapTimer > 0) return;
        tapTimer = TAP_COOLDOWN;
        
        int currentHints = saveManager.getHintUses();
        
        if (currentHints >= Constants.MAX_POWER_STOCK) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            return;
        }
        
        int cost = saveManager.getHintPrice();
        
        if (saveManager.spendNekoins(cost)) {
            saveManager.addHintUses(1);
            audioManager.playSound(AssetPaths.SFX_COIN);
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
        }
    }
    
    private void buyTimeFreeze() {
        if (tapTimer > 0) return;
        tapTimer = TAP_COOLDOWN;
        
        int currentFreeze = saveManager.getTimeFreezeUses();
        
        if (currentFreeze >= Constants.MAX_POWER_STOCK) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            return;
        }
        
        int cost = saveManager.getTimeFreezePrice();
        
        if (saveManager.spendNekoins(cost)) {
            saveManager.addTimeFreezeUses(1);
            audioManager.playSound(AssetPaths.SFX_COIN);
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
        }
    }
    
    private void buyTimeAttackUpgrade() {
        if (tapTimer > 0) return;
        tapTimer = TAP_COOLDOWN;
        
        int cost = saveManager.getTimeAttackUpgradeCost();
        
        if (cost < 0) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            return;
        }
        
        if (saveManager.purchaseTimeAttackUpgrade()) {
            audioManager.playSound(AssetPaths.SFX_VICTORY);
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
        }
    }
    
    private void openGacha() {
        if (tapTimer > 0) return;
        tapTimer = TAP_COOLDOWN;
        
        Array<Integer> locked = getLockedCards();
        if (locked.size == 0) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            return;
        }
        
        int cost = saveManager.getGachaCost();
        
        if (saveManager.spendNekoins(cost)) {
            saveManager.incrementGachaPulls();
            
            int randomIndex = MathUtils.random(0, locked.size - 1);
            int cardId = locked.get(randomIndex);
            
            saveManager.unlockCard(cardId);
            
            // OPTIMIZACIÓN: Solo cargamos ESTA carta
            loadGachaResultCard(cardId);
            
            audioManager.playSound(AssetPaths.SFX_VICTORY);
            lastUnlockedCardId = cardId;
            showingGachaResult = true;
            gachaResultTimer = GACHA_RESULT_DURATION;
            
            Gdx.app.log(TAG, "Gacha! Card " + cardId + " desbloqueada");
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
        }
    }
    
    /**
     * OPTIMIZACIÓN: Carga solo la carta ganada
     */
    private void loadGachaResultCard(int cardId) {
        // Liberar carta anterior si existe
        if (gachaResultTexture != null) {
            gachaResultTexture.dispose();
            gachaResultTexture = null;
        }
        
        int deck = SaveManager.getDeckFromCardId(cardId);
        int index = SaveManager.getCardIndexFromCardId(cardId);
        String path = AssetPaths.getCardPath(deck, index);
        
        try {
            gachaResultTexture = new Texture(Gdx.files.internal(path));
            Gdx.app.log(TAG, "Carta cargada para resultado: " + path);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando carta: " + path);
            gachaResultTexture = null;
        }
    }
    
    private Array<Integer> getLockedCards() {
        Array<Integer> locked = new Array<>();
        for (int i = 0; i < Constants.TOTAL_CARDS; i++) {
            if (!saveManager.isCardUnlocked(i)) {
                locked.add(i);
            }
        }
        return locked;
    }
    
    // ==================== UPDATE ====================
    
    @Override
    protected void update(float delta) {
        if (tapTimer > 0) tapTimer -= delta;
        
        if (showingGachaResult) {
            gachaResultTimer -= delta;
            if (gachaResultTimer <= 0 || (Gdx.input.justTouched() && tapTimer <= 0)) {
                showingGachaResult = false;
                
                // Liberar la textura de la carta mostrada
                if (gachaResultTexture != null) {
                    gachaResultTexture.dispose();
                    gachaResultTexture = null;
                }
                
                lastUnlockedCardId = -1;
                tapTimer = TAP_COOLDOWN;
            }
            return;
        }
        
        if (!isInputEnabled()) return;
        
        int hintUses = saveManager.getHintUses();
        if (hintBuyButton != null && hintUses < Constants.MAX_POWER_STOCK) {
            hintBuyButton.update(viewport);
        }
        
        int freezeUses = saveManager.getTimeFreezeUses();
        if (timefreezeBuyButton != null && freezeUses < Constants.MAX_POWER_STOCK) {
            timefreezeBuyButton.update(viewport);
        }
        
        int upgradeCost = saveManager.getTimeAttackUpgradeCost();
        if (timeAttackUpgradeButton != null && upgradeCost > 0) {
            timeAttackUpgradeButton.update(viewport);
        }
        
        if (gachaButton != null && getLockedCards().size > 0) {
            gachaButton.update(viewport);
        }
        
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
        drawTimeAttackUpgradeSection();
        drawGachaSection();
        
        if (backButton != null) backButton.draw(game.getBatch(), buttonFont);
        
        game.getBatch().end();
        
        if (showingGachaResult) {
            drawGachaResult();
        }
    }
    
    private void drawBackground() {
        if (patternTexture != null) {
            saveColor();
            game.getBatch().setColor(1f, 1f, 1f, 0.3f);
            int tileSize = 512;
            for (int x = 0; x < Constants.VIRTUAL_WIDTH; x += tileSize) {
                for (int y = 0; y < Constants.VIRTUAL_HEIGHT; y += tileSize) {
                    game.getBatch().draw(patternTexture, x, y, tileSize, tileSize);
                }
            }
            restoreColor();
        }
    }
    
    private void drawHeader() {
        String title = locale.get("bazaar.title");
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title,
            (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
            Constants.VIRTUAL_HEIGHT - 25f);
        
        if (nekoinIconTexture != null) {
            float iconSize = 32f;
            String nekoins = String.valueOf(saveManager.getNekoins());
            layout.setText(titleFont, nekoins);
            
            float totalWidth = iconSize + 10f + layout.width;
            float iconX = (Constants.VIRTUAL_WIDTH - totalWidth) / 2f;
            float iconY = Constants.VIRTUAL_HEIGHT - 75f;
            
            game.getBatch().draw(nekoinIconTexture, iconX, iconY, iconSize, iconSize);
            titleFont.setColor(Color.GOLD);
            titleFont.draw(game.getBatch(), nekoins, iconX + iconSize + 10f, iconY + iconSize - 5f);
            titleFont.setColor(Color.WHITE);
        }
    }
    
    private void drawHintSection() {
        float sectionY = Constants.VIRTUAL_HEIGHT - SECTION_START_Y;
        float iconSize = 50f;
        float textX = 80f;
        
        if (hintIconTexture != null) {
            game.getBatch().draw(hintIconTexture, 20f, sectionY - iconSize - 10f, iconSize, iconSize);
        }
        
        buttonFont.setColor(Color.WHITE);
        buttonFont.draw(game.getBatch(), locale.get("bazaar.hint"), textX, sectionY);
        
        int uses = saveManager.getHintUses();
        int price = saveManager.getHintPrice();
        
        String usesText = locale.format("bazaar.stock", uses, Constants.MAX_POWER_STOCK);
        smallFont.setColor(uses >= Constants.MAX_POWER_STOCK ? Color.GREEN : Color.WHITE);
        smallFont.draw(game.getBatch(), usesText, textX, sectionY - 22f);
        
        if (uses >= Constants.MAX_POWER_STOCK) {
            smallFont.setColor(Color.GREEN);
            smallFont.draw(game.getBatch(), locale.get("bazaar.stock.full"), textX, sectionY - 42f);
        } else {
            boolean canAfford = saveManager.getNekoins() >= price;
            smallFont.setColor(canAfford ? Color.GOLD : Color.RED);
            smallFont.draw(game.getBatch(), locale.format("bazaar.price", price), textX, sectionY - 42f);
            
            if (hintBuyButton != null) {
                hintBuyButton.draw(game.getBatch(), buttonFont);
            }
        }
        
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(game.getBatch(), locale.get("bazaar.hint.desc"), textX, sectionY - 62f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawTimeFreezeSection() {
        float sectionY = Constants.VIRTUAL_HEIGHT - SECTION_START_Y - SECTION_HEIGHT;
        float iconSize = 50f;
        float textX = 80f;
        
        if (timefreezeIconTexture != null) {
            game.getBatch().draw(timefreezeIconTexture, 20f, sectionY - iconSize - 10f, iconSize, iconSize);
        }
        
        buttonFont.setColor(Color.WHITE);
        buttonFont.draw(game.getBatch(), locale.get("bazaar.timefreeze"), textX, sectionY);
        
        int uses = saveManager.getTimeFreezeUses();
        int price = saveManager.getTimeFreezePrice();
        
        String usesText = locale.format("bazaar.stock", uses, Constants.MAX_POWER_STOCK);
        smallFont.setColor(uses >= Constants.MAX_POWER_STOCK ? Color.CYAN : Color.WHITE);
        smallFont.draw(game.getBatch(), usesText, textX, sectionY - 22f);
        
        if (uses >= Constants.MAX_POWER_STOCK) {
            smallFont.setColor(Color.CYAN);
            smallFont.draw(game.getBatch(), locale.get("bazaar.stock.full"), textX, sectionY - 42f);
        } else {
            boolean canAfford = saveManager.getNekoins() >= price;
            smallFont.setColor(canAfford ? Color.GOLD : Color.RED);
            smallFont.draw(game.getBatch(), locale.format("bazaar.price", price), textX, sectionY - 42f);
            
            if (timefreezeBuyButton != null) {
                timefreezeBuyButton.draw(game.getBatch(), buttonFont);
            }
        }
        
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(game.getBatch(), locale.get("bazaar.timefreeze.desc"), textX, sectionY - 62f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawTimeAttackUpgradeSection() {
        float sectionY = Constants.VIRTUAL_HEIGHT - SECTION_START_Y - SECTION_HEIGHT * 2;
        float iconSize = 50f;
        float textX = 80f;
        
        if (upgradeIconTexture != null) {
            game.getBatch().draw(upgradeIconTexture, 20f, sectionY - iconSize - 10f, iconSize, iconSize);
        }
        
        buttonFont.setColor(Color.ORANGE);
        buttonFont.draw(game.getBatch(), locale.get("bazaar.timeattack"), textX, sectionY);
        buttonFont.setColor(Color.WHITE);
        
        float currentTime = saveManager.getTimeAttackTime();
        int cost = saveManager.getTimeAttackUpgradeCost();
        
        String timeText = locale.format("game.time", formatTime(currentTime));
        smallFont.setColor(Color.WHITE);
        smallFont.draw(game.getBatch(), timeText, textX, sectionY - 22f);
        
        if (cost < 0 || currentTime >= Constants.TIME_ATTACK_MAX_TIME) {
            smallFont.setColor(Color.LIME);
            smallFont.draw(game.getBatch(), locale.get("bazaar.timeattack.max"), textX, sectionY - 42f);
        } else {
            boolean canAfford = saveManager.getNekoins() >= cost;
            smallFont.setColor(canAfford ? Color.GOLD : Color.RED);
            smallFont.draw(game.getBatch(), locale.format("bazaar.timeattack.upgrade", cost), textX, sectionY - 42f);
            
            if (timeAttackUpgradeButton != null) {
                timeAttackUpgradeButton.draw(game.getBatch(), buttonFont);
            }
        }
        
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(game.getBatch(), locale.format("bazaar.timeattack.desc", formatTime(Constants.TIME_ATTACK_MAX_TIME)), textX, sectionY - 62f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawGachaSection() {
        float sectionY = Constants.VIRTUAL_HEIGHT - SECTION_START_Y - SECTION_HEIGHT * 3;
        float iconSize = 50f;
        float textX = 80f;
        
        if (gachaIconTexture != null) {
            game.getBatch().draw(gachaIconTexture, 20f, sectionY - iconSize - 10f, iconSize, iconSize);
        }
        
        buttonFont.setColor(Color.MAGENTA);
        buttonFont.draw(game.getBatch(), locale.get("bazaar.gacha"), textX, sectionY);
        buttonFont.setColor(Color.WHITE);
        
        int unlocked = saveManager.getUnlockedCardCount();
        int cost = saveManager.getGachaCost();
        
        String progressText = locale.format("bazaar.cards", unlocked, Constants.TOTAL_CARDS);
        smallFont.setColor(unlocked >= Constants.TOTAL_CARDS ? Color.LIME : Color.WHITE);
        smallFont.draw(game.getBatch(), progressText, textX, sectionY - 22f);
        
        if (unlocked >= Constants.TOTAL_CARDS) {
            smallFont.setColor(Color.LIME);
            smallFont.draw(game.getBatch(), locale.get("bazaar.gacha.complete"), textX, sectionY - 42f);
        } else {
            boolean canAfford = saveManager.getNekoins() >= cost;
            smallFont.setColor(canAfford ? Color.GOLD : Color.RED);
            smallFont.draw(game.getBatch(), locale.format("bazaar.price", cost), textX, sectionY - 42f);
            
            if (gachaButton != null) {
                gachaButton.draw(game.getBatch(), buttonFont);
            }
        }
        
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(game.getBatch(), locale.format("bazaar.pulls", saveManager.getGachaPulls()), textX, sectionY - 62f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawGachaResult() {
        game.getBatch().begin();
        
        // Fondo oscuro
        saveColor();
        game.getBatch().setColor(0, 0, 0, 0.9f);
        if (cardBackTexture != null) {
            game.getBatch().draw(cardBackTexture, 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        }
        restoreColor();
        
        // Título
        titleFont.setColor(Color.GOLD);
        String title = locale.get("bazaar.newcard");
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                      Constants.VIRTUAL_HEIGHT * 0.82f);
        titleFont.setColor(Color.WHITE);
        
        // Carta ganada (OPTIMIZADO: solo esta carta está cargada)
        if (gachaResultTexture != null) {
            float cardWidth = 150f;
            float cardHeight = cardWidth * 1.4f;
            float cardX = (Constants.VIRTUAL_WIDTH - cardWidth) / 2f;
            float cardY = (Constants.VIRTUAL_HEIGHT - cardHeight) / 2f + 30f;
            game.getBatch().draw(gachaResultTexture, cardX, cardY, cardWidth, cardHeight);
        }
        
        // Info del deck
        int deck = SaveManager.getDeckFromCardId(lastUnlockedCardId);
        String[] deckNames = {"Base", "★", "★★", "★★★", "♥"};
        int[] nekoinValues = {1, 2, 3, 5, 7};
        
        String deckName = (deck >= 0 && deck < deckNames.length) ? deckNames[deck] : "???";
        int nekoinVal = (deck >= 0 && deck < nekoinValues.length) ? nekoinValues[deck] : 1;
        
        buttonFont.setColor(getDeckColor(deck));
        String deckText = locale.format("bazaar.deck", deckName);
        layout.setText(buttonFont, deckText);
        buttonFont.draw(game.getBatch(), deckText, 
                       (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                       Constants.VIRTUAL_HEIGHT * 0.28f);
        
        buttonFont.setColor(Color.GOLD);
        String valueText = locale.format("bazaar.pairvalue", nekoinVal);
        layout.setText(buttonFont, valueText);
        buttonFont.draw(game.getBatch(), valueText, 
                       (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                       Constants.VIRTUAL_HEIGHT * 0.22f);
        buttonFont.setColor(Color.WHITE);
        
        // Instrucción
        smallFont.setColor(Color.GRAY);
        String tapText = locale.get("bazaar.tapclose");
        layout.setText(smallFont, tapText);
        smallFont.draw(game.getBatch(), tapText, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                      Constants.VIRTUAL_HEIGHT * 0.10f);
        smallFont.setColor(Color.WHITE);
        
        game.getBatch().end();
    }
    
    private Color getDeckColor(int deckIndex) {
        switch (deckIndex) {
            case 0: return Color.LIGHT_GRAY;
            case 1: return Color.WHITE;
            case 2: return Color.YELLOW;
            case 3: return Color.GOLD;
            case 4: return Color.CORAL;
            default: return Color.WHITE;
        }
    }
    
    private String formatTime(float seconds) {
        int mins = (int)(seconds / 60);
        int secs = (int)(seconds % 60);
        return String.format("%d:%02d", mins, secs);
    }
    
    // ==================== DISPOSE (OPTIMIZADO) ====================
    
    @Override
    public void dispose() {
        Gdx.app.log(TAG, "Liberando recursos...");
        
        // Liberar referencias del SharedAssetManager
        for (String path : TEXTURE_PATHS) {
            assets.release(path);
        }
        
        // Liberar la carta del resultado gacha si existe
        if (gachaResultTexture != null) {
            gachaResultTexture.dispose();
            gachaResultTexture = null;
        }
        
        // Los botones no poseen las texturas, no hacer dispose
        
        Gdx.app.log(TAG, "Recursos liberados. Estado: " + assets.getMemoryUsage());
    }
                }
