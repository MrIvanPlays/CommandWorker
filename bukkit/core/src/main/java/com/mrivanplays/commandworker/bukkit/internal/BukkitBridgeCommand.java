package com.mrivanplays.commandworker.bukkit.internal;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.command.CommandSender;

public class BukkitBridgeCommand extends org.bukkit.command.Command {

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
  public boolean execute(CommandSender sender, String commandLabel, String[] args) {
    try {
      return command
          .getCommand()
          .execute(sender, commandLabel, new ArgumentHolder(args, command.getCommandStructure()));
    } catch (CommandSyntaxException e) {
      return true;
    }
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String alias, String[] args)
      throws IllegalArgumentException {
    return args.length == 0
        ? Collections.emptyList()
        : getCompletions(command.getCommandStructure(), args, alias);
  }

  private List<String> getCompletions(LiteralNode node, String[] args, String alias) {
    Objects.requireNonNull(node, "Completion node for alias " + alias + " is null.");
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
    Function<Argument, Stream<String>> TO_SUGGESTIONS =
        argument -> {
          if (argument.isLiteral()) {
            return Stream.of(argument.getName());
          } else {
            SuggestionsBuilder builder = new SuggestionsBuilder(lastArg, 0);
            if (argument.getSuggestionsConsumer() != null) {
              argument.getSuggestionsConsumer().accept(builder);
            } else {
              CompletableFuture<Suggestions> suggestionsFuture =
                  argument.getArgumentType().listSuggestions(null, builder);
              return suggestionsFuture.join().getList().stream().map(Suggestion::getText);
            }
            return builder.build().getList().stream().map(Suggestion::getText);
          }
        };
    if (args.length == 1) {
      completions.addAll(
          node.getArguments().stream()
              .flatMap(TO_SUGGESTIONS)
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
                        .flatMap(TO_SUGGESTIONS)
                        .filter(STARTS_WITH_LASTARG)
                        .collect(Collectors.toList()));
              });
    }

    return completions;
  }
}
