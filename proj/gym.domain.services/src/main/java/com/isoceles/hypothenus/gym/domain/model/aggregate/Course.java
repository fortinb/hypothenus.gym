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

	private Instant activatedOn;

	private Instant deactivatedOn;

	public Course() {
	}

	public Course(String gymId, String code, List<LocalizedString> name, List<LocalizedString> description,
			boolean isActive, Instant startedOn, Instant endedOn) {
		super(isActive);
		this.gymId = gymId;
		this.code = code;
		this.name = name;
		this.description = description;
		this.activatedOn = startedOn;
		this.deactivatedOn = endedOn;
	}
}
