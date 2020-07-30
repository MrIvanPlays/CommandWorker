package com.mrivanplays.commandworker.bukkit;

import com.mrivanplays.commandworker.bukkit.argtypes.MinecraftArgumentTypesAccessor;
import com.mrivanplays.commandworker.bukkit.internal.BukkitBridgeCommand;
import com.mrivanplays.commandworker.bukkit.registry.CmdRegistry;
import com.mrivanplays.commandworker.bukkit.registry.CmdRegistryHandler;
import com.mrivanplays.commandworker.core.Command;
import com.mrivanplays.commandworker.core.CommandManager;
import com.mrivanplays.commandworker.core.RegisteredCommand;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitCommandManager implements CommandManager<CommandSender> {

  private List<RegisteredCommand<CommandSender>> registeredCommands;

  private final JavaPlugin plugin;

  private boolean shouldFallback = true;

  public BukkitCommandManager(JavaPlugin plugin) {
    this.plugin = plugin;
    this.registeredCommands = new ArrayList<>();
    if (CmdRegistryHandler.isSupported()) {
      MinecraftArgumentTypesAccessor.ensureSetup();
    }
  }

  @Override
  public void register(
      @NotNull Command<CommandSender> command,
      @Nullable Predicate<CommandSender> permissionCheck,
      @NotNull String... aliases) {
    Objects.requireNonNull(command, "command");
    if (permissionCheck == null) {
      permissionCheck = (sender) -> true;
    }
    Objects.requireNonNull(aliases, "aliases");
    String[] populatedAliases = CommandManager.getAliases(plugin.getName(), aliases);
    if (!aliasesFree(registeredCommands, populatedAliases)) {
      return;
    }
    RegisteredCommand<CommandSender> registeredCommand =
        new RegisteredCommand<>(populatedAliases, command, permissionCheck);
    registeredCommands.add(registeredCommand);
    CmdRegistry registry = CmdRegistryHandler.getRegistry();
    if (!isBrigadierSupported()) {
      if (shouldFallback) {
        // fallback to registering with bukkit
        getBukkitCommandMap()
            .register(
                aliases[0], plugin.getName(), new BukkitBridgeCommand(registeredCommand, aliases));
      }
    } else {
      registry.register(registeredCommand);
    }
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
  public boolean isBrigadierSupported() {
    return CmdRegistryHandler.isSupported();
  }

  private CommandMap getBukkitCommandMap() {
    try {
      Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
      field.setAccessible(true);
      return (CommandMap) field.get(Bukkit.getServer());
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @NotNull
  public List<RegisteredCommand<CommandSender>> getRegisteredCommands() {
    return Collections.unmodifiableList(registeredCommands);
  }

  @Override
  public String toString() {
    return "BukkitCommandManager(plugin="
        + plugin
        + ", shouldFallback="
        + shouldFallback
        + ", brigadierSupported="
        + isBrigadierSupported()
        + ", registeredCommands="
        + registeredCommands;
  }
}
