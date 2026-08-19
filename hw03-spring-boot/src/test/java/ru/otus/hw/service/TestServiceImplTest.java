package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;


@ExtendWith(MockitoExtension.class)
public class TestServiceImplTest {

    @Mock
    private LocalizedIOService ioService;

    @Mock
    private QuestionDao questionDao;

    @InjectMocks
    private TestServiceImpl service;

    @Test
    void shouldDisplayQuestionsWithAnswers() {

        var student = mock(Student.class);
        List<Answer> answers = new ArrayList<>();
        answers.add(new Answer("Here we go, here we go, here we go again", true));
        answers.add(new Answer("5 dollars", false));
        List<Question> questions = List.of(new Question("How much is the fish?", answers
        ));

        when(questionDao.findAll()).thenReturn(questions);
        var prompt = "Enter a number between 1 and 2:";
        var error = "Input is not a number or not in range between 1 and 2";

        when(ioService.getMessage("TestService.answer.prompt", 2)).thenReturn(prompt);
        when(ioService.getMessage("TestService.answer.error", 2)).thenReturn(error);
        when(ioService.getMessage(anyString())).thenAnswer(invocation -> {
            var code = invocation.getArgument(0, String.class);
            return switch (code) {
                case "TestService.answer.correct" -> "Right";
                case "TestService.answer.wrong" -> "Wrong";
                default -> code;
            };
        });
        when(ioService.readIntForRangeWithPrompt(1, 2, prompt, error))
                .thenReturn(1);

        service.executeTestFor(student);

        InOrder inOrder = inOrder(ioService);

        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printLineLocalized("TestService.answer.the.questions");
        inOrder.verify(ioService).printLine("How much is the fish?");
        inOrder.verify(ioService, times(2)).printFormattedLine(anyString(), anyInt(), anyString());
        inOrder.verify(ioService).readIntForRangeWithPrompt(1, 2, prompt, error);
        inOrder.verify(ioService).printFormattedLine(anyString(), anyString(), anyString(), anyString());
        inOrder.verifyNoMoreInteractions();
    }

}
