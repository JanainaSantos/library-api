package com.jps.libraryapi.model.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.jps.libraryapi.model.entity.Book;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("teste")
@DataJpaTest
public class BookRepositoryTest {
	
	@Autowired
	TestEntityManager entityManager;
	
	@Autowired
	BookRepository repository;
	
	@Test
	@DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
	public void returnTrueWhenIsbnExists() {
		//cenario		
		String isbn = "123";
		Book book = createNewBook(isbn);
		//Book.builder().title("As aventuras").author("Janaina").isbn(isbn).build();
		entityManager.persist(book);
		
		//execucao
		boolean exists = repository.existsByIsbn(isbn);
		
		//verificacao
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("Deve retornar falso quando não existir um livro na base com o isbn informado")
	public void returnFalseWhenIsbnDontExists() {
		//cenario		
		String isbn = "123";
		
		//execucao
		boolean exists = repository.existsByIsbn(isbn);
		
		//verificacao
		assertThat(exists).isFalse();
	}
	
	
	@Test
	@DisplayName("Deve obter um livro por id.")
	public void findByIdTest() {
		//cenario		
		Book book = createNewBook("123");
		entityManager.persist(book);
		
		//execucao
		Optional<Book> foundBook = repository.findById(book.getId());
		
		//verificacao
		assertThat(foundBook.isPresent()).isTrue();
	}
	
	@Test
	@DisplayName("Deve salvar um livro.")
	public void saveBookTest() {
		//cenario		
		Book book = createNewBook("123");

		Book savedBook = repository.save(book);
		
		assertThat(savedBook.getId()).isNotNull();		
		
	}
	
	@Test
	@DisplayName("Deve deletar um livro.")
	public void deleteBookTest() {
		Book book = createNewBook("123");
		entityManager.persist(book);
		
		Book foundBook = entityManager.find(Book.class, book.getId());

		repository.delete(foundBook);
		
		Book deletedBook = entityManager.find(Book.class, book.getId());
		assertThat(deletedBook).isNull();		
		
	}
	
	private Book createNewBook(String isbn) {
		return Book.builder().title("As aventuras").author("Janaina").isbn(isbn).build();
	}
}
