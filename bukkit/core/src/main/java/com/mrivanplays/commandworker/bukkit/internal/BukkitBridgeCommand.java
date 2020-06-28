package com.mrivanplays.commandworker.bukkit.internal;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.IntegerSuggestion;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mrivanplays.commandworker.core.LiteralNode;
import com.mrivanplays.commandworker.core.RegisteredCommand;
import com.mrivanplays.commandworker.core.argument.Argument;
import com.mrivanplays.commandworker.core.argument.parser.ArgumentHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class BukkitBridgeCommand extends org.bukkit.command.Command {

  private static final Function<Suggestion, String> SUGGESTION_MAPPER =
      (suggestion) -> {
        if (suggestion instanceof IntegerSuggestion) {
          return Integer.toString(((IntegerSuggestion) suggestion).getValue());
        } else {
          return suggestion.getText();
        }
      };
  private static final BiFunction<String, Argument, Stream<String>> TO_SUGGESTIONS =
      (lastArg, argument) -> {
        if (argument.isLiteral()) {
          return Stream.of(argument.getName());
        } else {
          SuggestionsBuilder builder = new SuggestionsBuilder(lastArg, 0);
          if (argument.getSuggestionsConsumer() != null) {
            argument.getSuggestionsConsumer().accept(builder);
          } else {
            if (argument.getArgumentType() == null) {
              // why we check here? obviously a required argument's argument type should not be
              // null!
              // well, apparently the minecraft argument types would be null if this is even
              // registered! and that's why we have to have an exception here, returning empty
              // stream.
              return Stream.empty();
            }
            CompletableFuture<Suggestions> suggestionsFuture =
                argument.getArgumentType().listSuggestions(null, builder);
            return suggestionsFuture.join().getList().stream().map(SUGGESTION_MAPPER);
          }
          return builder.build().getList().stream().map(SUGGESTION_MAPPER);
        }
      };

  private final RegisteredCommand<CommandSender> command;

  public BukkitBridgeCommand(RegisteredCommand<CommandSender> command, String[] aliases) {
    super(
        aliases[0],
        "",
        "/" + aliases[0],
        Arrays.asList(Arrays.copyOfRange(aliases, 1, aliases.length)));
    this.command = command;
  }

  @Override
  public boolean testPermissionSilent(CommandSender sender) {
    return command.hasPermission(sender);
  }

  @Override
  public boolean execute(CommandSender sender, String commandLabel, String[] args) {
    LiteralNode structure = command.getCommandStructure();
    ArgumentHolder holder = new ArgumentHolder(String.join(" ", args), structure);
    if (args.length == 0 && structure.shouldExecuteCommand()) {
      return executeCommand(sender, commandLabel, holder);
    } else if (args.length == 0) {
      sender.sendMessage(
          ChatColor.RED
              + CommandSyntaxException.BUILT_IN_EXCEPTIONS
                  .dispatcherUnknownCommand()
                  .create()
                  .getMessage());
      return true;
    }
    Argument argument = holder.getLastArgument();
    if (argument.shouldExecuteCommand()) {
      return executeCommand(sender, commandLabel, holder);
    } else {
      sender.sendMessage(
          ChatColor.RED
              + CommandSyntaxException.BUILT_IN_EXCEPTIONS
                  .dispatcherUnknownCommand()
                  .create()
                  .getMessage());
      return true;
    }
  }

  private boolean executeCommand(CommandSender sender, String label, ArgumentHolder args) {
    try {
      return command.getCommand().execute(sender, label, args);
    } catch (CommandSyntaxException e) {
      sender.sendMessage(ChatColor.RED + e.getMessage());
      return true;
    }
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String alias, String[] args)
      throws IllegalArgumentException {
    return args.length == 0
        ? Collections.emptyList()
        : getCompletions(command.getCommandStructure(), args);
  }

  private List<String> getCompletions(LiteralNode node, String[] args) {
    if (node.getArguments().isEmpty()) {
      return Collections.emptyList();
    }
    String lastArg = args[args.length - 1].toLowerCase();
    String previousArg;
    if (args.length == 1) {
      previousArg = lastArg;
    } else {
      previousArg = args[args.length - 2].toLowerCase();
    }
    List<String> completions = new ArrayList<>();
    Predicate<String> STARTS_WITH_LASTARG = it -> it.startsWith(lastArg);
    if (args.length == 1) {
      completions.addAll(
          node.getArguments().stream()
              .flatMap(argument -> TO_SUGGESTIONS.apply(lastArg, argument))
              .filter(STARTS_WITH_LASTARG)
              .collect(Collectors.toList()));
    } else { // args.length > 1
      node.getArgumentByName(previousArg)
          .ifPresent(
              argument -> {
                if (argument.getChildren().isEmpty()) {
                  return;
                }
                completions.addAll(
                    argument.getChildren().stream()
                        .flatMap(arg -> TO_SUGGESTIONS.apply(lastArg, argument))
                        .filter(STARTS_WITH_LASTARG)
                        .collect(Collectors.toList()));
              });
    }

    return completions;
  }
}
