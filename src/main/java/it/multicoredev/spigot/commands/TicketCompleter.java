package it.multicoredev.spigot.commands;

import it.multicoredev.spigot.data.Cache;
import it.multicoredev.spigot.data.LocalTickets;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
public class TicketCompleter implements TabCompleter {
    private final LocalTickets tickets;
    private final Cache cache;

    public TicketCompleter(LocalTickets tickets, Cache cache) {
        this.tickets = tickets;
        this.cache = cache;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        int len = args.length;
        if (!(sender instanceof Player)) return null;

        Player player = (Player) sender;

        if (len == 0) {
            List<String> completion = new ArrayList<>();
            completion.add("create");
            completion.add("help");

            if (player.hasPermission("ticket.resolve")) {
                completion.add("list");
                completion.add("resolve");
                completion.add("close");
                if (cache.isResolvingTicket(player)) completion.add("undo");
            }

            return completion;
        } else if (len == 1) {
            if (args[0].equalsIgnoreCase("resolve")) {
                if (player.hasPermission("ticket.resolve")) return getActiveTickets();
            } else if (args[0].equalsIgnoreCase("close")) {
                if (player.hasPermission("ticket.resolve")) {
                    if (cache.isResolvingTicket(player)) return null;
                    return getActiveTickets();
                }
            }
        }

        return null;
    }

    private List<String> getActiveTickets() {
        List<String> completion = new ArrayList<>();
        tickets.getOpenTickets().forEach(ticket -> {
            if (!cache.isCached(ticket)) completion.add(String.valueOf(ticket.getId()));
        });

        return completion;
    }


}
