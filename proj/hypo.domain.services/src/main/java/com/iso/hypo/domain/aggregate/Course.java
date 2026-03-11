package com.iso.hypo.domain.aggregate;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.domain.BaseEntity;
import com.iso.hypo.domain.LocalizedString;
import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("course")
public class Course extends BaseEntity {

	@Id
	private String id;
	
	@Indexed
	@NonNull
	private String brandUuid;
	
	@Indexed
	private String uuid;

	@NonNull
	private String code;

	private List<LocalizedString> name;

	private List<LocalizedString> description;
	
	private Date startDate;
	
	private Date endDate;

	public Course() {
	}
 
	public Course(String brandUuid, String code, List<LocalizedString> name, List<LocalizedString> description,
			Date startDate, Date endDate, boolean isActive, Instant activatedOn, Instant deactivatedOn) {
		super(isActive);
		this.brandUuid = brandUuid;
		this.code = code;
		this.name = name;
		this.description = description;
		this.startDate = startDate;
		this.endDate = endDate;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}