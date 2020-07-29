package com.mrivanplays.commandworker.velocity;

import com.mrivanplays.commandworker.core.Command;
import com.velocitypowered.api.command.CommandSource;

/**
 * Represents a velocity command.
 *
 * <p>Velocity command stands for a command, with sender being the velocity {@link CommandSource}
 */
public interface VelocityCommand extends Command<CommandSource> {}
