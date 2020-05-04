package it.multicoredev.spigot.listeners;

import it.multicoredev.mclib.yaml.Configuration;
import it.multicoredev.spigot.Chat;
import it.multicoredev.spigot.data.LocalTickets;
import it.multicoredev.spigot.data.Ticket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
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
public class OnJoin implements Listener {
    private final Plugin plugin;
    private final LocalTickets tickets;
    private final Configuration config;

    public OnJoin(Plugin plugin, LocalTickets tickets, Configuration config) {
        this.plugin = plugin;
        this.tickets = tickets;
        this.config = config;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Ticket ticket = tickets.getTicket(player);
        if (ticket == null) return;

        Chat.send(getString("ticket-close-target")
                .replace("{id}", String.valueOf(ticket.getId()))
                .replace("{player}", player.getName())
                .replace("{message}", ticket.getMessage()), player, true);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ticket.setDelivered(true);
            tickets.setDelivered(ticket);
        });

        if (player.hasPermission("ticket.resolve")) {
            Chat.send("&f&m----&9 Ticket &f&m----", player, true);

            List<Ticket> openTickets = tickets.getOpenTickets();
            if (openTickets.isEmpty()) return;

            int t = Math.min(openTickets.size(), 10);
            for (int i = 0; i < t; i++) {
                Ticket ticket2 = openTickets.get(i);
                Chat.send(getString("ticket-list")
                                .replace("{id}", String.valueOf(ticket2.getId()))
                                .replace("{player}", ticket2.getUsername())
                                .replace("{world}", ticket2.getWorld())
                                .replace("{message}", ticket2.getMessage()),
                        player, true);
            }

            Chat.send("&f&m----&9 Ticket &f&m----", player, true);
        }
    }

    private String getString(String path) {
        return config.getString("messages." + path);
    }
}
