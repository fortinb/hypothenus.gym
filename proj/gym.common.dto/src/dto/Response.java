package com.isoceles.hypothenus.gym.admin.papi.dto;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Response<T> {
	private T data;
	private Set<Error> errors;
	private Set<Message> messages;

}
