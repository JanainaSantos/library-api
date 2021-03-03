package com.jps.libraryapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.jps.libraryapi.exception.BusinessException;
import com.jps.libraryapi.model.entity.Book;
import com.jps.libraryapi.model.repository.BookRepository;
import com.jps.libraryapi.service.impl.BookServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("teste")
public class BookServiceTeste {
	
	BookService service;
	
	@MockBean
	BookRepository repository;
	
	@BeforeEach
	public void setUp() {
		this.service = new BookServiceImpl( repository );
	} 
	
	@Test
	@DisplayName("Deve salvar um livro")
	public void saveBookTest() {
		//cenario
		Book book = createValidBook();
		Mockito.when( repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
		
		Mockito.when( repository.save(book) ).thenReturn(
							Book.builder().id(11)
							.isbn("123")
							.author("Janaina")
							.title("As aventuras").build()
						);
		
		//execucao
		Book savedBook = service.save(book);
		
		//verificacao
		assertThat(savedBook.getId()).isNotNull();
		assertThat(savedBook.getIsbn()).isEqualTo("123");
		assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
		assertThat(savedBook.getAuthor()).isEqualTo("Janaina");
	}	

	@Test
	@DisplayName("Deve lançar erro de negocio ao tentar salvar um livro com isbn duplicado")
	public void shouldNotSaveBookWitDupolicatedIsbnTest() {
		//cenario
		Book book = createValidBook();
		Mockito.when( repository.existsByIsbn(Mockito.anyString())).thenReturn(true);
		
		//execucao
		Throwable exception = Assertions.catchThrowable(() -> service.save(book));
		
		//verificacao
		assertThat(exception)
			.isInstanceOf(BusinessException.class)
			.hasMessage("Isbn já cadastrado");
		
		Mockito.verify(repository, Mockito.never()).save(book);
	}
	
	private Book createValidBook() {
		return Book.builder().isbn("123").author("Janaina").title("As aventuras").build();
	}
}
