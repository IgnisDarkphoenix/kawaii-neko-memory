package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

public class DeckEditorScreen extends BaseScreen {
    
    private static final String TAG = "DeckEditorScreen";
    
    private static final int ACTIVE_COLS = 5;
    private static final int ACTIVE_ROWS = 3;
    private static final float ACTIVE_CARD_SIZE = 70f;
    private static final float ACTIVE_SPACING = 8f;
    
    private static final int AVAILABLE_COLS = 7;
    private static final int AVAILABLE_ROWS = 5;
    private static final float AVAILABLE_CARD_SIZE = 55f;
    private static final float AVAILABLE_SPACING = 6f;
    
    private static final Color COLOR_EASY = new Color(0.3f, 0.85f, 0.3f, 1f);
    private static final Color COLOR_NORMAL = new Color(1f, 0.85f, 0.2f, 1f);
    private static final Color COLOR_ADVANCED = new Color(1f, 0.5f, 0.1f, 1f);
    private static final Color COLOR_HARD = new Color(0.9f, 0.2f, 0.2f, 1f);
    private static final Color COLOR_SELECTED = new Color(0.3f, 0.7f, 1f, 1f);
    
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    private Texture patternTexture;
    private Texture cardBackTexture;
    private Texture nekoinIconTexture;
    private Array<Texture> allCardTextures;
    
    private SimpleButton backButton;
    
    private Array<Rectangle> activeSlotBounds;
    private Array<Rectangle> availableCardBounds;
    
    private int selectedSlot = -1;
    
    private AudioManager audioManager;
    private SaveManager saveManager;
    private ShapeRenderer shapeRenderer;
    private final Vector2 touchPoint = new Vector2();
    
    private float activeGridX, activeGridY;
    private float availableGridX, availableGridY;
    
    public DeckEditorScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(1f, 0.92f, 0.95f);
        
        titleFont = game.getFontManager().getTitleFont();
        buttonFont = game.getFontManager().getButtonFont();
        smallFont = game.getFontManager().getSmallFont();
        layout = new GlyphLayout();
        
        audioManager = AudioManager.getInstance();
        saveManager = SaveManager.getInstance();
        shapeRenderer = new ShapeRenderer();
        
        allCardTextures = new Array<>();
        activeSlotBounds = new Array<>();
        availableCardBounds = new Array<>();
        
        audioManager.playMusic(AssetPaths.MUSIC_MENU, true);
        
        loadAssets();
        calculatePositions();
        createBounds();
        createButtons();
        
        Gdx.app.log(TAG, "Inicializado");
    }
    
    private void loadAssets() {
        try {
            patternTexture = new Texture(Gdx.files.internal(AssetPaths.PATTERN_HOME));
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando pattern");
        }
        
        try {
            cardBackTexture = new Texture(Gdx.files.internal(AssetPaths.CARD_BACK));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando card back");
        }
        
        try {
            nekoinIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_NEKOIN));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando nekoin icon");
        }
        
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
    
    private void calculatePositions() {
        float activeWidth = ACTIVE_COLS * ACTIVE_CARD_SIZE + (ACTIVE_COLS - 1) * ACTIVE_SPACING;
        activeGridX = (Constants.VIRTUAL_WIDTH - activeWidth) / 2f;
        activeGridY = Constants.VIRTUAL_HEIGHT - 100f;
        
        float availableWidth = AVAILABLE_COLS * AVAILABLE_CARD_SIZE + (AVAILABLE_COLS - 1) * AVAILABLE_SPACING;
        availableGridX = (Constants.VIRTUAL_WIDTH - availableWidth) / 2f;
        availableGridY = Constants.VIRTUAL_HEIGHT - 420f;
    }
    
    private void createBounds() {
        for (int row = 0; row < ACTIVE_ROWS; row++) {
            for (int col = 0; col < ACTIVE_COLS; col++) {
                float x = activeGridX + col * (ACTIVE_CARD_SIZE + ACTIVE_SPACING);
                float y = activeGridY - (row + 1) * (ACTIVE_CARD_SIZE + ACTIVE_SPACING);
                activeSlotBounds.add(new Rectangle(x, y, ACTIVE_CARD_SIZE, ACTIVE_CARD_SIZE));
            }
        }
        
        for (int row = 0; row < AVAILABLE_ROWS; row++) {
            for (int col = 0; col < AVAILABLE_COLS; col++) {
                float x = availableGridX + col * (AVAILABLE_CARD_SIZE + AVAILABLE_SPACING);
                float y = availableGridY - (row + 1) * (AVAILABLE_CARD_SIZE + AVAILABLE_SPACING);
                availableCardBounds.add(new Rectangle(x, y, AVAILABLE_CARD_SIZE, AVAILABLE_CARD_SIZE));
            }
        }
    }
    
    private void createButtons() {
        try {
            Texture backTex = new Texture(Gdx.files.internal(AssetPaths.BTN_BACK));
            float btnWidth = Constants.VIRTUAL_WIDTH * 0.4f;
            float btnHeight = btnWidth * 0.35f;
            float btnX = (Constants.VIRTUAL_WIDTH - btnWidth) / 2f;
            float btnY = 15f;
            
            backButton = new SimpleButton(backTex, "VOLVER", btnX, btnY, btnWidth, btnHeight);
            backButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new HomeScreen(game));
            });
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando boton");
        }
    }
    
    @Override
    protected void update(float delta) {
        if (!isInputEnabled()) return;
        
        if (backButton != null) backButton.update(viewport);
        
        if (Gdx.input.justTouched()) {
            viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY()));
            handleTouch(touchPoint.x, touchPoint.y);
        }
    }
    
    private void handleTouch(float x, float y) {
        for (int i = 0; i < activeSlotBounds.size; i++) {
            if (activeSlotBounds.get(i).contains(x, y)) {
                onActiveSlotClicked(i);
                return;
            }
        }
        
        for (int i = 0; i < availableCardBounds.size; i++) {
            if (availableCardBounds.get(i).contains(x, y)) {
                onAvailableCardClicked(i);
                return;
            }
        }
        
        if (selectedSlot >= 0) {
            selectedSlot = -1;
            audioManager.playSound(AssetPaths.SFX_BUTTON);
        }
    }
    
    private void onActiveSlotClicked(int slotIndex) {
        Array<Integer> activeCards = saveManager.getActiveCards();
        int cardInSlot = (slotIndex < activeCards.size) ? activeCards.get(slotIndex) : -1;
        
        if (selectedSlot == slotIndex) {
            selectedSlot = -1;
            audioManager.playSound(AssetPaths.SFX_BUTTON);
        } else if (selectedSlot >= 0) {
            saveManager.swapActiveCards(selectedSlot, slotIndex);
            selectedSlot = -1;
            audioManager.playSound(AssetPaths.SFX_MATCH);
        } else if (cardInSlot >= 0) {
            selectedSlot = slotIndex;
            audioManager.playSound(AssetPaths.SFX_BUTTON);
        }
    }
    
    private void onAvailableCardClicked(int cardId) {
        if (!saveManager.isCardUnlocked(cardId)) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            return;
        }
        
        if (saveManager.isCardActive(cardId)) {
            saveManager.removeActiveCard(cardId);
            audioManager.playSound(AssetPaths.SFX_BUTTON);
            return;
        }
        
        if (selectedSlot >= 0) {
            if (saveManager.setActiveCardSlot(selectedSlot, cardId)) {
                audioManager.playSound(AssetPaths.SFX_MATCH);
                selectedSlot = -1;
            } else {
                audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            }
        } else {
            int slot = saveManager.addActiveCard(cardId);
            if (slot >= 0) {
                audioManager.playSound(AssetPaths.SFX_MATCH);
            } else {
                audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            }
        }
    }
    
    @Override
    protected void draw() {
        game.getBatch().begin();
        drawBackground();
        drawHeader();
        game.getBatch().end();
        
        drawActiveGrid();
        drawAvailableGrid();
        
        game.getBatch().begin();
        drawInfo();
        if (backButton != null) backButton.draw(game.getBatch(), buttonFont);
        game.getBatch().end();
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
        String title = "CARTAS ACTIVAS";
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                      Constants.VIRTUAL_HEIGHT - 25f);
        
        if (nekoinIconTexture != null) {
            float iconSize = 28f;
            float iconX = Constants.VIRTUAL_WIDTH - 100f;
            float iconY = Constants.VIRTUAL_HEIGHT - 45f;
            game.getBatch().draw(nekoinIconTexture, iconX, iconY, iconSize, iconSize);
            
            String nekoins = String.valueOf(saveManager.getNekoins());
            buttonFont.draw(game.getBatch(), nekoins, iconX + iconSize + 5f, iconY + iconSize - 3f);
        }
    }
    
    private void drawActiveGrid() {
        Array<Integer> activeCards = saveManager.getActiveCards();
        
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        for (int i = 0; i < 15; i++) {
            Rectangle bounds = activeSlotBounds.get(i);
            int cardId = (i < activeCards.size) ? activeCards.get(i) : -1;
            
            Color borderColor = getSlotDifficultyColor(i);
            if (i == selectedSlot) {
                borderColor = COLOR_SELECTED;
            }
            
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(borderColor);
            shapeRenderer.rect(bounds.x - 4f, bounds.y - 4f, 
                              bounds.width + 8f, bounds.height + 8f);
            shapeRenderer.end();
            
            game.getBatch().begin();
            
            if (cardId >= 0 && cardId < allCardTextures.size) {
                Texture tex = allCardTextures.get(cardId);
                if (tex != null) {
                    game.getBatch().draw(tex, bounds.x, bounds.y, bounds.width, bounds.height);
                }
            } else {
                game.getBatch().setColor(0.3f, 0.3f, 0.3f, 0.5f);
                if (cardBackTexture != null) {
                    game.getBatch().draw(cardBackTexture, bounds.x, bounds.y, bounds.width, bounds.height);
                }
                game.getBatch().setColor(1f, 1f, 1f, 1f);
            }
            
            game.getBatch().end();
        }
    }
    
    private Color getSlotDifficultyColor(int slotIndex) {
        if (slotIndex < 6) return COLOR_EASY;
        if (slotIndex < 8) return COLOR_NORMAL;
        if (slotIndex < 10) return COLOR_ADVANCED;
        return COLOR_HARD;
    }
    
    private void drawAvailableGrid() {
        game.getBatch().begin();
        String sectionTitle = "Cartas Disponibles";
        layout.setText(smallFont, sectionTitle);
        smallFont.draw(game.getBatch(), sectionTitle, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                      availableGridY + 25f);
        game.getBatch().end();
        
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        for (int i = 0; i < 35; i++) {
            Rectangle bounds = availableCardBounds.get(i);
            boolean unlocked = saveManager.isCardUnlocked(i);
            boolean active = saveManager.isCardActive(i);
            
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            if (active) {
                shapeRenderer.setColor(COLOR_SELECTED);
            } else if (!unlocked) {
                shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f);
            } else {
                int deck = SaveManager.getDeckFromCardId(i);
                shapeRenderer.setColor(getDeckColor(deck));
            }
            shapeRenderer.rect(bounds.x - 3f, bounds.y - 3f, 
                              bounds.width + 6f, bounds.height + 6f);
            shapeRenderer.end();
            
            game.getBatch().begin();
            
            if (i < allCardTextures.size) {
                Texture tex = allCardTextures.get(i);
                
                if (!unlocked) {
                    game.getBatch().setColor(0.2f, 0.2f, 0.2f, 1f);
                } else if (active) {
                    game.getBatch().setColor(1f, 1f, 1f, 0.5f);
                }
                
                if (tex != null) {
                    game.getBatch().draw(tex, bounds.x, bounds.y, bounds.width, bounds.height);
                } else if (cardBackTexture != null) {
                    game.getBatch().draw(cardBackTexture, bounds.x, bounds.y, bounds.width, bounds.height);
                }
                
                game.getBatch().setColor(1f, 1f, 1f, 1f);
            }
            
            game.getBatch().end();
        }
    }
    
    private Color getDeckColor(int deckIndex) {
        switch (deckIndex) {
            case 0: return new Color(0.6f, 0.6f, 0.6f, 1f);
            case 1: return new Color(0.9f, 0.9f, 0.9f, 1f);
            case 2: return new Color(0.2f, 0.2f, 0.2f, 1f);
            case 3: return new Color(0.9f, 0.75f, 0.5f, 1f);
            case 4: return new Color(1f, 0.6f, 0.3f, 1f);
            default: return Color.WHITE;
        }
    }
    
    private void drawInfo() {
        float infoY = 85f;
        
        int activeCount = saveManager.getActiveCardCount();
        String countText = "Activas: " + activeCount + "/15";
        
        if (activeCount < 15) {
            smallFont.setColor(Color.RED);
        } else {
            smallFont.setColor(Color.GREEN);
        }
        
        layout.setText(smallFont, countText);
        smallFont.draw(game.getBatch(), countText, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f, infoY);
        smallFont.setColor(Color.WHITE);
        
        String hint = selectedSlot >= 0 ? 
                      "Toca una carta para colocar" : 
                      "Toca un slot o carta para editar";
        layout.setText(smallFont, hint);
        smallFont.setColor(Color.GRAY);
        smallFont.draw(game.getBatch(), hint, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f, infoY - 20f);
        smallFont.setColor(Color.WHITE);
    }
    
    @Override
    public void dispose() {
        if (patternTexture != null) patternTexture.dispose();
        if (cardBackTexture != null) cardBackTexture.dispose();
        if (nekoinIconTexture != null) nekoinIconTexture.dispose();
        
        for (Texture tex : allCardTextures) {
            if (tex != null) tex.dispose();
        }
        allCardTextures.clear();
        
        if (backButton != null) backButton.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}