package com.iso.hypo.services.clients;

import java.util.Optional;

import com.iso.hypo.domain.dto.MemberDto;
import com.microsoft.graph.models.AppRole;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.User;

public interface AzureGraphClientService {
	User createUser(MemberDto memberDto, String password) throws Exception;
	
	AppRole assignRoleToUser(String userId, String roleName) throws Exception;
	
	Optional<User> userExists(String email) throws Exception;

	boolean isUserMemberOfGroup(String userId, String groupName) throws Exception;

	void deleteUser(String userId) throws Exception;

	Group assignUserToGroup(String userId, String groupName) throws Exception;
	
	Group createGroup(String groupName, String groupDescription) throws Exception;
	
	void deleteAllGroup() throws Exception;
	
	void deleteAllUser() throws Exception;
}
