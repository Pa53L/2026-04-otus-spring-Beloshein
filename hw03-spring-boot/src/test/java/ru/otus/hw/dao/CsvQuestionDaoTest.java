package ru.otus.hw.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CsvQuestionDaoTest {

    @Mock
    private TestFileNameProvider fileNameProvider;

    @InjectMocks
    private CsvQuestionDao dao;

    @Test
    void shouldReturnQuestionsListWhenFileExist() {
        List<Question> expectedQuestions = List.of(
                new Question(
                        "What is 2 + 2?",
                        List.of(
                                new Answer("4", true),
                                new Answer("5", false)
                        )
                ),
                new Question(
                        "Is Java a programming language?",
                        List.of(
                                new Answer("Yes", true),
                                new Answer("No", false)
                        )
                )
        );

        when(fileNameProvider.getTestFileName()).thenReturn("questions-test.csv");
        assertThat(dao.findAll()).containsExactlyElementsOf(expectedQuestions);
    }

    @Test
    void shouldThrowExceptionWithIncorrectFileName() {
        when(fileNameProvider.getTestFileName()).thenReturn("non-existing-file.csv");

        var exception = assertThrows(QuestionReadException.class, () -> dao.findAll());
        assertEquals("CSV resource was not found: non-existing-file.csv", exception.getMessage());
    }
}
