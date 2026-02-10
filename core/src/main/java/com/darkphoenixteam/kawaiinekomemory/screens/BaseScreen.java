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
 * Pantalla del Bazaar - Compra de Powers, Gachapon y Mejoras de Time Attack
 * 
 * Precios:
 * - Powers: 500/1000/2000/2500/3500 (según cantidad actual)
 * - Gacha: 1000 + 500/1000/500/1000... (según tiradas totales)
 * - Time Attack: 500 + 250/500/250/500... (+5 seg por mejora)
 * 
 * @author DarkphoenixTeam
 * @version 3.0 - Nuevos precios + Time Attack upgrade
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
    
    // === TEXTURAS ===
    private Texture patternTexture;
    private Texture nekoinIconTexture;
    private Texture hintIconTexture;
    private Texture timefreezeIconTexture;
    private Texture gachaIconTexture;
    private Texture upgradeIconTexture;
    private Texture cardBackTexture;
    
    private Array<Texture> allCardTextures;
    
    // === BOTONES ===
    private SimpleButton backButton;
    private SimpleButton hintBuyButton;
    private SimpleButton timefreezeBuyButton;
    private SimpleButton gachaButton;
    private SimpleButton timeAttackUpgradeButton;
    
    // === GACHA RESULT ===
    private boolean showingGachaResult = false;
    private int lastUnlockedCardId = -1;
    private float gachaResultTimer = 0f;
    private static final float GACHA_RESULT_DURATION = 2.5f;
    
    // === SISTEMAS ===
    private AudioManager audioManager;
    private SaveManager saveManager;
    
    // === LAYOUT POSITIONS ===
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
        
        allCardTextures = new Array<>();
        
        audioManager.playMusic(AssetPaths.MUSIC_BAZAAR, true);
        
        loadAssets();
        createButtons();
        
        Gdx.app.log(TAG, "=== BAZAAR ===");
        Gdx.app.log(TAG, "Nekoins: " + saveManager.getNekoins());
        Gdx.app.log(TAG, "Hints: " + saveManager.getHintUses() + " | TimeFreeze: " + saveManager.getTimeFreezeUses());
        Gdx.app.log(TAG, "Time Attack: " + saveManager.getTimeAttackTime() + "s (" + saveManager.getTimeAttackUpgrades() + " upgrades)");
        Gdx.app.log(TAG, "Gacha pulls: " + saveManager.getGachaPulls());
    }
    
    // ==================== ASSETS ====================
    
    private void loadAssets() {
        try {
            patternTexture = new Texture(Gdx.files.internal(AssetPaths.PATTERN_BAZAAR));
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error pattern");
        }
        
        try {
            nekoinIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_NEKOIN));
            hintIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_HINT_HERO));
            timefreezeIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_TIMEFREEZE_HERO));
            gachaIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_GACHA));
            upgradeIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_UPGRADE));
            cardBackTexture = new Texture(Gdx.files.internal(AssetPaths.CARD_BACK));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error iconos: " + e.getMessage());
        }
        
        // Cargar todas las cartas para el gacha
        for (int deck = 0; deck < AssetPaths.TOTAL_DECKS; deck++) {
            for (int card = 0; card < AssetPaths.CARDS_PER_DECK; card++) {
                String path = AssetPaths.getCardPath(deck, card);
                try {
                    allCardTextures.add(new Texture(Gdx.files.internal(path)));
                } catch (Exception e) {
                    allCardTextures.add(null);
                }
            }
        }
    }
    
    // ==================== BOTONES ====================
    
    private void createButtons() {
        float centerX = Constants.VIRTUAL_WIDTH / 2f;
        float buttonWidth = Constants.VIRTUAL_WIDTH * BUTTON_WIDTH_PERCENT;
        
        Texture btnTexture = null;
        try {
            btnTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_EMPTY));
        } catch (Exception e) {
            try {
                btnTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_PLAY));
            } catch (Exception e2) {
                Gdx.app.error(TAG, "Error cargando textura de botón");
            }
        }
        
        if (btnTexture != null) {
            // Sección 1: Hint
            float hintY = Constants.VIRTUAL_HEIGHT - SECTION_START_Y - BUTTON_HEIGHT;
            hintBuyButton = new SimpleButton(btnTexture, "COMPRAR",
                centerX - buttonWidth / 2f, hintY, buttonWidth, BUTTON_HEIGHT);
            hintBuyButton.setOnClick(this::buyHint);
            
            // Sección 2: TimeFreeze
            float freezeY = hintY - SECTION_HEIGHT;
            timefreezeBuyButton = new SimpleButton(btnTexture, "COMPRAR",
                centerX - buttonWidth / 2f, freezeY, buttonWidth, BUTTON_HEIGHT);
            timefreezeBuyButton.setOnClick(this::buyTimeFreeze);
            
            // Sección 3: Time Attack Upgrade
            float upgradeY = freezeY - SECTION_HEIGHT;
            timeAttackUpgradeButton = new SimpleButton(btnTexture, "MEJORAR",
                centerX - buttonWidth / 2f, upgradeY, buttonWidth, BUTTON_HEIGHT);
            timeAttackUpgradeButton.setOnClick(this::buyTimeAttackUpgrade);
            
            // Sección 4: Gacha
            float gachaY = upgradeY - SECTION_HEIGHT;
            gachaButton = new SimpleButton(btnTexture, "ABRIR COFRE",
                centerX - buttonWidth / 2f, gachaY, buttonWidth, BUTTON_HEIGHT);
            gachaButton.setOnClick(this::openGacha);
        }
        
        // Botón Volver
        try {
            Texture backTex = new Texture(Gdx.files.internal(AssetPaths.BTN_BACK));
            float backWidth = Constants.VIRTUAL_WIDTH * 0.4f;
            float backHeight = backWidth * 0.35f;
            backButton = new SimpleButton(backTex, "VOLVER",
                (Constants.VIRTUAL_WIDTH - backWidth) / 2f, 15f, backWidth, backHeight);
            backButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new HomeScreen(game));
            });
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error botón back");
        }
    }
    
    // ==================== COMPRAS ====================
    
    private void buyHint() {
        if (tapTimer > 0) return;
        tapTimer = TAP_COOLDOWN;
        
        int currentHints = saveManager.getHintUses();
        
        if (currentHints >= Constants.MAX_POWER_STOCK) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Hint: Stock máximo alcanzado (" + Constants.MAX_POWER_STOCK + ")");
            return;
        }
        
        int cost = saveManager.getHintPrice();
        
        if (saveManager.spendNekoins(cost)) {
            saveManager.addHintUses(1);
            audioManager.playSound(AssetPaths.SFX_COIN);
            Gdx.app.log(TAG, "Hint comprado por " + cost + "! Total: " + saveManager.getHintUses() + 
                        " | Siguiente: " + saveManager.getHintPrice());
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Nekoins insuficientes para Hint (necesita " + cost + ")");
        }
    }
    
    private void buyTimeFreeze() {
        if (tapTimer > 0) return;
        tapTimer = TAP_COOLDOWN;
        
        int currentFreeze = saveManager.getTimeFreezeUses();
        
        if (currentFreeze >= Constants.MAX_POWER_STOCK) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "TimeFreeze: Stock máximo alcanzado (" + Constants.MAX_POWER_STOCK + ")");
            return;
        }
        
        int cost = saveManager.getTimeFreezePrice();
        
        if (saveManager.spendNekoins(cost)) {
            saveManager.addTimeFreezeUses(1);
            audioManager.playSound(AssetPaths.SFX_COIN);
            Gdx.app.log(TAG, "TimeFreeze comprado por " + cost + "! Total: " + saveManager.getTimeFreezeUses() +
                        " | Siguiente: " + saveManager.getTimeFreezePrice());
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Nekoins insuficientes para TimeFreeze (necesita " + cost + ")");
        }
    }
    
    private void buyTimeAttackUpgrade() {
        if (tapTimer > 0) return;
        tapTimer = TAP_COOLDOWN;
        
        int cost = saveManager.getTimeAttackUpgradeCost();
        
        if (cost < 0) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Time Attack: Ya está al máximo (" + Constants.TIME_ATTACK_MAX_TIME + "s)");
            return;
        }
        
        if (saveManager.purchaseTimeAttackUpgrade()) {
            audioManager.playSound(AssetPaths.SFX_VICTORY);
            Gdx.app.log(TAG, "Time Attack mejorado! Nuevo tiempo: " + saveManager.getTimeAttackTime() + "s" +
                        " | Siguiente costo: " + saveManager.getTimeAttackUpgradeCost());
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Nekoins insuficientes para upgrade (necesita " + cost + ")");
        }
    }
    
    private void openGacha() {
        if (tapTimer > 0) return;
        tapTimer = TAP_COOLDOWN;
        
        Array<Integer> locked = getLockedCards();
        if (locked.size == 0) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Gacha: Todas las cartas desbloqueadas");
            return;
        }
        
        int cost = saveManager.getGachaCost();
        
        if (saveManager.spendNekoins(cost)) {
            // Incrementar contador de tiradas ANTES de desbloquear
            saveManager.incrementGachaPulls();
            
            // Seleccionar carta aleatoria
            int randomIndex = MathUtils.random(0, locked.size - 1);
            int cardId = locked.get(randomIndex);
            
            saveManager.unlockCard(cardId);
            
            audioManager.playSound(AssetPaths.SFX_VICTORY);
            lastUnlockedCardId = cardId;
            showingGachaResult = true;
            gachaResultTimer = GACHA_RESULT_DURATION;
            
            Gdx.app.log(TAG, "Gacha: desbloqueada carta " + cardId + " por " + cost +
                        " | Siguiente costo: " + saveManager.getGachaCost());
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Nekoins insuficientes para Gacha (necesita " + cost + ")");
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
        if (tapTimer > 0) {
            tapTimer -= delta;
        }
        
        if (showingGachaResult) {
            gachaResultTimer -= delta;
            if (gachaResultTimer <= 0 || (Gdx.input.justTouched() && tapTimer <= 0)) {
                showingGachaResult = false;
                lastUnlockedCardId = -1;
                tapTimer = TAP_COOLDOWN;
            }
            return;
        }
        
        if (!isInputEnabled()) return;
        
        // Actualizar botones solo si están disponibles
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
            Constants.VIRTUAL_HEIGHT - 25f);
        
        // Nekoins
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
        
        // Icono
        if (hintIconTexture != null) {
            game.getBatch().draw(hintIconTexture, 20f, sectionY - iconSize - 10f, iconSize, iconSize);
        }
        
        // Título
        buttonFont.setColor(Color.WHITE);
        buttonFont.draw(game.getBatch(), "PISTA", textX, sectionY);
        
        int uses = saveManager.getHintUses();
        int price = saveManager.getHintPrice();
        
        // Stock
        String usesText = "Stock: " + uses + "/" + Constants.MAX_POWER_STOCK;
        smallFont.setColor(uses >= Constants.MAX_POWER_STOCK ? Color.GREEN : Color.WHITE);
        smallFont.draw(game.getBatch(), usesText, textX, sectionY - 22f);
        
        // Precio o MAX
        if (uses >= Constants.MAX_POWER_STOCK) {
            smallFont.setColor(Color.GREEN);
            smallFont.draw(game.getBatch(), "STOCK LLENO", textX, sectionY - 42f);
        } else {
            boolean canAfford = saveManager.getNekoins() >= price;
            smallFont.setColor(canAfford ? Color.GOLD : Color.RED);
            smallFont.draw(game.getBatch(), "Precio: " + price, textX, sectionY - 42f);
            
            // Botón
            if (hintBuyButton != null) {
                hintBuyButton.draw(game.getBatch(), buttonFont);
            }
        }
        
        // Descripción
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(game.getBatch(), "Revela 2 pares + 1 carta", textX, sectionY - 62f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawTimeFreezeSection() {
        float sectionY = Constants.VIRTUAL_HEIGHT - SECTION_START_Y - SECTION_HEIGHT;
        float iconSize = 50f;
        float textX = 80f;
        
        // Icono
        if (timefreezeIconTexture != null) {
            game.getBatch().draw(timefreezeIconTexture, 20f, sectionY - iconSize - 10f, iconSize, iconSize);
        }
        
        // Título
        buttonFont.setColor(Color.WHITE);
        buttonFont.draw(game.getBatch(), "CONGELAR TIEMPO", textX, sectionY);
        
        int uses = saveManager.getTimeFreezeUses();
        int price = saveManager.getTimeFreezePrice();
        
        // Stock
        String usesText = "Stock: " + uses + "/" + Constants.MAX_POWER_STOCK;
        smallFont.setColor(uses >= Constants.MAX_POWER_STOCK ? Color.CYAN : Color.WHITE);
        smallFont.draw(game.getBatch(), usesText, textX, sectionY - 22f);
        
        // Precio o MAX
        if (uses >= Constants.MAX_POWER_STOCK) {
            smallFont.setColor(Color.CYAN);
            smallFont.draw(game.getBatch(), "STOCK LLENO", textX, sectionY - 42f);
        } else {
            boolean canAfford = saveManager.getNekoins() >= price;
            smallFont.setColor(canAfford ? Color.GOLD : Color.RED);
            smallFont.draw(game.getBatch(), "Precio: " + price, textX, sectionY - 42f);
            
            // Botón
            if (timefreezeBuyButton != null) {
                timefreezeBuyButton.draw(game.getBatch(), buttonFont);
            }
        }
        
        // Descripción
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(game.getBatch(), "Pausa el timer 5 segundos", textX, sectionY - 62f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawTimeAttackUpgradeSection() {
        float sectionY = Constants.VIRTUAL_HEIGHT - SECTION_START_Y - SECTION_HEIGHT * 2;
        float iconSize = 50f;
        float textX = 80f;
        
        // Icono
        if (upgradeIconTexture != null) {
            game.getBatch().draw(upgradeIconTexture, 20f, sectionY - iconSize - 10f, iconSize, iconSize);
        }
        
        // Título
        buttonFont.setColor(Color.ORANGE);
        buttonFont.draw(game.getBatch(), "TIME ATTACK", textX, sectionY);
        buttonFont.setColor(Color.WHITE);
        
        float currentTime = saveManager.getTimeAttackTime();
        int upgrades = saveManager.getTimeAttackUpgrades();
        int cost = saveManager.getTimeAttackUpgradeCost();
        
        // Tiempo actual
        String timeText = "Tiempo: " + formatTime(currentTime);
        smallFont.setColor(Color.WHITE);
        smallFont.draw(game.getBatch(), timeText, textX, sectionY - 22f);
        
        // Precio o MAX
        if (cost < 0 || currentTime >= Constants.TIME_ATTACK_MAX_TIME) {
            smallFont.setColor(Color.LIME);
            smallFont.draw(game.getBatch(), "TIEMPO MÁXIMO", textX, sectionY - 42f);
        } else {
            boolean canAfford = saveManager.getNekoins() >= cost;
            smallFont.setColor(canAfford ? Color.GOLD : Color.RED);
            smallFont.draw(game.getBatch(), "Mejorar +5s: " + cost, textX, sectionY - 42f);
            
            // Botón
            if (timeAttackUpgradeButton != null) {
                timeAttackUpgradeButton.draw(game.getBatch(), buttonFont);
            }
        }
        
        // Descripción
        smallFont.setColor(Color.LIGHT_GRAY);
        String maxText = "Máximo: " + formatTime(Constants.TIME_ATTACK_MAX_TIME);
        smallFont.draw(game.getBatch(), maxText, textX, sectionY - 62f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawGachaSection() {
        float sectionY = Constants.VIRTUAL_HEIGHT - SECTION_START_Y - SECTION_HEIGHT * 3;
        float iconSize = 50f;
        float textX = 80f;
        
        // Icono
        if (gachaIconTexture != null) {
            game.getBatch().draw(gachaIconTexture, 20f, sectionY - iconSize - 10f, iconSize, iconSize);
        }
        
        // Título
        buttonFont.setColor(Color.MAGENTA);
        buttonFont.draw(game.getBatch(), "COFRE GACHAPON", textX, sectionY);
        buttonFont.setColor(Color.WHITE);
        
        int unlocked = saveManager.getUnlockedCardCount();
        int cost = saveManager.getGachaCost();
        
        // Progreso
        String progressText = "Cartas: " + unlocked + "/" + Constants.TOTAL_CARDS;
        smallFont.setColor(unlocked >= Constants.TOTAL_CARDS ? Color.LIME : Color.WHITE);
        smallFont.draw(game.getBatch(), progressText, textX, sectionY - 22f);
        
        if (unlocked >= Constants.TOTAL_CARDS) {
            smallFont.setColor(Color.LIME);
            smallFont.draw(game.getBatch(), "¡COLECCIÓN COMPLETA!", textX, sectionY - 42f);
        } else {
            // Precio
            boolean canAfford = saveManager.getNekoins() >= cost;
            smallFont.setColor(canAfford ? Color.GOLD : Color.RED);
            smallFont.draw(game.getBatch(), "Precio: " + cost, textX, sectionY - 42f);
            
            // Botón
            if (gachaButton != null) {
                gachaButton.draw(game.getBatch(), buttonFont);
            }
        }
        
        // Info de tiradas
        smallFont.setColor(Color.LIGHT_GRAY);
        String pullsText = "Tiradas: " + saveManager.getGachaPulls();
        smallFont.draw(game.getBatch(), pullsText, textX, sectionY - 62f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawGachaResult() {
        game.getBatch().begin();
        
        // Overlay
        game.getBatch().setColor(0, 0, 0, 0.9f);
        if (cardBackTexture != null) {
            game.getBatch().draw(cardBackTexture, 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        }
        game.getBatch().setColor(1, 1, 1, 1);
        
        // Título
        titleFont.setColor(Color.GOLD);
        String title = "¡NUEVA CARTA!";
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                      Constants.VIRTUAL_HEIGHT * 0.82f);
        titleFont.setColor(Color.WHITE);
        
        // Carta
        if (lastUnlockedCardId >= 0 && lastUnlockedCardId < allCardTextures.size) {
            Texture tex = allCardTextures.get(lastUnlockedCardId);
            if (tex != null) {
                float cardWidth = 150f;
                float cardHeight = cardWidth * 1.4f;
                float cardX = (Constants.VIRTUAL_WIDTH - cardWidth) / 2f;
                float cardY = (Constants.VIRTUAL_HEIGHT - cardHeight) / 2f + 30f;
                game.getBatch().draw(tex, cardX, cardY, cardWidth, cardHeight);
            }
        }
        
        // Info de la carta
        int deck = SaveManager.getDeckFromCardId(lastUnlockedCardId);
        String[] deckNames = {"Base", "Estrella ★", "2 Estrellas ★★", "3 Estrellas ★★★", "Corazón ♥"};
        int[] nekoinValues = {1, 2, 3, 5, 7};
        
        String deckName = (deck >= 0 && deck < deckNames.length) ? deckNames[deck] : "???";
        int nekoinVal = (deck >= 0 && deck < nekoinValues.length) ? nekoinValues[deck] : 1;
        
        // Nombre del deck
        buttonFont.setColor(getDeckColor(deck));
        String deckText = "Deck " + deckName;
        layout.setText(buttonFont, deckText);
        buttonFont.draw(game.getBatch(), deckText, 
                       (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                       Constants.VIRTUAL_HEIGHT * 0.28f);
        
        // Valor de nekoin
        buttonFont.setColor(Color.GOLD);
        String valueText = "+" + nekoinVal + " Nekoin por par";
        layout.setText(buttonFont, valueText);
        buttonFont.draw(game.getBatch(), valueText, 
                       (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                       Constants.VIRTUAL_HEIGHT * 0.22f);
        buttonFont.setColor(Color.WHITE);
        
        // Instrucción
        smallFont.setColor(Color.GRAY);
        String tapText = "Toca para continuar";
        layout.setText(smallFont, tapText);
        smallFont.draw(game.getBatch(), tapText, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                      Constants.VIRTUAL_HEIGHT * 0.10f);
        smallFont.setColor(Color.WHITE);
        
        game.getBatch().end();
    }
    
    private Color getDeckColor(int deckIndex) {
        switch (deckIndex) {
            case 0: return Color.LIGHT_GRAY;    // Base
            case 1: return Color.WHITE;          // Estrella
            case 2: return Color.YELLOW;         // 2 Estrellas
            case 3: return Color.GOLD;           // 3 Estrellas
            case 4: return Color.CORAL;          // Corazón
            default: return Color.WHITE;
        }
    }
    
    private String formatTime(float seconds) {
        int mins = (int)(seconds / 60);
        int secs = (int)(seconds % 60);
        return String.format("%d:%02d", mins, secs);
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
        if (timefreezeBuyButton != null) timefreezeBuyButton.dispose();
        if (timeAttackUpgradeButton != null) timeAttackUpgradeButton.dispose();
        if (gachaButton != null) gachaButton.dispose();
    }
    }
