package com.jps.libraryapi.api.resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jps.libraryapi.api.dto.BookDTO;
import com.jps.libraryapi.exception.BusinessException;
import com.jps.libraryapi.model.entity.Book;
import com.jps.libraryapi.service.BookService;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("teste")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {
	
	static String BOOK_API = "/api/books";

	@Autowired
	MockMvc mvc;
	
	@MockBean
	BookService service;
	
	@Test
	@DisplayName("Deve criar um livro com sucesso")
	public void createBookTest() throws Exception {
		
		BookDTO bookDTO = createNewBook();
		Book savedBook = Book.builder().id(101).author("Janaina").title("Maravilha").isbn("001").build();
		
		BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);
		
		String json = new ObjectMapper().writeValueAsString(bookDTO);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.post(BOOK_API)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content(json);
		
		mvc.perform(request)
			.andExpect( status().isCreated() )
			.andExpect( jsonPath( "id" ).value(101) )
			.andExpect( jsonPath( "title" ).value(bookDTO.getTitle()) )
			.andExpect( jsonPath( "author" ).value(bookDTO.getAuthor()) )
			.andExpect( jsonPath( "isbn" ).value(bookDTO.getIsbn()) )
		;
		
	}
	
	
	@Test
	@DisplayName("Deve lançar erro de validação quando não houver dados suficiente para criação do livro")
	public void createInvalidBookTest() throws Exception {
		
		String json = new ObjectMapper().writeValueAsString(new BookDTO());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
			.andExpect( status().isBadRequest() )
			.andExpect( jsonPath( "errors", Matchers.hasSize(3) ))
		;
	}
	
	@Test
	@DisplayName("Deve lançar erro ao tentar cadastrar um livro com ISBN já utilizado por outro.")
	public void createBookWithDuplicatedIsbnTest() throws Exception {
	
		BookDTO dto = createNewBook();
		String json = new ObjectMapper().writeValueAsString(dto);		
		String mensagemErro = "Isbn já cadastrado";
		//sumilação da classe de serviço
		BDDMockito.given(service.save(Mockito.any(Book.class)))
					.willThrow(new BusinessException(mensagemErro));
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
			.andExpect( status().isBadRequest() )
			.andExpect( jsonPath( "errors", Matchers.hasSize(1)))
			.andExpect( jsonPath( "errors[0]").value(mensagemErro))
		;
		
	}
	
	@Test
	@DisplayName("Deve obter informações de um livro.")
	public void getBookDetailsTest() throws Exception {
		//cenario
		int id = 11;
		
		Book book = Book.builder()
					.id(id)
					.title(createNewBook().getTitle())
					.author(createNewBook().getAuthor())
					.isbn(createNewBook().getIsbn())
					.build();
		
		BDDMockito.given(service.getBookByID(id)).willReturn(Optional.of(book));
		
		//execucao (when)
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.get(BOOK_API.concat("/"+id))
			.accept(MediaType.APPLICATION_JSON);
		
		mvc.perform(request)
			.andExpect(status().isOk())
			.andExpect( jsonPath( "id" ).value(id) )
			.andExpect( jsonPath( "title" ).value(createNewBook().getTitle()) )
			.andExpect( jsonPath( "author" ).value(createNewBook().getAuthor()) )
			.andExpect( jsonPath( "isbn" ).value(createNewBook().getIsbn()) )
			
			;
		
	}
	
	@Test
	@DisplayName("Deve retornar resouce not found quando o livro procurado não existir.")
	public void bookNotFoundTest() throws Exception {
		//cenario
		int id = 1;
		
		BDDMockito.given(service.getBookByID(Mockito.anyInt())).willReturn(Optional.empty());
		
		//execucao (when)
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.get(BOOK_API.concat("/" + id))
			.accept(MediaType.APPLICATION_JSON);
		
		mvc.perform(request)
			.andExpect(status().isNotFound());
		
	}
	
	@Test
	@DisplayName("Deve deletar um livro.")
	public void deleteBookTest() throws Exception {
	
		//cenario
		int id = 11;
		
		BDDMockito.given(service.getBookByID(anyInt())).willReturn(Optional.of(Book.builder().id(id).build()));
		
		//execucao (when)
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.delete(BOOK_API.concat("/" + id));
		
		mvc.perform(request)
			.andExpect(status().isNoContent());
				
	}
	
	@Test
	@DisplayName("Deve retornar resource not found quando não encontrar o livro para deletar.")
	public void deleteInexistenteBookTest() throws Exception {
	
		//cenario
		int id = 11;
		
		BDDMockito.given(service.getBookByID(anyInt())).willReturn(Optional.empty());
		
		//execucao (when)
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.delete(BOOK_API.concat("/" + id));
		
		mvc.perform(request)
			.andExpect(status().isNotFound());
				
	}
	
	private BookDTO createNewBook() {
		return BookDTO.builder().author("Janaina").title("Maravilha").isbn("001").build();
	}
}
