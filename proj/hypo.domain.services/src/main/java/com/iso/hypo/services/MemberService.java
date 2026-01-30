package com.iso.hypo.services;

import com.iso.hypo.domain.dto.MemberDto;
import com.iso.hypo.services.exception.MemberException;

public interface MemberService {

    MemberDto create(MemberDto memberDto) throws MemberException;

    MemberDto update(MemberDto memberDto) throws MemberException;

    MemberDto patch(MemberDto memberDto) throws MemberException;

    void delete(String brandUuid, String memberUuid) throws MemberException;

    MemberDto activate(String brandUuid, String memberUuid) throws MemberException;

    MemberDto deactivate(String brandUuid, String memberUuid) throws MemberException;
    
    void deleteAllByBrandUuid(String brandUuid) throws MemberException;
}