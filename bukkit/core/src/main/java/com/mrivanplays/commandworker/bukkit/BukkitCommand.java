package com.mrivanplays.commandworker.bukkit;

import com.mrivanplays.commandworker.core.Command;
import org.bukkit.command.CommandSender;

/**
 * Represents a bukkit command.
 *
 * <p>Bukkit command stands for a command, with sender being the bukkit {@link CommandSender}
 */
public interface BukkitCommand extends Command<CommandSender> {}
