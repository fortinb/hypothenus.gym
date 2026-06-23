package com.iso.hypo.finance.application.port;

import java.util.Optional;

import com.iso.hypo.finance.application.port.dto.MemberRef;

public interface MemberServicePort {

	Optional<MemberRef> find(String brandUuid, String memberUuid);
	
    boolean memberDeleted(String brandUuid, String memberUuid);
}