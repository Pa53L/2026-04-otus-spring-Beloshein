package ru.otus.hw.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ShellExamples implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        log.info("""

                Команды библиотеки:
                  aa                                  — список авторов и их id
                  abid 1                              — найти автора по id
                  ag                                  — список жанров и их id
                  ab                                  — список книг и их id
                  cbbid 1                             — комментарии к книге с id 1
                  bins "Новая книга" 1 1,6             — добавить книгу: название, id автора, id жанров
                  cins "Отличная книга" 1              — добавить комментарий: текст, id книги
                  bupd 1 "Новое название" 2 3,4         — изменить книгу: id, название, id автора, id жанров
                  cupd 1 "Новый текст" 1               — изменить комментарий: id, текст, id книги
                  help                                — справка по всем командам
                Подставьте нужные id из списков. Для комментария к новой книге используйте id из ответа bins.
                Текст с пробелами заключайте в двойные кавычки; id жанров разделяйте запятыми без пробелов.
                Авторы и жанры загружаются из начальных данных.
                """);
        logCompleteBookExample();
    }

    private void logCompleteBookExample() {
        log.info("""

                Пример: книга с автором 1, жанрами 1 и 6 и двумя комментариями.
                Скопируйте команды ниже и выполните по порядку сразу после запуска:

                bins "Гиперион" 1 1,6
                cins "Шраааайк!!!" 4
                cins "Кажется, техноцентр близко" 4
                bbid 4
                cbbid 4

                В начальной БД три книги, поэтому первая добавленная книга получит id 4.
                Если уже добавляли книги, заменить 4 на id, который вернула команда bins.
                bbid выводит книгу с автором и жанрами, cbbid — её комментарии.
                """);
    }
}
