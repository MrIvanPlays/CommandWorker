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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

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
  private Map<String, ArgumentData> argumentDataHolder;

  public ArgumentHolder(CommandContext<?> context, LiteralNode commandStructure) {
    this.context = context;

    String[] commandSplit = context.getInput().split(" ");
    this.args = Arrays.copyOfRange(commandSplit, 1, commandSplit.length);
    this.argumentDataHolder = new HashMap<>();

    this.populateMaps(commandStructure.getArguments());
  }

  public ArgumentHolder(String[] args, LiteralNode commandStructure) {
    this.args = args;
    this.argumentDataHolder = new HashMap<>();

    this.populateMaps(commandStructure.getArguments());
  }

  private void populateMaps(List<Argument> arguments) {
    if (arguments.isEmpty()) {
      return;
    }
    Map<Argument, Integer> newArgs = getRequiredArgs(arguments, -1);
    Set<Entry<Argument, Integer>> entrySet =
        newArgs.entrySet().stream()
            .sorted(Entry.comparingByValue())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    for (Map.Entry<Argument, Integer> entry : entrySet) {
      Argument argument = entry.getKey();
      int index = entry.getValue();
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
      argumentDataHolder.put(argument.getName(), ArgumentData.newArgumentData(range, argument));
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
  public <V> V getRequiredArgument(String name, Class<V> type) throws CommandSyntaxException {
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
    ArgumentData argumentData = argumentDataHolder.get(name);
    if (argumentData != null) {
      String raw = getRawRequiredArgument(argumentData.getIndex());
      if (raw == null) {
        return null;
      }
      ArgumentType<?> argumentType = argumentData.getArgument().getArgumentType();
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
    }
    return null;
  }

  /**
   * Returns the last argument, for which we have a value.
   *
   * @return last argument
   */
  public Argument getLastArgument() {
    List<Argument> candidates =
        argumentDataHolder.entrySet().stream()
            .filter(entry -> isTyped(entry.getKey()))
            .map(entry -> entry.getValue().getArgument())
            .collect(Collectors.toList());
    return candidates.get(0);
  }

  /**
   * Returns the {@link IndexRange} of the specified argument name, if the argument was defined in
   * the command structure.
   *
   * @param argumentName argument name
   * @return range if present
   */
  public IndexRange getArgumentIndex(String argumentName) {
    ArgumentData data = argumentDataHolder.get(argumentName);
    if (data != null) {
      return data.getIndex();
    }
    return null;
  }

  /**
   * Returns whether or not the specified argument is typed (being present when the command was
   * executed).
   *
   * @param argumentName the argument's name that you want to check if has value or not.
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
    return getRawRequiredArgument(getArgumentIndex(argumentName));
  }

  private String getRawRequiredArgument(IndexRange indexRange) {
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
