package com.vitorxp.SyncCordVX.listeners;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import java.util.Map;

public class Profiler {
    public static String[] getTop3Culprits(Plugin mainPlugin) {
        try {
            Thread serverThread = null;
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                if (thread.getName().equals("Server thread")) {
                    serverThread = thread;
                    break;
                }
            }

            if (serverThread == null) {
                return new String[]{"Desconhecido (Thread não encontrada)", "N/A", "N/A"};
            }

            StackTraceElement[] stack = serverThread.getStackTrace();

            for (StackTraceElement element : stack) {
                String className = element.getClassName();

                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    if (className.startsWith(plugin.getClass().getPackage().getName())) {
                        return new String[]{
                                "Plugin: " + plugin.getName(),
                                "Classe: " + className,
                                "Método: " + element.getMethodName()
                        };
                    }
                }
            }

            return new String[]{"Minecraft/Spigot (Nenhum plugin no stack)", "N/A", "N/A"};

        } catch (Exception e) {
            return new String[]{"Erro ao profilar", e.getMessage(), "N/A"};
        }
    }
}