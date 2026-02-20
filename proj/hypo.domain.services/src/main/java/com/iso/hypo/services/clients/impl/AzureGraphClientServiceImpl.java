package com.iso.hypo.services.clients.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
import com.microsoft.graph.models.ObjectIdentity;
import com.microsoft.graph.models.DirectoryRole;
import com.microsoft.graph.models.DirectoryObject;
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

		String upn = user.getUserPrincipalName();
		if (upn == null || upn.isBlank()) {
			throw new IllegalArgumentException("userPrincipalName must not be null or blank");
		}

		// Append tenant domain if caller provided only local part
		if (!upn.contains("@")) {
			upn = String.format("%s@%s", upn, domainName);
			user.setUserPrincipalName(upn);
		} else {
			user.setUserPrincipalName(upn);
		}

		// Ensure emailAddress sign-in identity exists so the user can sign in by email
		List<ObjectIdentity> identities = user.getIdentities();
		if (identities == null) {
			identities = new java.util.ArrayList<>();
			user.setIdentities(identities);
		}

		boolean hasEmailIdentity = identities.stream().anyMatch(i -> i != null && "emailAddress".equalsIgnoreCase(i.getSignInType()));
		if (!hasEmailIdentity) {
			ObjectIdentity emailIdentity = new ObjectIdentity();
			emailIdentity.setSignInType("emailAddress");
			// Use tenant domain as issuer so Graph treats this as a tenant email identity
			emailIdentity.setIssuer(domainName);
			emailIdentity.setIssuerAssignedId(user.getMail() != null && !user.getMail().isBlank() ? user.getMail() : upn);
			identities.add(emailIdentity);
		}

		// Optionally set the mail attribute if not provided
		if (user.getMail() == null || user.getMail().isBlank()) {
			user.setMail(upn);
		}

		return graphClient.users().post(user);
	}

	@Override
	public User updateUser(User user) throws Exception {
		if (user == null) {
			throw new IllegalArgumentException("User must not be null");
		}

		// Create a minimal patch object to avoid serializing read-only or server-managed
		// properties (for example: appRoleAssignments, id, createdDateTime, etc.)
		User patchUser = new User();

		if (user.getDisplayName() != null) {
			patchUser.setDisplayName(user.getDisplayName());
		}
		if (user.getGivenName() != null) {
			patchUser.setGivenName(user.getGivenName());
		}
		if (user.getSurname() != null) {
			patchUser.setSurname(user.getSurname());
		}
		if (user.getMail() != null) {
			patchUser.setMail(user.getMail());
		}
		if (user.getAccountEnabled() != null) {
			patchUser.setAccountEnabled(user.getAccountEnabled());
		}
		
		return graphClient.users().byUserId(user.getId()).patch(patchUser);
	}

	@Override
	public AppRole assignRole(String userId, String roleName) throws Exception {
		// Get application
		Application app = graphClient.applicationsWithAppId(clientId).get();

		// Find the role by name
		Optional<AppRole> roleMember = app.getAppRoles().stream().filter(r -> r.getValue().equals(roleName))
				.findFirst();
		AppRole role = roleMember.orElseThrow(() -> new IllegalStateException("App role not found"));

		ServicePrincipal servicePrincipal = graphClient.servicePrincipals()
				.get(req -> req.queryParameters.filter = "appId eq '" + app.getAppId() + "'").getValue().stream()
				.findFirst().orElseThrow(
						() -> new IllegalStateException("Service principal not found for appId " + app.getAppId()));

		// Retrieve the user's role assignments and verify if the role already exists to
		// avoid duplicate assignment
		var assignmentsPage = graphClient.users().byUserId(userId).appRoleAssignments().get(req -> {
			req.queryParameters.top = 999; // or page normally
			req.queryParameters.select = new String[] { "id", "appRoleId", "resourceId", "principalId" };
		});

		AppRoleAssignment existingAssignment = assignmentsPage.getValue().stream()
				.filter(a -> role.getId().equals(a.getAppRoleId())
						// Match by appRoleId and resourceId (service principal object id)
						&& a.getResourceId() != null && a.getResourceId().toString().equals(servicePrincipal.getId()))
				.findFirst().orElse(null);

		if (existingAssignment != null) {
			return role; // Assignment found, nothing to assigns
		}

		AppRoleAssignment assignment = new AppRoleAssignment();
		assignment.setPrincipalId(UUID.fromString(userId));
		assignment.setResourceId(UUID.fromString(servicePrincipal.getId()));
		assignment.setAppRoleId(role.getId());
		graphClient.users().byUserId(userId).appRoleAssignments().post(assignment);
		
		return role;
	}

	@Override
	public void unassignRole(String userId, String roleName) throws Exception {
		// Get application
		Application app = graphClient.applicationsWithAppId(clientId).get();

		// Find the role by name
		Optional<AppRole> roleMember = app.getAppRoles().stream().filter(r -> r.getValue().equals(roleName))
				.findFirst();
		AppRole role = roleMember.orElseThrow(() -> new IllegalStateException("App role not found"));

		// Find service principal for the app (needed to scope the assignment)
		ServicePrincipal servicePrincipal = graphClient.servicePrincipals()
				.get(req -> req.queryParameters.filter = "appId eq '" + app.getAppId() + "'").getValue().stream()
				.findFirst().orElseThrow(
						() -> new IllegalStateException("Service principal not found for appId " + app.getAppId()));

		// Retrieve the user's role assignments and find the one matching the role to unassigns
		var assignmentsPage = graphClient.users().byUserId(userId).appRoleAssignments().get(req -> {
			req.queryParameters.top = 999; // or page normally
			req.queryParameters.select = new String[] { "id", "appRoleId", "resourceId", "principalId" };
		});

		AppRoleAssignment existingAssignment = assignmentsPage.getValue().stream()
				.filter(a -> role.getId().equals(a.getAppRoleId())
						// Match by appRoleId and resourceId (service principal object id)
						&& a.getResourceId() != null && a.getResourceId().toString().equals(servicePrincipal.getId()))
				.findFirst().orElse(null);

		if (existingAssignment == null) {
			return; // Assignment found, nothing to assigns
		}

		graphClient.users().byUserId(userId).appRoleAssignments().byAppRoleAssignmentId(existingAssignment.getId())
				.delete();
	}

	@Override
	public AppRole getRole(AppRoleAssignment appRoleAssignment) throws Exception {
		// Get application
		Application app = graphClient.applicationsWithAppId(clientId).get();

		// Find the role by name
		Optional<AppRole> roleMember = app.getAppRoles().stream()
				.filter(r -> r.getId().equals(appRoleAssignment.getAppRoleId())).findFirst();
		return roleMember.orElseThrow(() -> new IllegalStateException("App role not found"));
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
		// 1) Resolve group by displayName with a short retry to handle Azure indexing lag

		String safeName = groupName.replace("'", "''");
		Group group = null;
		final int maxAttempts = 3;
		int attempt = 0;
		while (attempt < maxAttempts) {
			var groupsPage = graphClient.groups().get(req -> {
				req.queryParameters.filter = "displayName eq '" + safeName + "'";
				req.queryParameters.top = 1;
				req.queryParameters.select = new String[] { "id", "displayName" };
			});

			if (groupsPage != null && groupsPage.getValue() != null && !groupsPage.getValue().isEmpty()) {
				group = groupsPage.getValue().get(0);
				break;
			}

			attempt++;
			if (attempt >= maxAttempts) {
				break;
			}

			// wait ~1 second before retrying (total window ~3s)
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Interrupted while waiting for group lookup", ie);
			}
		}

		if (group == null) {
			throw new IllegalStateException("Group not found: " + groupName);
		}

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
			req.queryParameters.top = 999;
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
		// 1) List all users (include identities so we can detect Microsoft Accounts)
		var usersPage = graphClient.users().get(req -> {
			req.queryParameters.top = 999;
			req.queryParameters.select = new String[] { "id", "userPrincipalName", "identities" };
		});

		if (usersPage == null || usersPage.getValue() == null) {
			return;
		}

		// 2) Delete each user except Microsoft Account owners (issuer contains 'live.com')
		for (User user : usersPage.getValue()) {
			if (user == null || user.getId() == null) {
				continue;
			}

			// Skip global administrators
			if (isGlobalAdministrator(user.getId())) {
				//String name = user.getUserPrincipalName();
				continue;
			}

			List<ObjectIdentity> identities = user.getIdentities();
			if (identities != null) {
				for (ObjectIdentity oid : identities) {
					String issuer = oid.getIssuer() != null ? oid.getIssuer().toLowerCase() : "";
					String signInType = oid.getSignInType() != null ? oid.getSignInType().toLowerCase() : "";
					// Do not delete tenant owner's Microsoft Account users to avoid potential lockout of the tenant. 
					// These users typically have a signInType of "federated" and an issuer of "microsoftaccount" or "live.com"
					if (signInType.equalsIgnoreCase("federated") &&	issuer.equalsIgnoreCase("microsoftaccount")) {
						continue;
					}
				}
			}

			graphClient.users().byUserId(user.getId()).delete();
		}
	}

	/**
	 * Returns true if the user is a member of the tenant's Global Administrator
	 * role (Company Administrator - roleTemplateId 62e90394-69f5-4237-9190-012177145e10).
	 *
	 * This implementation checks the user's memberOf collection for a DirectoryRole
	 * with the matching roleTemplateId instead of listing role members.
	 */
	private boolean isGlobalAdministrator(String userId) throws Exception {
		if (userId == null || userId.isBlank()) {
			return false;
		}

		final String globalAdminRoleTemplateId = "62e90394-69f5-4237-9190-012177145e10";

		// Retrieve all directory objects the user is a direct member of and look for DirectoryRole
		var memberOfPage = graphClient.users().byUserId(userId).memberOf().get(req -> {
			req.queryParameters.top = 999;
			req.queryParameters.select = new String[] { "id", "roleTemplateId" };
		});

		if (memberOfPage == null || memberOfPage.getValue() == null) {
			return false;
		}

		for (DirectoryObject obj : memberOfPage.getValue()) {
			if (obj == null) {
				continue;
			}

			// DirectoryRole objects appear in the memberOf collection when the user is assigned
			if (obj instanceof DirectoryRole) {
				DirectoryRole dr = (DirectoryRole) obj;
				String templateId = dr.getRoleTemplateId();
				if (templateId != null && templateId.equalsIgnoreCase(globalAdminRoleTemplateId)) {
					return true;
				}
			}
		}

		return false;
	}
}