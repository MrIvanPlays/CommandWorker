package com.mrivanplays.commandworker.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents abstracted command manager.
 *
 * @param <S> sender type
 * @see CommandManager
 */
public abstract class AbstractCommandManager<S> implements CommandManager<S> {

  protected List<RegisteredCommand<S>> registeredCommands;
  protected boolean shouldFallback = true;

  public AbstractCommandManager() {
    registeredCommands = new ArrayList<>();
  }

  @Override
  public void register(
      @NotNull Command<S> command,
      @Nullable Predicate<S> permissionCheck,
      @NotNull String... aliases) {
    Objects.requireNonNull(command, "command");
    if (permissionCheck == null) {
      permissionCheck = (sender) -> true;
    }
    Objects.requireNonNull(aliases, "aliases");
    if (!aliasesFree(registeredCommands, aliases)) {
      return;
    }
    RegisteredCommand<S> registeredCommand =
        new RegisteredCommand<>(aliases, command, permissionCheck);
    registeredCommands.add(registeredCommand);
    handleRegistration(registeredCommand);
  }

  protected abstract void handleRegistration(RegisteredCommand<S> command);

  @Override
  public boolean shouldFallback() {
    return shouldFallback;
  }

  @Override
  public void setShouldFallback(boolean shouldFallback) {
    this.shouldFallback = shouldFallback;
  }

  @Override
  @NotNull
  public List<RegisteredCommand<S>> getRegisteredCommands() {
    return Collections.unmodifiableList(registeredCommands);
  }
}
