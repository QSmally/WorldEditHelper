package org.stonecipher;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.Random;

public class WorldEditHelper extends JavaPlugin implements Listener {

    Random rand = new Random();
    WorldEditPlugin worldEdit = null;

    @Override
    public void onEnable() {
        worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        if (cmd.getName().equals("hidehelper")) {
            hideHelper((Player) sender);
            ((Player) sender).performCommand("/desel");
        }

        return true;
    }

    @EventHandler
    private void onClick(PlayerCommandPreprocessEvent e) {

        String[] tokens = e.getMessage().substring(1).split("\\s+");
        final Player p = e.getPlayer();

        if (tokens[0].equals("/sel") || tokens[0].equals("/desel") || tokens[0].equals("/deselect")) {
            hideHelper(p);
        } else if (!tokens[0].equals("hidehelper")) {
            new BukkitRunnable() {
                @Override
                public void run() { setPlayerSelection(p); }
            }.runTaskLater(this, 1);
        }
    }

    @EventHandler
    private void onClick(PlayerInteractEvent e) {
        if ((e.getAction().equals(Action.LEFT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                && e.hasItem()
                && e.getItem().getType().equals(Material.WOODEN_AXE)) {
            setPlayerSelection(e.getPlayer());
        }
    }

    private void setPlayerSelection(Player p) {

        LocalSession session = worldEdit.getSession(p);
        if (session.getSelectionWorld() == null) return;

        try {

            if (!session.isSelectionDefined(session.getSelectionWorld())) return;

            Region region       = session.getSelection(session.getSelectionWorld());
            BlockVector3 bvMax  = region.getMaximumPoint();
            BlockVector3 bvMin  = region.getMinimumPoint();

            ScoreboardManager manager   = Bukkit.getScoreboardManager();
            Scoreboard board            = manager.getNewScoreboard();
            Objective objective         = board.registerNewObjective(Integer.toString(rand.nextInt(1234567890)), "dummy");

            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.setDisplayName(ChatColor.RED + "Current Selection");

            addLineToScoreboard(objective, 6, ChatColor.GREEN + "Position A");
            addLineToScoreboard(objective, 5, "   " + ChatColor.GRAY + bvMax.getBlockX() + ChatColor.WHITE + "," + ChatColor.GRAY + bvMax.getBlockY() + ChatColor.WHITE + "," + ChatColor.GRAY + bvMax.getBlockZ()); // This is a disgusting line

            int volume = region.getArea();

            if (volume != 1) {
                addLineToScoreboard(objective, 4, ChatColor.GREEN + "Position B");
                addLineToScoreboard(objective, 3, "   " + ChatColor.GRAY + bvMin.getBlockX() + ChatColor.WHITE + "," + ChatColor.GRAY + bvMin.getBlockY() + ChatColor.WHITE + "," + ChatColor.GRAY + bvMin.getBlockZ());
            }

            var COLOR = ChatColor.GREEN;
            addLineToScoreboard(objective, 2, COLOR + "Volume");

            if (volume < 100000)        COLOR = ChatColor.GREEN;
            else if (volume < 1000000)  COLOR = ChatColor.YELLOW;
            else if (volume < 2000000)  COLOR = ChatColor.RED;
            else                        COLOR = ChatColor.DARK_RED;

            addLineToScoreboard(objective, 1, "   " + ChatColor.GREEN + volume);

            p.setScoreboard(board);
        } catch (IncompleteRegionException exception) {
            exception.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void hideHelper(Player p) {
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    private void addLineToScoreboard(Objective obj, int order, String line) {
        Score tmp = obj.getScore(line);
        tmp.setScore(order);
    }
}
