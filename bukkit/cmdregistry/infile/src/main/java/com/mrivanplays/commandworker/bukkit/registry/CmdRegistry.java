package com.mrivanplays.commandworker.bukkit.registry;

import com.mrivanplays.commandworker.core.RegisteredCommand;
import org.bukkit.command.CommandSender;

public interface CmdRegistry {

  void register(RegisteredCommand<CommandSender> command);
}
