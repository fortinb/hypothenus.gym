package com.iso.hypo.gym.dto;

import java.util.Date;
import java.util.List;

import com.iso.hypo.common.domain.LocalizedString;
import com.iso.hypo.common.dto.BaseEntityDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseDto extends BaseEntityDto {

    private String id;

    private String brandId;

    private String gymId;

    private String code;

    private List<LocalizedString> name;

    private List<LocalizedString> description;

    private Date startDate;

    private Date endDate;

    private List<CoachDto> coachs;

}