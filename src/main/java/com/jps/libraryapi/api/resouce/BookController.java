package com.jps.libraryapi.api.resouce;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.jps.libraryapi.api.dto.BookDTO;
import com.jps.libraryapi.api.exceptions.ApiErrors;
import com.jps.libraryapi.exception.BusinessException;
import com.jps.libraryapi.model.entity.Book;
import com.jps.libraryapi.service.BookService;

@RestController
@RequestMapping("/api/books")
public class BookController {

	private BookService service;
	private ModelMapper modelMapper;
	
	public BookController(BookService service, ModelMapper modelMapper) {
		this.service = service;
		this.modelMapper = modelMapper;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BookDTO create( @RequestBody @Valid BookDTO bookDTO ) {
		
		Book book = modelMapper.map( bookDTO, Book.class);
		book = service.save(book);		
		BookDTO bookR = modelMapper.map( book, BookDTO.class);
		return bookR;
	}
	
	@GetMapping("{id}")
	public BookDTO get( @PathVariable Long id) {		
		return service
					.getById(id).map(book -> modelMapper.map( book, BookDTO.class))
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}
	
	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete( @PathVariable Long id) {	
		Book book = service
					.getById(id)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		
		service.delete(book);		
	}
	
	@PutMapping("{id}")
	public BookDTO update( @PathVariable Long id, BookDTO bookDTO) {	
		return service
					.getById(id)
					.map( book -> {
						
						book.setAuthor(bookDTO.getAuthor());
						book.setTitle(bookDTO.getTitle());
						service.update(book);
						BookDTO bookR = modelMapper.map( book, BookDTO.class);
						return bookR;
						
					})
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		
		
		
		
	}
	
	public Page<BookDTO> find( BookDTO bookDTO, Pageable pageRequest ){
        Book filter = modelMapper.map(bookDTO, Book.class);
        Page<Book> result = service.find(filter, pageRequest);
        List<BookDTO> list = result.getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<BookDTO>( list, pageRequest, result.getTotalElements() );
    }
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrors handleValidationExceptions(MethodArgumentNotValidException ex) {
		BindingResult bindingResult = ex.getBindingResult();		
		return new ApiErrors(bindingResult);
	}
	
	@ExceptionHandler(BusinessException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrors handleBusinessExceptions(BusinessException ex) {
		return new ApiErrors(ex);
	}
	
}
