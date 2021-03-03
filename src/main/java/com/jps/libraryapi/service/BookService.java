package com.jps.libraryapi.service;

import java.util.Optional;
import com.jps.libraryapi.model.entity.Book;

public interface BookService {

	Book save(Book any);

	Optional<Book> getBookByID(int id);

	void delete(Book book);

}
