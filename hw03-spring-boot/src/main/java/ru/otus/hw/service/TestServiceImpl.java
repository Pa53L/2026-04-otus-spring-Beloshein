package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private static final String RESET = "\u001B[0m";

    private static final String GREEN = "\u001B[32m";

    private static final String RED = "\u001B[31m";

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {

        printGreetingMessage();

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
        var color = correct ? GREEN : RED;
        var messageCode = correct ? "TestService.answer.correct" : "TestService.answer.wrong";
        ioService.printFormattedLine("%s%s%s", color, ioService.getMessage(messageCode), RESET);
    }

    private int printPromptAndError(int answerSize) {
        return ioService.readIntForRangeWithPrompt(1, answerSize,
                ioService.getMessage("TestService.answer.prompt", answerSize),
                ioService.getMessage("TestService.answer.error", answerSize)
        );
    }

    private void printGreetingMessage() {
        ioService.printLine("");
        ioService.printLineLocalized("TestService.answer.the.questions");
        ioService.printLine("");
    }

}
