package com.iso.hypo.brand.domain.model;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.iso.hypo.common.domain.model.BaseEntity;
import com.iso.hypo.common.domain.model.LocalizedString;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Course extends BaseEntity {

	private String id;
	
	private String brandUuid;
	
	private String uuid;

	private String code;

	private List<LocalizedString> name;

	private List<LocalizedString> description;
	
	private Date startDate;
	
	private Date endDate;

	public Course() {
	}
 
	public Course(String brandUuid, String code, List<LocalizedString> name, List<LocalizedString> description,
			Date startDate, Date endDate, boolean active, Instant activatedOn, Instant deactivatedOn) {
		super(active);
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