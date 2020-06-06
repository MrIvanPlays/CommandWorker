package com.mrivanplays.commandworker.core.argument.parser;

import com.mrivanplays.commandworker.core.argument.Argument;
import java.util.Objects;

/**
 * Represents argument data.
 */
public final class ArgumentData {

  public static ArgumentData newArgumentData(IndexRange index, Argument argument) {
    return new ArgumentData(index, argument);
  }

  private final IndexRange index;
  private final Argument argument;

  private ArgumentData(IndexRange index, Argument argument) {
    this.index = index;
    this.argument = Objects.requireNonNull(argument, "argument");
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
}
