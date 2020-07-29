package com.mrivanplays.commandworker.velocity.internal;

import com.mrivanplays.commandworker.core.RegisteredCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

public interface CommandRegistration {

  void register(ProxyServer proxy, RegisteredCommand<CommandSource> command);
}
