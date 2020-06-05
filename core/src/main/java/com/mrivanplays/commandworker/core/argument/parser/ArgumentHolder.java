package com.mrivanplays.commandworker.core.argument.parser;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mrivanplays.commandworker.core.LiteralNode;
import com.mrivanplays.commandworker.core.argument.Argument;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Represents a holder of arguments. */
public final class ArgumentHolder {

  private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = new HashMap<>();

  static {
    PRIMITIVE_TO_WRAPPER.put(boolean.class, Boolean.class);
    PRIMITIVE_TO_WRAPPER.put(byte.class, Byte.class);
    PRIMITIVE_TO_WRAPPER.put(short.class, Short.class);
    PRIMITIVE_TO_WRAPPER.put(char.class, Character.class);
    PRIMITIVE_TO_WRAPPER.put(int.class, Integer.class);
    PRIMITIVE_TO_WRAPPER.put(long.class, Long.class);
    PRIMITIVE_TO_WRAPPER.put(float.class, Float.class);
    PRIMITIVE_TO_WRAPPER.put(double.class, Double.class);
  }

  private CommandContext<?> context;
  private final String[] args;
  private Map<String, ArgumentType<?>> argumentTypes;
  private Map<String, IndexRange> byIndex;

  public ArgumentHolder(CommandContext<?> context, LiteralNode commandStructure) {
    this.context = context;

    String[] commandSplit = context.getInput().split(" ");
    this.args = Arrays.copyOfRange(commandSplit, 1, commandSplit.length);
    this.byIndex = new HashMap<>();

    this.populateMaps(commandStructure.getArguments(), false);
  }

  public ArgumentHolder(String[] args, LiteralNode commandStructure) {
    this.args = args;
    this.argumentTypes = new HashMap<>();
    this.byIndex = new HashMap<>();

    this.populateMaps(commandStructure.getArguments(), true);
  }

  private void populateMaps(List<Argument> arguments, boolean addToArgumentTypes) {
    if (arguments.isEmpty()) {
      return;
    }
    Map<Argument, Integer> newArgs = getRequiredArgs(arguments, -1);
    for (Map.Entry<Argument, Integer> entry : newArgs.entrySet()) {
      Argument argument = entry.getKey();
      int index = entry.getValue();
      if (addToArgumentTypes) {
        argumentTypes.put(argument.getName(), argument.getArgumentType());
      }
      IndexRange range;
      if (argument.getArgumentType().getClass().isAssignableFrom(StringArgumentType.class)) {
        StringArgumentType.StringType type =
            StringArgumentType.class.cast(argument.getArgumentType()).getType();
        if (type == StringArgumentType.StringType.GREEDY_PHRASE) {
          range = new IndexRange(index, args.length - 1);
        } else {
          range = new IndexRange(index);
        }
      } else {
        range = new IndexRange(index);
      }
      byIndex.put(argument.getName(), range);
      if (range.isRange()) {
        break;
      }
    }
  }

  private Map<Argument, Integer> getRequiredArgs(List<Argument> arguments, int previousIndex) {
    if (arguments.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<Argument, Integer> args = new HashMap<>();
    for (Argument argument : arguments) {
      int currentIndex = previousIndex + 1;
      if (!argument.isLiteral()) {
        args.put(argument, currentIndex);
      }
      if (argument.getChildren().isEmpty()) {
        continue;
      }
      args.putAll(getRequiredArgs(argument.getChildren(), currentIndex));
    }
    return args;
  }

  /**
   * Retrieves the required argument with the specified name, which should be parsed to the
   * specified type.
   *
   * @param name argument name
   * @param type argument type
   * @param <V> value type
   * @return value
   */
  public <V> V getRequiredArgument(String name, Class<V> type) {
    if (context != null) {
      try {
        return context.getArgument(name, type);
      } catch (IllegalArgumentException e) {
        if (e.getMessage().contains("No such argument")) {
          return null;
        }
        throw e;
      }
    }
    ArgumentType<?> argumentType = argumentTypes.get(name);
    if (argumentType != null) {
      String raw = getRawRequiredArgument(name);
      if (raw == null) {
        return null;
      }
      try {
        Object parsed = argumentType.parse(new StringReader(raw));
        if (PRIMITIVE_TO_WRAPPER.getOrDefault(type, type).isAssignableFrom(parsed.getClass())) {
          return (V) parsed;
        } else {
          throw new IllegalArgumentException(
              "Argument '"
                  + name
                  + "' is defined as "
                  + parsed.getClass().getSimpleName()
                  + ", not "
                  + type);
        }
      } catch (CommandSyntaxException e) {
        return null;
      }
    }
    return null;
  }

  /**
   * Returns the {@link IndexRange} of the specified argument name, if the argument was defined in
   * the command structure.
   *
   * @param argumentName argument name
   * @return range if present
   */
  public IndexRange getArgumentIndex(String argumentName) {
    return byIndex.get(argumentName);
  }

  /**
   * Returns whether or not the specified argument name has argument value.
   *
   * @param argumentName argument name
   * @return <code>true</code> if typed, <code>false</code> otherwise
   */
  public boolean isTyped(String argumentName) {
    IndexRange index = getArgumentIndex(argumentName);
    return index != null && (size() >= index.getStart() + 1);
  }

  /**
   * Returns the specified required argument's name's argument if is typed in its raw state.
   *
   * @param argumentName argument name
   * @return argument
   */
  public String getRawRequiredArgument(String argumentName) {
    IndexRange indexRange = getArgumentIndex(argumentName);
    if (indexRange == null) {
      return null;
    }
    String value;
    if (!indexRange.isRange()) {
      value = args[indexRange.getIndex()];
    } else {
      StringBuilder buffer = new StringBuilder();
      for (int i = indexRange.getStart(); i <= indexRange.getEnd(); i++) {
        buffer.append(args[i]).append(' ');
      }
      value = buffer.substring(0, buffer.length() - 1);
    }
    return value;
  }

  /**
   * Returns the arguments length.
   *
   * @return length
   */
  public int size() {
    return args.length;
  }

  /**
   * Returns the arguments in their raw state.
   *
   * @return raw arguments
   */
  public String[] getRawArgs() {
    return args;
  }
}
