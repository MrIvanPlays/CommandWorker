package com.mrivanplays.commandworker.core;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mrivanplays.commandworker.core.argument.parser.ArgumentHolder;

/**
 * Represents a command.
 *
 * @param <S> the sender type.
 */
public interface Command<S> {

  /**
   * Execute this command with the provided sender, label and arguments.
   *
   * @param sender sender
   * @param label label
   * @param args arguments
   * @return success state
   */
  boolean execute(S sender, String label, ArgumentHolder args) throws CommandSyntaxException;

  /**
   * Creates the command structure for this command.
   *
   * @return literal node
   * @see LiteralNode
   */
  LiteralNode createCommandStructure();
}
