package com.mrivanplays.commandworker.core.argument.parser;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mrivanplays.commandworker.core.argument.Argument;
import java.util.Objects;

/** Represents argument data. */
public final class ArgumentData {

  public static ArgumentData newArgumentData(
      IndexRange index,
      Argument argument,
      Object parsed,
      String rawValue,
      CommandSyntaxException exception) {
    return new ArgumentData(index, argument, parsed, rawValue, exception);
  }

  private final IndexRange index;
  private final Argument argument;
  private final Object parsed;
  private final String rawValue;
  private final CommandSyntaxException commandSyntaxException;

  private ArgumentData(
      IndexRange index,
      Argument argument,
      Object parsed,
      String rawValue,
      CommandSyntaxException commandSyntaxException) {
    this.index = index;
    this.argument = Objects.requireNonNull(argument, "argument");
    this.parsed = parsed;
    this.rawValue = rawValue;
    this.commandSyntaxException = commandSyntaxException;
  }

  /**
   * Returns the {@link IndexRange} for the argument.
   *
   * @return index range
   */
  public IndexRange getIndex() {
    return index;
  }
  /**
   * Returns the {@link Argument}, data for is held here.
   *
   * @return argument
   */
  public Argument getArgument() {
    return argument;
  }

  /**
   * Returns the parsed value of the held argument.
   *
   * @return parsed
   */
  public Object getParsedValue() {
    return parsed;
  }

  /**
   * Returns the raw value of the held argument.
   *
   * @return raw
   */
  public String getRawValue() {
    return rawValue;
  }

  /**
   * Returns the thrown command syntax exception while the argument was parsed. It will get thrown
   * when the argument is retrieved.
   *
   * @return command syntax exceptions
   */
  public CommandSyntaxException getCommandSyntaxException() {
    return commandSyntaxException;
  }
}
