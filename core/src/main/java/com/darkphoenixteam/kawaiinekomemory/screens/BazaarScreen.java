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
 * Pantalla del Bazaar - Compra de Powers y Gachapon
 * Precios dinámicos basados en cantidad de powers acumulados
 * 
 * @author DarkphoenixTeam
 * @version 2.1 - Precios dinámicos + tap delay
 */
public class BazaarScreen extends BaseScreen {
    
    private static final String TAG = "BazaarScreen";
    
    // === TAP DELAY ===
    private static final float TAP_COOLDOWN = 0.3f;
    private float tapTimer = 0f;
    
    // === PRECIOS DINÁMICOS POR CANTIDAD ===
    // Índice = cantidad actual de powers del jugador
    private static final int[] POWER_PRICES = {50, 100, 200, 350, 500};
    private static final int MAX_POWER_STOCK = 5;
    
    // === GACHA ===
    private static final int GACHA_COST = 50;
    
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    private Texture patternTexture;
    private Texture nekoinIconTexture;
    private Texture hintIconTexture;
    private Texture timefreezeIconTexture;
    private Texture gachaIconTexture;
    private Texture cardBackTexture;
    
    private Array<Texture> allCardTextures;
    
    private SimpleButton backButton;
    private SimpleButton hintBuyButton;
    private SimpleButton timefreezeBuyButton;
    private SimpleButton gachaButton;
    
    private boolean showingGachaResult = false;
    private int lastUnlockedCardId = -1;
    private float gachaResultTimer = 0f;
    private static final float GACHA_RESULT_DURATION = 2.5f;
    
    private AudioManager audioManager;
    private SaveManager saveManager;
    
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
        
        Gdx.app.log(TAG, "Nekoins: " + saveManager.getNekoins());
        Gdx.app.log(TAG, "Hints: " + saveManager.getHintUses() + " | TimeFreeze: " + saveManager.getTimeFreezeUses());
    }
    
    // ==================== PRECIOS DINÁMICOS ====================
    
    /**
     * Obtiene el precio de un power basado en la cantidad actual
     * 0 powers = 50, 1 = 100, 2 = 200, 3 = 350, 4 = 500
     * Si ya tiene 5+, no puede comprar más
     */
    private int getPowerPrice(int currentAmount) {
        if (currentAmount >= MAX_POWER_STOCK) return -1; // No puede comprar
        if (currentAmount < 0) currentAmount = 0;
        if (currentAmount >= POWER_PRICES.length) return POWER_PRICES[POWER_PRICES.length - 1];
        return POWER_PRICES[currentAmount];
    }
    
    private int getHintPrice() {
        return getPowerPrice(saveManager.getHintUses());
    }
    
    private int getTimeFreezePrice() {
        return getPowerPrice(saveManager.getTimeFreezeUses());
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
            cardBackTexture = new Texture(Gdx.files.internal(AssetPaths.CARD_BACK));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error iconos");
        }
        
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
        float buttonWidth = Constants.VIRTUAL_WIDTH * 0.5f;
        float buttonHeight = 50f;
        
        // Sección Hint
        float hintY = Constants.VIRTUAL_HEIGHT - 220f;
        
        try {
            Texture btnTex = new Texture(Gdx.files.internal(AssetPaths.BTN_PLAY));
            
            hintBuyButton = new SimpleButton(btnTex, "COMPRAR", 
                centerX - buttonWidth / 2f, hintY, buttonWidth, buttonHeight);
            hintBuyButton.setOnClick(this::buyHint);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error botón Hint");
        }
        
        // Sección TimeFreeze
        float freezeY = Constants.VIRTUAL_HEIGHT - 400f;
        
        try {
            Texture btnTex = new Texture(Gdx.files.internal(AssetPaths.BTN_PLAY));
            
            timefreezeBuyButton = new SimpleButton(btnTex, "COMPRAR",
                centerX - buttonWidth / 2f, freezeY, buttonWidth, buttonHeight);
            timefreezeBuyButton.setOnClick(this::buyTimeFreeze);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error botón TimeFreeze");
        }
        
        // Sección Gacha
        float gachaY = Constants.VIRTUAL_HEIGHT - 580f;
        
        try {
            Texture btnTex = new Texture(Gdx.files.internal(AssetPaths.BTN_BAZAAR));
            gachaButton = new SimpleButton(btnTex, "ABRIR x" + GACHA_COST,
                centerX - buttonWidth / 2f, gachaY, buttonWidth, buttonHeight);
            gachaButton.setOnClick(this::openGacha);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error botón Gacha");
        }
        
        // Botón Volver
        try {
            Texture backTex = new Texture(Gdx.files.internal(AssetPaths.BTN_BACK));
            float backWidth = Constants.VIRTUAL_WIDTH * 0.4f;
            float backHeight = backWidth * 0.35f;
            backButton = new SimpleButton(backTex, "VOLVER",
                (Constants.VIRTUAL_WIDTH - backWidth) / 2f, 20f, backWidth, backHeight);
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
        
        if (currentHints >= MAX_POWER_STOCK) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Hint: Stock máximo alcanzado (" + MAX_POWER_STOCK + ")");
            return;
        }
        
        int cost = getHintPrice();
        
        if (saveManager.spendNekoins(cost)) {
            saveManager.addHintUses(1);
            audioManager.playSound(AssetPaths.SFX_COIN);
            Gdx.app.log(TAG, "Hint comprado por " + cost + "! Total: " + saveManager.getHintUses() + 
                        " | Siguiente precio: " + getPowerPrice(saveManager.getHintUses()));
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Nekoins insuficientes para Hint (necesita " + cost + ")");
        }
    }
    
    private void buyTimeFreeze() {
        if (tapTimer > 0) return;
        tapTimer = TAP_COOLDOWN;
        
        int currentFreeze = saveManager.getTimeFreezeUses();
        
        if (currentFreeze >= MAX_POWER_STOCK) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "TimeFreeze: Stock máximo alcanzado (" + MAX_POWER_STOCK + ")");
            return;
        }
        
        int cost = getTimeFreezePrice();
        
        if (saveManager.spendNekoins(cost)) {
            saveManager.addTimeFreezeUses(1);
            audioManager.playSound(AssetPaths.SFX_COIN);
            Gdx.app.log(TAG, "TimeFreeze comprado por " + cost + "! Total: " + saveManager.getTimeFreezeUses() + 
                        " | Siguiente precio: " + getPowerPrice(saveManager.getTimeFreezeUses()));
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Nekoins insuficientes para TimeFreeze (necesita " + cost + ")");
        }
    }
    
    private void openGacha() {
        if (tapTimer > 0) return;
        tapTimer = TAP_COOLDOWN;
        
        Array<Integer> locked = getLockedCards();
        if (locked.size == 0) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Todas las cartas desbloqueadas");
            return;
        }
        
        if (saveManager.spendNekoins(GACHA_COST)) {
            int randomIndex = MathUtils.random(0, locked.size - 1);
            int cardId = locked.get(randomIndex);
            
            saveManager.unlockCard(cardId);
            
            audioManager.playSound(AssetPaths.SFX_VICTORY);
            lastUnlockedCardId = cardId;
            showingGachaResult = true;
            gachaResultTimer = GACHA_RESULT_DURATION;
            
            Gdx.app.log(TAG, "Gacha: desbloqueada carta " + cardId);
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
        }
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
        
        if (hintBuyButton != null) hintBuyButton.update(viewport);
        if (timefreezeBuyButton != null) timefreezeBuyButton.update(viewport);
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
        
        // Botón Hint (solo si no ha alcanzado el máximo)
        int hintUses = saveManager.getHintUses();
        if (hintBuyButton != null && hintUses < MAX_POWER_STOCK) {
            hintBuyButton.draw(game.getBatch(), buttonFont);
        }
        
        // Botón TimeFreeze (solo si no ha alcanzado el máximo)
        int freezeUses = saveManager.getTimeFreezeUses();
        if (timefreezeBuyButton != null && freezeUses < MAX_POWER_STOCK) {
            timefreezeBuyButton.draw(game.getBatch(), buttonFont);
        }
        
        // Botón Gacha (solo si hay cartas por desbloquear)
        if (gachaButton != null && getLockedCards().size > 0) {
            gachaButton.draw(game.getBatch(), buttonFont);
        }
        
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
        String title = "BAZAAR";
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title,
            (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
            Constants.VIRTUAL_HEIGHT - 30f);
        
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
        
        if (hintIconTexture != null) {
            game.getBatch().draw(hintIconTexture, 30f, sectionY - 70f, 60f, 60f);
        }
        
        buttonFont.setColor(Color.WHITE);
        buttonFont.draw(game.getBatch(), "PISTA", 100f, sectionY);
        
        int uses = saveManager.getHintUses();
        int price = getHintPrice();
        
        // Stock actual
        String usesText = "Stock: " + uses + "/" + MAX_POWER_STOCK;
        smallFont.setColor(uses >= MAX_POWER_STOCK ? Color.GREEN : Color.WHITE);
        smallFont.draw(game.getBatch(), usesText, 100f, sectionY - 25f);
        
        // Precio o MAX
        if (uses >= MAX_POWER_STOCK) {
            smallFont.setColor(Color.GREEN);
            smallFont.draw(game.getBatch(), "STOCK LLENO", 100f, sectionY - 45f);
        } else {
            // Color del precio según si puede pagar
            boolean canAfford = saveManager.getNekoins() >= price;
            smallFont.setColor(canAfford ? Color.GOLD : Color.RED);
            smallFont.draw(game.getBatch(), "Precio: " + price + " Nekoins", 100f, sectionY - 45f);
        }
        
        // Descripción
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(game.getBatch(), "Revela 2 pares + 1 carta", 100f, sectionY - 65f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawTimeFreezeSection() {
        float sectionY = Constants.VIRTUAL_HEIGHT - 310f;
        
        if (timefreezeIconTexture != null) {
            game.getBatch().draw(timefreezeIconTexture, 30f, sectionY - 70f, 60f, 60f);
        }
        
        buttonFont.setColor(Color.WHITE);
        buttonFont.draw(game.getBatch(), "CONGELAR TIEMPO", 100f, sectionY);
        
        int uses = saveManager.getTimeFreezeUses();
        int price = getTimeFreezePrice();
        
        // Stock actual
        String usesText = "Stock: " + uses + "/" + MAX_POWER_STOCK;
        smallFont.setColor(uses >= MAX_POWER_STOCK ? Color.CYAN : Color.WHITE);
        smallFont.draw(game.getBatch(), usesText, 100f, sectionY - 25f);
        
        // Precio o MAX
        if (uses >= MAX_POWER_STOCK) {
            smallFont.setColor(Color.CYAN);
            smallFont.draw(game.getBatch(), "STOCK LLENO", 100f, sectionY - 45f);
        } else {
            boolean canAfford = saveManager.getNekoins() >= price;
            smallFont.setColor(canAfford ? Color.GOLD : Color.RED);
            smallFont.draw(game.getBatch(), "Precio: " + price + " Nekoins", 100f, sectionY - 45f);
        }
        
        // Descripción
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(game.getBatch(), "Pausa el timer 5 segundos", 100f, sectionY - 65f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawGachaSection() {
        float sectionY = Constants.VIRTUAL_HEIGHT - 490f;
        float centerX = Constants.VIRTUAL_WIDTH / 2f;
        
        if (gachaIconTexture != null) {
            game.getBatch().draw(gachaIconTexture, centerX - 30f, sectionY - 10f, 60f, 60f);
        }
        
        String title = "GACHAPON";
        layout.setText(buttonFont, title);
        buttonFont.draw(game.getBatch(), title, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, sectionY + 70f);
        
        int unlocked = saveManager.getUnlockedCardCount();
        
        if (unlocked >= 35) {
            smallFont.setColor(Color.GREEN);
            String status = "¡Colección Completa!";
            layout.setText(smallFont, status);
            smallFont.draw(game.getBatch(), status, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, sectionY - 70f);
        } else {
            // Precio
            boolean canAfford = saveManager.getNekoins() >= GACHA_COST;
            smallFont.setColor(canAfford ? Color.GOLD : Color.RED);
            String priceText = "Precio: " + GACHA_COST + " Nekoins";
            layout.setText(smallFont, priceText);
            smallFont.draw(game.getBatch(), priceText, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, sectionY - 55f);
            
            // Progreso
            smallFont.setColor(Color.WHITE);
            String status = "Cartas: " + unlocked + "/35";
            layout.setText(smallFont, status);
            smallFont.draw(game.getBatch(), status, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, sectionY - 75f);
        }
        
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawGachaResult() {
        game.getBatch().begin();
        
        game.getBatch().setColor(0, 0, 0, 0.85f);
        if (cardBackTexture != null) {
            game.getBatch().draw(cardBackTexture, 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        }
        game.getBatch().setColor(1, 1, 1, 1);
        
        titleFont.setColor(Color.GOLD);
        String title = "¡NUEVA CARTA!";
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, Constants.VIRTUAL_HEIGHT * 0.78f);
        titleFont.setColor(Color.WHITE);
        
        if (lastUnlockedCardId >= 0 && lastUnlockedCardId < allCardTextures.size) {
            Texture tex = allCardTextures.get(lastUnlockedCardId);
            if (tex != null) {
                float w = 140f;
                float h = w * 1.4f;
                game.getBatch().draw(tex, (Constants.VIRTUAL_WIDTH - w) / 2f, (Constants.VIRTUAL_HEIGHT - h) / 2f, w, h);
            }
        }
        
        int deck = SaveManager.getDeckFromCardId(lastUnlockedCardId);
        String[] names = {"Base", "Estrella", "2 Estrellas", "3 Estrellas", "Corazón"};
        int[] vals = {1, 2, 3, 5, 7};
        String deckName = (deck >= 0 && deck < names.length) ? names[deck] : "???";
        int nekoinVal = (deck >= 0 && deck < vals.length) ? vals[deck] : 1;
        String info = "Deck " + deckName + " | +" + nekoinVal + " Nekoin/par";
        layout.setText(buttonFont, info);
        buttonFont.draw(game.getBatch(), info, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, Constants.VIRTUAL_HEIGHT * 0.25f);
        
        smallFont.setColor(Color.GRAY);
        String tap = "Toca para continuar";
        layout.setText(smallFont, tap);
        smallFont.draw(game.getBatch(), tap, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, Constants.VIRTUAL_HEIGHT * 0.12f);
        smallFont.setColor(Color.WHITE);
        
        game.getBatch().end();
    }
    
    // ==================== DISPOSE ====================
    
    @Override
    public void dispose() {
        if (patternTexture != null) patternTexture.dispose();
        if (nekoinIconTexture != null) nekoinIconTexture.dispose();
        if (hintIconTexture != null) hintIconTexture.dispose();
        if (timefreezeIconTexture != null) timefreezeIconTexture.dispose();
        if (gachaIconTexture != null) gachaIconTexture.dispose();
        if (cardBackTexture != null) cardBackTexture.dispose();
        
        for (Texture tex : allCardTextures) {
            if (tex != null) tex.dispose();
        }
        
        if (backButton != null) backButton.dispose();
        if (hintBuyButton != null) hintBuyButton.dispose();
        if (timefreezeBuyButton != null) timefreezeBuyButton.dispose();
        if (gachaButton != null) gachaButton.dispose();
    }
}
