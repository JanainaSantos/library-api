package com.jps.libraryapi.api.resource;

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
			.andExpect( MockMvcResultMatchers.status().isCreated() )
			.andExpect( MockMvcResultMatchers.jsonPath( "id" ).value(101) )
			.andExpect( MockMvcResultMatchers.jsonPath( "title" ).value(bookDTO.getTitle()) )
			.andExpect( MockMvcResultMatchers.jsonPath( "author" ).value(bookDTO.getAuthor()) )
			.andExpect( MockMvcResultMatchers.jsonPath( "isbn" ).value(bookDTO.getIsbn()) )
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
			.andExpect( MockMvcResultMatchers.status().isBadRequest() )
			.andExpect( MockMvcResultMatchers.jsonPath( "errors", Matchers.hasSize(3) ))
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
			.andExpect( MockMvcResultMatchers.status().isBadRequest() )
			.andExpect( MockMvcResultMatchers.jsonPath( "errors", Matchers.hasSize(1)))
			.andExpect( MockMvcResultMatchers.jsonPath( "errors[0]").value(mensagemErro))
		;
		
	}
	
	private BookDTO createNewBook() {
		return BookDTO.builder().author("Janaina").title("Maravilha").isbn("001").build();
	}
}
