package com.jps.libraryapi.api.exceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.validation.BindingResult;

import com.jps.libraryapi.exception.BusinessException;

public class ApiErrors {

	private List<String> errors;
	
	public ApiErrors(BindingResult bindingResult) {
		this.errors = new ArrayList<>();		
		bindingResult.getAllErrors().forEach( error -> this.errors.add(error.getDefaultMessage()) );		
	}
	
	public ApiErrors(BusinessException ex) {
		this.errors = Arrays.asList(ex.getMessage());
	}
	
	public List<String> getErrors(){
		return errors;
	}
}
