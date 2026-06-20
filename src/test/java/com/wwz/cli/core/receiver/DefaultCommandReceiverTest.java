package com.wwz.cli.core.receiver;

import com.wwz.cli.core.command.CommandHolder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultCommandReceiverTest {

    @Test
    void shouldDelegateToParser() {
        var receivedLine = new AtomicReference<String>();
        var receiver = new DefaultCommandReceiver(line -> {
            receivedLine.set(line);
            return new CommandHolder("ok", List.of(), Map.of());
        });

        var holder = receiver.receive("help");

        assertThat(receivedLine).hasValue("help");
        assertThat(holder.getName()).isEqualTo("ok");
    }
}
