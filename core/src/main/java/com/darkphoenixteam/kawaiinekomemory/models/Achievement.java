package com.darkphoenixteam.kawaiinekomemory.models;

/**
 * Enum con todos los logros del juego
 * 
 * @author DarkphoenixTeam
 */
public enum Achievement {
    
    // === PRIMEROS PASOS ===
    FIRST_WIN("Primer Maullido", "Completa tu primer nivel", 10),
    FIRST_3_STAR("Estrella Naciente", "Consigue 3 estrellas por primera vez", 25),
    FIRST_SHOP("Cliente Favorito", "Realiza tu primera compra en el Bazaar", 15),
    FIRST_POWER("¡Poder Gatuno!", "Utiliza un Power-up por primera vez", 10),
    FIRST_FAIL("No te rindas", "Pierde una partida por primera vez", 5),
    
    // === COMPLETAR DIFICULTADES ===
    EASY_COMPLETION("Paseo por el Parque", "Completa todos los niveles en Fácil", 50),
    NORMAL_COMPLETION("Memoria de Gato", "Completa todos los niveles en Normal", 100),
    ADVANCED_COMPLETION("Mente Brillante", "Completa todos los niveles en Avanzado", 150),
    HARD_COMPLETION("Cerebro Galáctico", "Completa todos los niveles en Difícil", 200),
    
    // === PERFECCIÓN (3 estrellas) ===
    ALL_STARS_EASY("Perfección: Fácil", "3 estrellas en todos los niveles Fácil", 100),
    ALL_STARS_NORMAL("Perfección: Normal", "3 estrellas en todos los niveles Normal", 200),
    ALL_STARS_ADVANCED("Perfección: Avanzado", "3 estrellas en todos los niveles Avanzado", 300),
    ALL_STARS_HARD("Leyenda Neko", "3 estrellas en todos los niveles Difícil", 500),
    
    // === HABILIDAD ===
    SPEED_DEMON("Gato Veloz", "Termina un nivel en menos de 15 segundos", 50),
    NO_MISTAKES("Vista de Lince", "Completa un nivel sin errores", 30),
    COMBO_MASTER("Racha Imparable", "Encuentra 5 pares seguidos sin fallar", 40),
    CLOSE_CALL("Por los Bigotes", "Gana con menos de 3 segundos restantes", 35),
    
    // === COLECCIÓN ===
    GALLERY_UNLOCK("Álbum Completo", "Desbloquea todas las cartas", 150),
    
    // === ECONOMÍA ===
    RICH_NEKO("Hucha Llena", "Acumula 1,000 Nekoins en total", 50),
    BIG_SPENDER("Gato con Botas", "Gasta 2,000 Nekoins en total", 75),
    
    // === DEDICACIÓN ===
    POWER_OVERLOAD("¿Era necesario?", "Usa 3 poderes en un mismo nivel", 25),
    PERSISTENT("Mil Pares", "Encuentra 1,000 pares en total", 100),
    
    // === SECRETO ===
    CLICKER_CAT("Curioso", "Toca el logo 10 veces en el menú", 10);
    
    public final String name;
    public final String description;
    public final int reward;  // Nekoins de recompensa
    
    Achievement(String name, String description, int reward) {
        this.name = name;
        this.description = description;
        this.reward = reward;
    }
    
    /**
     * Obtiene el índice del logro para guardado
     */
    public int getIndex() {
        return ordinal();
    }
    
    /**
     * Obtiene logro por índice
     */
    public static Achievement fromIndex(int index) {
        Achievement[] values = values();
        if (index >= 0 && index < values.length) {
            return values[index];
        }
        return null;
    }
    
    /**
     * Total de logros en el juego
     */
    public static int count() {
        return values().length;
    }
}