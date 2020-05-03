package it.multicoredev.spigot.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

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
public class CachedTicket {
    private String uuid;
    private String world;
    private double x;
    private double y;
    private double z;
    private boolean isInvulnerable;
    private String gamemode;
    private boolean allowFlight;
    private boolean isFlying;
    private Ticket ticket;

    public CachedTicket(Player player, Ticket ticket) {
        uuid = player.getUniqueId().toString();
        world = player.getWorld().getName();
        x = player.getLocation().getX();
        y = player.getLocation().getY();
        z = player.getLocation().getZ();
        isInvulnerable = player.isInvulnerable();
        gamemode = player.getGameMode().name();
        allowFlight = player.getAllowFlight();
        isFlying = player.isFlying();
        this.ticket = ticket;
    }

    public UUID getUuid() {
        return UUID.fromString(uuid);
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public boolean isInvulnerable() {
        return isInvulnerable;
    }

    public String getGamemode() {
        return gamemode;
    }

    public boolean isAllowFlight() {
        return allowFlight;
    }

    public boolean isFlying() {
        return isFlying;
    }

    public Ticket getTicket() {
        return ticket;
    }
}
