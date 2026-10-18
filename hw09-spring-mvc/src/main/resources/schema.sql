create sequence authors_id_seq start with 1 increment by 1;
create sequence genres_id_seq start with 1 increment by 1;
create sequence books_id_seq start with 1 increment by 1;
create sequence comments_id_seq start with 1 increment by 1;

create table authors (
    id bigint default next value for authors_id_seq,
    full_name varchar(255) not null,
    primary key (id)
);

create table genres (
    id bigint default next value for genres_id_seq,
    name varchar(255) not null,
    primary key (id)
);

create table books (
    id bigint default next value for books_id_seq,
    title varchar(255) not null,
    author_id bigint not null,
    primary key (id),
    foreign key (author_id) references authors(id)
);

create table books_genres (
    book_id bigint not null,
    genre_id bigint not null,
    primary key (book_id, genre_id),
    foreign key (book_id) references books(id) on delete cascade,
    foreign key (genre_id) references genres(id) on delete cascade
);

create table comments (
    id bigint default next value for comments_id_seq,
    text varchar(255) not null,
    book_id bigint not null,
    primary key (id),
    foreign key (book_id) references books(id) on delete cascade
);