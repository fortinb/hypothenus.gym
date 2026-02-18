package com.iso.hypo.domain.security;

import java.util.List;

public class Roles {
	public static final String Admin = "admin";
	public static final String Manager = "manager";
	public static final String Coach = "coach";
	public static final String Member = "member";
	public static final String System = "system";

	/**
	 * Determine whether a user who holds any of the roles in currentUserRoles is
	 * allowed to assign all roles in targetRoles.
	 *
	 * Rules (hierarchy-based): - admin (level 1) can assign any role - manager
	 * (level 2) can assign manager, coach, member - coach (level 3) can assign
	 * coach, member - member (level 4) cannot assign any role - system is treated
	 * as a super-admin (level 0) and may assign any role
	 *
	 * Algorithm: compute the "authority level" (the smaller the number, the higher
	 * the authority) of the best role the current user has (minimum level). For
	 * each target role ensure its level is greater than or equal to the current
	 * user's minimum level. Special-case: if the current user's minimum level is
	 * the "member" level (4), they are not allowed to assign anything.
	 *
	 * Assumptions: - RoleEnum contains the expected values (admin, manager, coach,
	 * member, system) - "system" is treated like a super-admin (highest authority)
	 *
	 * @param requestContextUserRoles list of roles the acting user has (may be
	 *                                null/empty)
	 * @param targetRoles             list of roles to be assigned to the target
	 *                                user (may be null/empty)
	 * @return true if assignment is allowed, false otherwise
	 */
	public static boolean isRolesAssignmentAllowed(List<RoleEnum> requestContextUserRoles, List<RoleEnum> sourceRoles,
			List<RoleEnum> targetRoles) {
		// Null/empty target -> nothing to assign => allowed by default
		if (targetRoles == null) {
			throw new IllegalArgumentException("targetRoles cannot be null");
		}

		// If acting user has no roles, they cannot assign anything
		if (requestContextUserRoles == null || requestContextUserRoles.isEmpty()) {
			return false;
		}

		// Convert a RoleEnum to its numeric level (lower == more authority)
		java.util.function.Function<RoleEnum, Integer> levelOf = role -> {
			if (role == null)
				return Integer.MAX_VALUE;
			switch (role) {
			case system:
				return 0; // super-admin
			case admin:
				return 1;
			case manager:
				return 2;
			case coach:
				return 3;
			case member:
				return 4;
			default:
				return Integer.MAX_VALUE;
			}
		};

		// Determine the best authority the current user has (minimum level)
		int minCurrentLevel = Integer.MAX_VALUE;
		for (RoleEnum r : requestContextUserRoles) {
			minCurrentLevel = Math.min(minCurrentLevel, levelOf.apply(r));
		}

		// If the acting user's best role is "member", they cannot assign any roles
		if (minCurrentLevel == 4) {
			return false;
		}

		// Each target role must have a level >= minCurrentLevel
		for (RoleEnum target : targetRoles) {
			int targetLevel = levelOf.apply(target);
			// If any target role is of higher authority (smaller level) than the current
			// user's best,
			// the assignment is not allowed.
			if (targetLevel < minCurrentLevel) {
				return false;
			}
		}

		// Verify removed roles are not of higher authority than the current user's best role
		if (sourceRoles != null) {
			for (RoleEnum source : sourceRoles) {
				if (!targetRoles.contains(source)) {
					int sourceLevel = levelOf.apply(source);
					if (sourceLevel < minCurrentLevel) {
						return false;
					}
				}
			}
		}

		return true;
	}
}