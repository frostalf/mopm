package __0x277F.plugins.mopm.bukkit;

import __0x277F.plugins.mopm.common.ProxyBlacklist;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class ConnectionListener implements Listener {
    private MopmBukkitPlugin plugin;
    private Map<UUID, List<Predicate<Player>>> onJoinActions;
    private Map<UUID, ProxyBlacklist> finders;

    public ConnectionListener(MopmBukkitPlugin plugin) {
        this.plugin = plugin;
        this.onJoinActions = new HashMap<>();
        this.finders = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        plugin.getLookupThread().scheduleLookup(event.getAddress(), proxyBlacklist -> {
            if(proxyBlacklist != null) {
                AsyncProxyDetectedEvent detectedEvent = new AsyncProxyDetectedEvent(proxyBlacklist, event.getAddress(), event.getUniqueId());
                plugin.getServer().getPluginManager().callEvent(detectedEvent);
                this.onJoinActions.put(detectedEvent.getUuid(), detectedEvent.getActions());
                this.finders.put(event.getUniqueId(), proxyBlacklist);
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(onJoinActions.containsKey(event.getPlayer().getUniqueId())) {
            ProxyBlacklist blacklist = finders.get(event.getPlayer().getUniqueId());
            plugin.getLogger().info("Player " + event.getPlayer().getName() + " connected with an open proxy at " + event.getPlayer().getAddress().getAddress().getHostAddress() + " as detected by " + blacklist.getName());
            if(event.getPlayer().hasPermission("mopm.bypass")) {
                plugin.getLogger().info("Player " + event.getPlayer().getName() + " bypassed open proxy detection by permission");
            }
            for(Predicate<Player> p : onJoinActions.get(event.getPlayer().getUniqueId())) {
                if(p.test(event.getPlayer())) {
                    return;
                }
            }
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), blacklist.getDefaultCommand()
                    .replace("%player%", event.getPlayer().getName())
                    .replace("%bl%", blacklist.getName()));
        }
    }
}
