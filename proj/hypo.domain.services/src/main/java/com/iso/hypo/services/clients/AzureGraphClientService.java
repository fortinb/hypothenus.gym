package com.iso.hypo.services.clients;

import java.util.Optional;

import com.microsoft.graph.models.AppRole;
import com.microsoft.graph.models.AppRoleAssignment;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.User;

public interface AzureGraphClientService {
	
	User createUser(User user) throws Exception;

	User updateUser(User user) throws Exception;
	
	Optional<User> userExists(String email) throws Exception;
	
	Optional<User> findUser(String userId) throws Exception;
	
	void deleteUser(String userId) throws Exception;
	
	AppRole assignRole(String userId, String roleName) throws Exception;
	
	void unassignRole(String userId, String roleName) throws Exception;

	AppRole getRole(AppRoleAssignment appRoleAssignment) throws Exception;
	
	boolean isMemberOfGroup(String userId, String groupName) throws Exception;

	Group addToGroup(String userId, String groupName) throws Exception;
	
	void removeFromGroup(String userId, String groupName) throws Exception;
	
	Group createGroup(String groupName, String groupDescription) throws Exception;
	
	void deleteAllGroup() throws Exception;
	
	void deleteAllUser() throws Exception;


}
