package com.vitorxp.SyncCordVX;

import com.comphenix.protocol.ProtocolLibrary;
import com.vitorxp.SyncCordVX.bot.BotManager;
import com.vitorxp.SyncCordVX.chunks.ChunkManager;
import com.vitorxp.SyncCordVX.commands.*;
import com.vitorxp.SyncCordVX.database.DatabaseManager;
import com.vitorxp.SyncCordVX.database.LinkDAO;
import com.vitorxp.SyncCordVX.linking.LinkManager;
import com.vitorxp.SyncCordVX.listeners.*;
import com.vitorxp.SyncCordVX.managers.ConfigManager;
import com.vitorxp.SyncCordVX.managers.PunishmentManager;
import com.vitorxp.SyncCordVX.npcs.NPCManager;
import com.vitorxp.SyncCordVX.npcs.NPCRankUpdater;
import com.vitorxp.SyncCordVX.npcs.rankup.RankupNPCManager;
import com.vitorxp.SyncCordVX.npcs.rankup.RankupNPCUpdater;
import com.vitorxp.SyncCordVX.pads.SlimePadManager;
import com.vitorxp.SyncCordVX.pads.SlimePadParticleTask;
import com.vitorxp.SyncCordVX.tasks.ChunkKeepAliveTask;
import com.vitorxp.SyncCordVX.tasks.LinkReminderTask;
import com.vitorxp.SyncCordVX.utils.TPSMonitor;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class SyncCordVX extends JavaPlugin {

    private LinkDAO linkDAO;
    private ChunkManager chunkManager;
    private LinkManager linkManager;
    private NPCManager npcManager;
    private SlimePadManager slimePadManager;
    private static SyncCordVX instance;
    private long startupTime;
    private DatabaseManager databaseManager;
    private PunishmentManager punishmentManager;
    private RankupNPCManager rankupNpcManager;
    private ConfigManager configManager;
    private final Set<UUID> playersInBuildMode = new HashSet<>();
    private BotManager botManager;

    private void logStartupMessage() {
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        String version = getDescription().getVersion();
        String author = "vitorxp";

        boolean citizens = getServer().getPluginManager().getPlugin("Citizens") != null;
        boolean holographic = getServer().getPluginManager().getPlugin("HolographicDisplays") != null;
        boolean vault = getServer().getPluginManager().getPlugin("Vault") != null;
        boolean papi = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;

        console.sendMessage("§8§m----------------------------------------------------");
        console.sendMessage(" ");
        console.sendMessage("§b           SyncCord§3VX §f" + version);
        console.sendMessage("§7           Sistema de Sincronização com Discord");
        console.sendMessage(" ");
        console.sendMessage("§7  Status das Integrações:");
        console.sendMessage(vault ? "§f    - §aVault: §2Encontrado (Economia OK)" : "§f    - §cVault: §4Não Encontrado");
        console.sendMessage(papi ? "§f    - §aPlaceholderAPI: §2Encontrado (Placeholders OK)" : "§f    - §cPlaceholderAPI: §4Não Encontrado");

        if (citizens && holographic) {
            console.sendMessage("§f    - §aNPCs de Rank: §bAtivado §7(Citizens & HD Ok)");
        } else {
            console.sendMessage("§f    - §cNPCs de Rank: §4Desativado §7(Dependências ausentes)");
        }

        console.sendMessage(" ");
        console.sendMessage("§aPlugin habilitado com sucesso!");
        console.sendMessage("§7Criado por: §b" + author);
        console.sendMessage(" ");
        console.sendMessage("§8§m----------------------------------------------------");
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        logStartupMessage();

        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this);
        this.slimePadManager = new SlimePadManager(this);
        punishmentManager = new PunishmentManager(this, databaseManager);
        botManager = new BotManager(this);
        this.chunkManager = new ChunkManager(this);
        this.rankupNpcManager = new RankupNPCManager(this);
        this.linkDAO = new LinkDAO(this, databaseManager);
        this.linkManager = new LinkManager(this);

        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            getLogger().info("WorldGuard encontrado! Ativando sistema de proteção de construção.");
            getCommand("build").setExecutor(new BuildModeCommand(this));
            getServer().getPluginManager().registerEvents(new BuildProtectionListener(this), this);
        } else {
            getLogger().warning("WorldGuard não encontrado. O sistema de proteção de construção foi desativado.");
        }

        this.startupTime = System.currentTimeMillis();

        TPSMonitor.getInstance().runTaskTimer(this, 100L, 20L);
        //new LinkReminderTask(this).runTaskTimer(this, 1200L, 6000L);
        new ChunkKeepAliveTask(this).runTaskTimer(this, 100L, 200L);

        new BukkitRunnable() {
            @Override
            public void run() {
                TPSMonitor.tick();
            }
        }.runTaskTimer(this, 0L, 1L);

        new SlimePadParticleTask(this).runTaskTimer(this, 100L, 20L);

        if (getServer().getPluginManager().getPlugin("Citizens") != null && getServer().getPluginManager().getPlugin("HolographicDisplays") != null) {
            this.npcManager = new NPCManager(this);
            new NPCRankUpdater(this).runTaskTimerAsynchronously(this, 40L, 5 * 60 * 20L);
            new RankupNPCUpdater(this).runTaskTimerAsynchronously(this, 60L, 5 * 60 * 20L);
            getCommand("setnpcrank").setExecutor(new SetNPCRankCommand(this));
            getCommand("reloadrankupnpc").setExecutor(new ReloadRankupNPCCommand(this));
            getCommand("reloadnpcs").setExecutor(new ReloadNPCsCommand(this));
            getCommand("setrankupnpc").setExecutor(new SetRankupNPCCommand(this));
        }

        registerCommands();
        registerListeners();
    }

    public long getUptime() {
        return System.currentTimeMillis() - startupTime;
    }

    public boolean toggleBuildMode(UUID playerUUID) {
        if (playersInBuildMode.contains(playerUUID)) {
            playersInBuildMode.remove(playerUUID);
            return false;
        } else {
            playersInBuildMode.add(playerUUID);
            return true;
        }
    }

    public boolean isInBuildMode(UUID playerUUID) {
        return playersInBuildMode.contains(playerUUID);
    }

    public void removePlayerFromBuildMode(UUID playerUUID) {
        playersInBuildMode.remove(playerUUID);
    }

    @Override
    public void onDisable() {
        getLogger().info("Iniciando o processo de desligamento do SyncCordVX...");

        Bukkit.getScheduler().cancelTasks(this);

        if (npcManager != null) {
            npcManager.shutdown();
            getLogger().info("Sistema de NPCs finalizado.");
        }

        if (botManager != null) {
            botManager.shutdown();
        }

        if (rankupNpcManager != null) {
            rankupNpcManager.shutdown();
        }

        playersInBuildMode.clear();

        if (databaseManager != null) {
            databaseManager.closeConnection();
            getLogger().info("Conexão com o banco de dados fechada.");
        }

        getLogger().info("SyncCordVX foi desabilitado com sucesso.");
    }

    private void registerCommands() {
        getCommand("historico").setExecutor(new HistoricoCommand(this));
        getCommand("punir").setExecutor(new PunirCommand(this));
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("kick").setExecutor(new KickCommand(this));
        getCommand("banip").setExecutor(new BanIPCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("unbanip").setExecutor(new UnbanIPCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this));
        getCommand("despunir").setExecutor(new DespunirCommand(this));
        getCommand("punicoes").setExecutor(new PunicoesCommand(this));
        getCommand("vincular").setExecutor(new VincularCommand(this));
        getCommand("manutencao").setExecutor(new ManutencaoCommand(this));
        getCommand("desvincular").setExecutor(new DesvincularCommand(this));
        getCommand("conta").setExecutor(new ContaCommand(this));
        getCommand("test").setExecutor(new testCommand(this));
        getCommand("setslimepad").setExecutor(new SetSlimePadCommand(this));
        getCommand("aviso-restart").setExecutor(new AvisoRestartCommand(this));
        getCommand("synccord").setExecutor(new SyncCordCommand(this));
        getCommand("keepchunk").setExecutor(new KeepChunkCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new LinkListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIClickListener(this), this);
        getServer().getPluginManager().registerEvents(new MaintenanceListener(this), this);
        ProtocolLibrary.getProtocolManager().addPacketListener(new TabPacketListener(this));
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
    }

    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    public void reloadAllConfigs() {
        reloadConfig();

        if (slimePadManager != null) {
            slimePadManager.loadPads();
        }

        getLogger().info("Arquivos de configuração do SyncCordVX foram recarregados.");
    }

    public RankupNPCManager getRankupNpcManager() {
        return rankupNpcManager;
    }

    public SlimePadManager getSlimePadManager() {
        return slimePadManager;
    }

    public static SyncCordVX getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BotManager getBotManager() {
        return botManager;
    }

    public LinkDAO getLinkDAO() {
        return linkDAO;
    }

    public LinkManager getLinkManager() {
        return linkManager;
    }

    public NPCManager getNpcManager() {
        return npcManager;
    }
}