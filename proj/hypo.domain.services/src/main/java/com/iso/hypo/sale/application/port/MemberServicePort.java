package com.iso.hypo.sale.application.port;

import java.util.Optional;

import com.iso.hypo.sale.application.port.dto.MemberRef;

public interface MemberServicePort {

	Optional<MemberRef> find(String brandUuid, String memberUuid);
	
	boolean memberDeleted(String brandUuid, String memberUuid);
}