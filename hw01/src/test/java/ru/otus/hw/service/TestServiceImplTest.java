package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

public class TestServiceImplTest {

    @Test
    void shouldPrintQuestionsWithAnswers() {
        try(var context = new ClassPathXmlApplicationContext("spring-test-context.xml")) {
            var testService = context.getBean(TestService.class);
            var questionDao = context.getBean(QuestionDao.class);
            var ioService = context.getBean(IOService.class);

            var questions = new ArrayList<Question>();
            var question = new Question("How much is the fish?", List.of(
                    new Answer("Here we go, here we go, here we go again", true),
                    new Answer("5 dollars", false)
                )
            );
            questions.add(question);

            when(questionDao.findAll()).thenReturn(questions);

            testService.executeTest();

            InOrder inOrder = inOrder(ioService, questionDao);
            inOrder.verify(ioService).printLine("");
            inOrder.verify(ioService).printFormattedLine("Please answer the questions below%n");
            inOrder.verify(questionDao).findAll();
            inOrder.verify(ioService).printFormattedLine("How much is the fish?");
            inOrder.verify(ioService).printFormattedLine("\t%s", "Here we go, here we go, here we go again");
            inOrder.verify(ioService).printFormattedLine("\t%s", "5 dollars");
            inOrder.verifyNoMoreInteractions();

        }
    }
}
