package com.vitorxp.SyncCordVX.commands;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class FireworkCommand implements CommandExecutor {

    private final Plugin plugin;
    private final Map<UUID, Long> lastUse = new ConcurrentHashMap<>();
    private final long COOLDOWN_MS = 3000L;

    public FireworkCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem executar este comando!");
            return true;
        }

        Player player = (Player) sender;

        boolean bypass = player.hasPermission("fw.nocooldown");

        if (!bypass) {
            long now = System.currentTimeMillis();
            Long last = lastUse.get(player.getUniqueId());
            if (last != null) {
                long diff = now - last;
                if (diff < COOLDOWN_MS) {
                    long remaining = (COOLDOWN_MS - diff + 999) / 1000;
                    player.sendMessage("§cAguarde " + remaining + "s para usar novamente.");
                    return true;
                }
            }
            lastUse.put(player.getUniqueId(), now);
        }

        spawnFireworkAtPlayer(player);
        player.sendMessage("§aFogos lançados!");

        return true;
    }

    private void spawnFireworkAtPlayer(Player player) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Location loc = player.getLocation().clone();
            loc.setY(loc.getY() + 0.1);

            Firework fw = player.getWorld().spawn(loc, Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();

            FireworkEffect effect = FireworkEffect.builder()
                    .with(FireworkEffect.Type.values()[ThreadLocalRandom.current().nextInt(FireworkEffect.Type.values().length)])
                    .withColor(randomColor(), randomColor(), randomColor())
                    .withFade(randomColor(), randomColor())
                    .trail(true)
                    .flicker(true)
                    .build();

            meta.addEffect(effect);
            meta.setPower(ThreadLocalRandom.current().nextInt(1, 3));
            fw.setFireworkMeta(meta);
        });
    }

    private Color randomColor() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        switch (r.nextInt(8)) {
            case 0: return Color.RED;
            case 1: return Color.BLUE;
            case 2: return Color.GREEN;
            case 3: return Color.YELLOW;
            case 4: return Color.FUCHSIA;
            case 5: return Color.ORANGE;
            case 6: return Color.AQUA;
            default: return Color.PURPLE;
        }
    }
}
