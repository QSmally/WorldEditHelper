package org.stonecipher;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public class WorldEditHelper extends JavaPlugin implements Listener {

    WorldEditPlugin worldEdit = null;

    @Override
    public void onEnable() {
        worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) return true;
        if(cmd.getName().equals("hidehelper")) {
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
        } else if(!tokens[0].equals("hidehelper")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    setPlayerSelection(p);
                }
            }.runTaskLater(this, 1);
        }
    }

    @EventHandler
    private void onClick(PlayerInteractEvent e) {
        setPlayerSelection(e.getPlayer());
    }

    private void setPlayerSelection(Player p) {
        LocalSession session = worldEdit.getSession(p);
        if (session.getSelectionWorld() == null) return;

        try {

            if (!session.isSelectionDefined(session.getSelectionWorld())) return;

            Region region = session.getSelection(session.getSelectionWorld());
            BlockVector3 bvMax = region.getMaximumPoint();
            BlockVector3 bvMin = region.getMinimumPoint();

            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard board = manager.getNewScoreboard();
            Objective objective = board.registerNewObjective(p.getDisplayName() + "weh", "dummy");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.setDisplayName(ChatColor.RED + "Current Selection");

            addLineToScoreboard(objective, 6, ChatColor.DARK_GREEN + "Position A:");
            addLineToScoreboard(objective, 5, "   " + ChatColor.GRAY + bvMax.getBlockX() + ChatColor.WHITE + "," + ChatColor.GRAY + bvMax.getBlockY() + ChatColor.WHITE + "," + ChatColor.GRAY + bvMax.getBlockZ());

            int volume = region.getArea();

            if (volume != 1) {
                addLineToScoreboard(objective, 4, ChatColor.DARK_GREEN + "Position B:");
                addLineToScoreboard(objective, 3, "   " + ChatColor.GRAY + bvMin.getBlockX() + ChatColor.WHITE + "," + ChatColor.GRAY + bvMin.getBlockY() + ChatColor.WHITE + "," + ChatColor.GRAY + bvMin.getBlockZ());
            }

            addLineToScoreboard(objective, 2, ChatColor.DARK_GREEN + "Volume:");

            if (volume < 100000)
                addLineToScoreboard(objective, 1, "   " + ChatColor.GREEN + volume);
            else if (volume < 1000000)
                addLineToScoreboard(objective, 1, "   " + ChatColor.YELLOW + volume);
            else if (volume < 2000000)
                addLineToScoreboard(objective, 1, "   " + ChatColor.RED + volume);
            else
                addLineToScoreboard(objective, 1, "   " + ChatColor.DARK_RED + volume);

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
