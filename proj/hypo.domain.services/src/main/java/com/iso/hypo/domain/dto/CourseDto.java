package com.iso.hypo.domain.dto;

import java.util.Date;
import java.util.List;

import com.iso.hypo.domain.LocalizedString;
import com.iso.hypo.common.dto.BaseEntityDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseDto extends BaseEntityDto {

    private String brandUuid;

    private String gymUuid;
    
    private String uuid;

    private String code;

    private List<LocalizedString> name;

    private List<LocalizedString> description;

    private Date startDate;

    private Date endDate;

    private List<CoachDto> coachs;

}
