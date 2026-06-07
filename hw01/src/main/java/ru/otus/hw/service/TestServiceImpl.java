package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");

        var questionList = questionDao.findAll();
        questionList.forEach(question -> {
                    ioService.printFormattedLine(question.text());
                    var answers = question.answers();
                    answers.forEach(answer ->
                                ioService.printFormattedLine("\t%s", answer.text())

                    );
                }
        );
    }
}
