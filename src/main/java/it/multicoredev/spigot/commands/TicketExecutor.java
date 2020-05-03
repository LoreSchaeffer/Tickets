package it.multicoredev.spigot.commands;

import it.multicoredev.mclib.yaml.Configuration;
import it.multicoredev.spigot.Chat;
import it.multicoredev.spigot.data.Cache;
import it.multicoredev.spigot.data.CachedTicket;
import it.multicoredev.spigot.data.LocalTickets;
import it.multicoredev.spigot.data.Ticket;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Copyright © 2020 by Lorenzo Magni
 * This file is part of Tickets.
 * Tickets is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public class TicketExecutor implements CommandExecutor {
    private final Plugin plugin;
    private final Configuration config;
    private final LocalTickets tickets;
    private final Cache cache;

    public TicketExecutor(Plugin plugin, Configuration config, LocalTickets tickets, Cache cache) {
        this.plugin = plugin;
        this.config = config;
        this.tickets = tickets;
        this.cache = cache;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Chat.send(getString("not-player"), sender, true);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if (!player.hasPermission("ticket.resolve")) {
                insufficientPerms(player);
                return true;
            }

            sendTicketList(player);
        } else if (args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("create")) {
            openTicket(player, Chat.builder(args, 1));
        } else if (args[0].equalsIgnoreCase("resolve")) {
            if (!player.hasPermission("ticket.resolve")) {
                insufficientPerms(player);
                return true;
            }

            if (cache.isResolvingTicket(player)) {
                notNow(player);
                return true;
            }

            if (args.length < 2) {
                incorrectUsage(player);
                return true;
            }

            int id;
            try {
                id = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
                incorrectUsage(player);
                return true;
            }

            Ticket ticket = tickets.getTicket(id);

            if (ticket == null) {
                incorrectUsage(player);
                return true;
            }

            resolveTicket(player, ticket);
        } else if (args[0].equalsIgnoreCase("undo")) {
            if (!player.hasPermission("ticket.resolve")) {
                insufficientPerms(player);
                return true;
            }

            if (!cache.isResolvingTicket(player)) {
                notNow(player);
                return true;
            }

            undo(player);
        } else if (args[0].equalsIgnoreCase("close")) {
            if (!player.hasPermission("ticket.resolve")) {
                insufficientPerms(player);
                return true;
            }

            Ticket ticket;
            boolean goBack;
            String msg;

            if (cache.isResolvingTicket(player)) {
                ticket = cache.getCachedTicket(player).getTicket();
                goBack = true;
                msg = Chat.builder(args, 1);
            } else {
                if (args.length < 3) {
                    incorrectUsage(player);
                    return true;
                }

                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {
                    incorrectUsage(player);
                    return true;
                }

                ticket = tickets.getTicket(id);
                goBack = false;
                msg = Chat.builder(args, 2);
            }

            if (ticket == null) {
                Chat.send(getString("internal-error"), player, true);
                return true;
            }

            closeTicket(player, ticket, goBack, msg);
        } else {
            openTicket(player, Chat.builder(args));
        }

        return true;
    }

    private void insufficientPerms(Player player) {
        Chat.send(getString("insufficient-perms"), player, true);
    }

    private void notNow(Player player) {
        Chat.send(getString("not-now"), player, true);
    }

    private void incorrectUsage(Player player) {
        Chat.send(getString("incorrect-usage"), player, true);
    }

    private void internalError(Player player) {
        Chat.send(getString("internal-error"), player, true);
    }

    private void sendHelp(Player player) {
        Chat.send("&f&m----&9 Ticket &f&m----", player, true);
        Chat.send("&7&oParams between [] are optional, params between <> are mandatory.", player, true);
        Chat.send("&9/ticket [help] &3- &rShow help this help message.", player, true);
        Chat.send("&9/ticket [open] <message> &3- &rCreate a new ticket.", player, true);

        if (player.hasPermission("ticket.resolve")) {
            Chat.send("&9/ticket list &3- &rShow a list of open tickets.", player, true);
            Chat.send("&9/ticket resolve <id> &3- &rActivate solve mode to solve a ticket.", player, true);
            Chat.send("&9/ticket undo &3- &rGo back to your previous location and exit from solve mode.", player, true);
            Chat.send("&9/ticket close [id] <message> &3- &rClose a ticket leaving a message for the creator.", player, true);
            Chat.send("", player, true);
        }

        Chat.send("&f&m----&9 Ticket &f&m----", player, true);
    }

    private void openTicket(Player player, String msg) {
        Ticket ticket = new Ticket();
        ticket.setId(tickets.getLastId());
        ticket.setUsername(player.getName());
        ticket.setUuid(player.getUniqueId());
        ticket.setLocation(player.getLocation());
        ticket.setMessage(msg);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!tickets.openTicket(ticket)) {
                internalError(player);
                return;
            }

            Chat.send(getString("ticket-open").replace("{id}", String.valueOf(ticket.getId())), player, true);
            broadcastToStaff(getString("ticket-open-staff")
                    .replace("{player}", ticket.getUsername())
                    .replace("{id}", String.valueOf(ticket.getId()))
                    .replace("{message}", ticket.getMessage()), null);
        });
    }

    private void sendTicketList(Player player) {
        Chat.send("&f&m----&9 Ticket &f&m----", player, true);

        List<Ticket> openTickets = tickets.getOpenTickets();
        int t = Math.min(openTickets.size(), 10);
        for (int i = 0; i < t; i++) {
            Ticket ticket = openTickets.get(i);
            Chat.send(getString("ticket-list")
                            .replace("{id}", String.valueOf(ticket.getId()))
                            .replace("{player}", ticket.getUsername())
                            .replace("{world}", ticket.getWorld())
                            .replace("{message}", ticket.getMessage()),
                    player, true);
        }

        Chat.send("&f&m----&9 Ticket &f&m----", player, true);
    }

    private void resolveTicket(Player player, Ticket ticket) {
        cache.addTicket(player, ticket);
        Location location = new Location(Bukkit.getWorld(ticket.getWorld()), ticket.getX(), ticket.getY(), ticket.getZ());

        player.setInvulnerable(true);
        player.setAllowFlight(true);
        player.setFlying(true);
        if (player.hasPermission("ticket.creative")) player.setGameMode(GameMode.CREATIVE);
        player.teleport(location);

        Chat.send(getString("ticket-resolving")
                        .replace("{player}", ticket.getUsername())
                        .replace("{id}", String.valueOf(ticket.getId()))
                        .replace("{message}", ticket.getMessage())
                , player, true);
        broadcastToStaff(getString("ticket-resolving-staff")
                .replace("{staff}", player.getName())
                .replace("{player}", ticket.getUsername())
                .replace("{id}", String.valueOf(ticket.getId()))
                .replace("{message}", ticket.getMessage()), player);
    }

    private void undo(Player player) {
        CachedTicket ticket = cache.getCachedTicket(player);
        cache.removeTicket(ticket.getTicket());

        player.setInvulnerable(ticket.isInvulnerable());
        player.setAllowFlight(ticket.isAllowFlight());
        player.setFlying(ticket.isFlying());
        player.setGameMode(GameMode.valueOf(ticket.getGamemode()));
        player.teleport(ticket.getLocation());

        Chat.send(getString("ticket-undo"), player, true);
    }

    private void closeTicket(Player player, Ticket ticket, boolean goBack, String msg) {
        ticket.setStaff(player.getName());
        ticket.setStaffMessage(msg);
        ticket.setOpen(false);

        Player target = Bukkit.getPlayer(ticket.getUuid());
        ticket.setDelivered(target != null);


        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (tickets.closeTicket(ticket)) {
                internalError(player);
                return;
            }

            if (goBack) {
                CachedTicket cached = cache.getCachedTicket(player);
                cache.removeTicket(ticket);

                player.setInvulnerable(cached.isInvulnerable());
                player.setAllowFlight(cached.isAllowFlight());
                player.setFlying(cached.isFlying());
                player.setGameMode(GameMode.valueOf(cached.getGamemode()));
                player.teleport(cached.getLocation());
            }

            if (target != null) Chat.send(getString("ticket-close-target")
                    .replace("{id}", String.valueOf(ticket.getId()))
                    .replace("{player}", player.getName())
                    .replace("{message}", ticket.getMessage()), target, true);
            Chat.send(getString("ticket-close")
                    .replace("{player}", ticket.getUsername())
                    .replace("{id}", String.valueOf(ticket.getId())), player, true);
            broadcastToStaff(getString("ticket-close-staff")
                    .replace("{id}", String.valueOf(ticket.getId()))
                    .replace("{player}", player.getName())
                    .replace("{message}", ticket.getMessage()), player);
        });
    }

    private String getString(String path) {
        return config.getString("messages." + path);
    }

    private void broadcastToStaff(String msg, Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("ticket.resolve")) continue;
            if (player != null && player.getUniqueId().equals(p.getUniqueId())) continue;
            Chat.send(msg, p, true);
        }
    }
}
