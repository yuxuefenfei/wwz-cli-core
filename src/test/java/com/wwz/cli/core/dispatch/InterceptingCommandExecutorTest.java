package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InterceptingCommandExecutorTest {

    @Test
    void shouldApplyBeforeAndAfterInterceptors() {
        var executor = new InterceptingCommandExecutor(
                holder -> CommandResult.ok(holder.option("name", "none")),
                List.of(new CommandInterceptor() {
                    @Override
                    public CommandHolder beforeExecute(CommandHolder holder) {
                        return new CommandHolder(holder.getName(), holder.getArgs(), Map.of("name", "changed"));
                    }

                    @Override
                    public CommandResult afterExecute(CommandHolder holder, CommandResult result) {
                        return CommandResult.ok(result.getOutput() + "!");
                    }
                }));

        var result = executor.execute(new CommandHolder("hello", List.of(), Map.of("name", "origin")));

        assertThat(result.getOutput()).isEqualTo("changed!");
    }

    @Test
    void shouldAllowInterceptorToHandleErrors() {
        var executor = new InterceptingCommandExecutor(
                holder -> { throw new IllegalStateException("boom"); },
                List.of(new CommandInterceptor() {
                    @Override
                    public CommandResult onError(CommandHolder holder, RuntimeException ex) {
                        return CommandResult.error("handled:" + ex.getMessage());
                    }
                }));

        var result = executor.execute(new CommandHolder("hello", List.of(), Map.of()));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getOutput()).isEqualTo("handled:boom");
    }
}
