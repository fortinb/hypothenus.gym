package com.iso.hypo.brand.application.dto;

import java.util.Date;
import java.util.List;

import com.iso.hypo.common.application.dto.BaseEntityDto;
import com.iso.hypo.common.application.dto.LocalizedStringDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseDto extends BaseEntityDto {

    private String brandUuid;
    
    private String uuid;

    private String code;

    private List<LocalizedStringDto> name;

    private List<LocalizedStringDto> description;

    private Date startDate;

    private Date endDate;
}