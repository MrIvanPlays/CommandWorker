package com.mrivanplays.commandworker.core;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mrivanplays.commandworker.core.argument.parser.ArgumentHolder;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a command.
 *
 * @param <S> the sender type.
 */
public interface Command<S> {

  /**
   * Execute this command with the provided sender, label and arguments.
   *
   * <p>If the framework this command is being registered on doesn't give a label, the label
   * specified is "unknown".
   *
   * @param sender sender
   * @param label label
   * @param args arguments
   * @return success state
   */
  boolean execute(@NotNull S sender, @NotNull String label, @NotNull ArgumentHolder args)
      throws CommandSyntaxException;

  /**
   * Creates the command structure for this command.
   *
   * @return literal node
   * @see LiteralNode
   */
  @NotNull
  LiteralNode createCommandStructure();

  /**
   * Helper method for creating {@link CommandSyntaxException}s
   *
   * @param message message
   * @return command syntax exception
   */
  @NotNull
  default CommandSyntaxException syntaxException(@NotNull String message) {
    Objects.requireNonNull(message, "message");
    return new SimpleCommandExceptionType(new LiteralMessage(message)).create();
  }
}
