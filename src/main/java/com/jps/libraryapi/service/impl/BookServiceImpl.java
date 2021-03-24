package com.jps.libraryapi.service.impl;

import org.springframework.data.domain.*;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

	@Override
	public Optional<Book> getById(Long id) {
		return this.repository.findById(id);
	}

	@Override
	public void delete(Book book) {
		if(book == null || book.getId() == null) {
			throw new IllegalArgumentException("Book id cant be null");
		}
		repository.delete(book);
	}
	
	@Override
	public Book update(Book book) {
		if(book == null || book.getId() == null) {
			throw new IllegalArgumentException("Book id cant be null");
		}
				
		return repository.save(book);
	}

	@Override
    public Page<Book> find( Book filter, Pageable pageRequest ) {
        Example<Book> example = Example.of(filter,
                    ExampleMatcher
                            .matching()
                            .withIgnoreCase()
                            .withIgnoreNullValues()
                            .withStringMatcher( ExampleMatcher.StringMatcher.CONTAINING )
        ) ;
        return repository.findAll(example, pageRequest);
    }

}
