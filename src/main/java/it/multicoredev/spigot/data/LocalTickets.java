package it.multicoredev.spigot.data;

import it.multicoredev.mclib.db.CompositeResult;
import it.multicoredev.mclib.db.MySQL;
import it.multicoredev.spigot.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
public class LocalTickets {
    private final Plugin plugin;
    private final MySQL db;
    private List<Ticket> tickets;
    private int lastId = 1;
    private BukkitTask task = null;

    public LocalTickets(Plugin plugin, MySQL db) {
        this.plugin = plugin;
        this.db = db;
        this.tickets = new ArrayList<>();
    }

    public void startTask() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            String query = "SELECT * FROM " + db.getTable() + " WHERE `open` = 1 OR DELIVERED = 0";
            CompositeResult result = null;
            try {
                result = db.executeQuery(query);
                tickets.clear();

                while (result.next()) {
                    Ticket ticket = new Ticket();
                    ticket.setId(result.getInt("id"));
                    ticket.setUsername(result.getString("username"));
                    ticket.setUuid(result.getUUID("uuid"));
                    ticket.setWorld(result.getString("world"));
                    ticket.setX(result.getDouble("x"));
                    ticket.setY(result.getDouble("y"));
                    ticket.setZ(result.getDouble("z"));
                    ticket.setMessage(result.getString("message"));
                    ticket.setStaff(result.getString("staff"));
                    ticket.setStaffMessage(result.getString("s_message"));
                    ticket.setOpen(result.getBoolean("open"));
                    ticket.setDelivered(result.getBoolean("delivered"));

                    tickets.add(ticket);
                }

                result.close();
                query = "SELECT `id` FROM " + db.getTable() + " WHERE 1";
                result = db.executeQuery(query);

                while (result.next()) {
                    int id = result.getInt("id");
                    if (lastId < id) {
                        lastId = id;
                    }
                }
            } catch (SQLException e) {
                Chat.getLogger("&4" + e.getMessage(), "severe");
            } finally {
                if (result != null) result.close();
            }

            Collections.sort(tickets);
        }, 0, 100);
    }

    public boolean isRunning() {
        return task != null;
    }

    public void stopTask() {
        task.cancel();
        task = null;
    }

    public int getLastId() {
        return ++lastId;
    }

    public Ticket getTicket(int id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId() == id) return ticket;
        }

        return null;
    }

    public Ticket getTicket(Player player) {
        for (Ticket ticket : tickets) {
            if (ticket.getUuid().equals(player.getUniqueId()) && !ticket.isDelivered()) return ticket;
        }

        return null;
    }

    public List<Ticket> getOpenTickets() {
        List<Ticket> open = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket.isOpen()) open.add(ticket);
        }

        Collections.sort(open);
        return open;
    }

    public boolean openTicket(Ticket ticket) {
        try {
            db.addLine(new String[]{
                    "id",
                    "username",
                    "uuid",
                    "world",
                    "x",
                    "y",
                    "z",
                    "message"
            }, new Object[]{
                    ticket.getId(),
                    ticket.getUsername(),
                    ticket.getUuid(),
                    ticket.getWorld(),
                    ticket.getX(),
                    ticket.getY(),
                    ticket.getZ(),
                    ticket.getMessage()
            });
        } catch (SQLException e) {
            Chat.getLogger("&4" + e.getMessage());
            e.printStackTrace();
            return false;
        }

        tickets.add(ticket);
        return true;
    }

    public boolean closeTicket(Ticket ticket) {
        try {
            db.set(new String[]{
                    "staff",
                    "s_message",
                    "open",
                    "delivered"
            }, new Object[]{
                    ticket.getStaff(),
                    ticket.getStaffMessage(),
                    ticket.isOpen() ? 1 : 0,
                    ticket.isDelivered() ? 1 : 0
            }, "id", ticket.getId());
        } catch (SQLException e) {
            Chat.getLogger("&4" + e.getMessage());
            e.printStackTrace();
            return false;
        }

        tickets.removeIf(t -> t.getId() == ticket.getId());
        return true;
    }

    public boolean setDelivered(Ticket ticket) {
        try {
            db.set("delivered", ticket.isDelivered() ? 1 : 0, "id", ticket.getId());
        } catch (SQLException e) {
            Chat.getLogger("&4" + e.getMessage());
            e.printStackTrace();
            return false;
        }

        tickets.removeIf(t -> t.getId() == ticket.getId());
        return true;
    }
}
