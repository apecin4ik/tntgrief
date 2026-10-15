package ru.grief.chesttnt;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestTnt extends JavaPlugin implements Listener {

    // Сундуки, найденные в самом начале обработки взрыва (до WorldGuard)
    private final Map<EntityExplodeEvent, List<Block>> saved = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    private boolean applies(EntityExplodeEvent e) {
        Entity en = e.getEntity();
        if (!(en instanceof TNTPrimed) && !(en instanceof ExplosiveMinecart)) return false;
        return e.getLocation().getWorld().getName().startsWith("grief-1");
    }

    private boolean isChest(Block b) {
        Material m = b.getType();
        return m == Material.CHEST || m == Material.TRAPPED_CHEST;
    }

    // Самый ранний этап: запоминаем сундуки из полного списка взрыва
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void early(EntityExplodeEvent e) {
        if (!applies(e)) return;
        List<Block> chests = new ArrayList<>();
        for (Block b : e.blockList()) {
            if (isChest(b)) chests.add(b);
        }
        saved.put(e, chests);
    }

    // Самый поздний этап: после WorldGuard и GriefFix оставляем только сундуки
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void late(EntityExplodeEvent e) {
        List<Block> chests = saved.remove(e);
        if (chests == null) return;
        e.setCancelled(false);
        e.blockList().clear();
        e.blockList().addAll(chests);
    }
}
