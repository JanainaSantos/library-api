package com.jps.libraryapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.stereotype.Service;

import com.jps.libraryapi.exception.BusinessException;
import com.jps.libraryapi.model.entity.Book;
import com.jps.libraryapi.model.repository.BookRepository;
import com.jps.libraryapi.service.BookService;

@Service
public class BookServiceImpl implements BookService {

	private BookRepository repository;
	
	public BookServiceImpl(BookRepository repository) {
		this.repository = repository;
	}
	
	@Override
	public Book save(Book book) {
		if( repository.existsByIsbn(book.getIsbn()) ) {
			throw new BusinessException("Isbn já cadastrado");
		}
		return repository.save(book);
	}

}
