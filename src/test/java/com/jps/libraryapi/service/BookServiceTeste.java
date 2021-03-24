package com.jps.libraryapi.service;

import com.jps.libraryapi.exception.BusinessException;
import com.jps.libraryapi.model.entity.Book;
import com.jps.libraryapi.model.repository.BookRepository;
import com.jps.libraryapi.service.impl.BookServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
							Book.builder().id(1l)
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
	
	@Test
	@DisplayName("Deve obter um livro por ID")
	public void getByIdTest() {
		Long id = 1L;
		
		Book book = createValidBook();
		book.setId(id);
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));
		
		//execucao
		Optional<Book> foundBook = service.getById(id);
		
		assertThat(foundBook.isPresent()).isTrue();
		assertThat(foundBook.get().getId()).isEqualTo(id);
		assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
		assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
		assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
		
	}
	
	@Test
	@DisplayName("Deve retornar vazio ao obter um livro por ID quando ele não existe")
	public void bookNotFoundByIdTest() {
		Long id = 1L;
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
		
		//execucao
		Optional<Book> book = service.getById(id);
		
		assertThat(book.isPresent()).isFalse();
		
	}
	
	@Test
	@DisplayName("Deve deletar um livro.")
	public void deleteBookTest() {
		Long id = 1L;
		
		Book book = createValidBook();
		book.setId(id);
		
		//execucao
		assertDoesNotThrow(() -> service.delete(book));
		
		//verificação
		Mockito.verify(repository, Mockito.times(1)).delete(book);
		
	}
	
	@Test
	@DisplayName("Deve ocorrer erro ao tentar deletar um livro inexistente.")
	public void deleteInvalidBookTest() {
		Book book = new Book();
		
		assertThrows(IllegalArgumentException.class, () -> service.delete(book));
		
		//verificação
		Mockito.verify(repository, Mockito.never()).delete(book);
		
	}
	
	@Test
	@DisplayName("Deve atualizar um livro.")
	public void updateBookTest() {
		Long id = 1L;
		
		//livro para atualizar
		Book updatingBook = Book.builder().id(id).build();
		
		//livro simulação
		Book updatedBook = createValidBook();
		updatedBook.setId(id);
		
		Mockito.when(repository.save(updatingBook)).thenReturn(updatedBook);
		
		//execucao
		Book book = service.update(updatingBook);
		
		//verificação
		assertThat(book.getId()).isEqualTo(updatedBook.getId());
		assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
		assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
		assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
		
	}
	
	@Test
	@DisplayName("Deve ocorrer erro ao tentar alterar um livro inexistente.")
	public void updateInvalidBookTest() {
		Book book = new Book();
		
		assertThrows(IllegalArgumentException.class, () -> service.update(book));
		
		//verificação
		Mockito.verify(repository, Mockito.never()).save(book);
			
	}
	
	@Test
	@DisplayName("Deve filtrar livros pelas propriedades.")
	public void findBookTest() {
		
		Book book = createValidBook();
		
		PageRequest pageRequest = PageRequest.of(0, 10);
		List<Book> lista = Arrays.asList(book);
				
		Page<Book> page = new PageImpl<Book>(lista, pageRequest, 1);
		
		Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
			.thenReturn(page);
		
		//execucao
		Page<Book> result = service.find(book, pageRequest);
		
		//verificacoes
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
	}
	
	private Book createValidBook() {
		return Book.builder().isbn("123").author("Janaina").title("As aventuras").build();
	}
}
