package it.multicoredev.spigot.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import it.multicoredev.spigot.Chat;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
public class Cache {
    private File file;
    @Expose
    private List<CachedTicket> tickets;

    public Cache() {
        tickets = new ArrayList<>();
    }

    public void setFile(File file) {
        this.file = file;
    }

    public List<Ticket> getCachedTickets() {
        List<Ticket> cachedTickets = new ArrayList<>();
        tickets.forEach(cached -> cachedTickets.add(cached.getTicket()));
        return cachedTickets;
    }

    public boolean isCached(Ticket ticket) {
        for (CachedTicket t : tickets) {
            if (t.getTicket().getId() == ticket.getId()) return true;
        }

        return false;
    }

    public void addTicket(Player player, Ticket ticket) {
        tickets.add(new CachedTicket(player, ticket));
    }

    public void removeTicket(Ticket ticket) {
        tickets.removeIf(cached -> cached.getTicket().getId() == ticket.getId());
    }

    public boolean isResolvingTicket(Player player) {
        for (CachedTicket cached : tickets) {
            if (cached.getUuid().equals(player.getUniqueId())) return true;
        }

        return false;
    }

    public CachedTicket getCachedTicket(Player player) {
        for (CachedTicket cached : tickets) {
            if (cached.getUuid().equals(player.getUniqueId())) return cached;
        }

        return null;
    }

    private void save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            Chat.getLogger("&4" + e.getMessage(), "warning");
            e.printStackTrace();
        }
    }
}
