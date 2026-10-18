delete from comments;
delete from books_genres;
delete from books;
delete from genres;
delete from authors;
alter sequence authors_id_seq restart with 1;
alter sequence genres_id_seq restart with 1;
alter sequence books_id_seq restart with 1;
alter sequence comments_id_seq restart with 1;
