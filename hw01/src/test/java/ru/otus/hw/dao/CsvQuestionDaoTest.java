package ru.otus.hw.dao;

import org.junit.jupiter.api.Test;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CsvQuestionDaoTest {

    @Test
    void findAllShouldReturnAllQuestionsWhenResourceExists() {
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
        var dao = new CsvQuestionDao(() -> "questions-test.csv");
        assertThat(dao.findAll()).containsExactlyElementsOf(expectedQuestions);
    }

    @Test
    void findAllShouldThrowQuestionReadExceptionWhenResourceDoesNotExist() {
        var missingDao = new CsvQuestionDao(() -> "missing-questions.csv");
        assertThatThrownBy(missingDao::findAll)
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining("missing-questions.csv");
    }

}
