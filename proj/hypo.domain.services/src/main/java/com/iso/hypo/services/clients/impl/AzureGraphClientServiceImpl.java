package com.iso.hypo.services.clients.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.iso.hypo.services.clients.AzureGraphClientService;
import com.microsoft.graph.models.AppRole;
import com.microsoft.graph.models.AppRoleAssignment;
import com.microsoft.graph.models.Application;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.ReferenceCreate;
import com.microsoft.graph.models.ServicePrincipal;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.users.item.checkmembergroups.CheckMemberGroupsPostRequestBody;
import com.microsoft.graph.users.item.checkmembergroups.CheckMemberGroupsPostResponse;

public class AzureGraphClientServiceImpl implements AzureGraphClientService {

	private final GraphServiceClient graphClient;

	String clientId;
	String clientSecret;
	String tenantId;
	private String domainName;

	public AzureGraphClientServiceImpl(String clientId, String clientSecret, String tenantId, String domainName) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.tenantId = tenantId;
		this.domainName = domainName;

		ClientSecretCredential credential = new ClientSecretCredentialBuilder().clientId(clientId)
				.clientSecret(clientSecret).tenantId(tenantId).build();

		// REQUIRED scope for app‑only tokens
		String[] scopes = new String[] { "https://graph.microsoft.com/.default" };

		// Instantiate the GraphServiceClient
		graphClient = new GraphServiceClient(credential, scopes);
	}

	@Override
	public User createUser(User user) throws Exception {
		if (user == null) {
			throw new IllegalArgumentException("User must not be null");
		}
		
		user.setUserPrincipalName(String.format("%s@%s", user.getUserPrincipalName(), domainName));
		
		return graphClient.users().post(user);
	}
	
	@Override
	public User updateUser(User user) throws Exception {
		if (user == null) {
			throw new IllegalArgumentException("User must not be null");
		}
		
		return graphClient.users().byUserId(user.getId()).patch(user);
	}
	
	@Override
	public AppRole assignRole(String userId, String roleName) throws Exception {
		// Get application
		Application app = graphClient.applicationsWithAppId(clientId).get();

		// Find the role by name
		Optional<AppRole> roleMember = app.getAppRoles().stream().filter(r -> r.getDisplayName().equals(roleName))
				.findFirst();
		AppRole role = roleMember.orElseThrow(() -> new IllegalStateException("App role not found"));

		ServicePrincipal servicePrincipal = graphClient.servicePrincipals()
				.get(req -> req.queryParameters.filter = "appId eq '" + app.getAppId() + "'").getValue().stream()
				.findFirst().orElseThrow(
						() -> new IllegalStateException("Service principal not found for appId " + app.getAppId()));

		// Retrieve the user's role assignments and find the one matching the role to unassigns
		var assignmentsPage = graphClient.users().byUserId(userId).appRoleAssignments().get(req -> {
			req.queryParameters.filter = "appRoleId eq " + role.getId();
			req.queryParameters.top = 1;
			req.queryParameters.select = new String[] { "id" };
		});
				
		if (assignmentsPage != null && assignmentsPage.getValue() != null && !assignmentsPage.getValue().isEmpty()) {
			return role; // Assignment found, nothing to assigns
		}
		
		AppRoleAssignment assignment = new AppRoleAssignment();
		assignment.setPrincipalId(UUID.fromString(userId));
		assignment.setResourceId(UUID.fromString(servicePrincipal.getId()));
		assignment.setAppRoleId(role.getId());

		// 5. Assign role to user
		graphClient.users().byUserId(userId).appRoleAssignments().post(assignment);

		return role;
	}
	
	@Override
	public String getRoleName(AppRoleAssignment appRoleAssignment) throws Exception {
		// Get application
		Application app = graphClient.applicationsWithAppId(clientId).get();

		// Find the role by name
		Optional<AppRole> roleMember = app.getAppRoles().stream().filter(r -> r.getId().equals(appRoleAssignment.getAppRoleId()))
				.findFirst();
		AppRole role = roleMember.orElseThrow(() -> new IllegalStateException("App role not found"));
		
		return role.getValue();
	}
	
	@Override
	public void unassignRole(String userId, String roleName) throws Exception {
		// Get application
		Application app = graphClient.applicationsWithAppId(clientId).get();

		// Find the role by name
		Optional<AppRole> roleMember = app.getAppRoles().stream().filter(r -> r.getDisplayName().equals(roleName))
				.findFirst();
		AppRole role = roleMember.orElseThrow(() -> new IllegalStateException("App role not found"));
		
		// Retrieve the user's role assignments and find the one matching the role to unassigns
		var assignmentsPage = graphClient.users().byUserId(userId).appRoleAssignments().get(req -> {
			req.queryParameters.filter = "appRoleId eq " + role.getId();
			req.queryParameters.top = 1;
			req.queryParameters.select = new String[] { "id" };
		});

		if (assignmentsPage == null || assignmentsPage.getValue() == null || assignmentsPage.getValue().isEmpty()) {
			return; // No assignment found, nothing to unassigns
		}

		String assignmentId = assignmentsPage.getValue().get(0).getId();

		graphClient.users().byUserId(userId).appRoleAssignments().byAppRoleAssignmentId(assignmentId).delete();
	}

	@Override
	public Optional<User> userExists(String email) throws Exception {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email must not be null or blank");
		}

		// Escape single quotes for OData filter
		String safeEmail = email.trim().replace("'", "''");

		// Try both `mail` and `userPrincipalName`
		String filter = "mail eq '" + safeEmail + "' or userPrincipalName eq '" + safeEmail + "'";

		var page = graphClient.users().get(req -> {
			req.queryParameters.filter = filter;
			req.queryParameters.top = 1;
			req.queryParameters.select = new String[] { "id", "displayName", "mail" };
		});

		if (page == null || page.getValue() == null || page.getValue().isEmpty()) {
			return Optional.empty();
		}

		String userId = page.getValue().get(0).getId();
		if (userId == null || userId.isBlank()) {
			return Optional.empty();
		}

		return Optional.of(graphClient.users().byUserId(userId).get());
	}
	
	@Override
	public Optional<User> findUser(String userId) throws Exception {
		if (userId == null || userId.isBlank()) {
			throw new IllegalArgumentException("userId must not be null or blank");
		}
		
		// Get user
		Optional<User> user = Optional.of(graphClient.users().byUserId(userId).get());
		if (user.isEmpty()) {
			return Optional.empty();
		}
		
		// Retrieve roles
		var assignments = graphClient.users().byUserId(userId).appRoleAssignments().get();
		user.get().setAppRoleAssignments(assignments.getValue());
		
		return user;
	}

	@Override
	public boolean isMemberOfGroup(String userId, String groupName) throws Exception {
		if (userId == null || userId.isBlank()) {
			throw new IllegalArgumentException("userId must not be null or blank");
		}
		if (groupName == null || groupName.isBlank()) {
			throw new IllegalArgumentException("groupName must not be null or blank");
		}

		String safeName = groupName.trim().replace("'", "''");

		// 1) Resolve group by displayName
		var groupsPage = graphClient.groups().get(req -> {
			req.queryParameters.filter = "displayName eq '" + safeName + "'";
			req.queryParameters.top = 1;
			req.queryParameters.select = new String[] { "id" };
		});

		if (groupsPage == null || groupsPage.getValue() == null || groupsPage.getValue().isEmpty()) {
			return false;
		}

		String groupId = groupsPage.getValue().get(0).getId();
		if (groupId == null || groupId.isBlank()) {
			return false;
		}

		// 2) Check membership
		CheckMemberGroupsPostRequestBody body = new CheckMemberGroupsPostRequestBody();
		body.setGroupIds(List.of(groupId));

		CheckMemberGroupsPostResponse matched = graphClient.users().byUserId(userId).checkMemberGroups().post(body);

		return matched != null && !matched.getValue().isEmpty();
	}

	@Override
	public void deleteUser(String userId) throws Exception {
		graphClient.users().byUserId(userId).delete();
	}

	@Override
	public Group addToGroup(String userId, String groupName) {
		// 1) Resolve group by displayName
		var groupsPage = graphClient.groups().get(req -> {
			req.queryParameters.filter = "displayName eq '" + groupName.replace("'", "''") + "'";
			req.queryParameters.top = 1;
			req.queryParameters.select = new String[] { "id", "displayName" };
		});

		if (groupsPage == null || groupsPage.getValue() == null || groupsPage.getValue().isEmpty()) {
			throw new IllegalStateException("Group not found: " + groupName);
		}

		Group group = groupsPage.getValue().get(0);

		// 2) Add user to group: POST /groups/{groupId}/members/$ref
		ReferenceCreate ref = new ReferenceCreate();
		ref.setOdataId("https://graph.microsoft.com/v1.0/directoryObjects/" + userId);

		graphClient.groups().byGroupId(group.getId()).members().ref().post(ref);

		return group;
	}
	
	@Override
	public void removeFromGroup(String userId, String groupName) throws Exception {
		// 1) Resolve group by displayName
		var groupsPage = graphClient.groups().get(req -> {
			req.queryParameters.filter = "displayName eq '" + groupName.replace("'", "''") + "'";
			req.queryParameters.top = 1;
			req.queryParameters.select = new String[] { "id", "displayName" };
		});

		if (groupsPage == null || groupsPage.getValue() == null || groupsPage.getValue().isEmpty()) {
			throw new IllegalStateException("Group not found: " + groupName);
		}

		Group group = groupsPage.getValue().get(0);

		// 2) Remove user from group: DELETE /groups/{groupId}/members/{userId}/$ref
		graphClient.groups().byGroupId(group.getId()).members().byDirectoryObjectId(userId).ref().delete();

	}

	@Override
	public Group createGroup(String groupName, String groupDescription) throws Exception {
		Group newGroup = new Group();
		
		newGroup.setDisplayName(groupName);
		newGroup.setDescription(groupDescription);
		newGroup.setMailEnabled(false);
		newGroup.setSecurityEnabled(true);
		newGroup.setMailNickname(groupName);

		return graphClient.groups().post(newGroup);
	}

	@Override
	public void deleteAllGroup() throws Exception {
		// 1) List all groups
		var groupsPage = graphClient.groups().get(req -> {
			req.queryParameters.top = 1000;
			req.queryParameters.select = new String[] { "id" };
		});

		if (groupsPage == null || groupsPage.getValue() == null) {
			return;
		}

		// 2) Delete each group
		for (Group group : groupsPage.getValue()) {
			graphClient.groups().byGroupId(group.getId()).delete();
		}

	}

	@Override
	public void deleteAllUser() throws Exception {
		// 1) List all users
		var usersPage = graphClient.users().get(req -> {
			req.queryParameters.top = 1000;
			req.queryParameters.select = new String[] { "id" };
		});

		if (usersPage == null || usersPage.getValue() == null) {
			return;
		}

		// 2) Delete each user
		for (User user : usersPage.getValue()) {
			graphClient.users().byUserId(user.getId()).delete();
		}
	}
}
