package net.azisaba.lgw.core.util;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperty;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.configs.MapsConfig;

public class MapLoader {

    public static void loadMap(String mapName){

        World world = Bukkit.getWorld(mapName);

        if (world != null) {
            Logging.warn(ChatColor.RED + "World " + mapName + " is already loaded!");
            LeonGunWar.getPlugin().getMatchQueueManager().setWorld(world);

            return;
        }

        SlimePlugin swm = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        SlimeLoader slimeLoader = swm.getLoader("mysql");

        Bukkit.getScheduler().runTaskAsynchronously(LeonGunWar.getPlugin(),() -> {

            try {
                SlimeWorld slimeWorld = swm.loadWorld(slimeLoader,mapName,true,new SlimePropertyMap());

                Bukkit.getScheduler().runTask(LeonGunWar.getPlugin(),() -> {

                    swm.generateWorld(slimeWorld);

                    World loadedWorld = Bukkit.getWorld(mapName);

                    LeonGunWar.getPlugin().getMatchQueueManager().setWorld(loadedWorld);
                    LeonGunWar.getPlugin().getMatchQueueManager().setLoaded(true);

                });

            } catch ( UnknownWorldException | WorldInUseException | NewerFormatException | CorruptedWorldException | IOException e ) {
                Logging.warn("Failed load map " + mapName + " !");
                e.printStackTrace();
            }

        });

    }

}
