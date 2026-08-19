package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.config.TestConfig;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResultServiceImplTest {
    @Mock
    private TestConfig testConfig;

    @Mock
    private LocalizedIOService ioService;

    @InjectMocks
    private ResultServiceImpl resultService;

    @Test
    void shouldPrintPassedMessageWhenEnoughRightAnswers() {
        when(testConfig.getRightAnswersCountToPass()).thenReturn(1);

        var result = new TestResult(new Student("Ivan", "Petrov"));
        result.applyAnswer(new Question("Q", List.of()), true);

        resultService.showResult(result);

        verify(ioService).printLineLocalized("ResultService.passed.test");
        verify(ioService, never()).printLineLocalized("ResultService.fail.test");
    }

    @Test
    void shouldPrintFailMessageWhenNotEnoughRightAnswers() {
        when(testConfig.getRightAnswersCountToPass()).thenReturn(2);

        var result = new TestResult(new Student("Ivan", "Petrov"));
        result.applyAnswer(new Question("Q", List.of()), true);

        resultService.showResult(result);

        verify(ioService).printLineLocalized("ResultService.fail.test");
    }
}
