package com.mrivanplays.commandworker.velocity;

import com.mrivanplays.commandworker.core.Command;
import com.mrivanplays.commandworker.core.CommandManager;
import com.mrivanplays.commandworker.core.RegisteredCommand;
import com.mrivanplays.commandworker.velocity.internal.CommandRegistryHandler;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class VelocityCommandManager implements CommandManager<CommandSource> {

  public static boolean isVelocity1_1_0AndNewer() {
    try {
      Class.forName("com.velocitypowered.api.command.BrigadierCommand");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private final CommandRegistryHandler registryHandler;

  private boolean shouldFallback = true;
  private List<RegisteredCommand<CommandSource>> registeredCommands;

  public VelocityCommandManager(ProxyServer proxy) {
    this.registryHandler = new CommandRegistryHandler(proxy);
    registeredCommands = new ArrayList<>();
  }

  @Override
  public void register(
      Command<CommandSource> command, Predicate<CommandSource> permissionCheck, String... aliases) {
    if (aliasesFree(registeredCommands, aliases)) {
      RegisteredCommand<CommandSource> registered =
          new RegisteredCommand<>(aliases, command, permissionCheck);
      registeredCommands.add(registered);
      registryHandler.registerCommand(registered, shouldFallback);
    }
  }

  @Override
  public boolean isBrigadierSupported() {
    return isVelocity1_1_0AndNewer();
  }

  @Override
  public boolean shouldFallback() {
    return shouldFallback;
  }

  @Override
  public void setShouldFallback(boolean shouldFallback) {
    this.shouldFallback = shouldFallback;
  }

  @Override
  public List<RegisteredCommand<CommandSource>> getRegisteredCommands() {
    return Collections.unmodifiableList(registeredCommands);
  }

  @Override
  public String toString() {
    return "VelocityCommandManager{"
        + ", shouldFallback="
        + shouldFallback
        + ", registeredCommands="
        + registeredCommands
        + '}';
  }
}
