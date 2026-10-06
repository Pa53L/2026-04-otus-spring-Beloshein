package ru.otus.hw.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
@DisplayName("Стартовая подсказка команд библиотеки")
class ShellExamplesTest {

    @DisplayName("Должен выводить готовые для копирования примеры создания и поиска")
    @Test
    void shouldPrintCopyableCreationAndLookupExamples(CapturedOutput output) {
        new ShellExamples().run(new DefaultApplicationArguments());
        assertThat(output.getOut()).contains("bbid 4", "cbbid 4", "abid 1")
                .containsPattern("(?m)^bins \"[^\"]+\" 1 1,6\\s*$")
                .containsPattern("(?m)^cins \"[^\"]+\" 4\\s*$");
    }
}
