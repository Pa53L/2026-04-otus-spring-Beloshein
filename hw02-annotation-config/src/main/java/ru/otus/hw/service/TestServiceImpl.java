package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.Collections;

@RequiredArgsConstructor
@Service
public class TestServiceImpl implements TestService {

    private static final String RESET = "\u001B[0m";

    private static final String GREEN = "\u001B[32m";

    private static final String RED = "\u001B[31m";

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question: questions) {
            var isAnswerValid = false;
            ioService.printLine(question.text());
            var answers = question.answers();
            Collections.shuffle(answers);
            int answerNumber = 1;
            for (var answer: answers) {
                ioService.printFormattedLine("\t%d. %s", answerNumber, answer.text());
                answerNumber++;
            }
            int userAnswer = printPromptAndError(answers.size());
            isAnswerValid = answers.get(userAnswer - 1).isCorrect();
            printResult(isAnswerValid);
            testResult.applyAnswer(question, isAnswerValid);
        }
        return testResult;
    }

    private void printResult(boolean correct) {
        ioService.printFormattedLine("%s%s%s", correct ? GREEN : RED, correct ? "Right" : "Wrong", RESET);
    }

    private int printPromptAndError(int answerSize) {
        return ioService.readIntForRangeWithPrompt(1, answerSize,
                "Enter a number between 1 and " + answerSize + ": ",
                "Input is not a number or not in range between 1 and " + answerSize
        );
    }
}
