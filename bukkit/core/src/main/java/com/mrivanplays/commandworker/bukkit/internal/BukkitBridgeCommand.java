package com.mrivanplays.commandworker.bukkit.internal;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mrivanplays.commandworker.core.LiteralNode;
import com.mrivanplays.commandworker.core.RegisteredCommand;
import com.mrivanplays.commandworker.core.argument.Argument;
import com.mrivanplays.commandworker.core.argument.parser.ArgumentHolder;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class BukkitBridgeCommand extends org.bukkit.command.Command {

  private final RegisteredCommand<CommandSender> command;

  public BukkitBridgeCommand(RegisteredCommand<CommandSender> command, String[] aliases) {
    super(
        aliases[0],
        "",
        command.getCommandStructure().buildUsage(aliases[0]),
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
    return command.getCommandStructure().completeToStringList(args);
  }
}
