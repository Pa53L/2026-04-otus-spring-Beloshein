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
    private IOService ioService;

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
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1);

        service.executeTestFor(student);

        InOrder inOrder = inOrder(ioService);

        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printFormattedLine("Please answer the questions below%n");
        inOrder.verify(ioService).printLine("How much is the fish?");
        inOrder.verify(ioService, times(2)).printFormattedLine(anyString(), anyInt(), anyString());
        inOrder.verify(ioService).readIntForRangeWithPrompt(1, 2, "Enter a number between 1 and 2: ", "Input is not a number or not in range between 1 and 2");
        inOrder.verify(ioService).printFormattedLine(anyString(), anyString(), anyString(), anyString());
        inOrder.verifyNoMoreInteractions();
    }

}
