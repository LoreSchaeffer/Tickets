package it.multicoredev.spigot;

import com.google.gson.Gson;
import it.multicoredev.mclib.db.MySQL;
import it.multicoredev.mclib.db.connectors.PoolSettings;
import it.multicoredev.mclib.yaml.Configuration;
import it.multicoredev.spigot.commands.TicketCompleter;
import it.multicoredev.spigot.commands.TicketExecutor;
import it.multicoredev.spigot.data.Cache;
import it.multicoredev.spigot.data.LocalTickets;
import it.multicoredev.spigot.listeners.OnJoin;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

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
public class Tickets extends JavaPlugin {
    private Configuration config;
    private MySQL db;
    private LocalTickets tickets;
    private Cache cache;

    @Override
    public void onEnable() {
        if (!loadConfig()) {
            onDisable();
            return;
        }

        if (!loadDatabase()) {
            onDisable();
            return;
        }

        tickets = new LocalTickets(this, db);
        tickets.startTask();

        loadCache();

        PluginCommand ticketCommand = getCommand("ticket");
        if (ticketCommand == null) {
            onDisable();
            return;
        }

        ticketCommand.setExecutor(new TicketExecutor(this, config, tickets, cache));
        ticketCommand.setTabCompleter(new TicketCompleter(tickets, cache));

        getServer().getPluginManager().registerEvents(new OnJoin(this, tickets, config), this);
    }

    @Override
    public void onDisable() {
        if (tickets.isRunning()) tickets.stopTask();
    }

    private boolean loadConfig() {
        config = new Configuration(new File(getDataFolder(), "config.yml"), getResource("config.yml"));

        try {
            if (!getDataFolder().exists() || !getDataFolder().isDirectory()) {
                if (!getDataFolder().mkdirs()) return false;
            }

            config.autoload();
            return true;
        } catch (IOException e) {
            Chat.getLogger("&4" + e.getMessage(), "severe");
            e.printStackTrace();
            return false;
        }
    }

    private boolean loadDatabase() {
        String host = config.getString("mysql.host");
        int port = config.getInt("mysql.port");
        String database = config.getString("mysql.database");
        String username = config.getString("mysql.username");
        String password = config.getString("mysql.password");

        PoolSettings pool = new PoolSettings();
        pool.setPoolName("tickets");
        pool.setMaximumPoolSize(4);
        pool.setMinimumIdle(2);

        db = new MySQL(host, port, database, username, password, "tickets", true, pool);

        try {
            db.createTable(new String[]{
                    "`id` INT(10) PRIMARY KEY",
                    "`username` VARCHAR(200) NOT NULL",
                    "`uuid` VARCHAR(128) NOT NULL",
                    "`world` VARCHAR(100) NOT NULL",
                    "`x` DOUBLE NOT NULL",
                    "`y` DOUBLE NOT NULL",
                    "`z` DOUBLE NOT NULL",
                    "`message` TEXT",
                    "`staff` VARCHAR(200) NULL",
                    "`s_message` TEXT NULL",
                    "`open` BOOLEAN DEFAULT 1",
                    "`delivered` BOOLEAN DEFAULT 0"
            }, "utf8mb4");
            return true;
        } catch (SQLException e) {
            Chat.getLogger("&4" + e.getMessage(), "severe");
            e.printStackTrace();
            return false;
        }
    }

    private void loadCache() {
        File file = new File(getDataFolder(), "cache.json");

        if (file.exists() && file.isFile()) {
            Gson gson = new Gson();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                cache = gson.fromJson(reader, Cache.class);
            } catch (IOException e) {
                Chat.getLogger("&4" + e.getMessage(), "severe");
                e.printStackTrace();

                cache = new Cache();
                cache.setFile(file);
            }
        } else {
            cache = new Cache();
            cache.setFile(file);
        }
    }
}
