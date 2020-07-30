package com.mrivanplays.commandworker.velocity;

import com.mrivanplays.commandworker.core.AbstractCommandManager;
import com.mrivanplays.commandworker.core.RegisteredCommand;
import com.mrivanplays.commandworker.velocity.internal.CommandRegistryHandler;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

public class VelocityCommandManager extends AbstractCommandManager<CommandSource> {

  public static boolean isVelocity1_1_0AndNewer() {
    try {
      Class.forName("com.velocitypowered.api.command.BrigadierCommand");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private final CommandRegistryHandler registryHandler;

  public VelocityCommandManager(ProxyServer proxy) {
    super();
    this.registryHandler = new CommandRegistryHandler(proxy);
  }

  @Override
  protected void handleRegistration(RegisteredCommand<CommandSource> registered) {
    registryHandler.registerCommand(registered, shouldFallback);
  }

  @Override
  public boolean isBrigadierSupported() {
    return isVelocity1_1_0AndNewer();
  }

  @Override
  public String toString() {
    return "VelocityCommandManager{"
        + ", shouldFallback="
        + shouldFallback
        + ", registeredCommands="
        + registeredCommands
        + ", brigadierSupported="
        + isBrigadierSupported()
        + '}';
  }
}
