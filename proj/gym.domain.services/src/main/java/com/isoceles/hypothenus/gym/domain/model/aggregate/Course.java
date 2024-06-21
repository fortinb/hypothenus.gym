package com.isoceles.hypothenus.gym.domain.model.aggregate;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.isoceles.hypothenus.gym.domain.model.LocalizedString;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("course")
public class Course extends BaseEntity {

	@Id
	private String id;

	@Indexed
	private String gymId;

	private String code;

	private List<LocalizedString> name;

	private List<LocalizedString> description;

	private boolean isActive;

	private Instant startedOn;

	private Instant endedOn;

	public Course() {
	}

	public Course(String gymId, String code, List<LocalizedString> name, List<LocalizedString> description, boolean isActive, Instant startedOn,
			Instant endedOn) {
		super();
		this.gymId = gymId;
		this.code = code;
		this.name = name;
		this.description = description;
		this.isActive = isActive;
		this.startedOn = startedOn;
		this.endedOn = endedOn;
	}
}
