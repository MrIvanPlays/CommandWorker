package com.mrivanplays.commandworker.velocity.internal;

import com.mrivanplays.commandworker.core.RegisteredCommand;
import com.mrivanplays.commandworker.velocity.VelocityCommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

public class CommandRegistryHandler {

  private final ProxyServer proxy;
  private final CommandRegistration registration;

  public CommandRegistryHandler(ProxyServer proxy) {
    this.proxy = proxy;
    if (VelocityCommandManager.isVelocity1_1_0AndNewer()) {
      registration = new BrigadierCommandRegistration();
    } else {
      registration = new LegacyCommandRegistration();
    }
  }

  public void registerCommand(RegisteredCommand<CommandSource> command, boolean shouldFallback) {
    if (VelocityCommandManager.isVelocity1_1_0AndNewer()) {
      registration.register(proxy, command);
    } else if (shouldFallback) {
      registration.register(proxy, command);
    }
  }
}
