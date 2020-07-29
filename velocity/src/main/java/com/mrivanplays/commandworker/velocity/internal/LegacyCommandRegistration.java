package com.mrivanplays.commandworker.velocity.internal;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mrivanplays.commandworker.core.LiteralNode;
import com.mrivanplays.commandworker.core.RegisteredCommand;
import com.mrivanplays.commandworker.core.argument.Argument;
import com.mrivanplays.commandworker.core.argument.parser.ArgumentHolder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.List;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;

public class LegacyCommandRegistration implements CommandRegistration {

  @Override
  public void register(ProxyServer proxy, RegisteredCommand<CommandSource> command) {
    //noinspection deprecation
    proxy.getCommandManager().register(new LegacyCommand(command), command.getAliases());
  }

  @SuppressWarnings("deprecation")
  private static final class LegacyCommand implements com.velocitypowered.api.command.Command {

    private final RegisteredCommand<CommandSource> command;

    public LegacyCommand(RegisteredCommand<CommandSource> command) {
      this.command = command;
    }

    @Override
    public void execute(CommandSource source, String[] args) {
      LiteralNode structure = command.getCommandStructure();
      ArgumentHolder holder = new ArgumentHolder(String.join(" ", args), structure);
      if (args.length == 0 && structure.shouldExecuteCommand()) {
        executeCommand(source, holder);
        return;
      } else if (args.length == 0) {
        source.sendMessage(
            TextComponent.of(
                    CommandSyntaxException.BUILT_IN_EXCEPTIONS
                        .dispatcherUnknownCommand()
                        .create()
                        .getMessage())
                .color(TextColor.RED));
        return;
      }
      Argument argument = holder.getLastArgument();
      if (argument.shouldExecuteCommand()) {
        executeCommand(source, holder);
      } else {
        source.sendMessage(
            TextComponent.of(
                    CommandSyntaxException.BUILT_IN_EXCEPTIONS
                        .dispatcherUnknownCommand()
                        .create()
                        .getMessage())
                .color(TextColor.RED));
      }
    }

    private void executeCommand(CommandSource sender, ArgumentHolder args) {
      try {
        command.getCommand().execute(sender, "unknown", args);
      } catch (CommandSyntaxException e) {
        sender.sendMessage(TextComponent.of(e.getMessage()).color(TextColor.RED));
      }
    }

    @Override
    public List<String> suggest(CommandSource source, String[] currentArgs) {
      return command.getCommandStructure().completeToStringList(currentArgs);
    }

    @Override
    public boolean hasPermission(CommandSource source, String[] args) {
      return command.hasPermission(source);
    }
  }
}
