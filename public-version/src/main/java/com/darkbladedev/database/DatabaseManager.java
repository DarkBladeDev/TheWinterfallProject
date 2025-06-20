package com.darkbladedev.database;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.mechanics.NutritionSystem.NutrientType;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Gestor de base de datos para el plugin Savage Frontier
 * Soporta SQLite y MySQL para almacenar y recuperar la información de los jugadores
 */
public class DatabaseManager {

    private final SavageFrontierMain plugin;
    private Connection connection;
    private String dbFile;
    private BukkitTask saveTask;
    private int saveInterval;
    private String dbType;
    private HikariDataSource dataSource;
    
    // Configuración MySQL
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private int poolSize;
    
    /**
     * Constructor del gestor de base de datos
     * @param plugin Instancia del plugin principal
     */
    public DatabaseManager(SavageFrontierMain plugin) {
        this.plugin = plugin;
        this.saveInterval = plugin.getConfig().getInt("database.save_interval", 300);
        this.dbType = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
        
        if (dbType.equals("sqlite")) {
            this.dbFile = plugin.getConfig().getString("database.file", "database.db");
            
            // Reemplazar la ruta relativa con la ruta absoluta
            if (!dbFile.contains(":") && !dbFile.startsWith("/")) {
                dbFile = plugin.getDataFolder() + File.separator + dbFile;
            }
            
            // Crear directorio si no existe
            File dbDirectory = new File(dbFile).getParentFile();
            if (!dbDirectory.exists()) {
                dbDirectory.mkdirs();
            }
        } else if (dbType.equals("mysql")) {
            this.host = plugin.getConfig().getString("database.mysql.host", "localhost");
            this.port = plugin.getConfig().getInt("database.mysql.port", 3306);
            this.database = plugin.getConfig().getString("database.mysql.database", "savage_frontier");
            this.username = plugin.getConfig().getString("database.mysql.username", "root");
            this.password = plugin.getConfig().getString("database.mysql.password", "");
            this.poolSize = plugin.getConfig().getInt("database.mysql.pool_size", 10);
        }

        // Inicializar el archivo de configuración
        plugin.saveDefaultConfig();
    }
    
    /**
     * Inicializa la conexión a la base de datos y crea las tablas necesarias
     */
    public void initialize() {
        try {
            boolean connectionSuccess = false;
            
            // Si el tipo es MySQL, intentamos conectar primero
            if (dbType.equals("mysql")) {
                try {
                    initializeMySQL();
                    connectionSuccess = true;
                    ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Conexión a MySQL establecida correctamente"));
                } catch (Exception e) {
                    ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>No se pudo conectar a MySQL: " + e.getMessage()));
                    ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>Intentando conectar a SQLite como respaldo..."));
                    
                    // Cambiar a SQLite como respaldo
                    dbType = "sqlite";
                }
            }
            
            // Si no se conectó a MySQL o el tipo original era SQLite
            if (!connectionSuccess) {
                if (dbType.equals("sqlite")) {
                    initializeSQLite();
                } else {
                    ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Tipo de base de datos no soportado: " + dbType));
                    return;
                }
            }
            
            // Crear tablas si no existen
            createTables();
            
            // Iniciar tarea de guardado automático
            startSaveTask();
            
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Base de datos " + dbType.toUpperCase() + " inicializada correctamente"));
        } catch (Exception e) {
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al inicializar la base de datos: " + e.getMessage()));
            e.printStackTrace();
        }
    }
    
    /**
     * Inicializa la conexión a SQLite
     */
    private void initializeSQLite() throws SQLException, ClassNotFoundException {
        // Cargar el driver de SQLite
        Class.forName("org.sqlite.JDBC");
        
        // Establecer conexión
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
        
        // Configurar la base de datos para mejor rendimiento
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL;");
            statement.execute("PRAGMA synchronous=NORMAL;");
            statement.execute("PRAGMA foreign_keys=ON;");
        }
    }
    
    /**
     * Inicializa la conexión a MySQL usando HikariCP
     */
    private void initializeMySQL() {
        try {
            // Configurar HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true");
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(poolSize);
            
            // Configurar timeouts para evitar bloqueos prolongados
            config.setConnectionTimeout(5000); // 5 segundos de timeout para conexión
            config.setInitializationFailTimeout(10000); // 10 segundos antes de fallar la inicialización
            
            // Configuración adicional para mejor rendimiento
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            // Crear el pool de conexiones
            dataSource = new HikariDataSource(config);
            
            // Obtener una conexión del pool
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al conectar con la base de datos MySQL: " + e.getMessage()));
            e.printStackTrace();
        }
    }
    
    /**
     * Crea las tablas necesarias en la base de datos
     */
    private void createTables() throws SQLException {
        Statement statement = connection.createStatement();
        
        // Tabla de hidratación
        statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS hydration (" +
            "uuid VARCHAR(36) PRIMARY KEY, " +
            "level INTEGER NOT NULL);"
        );
        
        // Tabla de nutrición
        statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS nutrition (" +
            "uuid VARCHAR(36) PRIMARY KEY, " +
            "protein INTEGER NOT NULL, " +
            "fat INTEGER NOT NULL, " +
            "carbs INTEGER NOT NULL, " +
            "vitamins INTEGER NOT NULL);"
        );
        
        // Tabla de daño en extremidades
        statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS limb_damage (" +
            "uuid VARCHAR(36) NOT NULL, " +
            "limb_type VARCHAR(20) NOT NULL, " +
            "damage_level INTEGER NOT NULL, " +
            "PRIMARY KEY (uuid, limb_type));"
        );
        
        statement.close();
    }
    
    /**
     * Inicia la tarea de guardado automático
     */
    private void startSaveTask() {
        saveTask = new BukkitRunnable() {
            @Override
            public void run() {
                saveAllPlayerData();
            }
        }.runTaskTimerAsynchronously(plugin, saveInterval * 20L, saveInterval * 20L);
    }
    
    /**
     * Guarda los datos de todos los jugadores conectados
     */
    public void saveAllPlayerData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            savePlayerData(player.getUniqueId());
        }
    }
    
    /**
     * Guarda los datos de un jugador específico
     * @param playerId UUID del jugador
     */
    public void savePlayerData(UUID playerId) {
        try {
            // Guardar hidratación
            saveHydrationData(playerId);
            
            // Guardar nutrición
            saveNutritionData(playerId);
            
            // Guardar daño en extremidades
            saveLimbDamageData(playerId);
        } catch (SQLException e) {
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al guardar datos del jugador: " + e.getMessage()));
        }
    }
    
    /**
     * Guarda los datos de hidratación de un jugador
     * @param playerId UUID del jugador
     */
    private void saveHydrationData(UUID playerId) throws SQLException {
        int hydrationLevel = plugin.getHydrationSystem().getHydrationLevel(Bukkit.getPlayer(playerId));
        
        String query;
        if (dbType.equals("sqlite")) {
            query = "INSERT OR REPLACE INTO hydration (uuid, level) VALUES (?, ?);";
        } else {
            // Para MySQL/MariaDB
            query = "REPLACE INTO hydration (uuid, level) VALUES (?, ?);";
        }
        
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, playerId.toString());
        statement.setInt(2, hydrationLevel);
        statement.executeUpdate();
        statement.close();
    }
    
    /**
     * Guarda los datos de nutrición de un jugador
     * @param playerId UUID del jugador
     */
    private void saveNutritionData(UUID playerId) throws SQLException {
        Map<NutrientType, Integer> nutrients = plugin.getNutritionSystem().getAllNutrientLevels(Bukkit.getPlayer(playerId));
        
        String query;
        if (dbType.equals("sqlite")) {
            query = "INSERT OR REPLACE INTO nutrition (uuid, protein, fat, carbs, vitamins) VALUES (?, ?, ?, ?, ?);";
        } else {
            // Para MySQL/MariaDB
            query = "REPLACE INTO nutrition (uuid, protein, fat, carbs, vitamins) VALUES (?, ?, ?, ?, ?);";
        }
        
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, playerId.toString());
        statement.setInt(2, nutrients.get(NutrientType.PROTEIN));
        statement.setInt(3, nutrients.get(NutrientType.FAT));
        statement.setInt(4, nutrients.get(NutrientType.CARBS));
        statement.setInt(5, nutrients.get(NutrientType.VITAMINS));
        statement.executeUpdate();
        statement.close();
    }
    
    /**
     * Guarda los datos de daño en extremidades de un jugador
     * @param playerId UUID del jugador
     */
    private void saveLimbDamageData(UUID playerId) throws SQLException {
        // Eliminar datos antiguos
        PreparedStatement deleteStatement = connection.prepareStatement(
            "DELETE FROM limb_damage WHERE uuid = ?;"
        );
        deleteStatement.setString(1, playerId.toString());
        deleteStatement.executeUpdate();
        deleteStatement.close();
        
        // Insertar datos actuales
        for (com.darkbladedev.mechanics.LimbDamageSystem.LimbType limbType : com.darkbladedev.mechanics.LimbDamageSystem.LimbType.values()) {
            int damageLevel = plugin.getLimbDamageSystem().getLimbDamageLevel(Bukkit.getPlayer(playerId), limbType);
            
            PreparedStatement insertStatement = connection.prepareStatement(
                "INSERT INTO limb_damage (uuid, limb_type, damage_level) VALUES (?, ?, ?);"
            );
            insertStatement.setString(1, playerId.toString());
            insertStatement.setString(2, limbType.name());
            insertStatement.setInt(3, damageLevel);
            insertStatement.executeUpdate();
            insertStatement.close();
        }
    }
    
    /**
     * Carga los datos de un jugador desde la base de datos
     * @param player Jugador
     */
    public void loadPlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        
        try {
            // Cargar hidratación
            loadHydrationData(playerId);
            
            // Cargar nutrición
            loadNutritionData(playerId);
            
            // Cargar daño en extremidades
            loadLimbDamageData(playerId);
        } catch (SQLException e) {
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al cargar datos del jugador: " + e.getMessage()));
        }
    }
    
    /**
     * Carga los datos de hidratación de un jugador
     * @param playerId UUID del jugador
     */
    private void loadHydrationData(UUID playerId) throws SQLException {
        // Verificar que la conexión esté inicializada
        if (connection == null) {
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error: La conexión a la base de datos no está inicializada"));
            // Intentar reinicializar la conexión
            initialize();
            
            // Si aún es nula, lanzar excepción
            if (connection == null) {
                throw new SQLException("No se pudo establecer la conexión con la base de datos");
            }
        }
        
        PreparedStatement statement = connection.prepareStatement(
            "SELECT level FROM hydration WHERE uuid = ?;"
        );
        statement.setString(1, playerId.toString());
        ResultSet resultSet = statement.executeQuery();
        
        if (resultSet.next()) {
            int level = resultSet.getInt("level");
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                plugin.getHydrationSystem().setHydrationLevel(player, level);
            }
        }

        resultSet.close();
        statement.close();
    }
    
    /**
     * Carga los datos de nutrición de un jugador
     * @param playerId UUID del jugador
     */
    private void loadNutritionData(UUID playerId) throws SQLException {
        // Verificar que la conexión esté inicializada
        if (connection == null) {
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error: La conexión a la base de datos no está inicializada"));
            // Intentar reinicializar la conexión
            initialize();
            
            // Si aún es nula, lanzar excepción
            if (connection == null) {
                throw new SQLException("No se pudo establecer la conexión con la base de datos");
            }
        }
        
        PreparedStatement statement = connection.prepareStatement(
            "SELECT protein, fat, carbs, vitamins FROM nutrition WHERE uuid = ?;"
        );
        statement.setString(1, playerId.toString());
        ResultSet resultSet = statement.executeQuery();
        
        if (resultSet.next()) {
            Map<NutrientType, Integer> nutrients = new HashMap<>();
            nutrients.put(NutrientType.PROTEIN, resultSet.getInt("protein"));
            nutrients.put(NutrientType.FAT, resultSet.getInt("fat"));
            nutrients.put(NutrientType.CARBS, resultSet.getInt("carbs"));
            nutrients.put(NutrientType.VITAMINS, resultSet.getInt("vitamins"));
            
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                plugin.getNutritionSystem().setAllNutrientLevels(player, nutrients);
            }
        }
        
        resultSet.close();
        statement.close();
    }
    
    /**
     * Carga los datos de daño en extremidades de un jugador
     * @param playerId UUID del jugador
     */
    private void loadLimbDamageData(UUID playerId) throws SQLException {
        // Verificar que la conexión esté inicializada
        if (connection == null) {
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error: La conexión a la base de datos no está inicializada"));
            // Intentar reinicializar la conexión
            initialize();
            
            // Si aún es nula, lanzar excepción
            if (connection == null) {
                throw new SQLException("No se pudo establecer la conexión con la base de datos");
            }
        }
        
        PreparedStatement statement = connection.prepareStatement(
            "SELECT limb_type, damage_level FROM limb_damage WHERE uuid = ?;"
        );
        statement.setString(1, playerId.toString());
        ResultSet resultSet = statement.executeQuery();
        
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            while (resultSet.next()) {
                String limbTypeName = resultSet.getString("limb_type");
                int damageLevel = resultSet.getInt("damage_level");
                
                try {
                    com.darkbladedev.mechanics.LimbDamageSystem.LimbType limbType = 
                        com.darkbladedev.mechanics.LimbDamageSystem.LimbType.valueOf(limbTypeName);
                    plugin.getLimbDamageSystem().setLimbDamage(player, limbType, damageLevel);
                } catch (IllegalArgumentException e) {
                    ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Tipo de extremidad desconocido: " + limbTypeName));
                }
            }
        }
        
        resultSet.close();
        statement.close();
    }
    
    /**
     * Cierra la conexión a la base de datos y detiene las tareas
     */
    public void shutdown() {
        // Guardar datos de todos los jugadores antes de cerrar
        saveAllPlayerData();
        
        // Detener tarea de guardado automático
        if (saveTask != null) {
            saveTask.cancel();
        }
        
        // Cerrar conexión
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            
            // Cerrar pool de conexiones si estamos usando MySQL
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
        } catch (SQLException e) {
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + "<red>Error al cerrar la conexión con la base de datos: " + e.getMessage()));
        }
    }
    
    /**
     * Obtiene una conexión a la base de datos
     * @return Conexión a la base de datos
     */
    public Connection getConnection() throws SQLException {
        // Verificar si la conexión está cerrada o es nula
        if (connection == null || connection.isClosed()) {
            // Reinicializar la conexión
            if (dbType.equals("sqlite")) {
                try {
                    initializeSQLite();
                } catch (ClassNotFoundException e) {
                    throw new SQLException("Error al cargar el driver de SQLite", e);
                }
            } else if (dbType.equals("mysql")) {
                if (dataSource == null || dataSource.isClosed()) {
                    initializeMySQL();
                } else {
                    connection = dataSource.getConnection();
                }
            }
        }
        return connection;
    }
}