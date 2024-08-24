package com.isoceles.hypothenus.gym.domain.model.aggregate;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
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
	
	private Date startDate;
	
	private Date endDate;
	
	@DBRef
	private List<Coach> coachs;

	public Course() {
	}

	public Course(String gymId, String code, List<LocalizedString> name, List<LocalizedString> description,
			List<Coach> coachs, Date startDate, Date endDate, boolean isActive, Instant activatedOn, Instant deactivatedOn) {
		super(isActive);
		this.gymId = gymId;
		this.code = code;
		this.name = name;
		this.description = description;
		this.coachs = coachs;
		this.startDate = startDate;
		this.endDate = endDate;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
