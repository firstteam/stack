/*******************************************************************************
 * Copyright (c) 2010, 2011 Ed Anuff and Usergrid, all rights reserved.
 * http://www.usergrid.com
 * 
 * This file is part of Usergrid Stack.
 * 
 * Usergrid Stack is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * Usergrid Stack is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along
 * with Usergrid Stack. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU AGPL version 3 section 7
 * 
 * Linking Usergrid Stack statically or dynamically with other modules is making
 * a combined work based on Usergrid Stack. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * In addition, as a special exception, the copyright holders of Usergrid Stack
 * give you permission to combine Usergrid Stack with free software programs or
 * libraries that are released under the GNU LGPL and with independent modules
 * that communicate with Usergrid Stack solely through:
 * 
 *   - Classes implementing the org.usergrid.services.Service interface
 *   - Apache Shiro Realms and Filters
 *   - Servlet Filters and JAX-RS/Jersey Filters
 * 
 * You may copy and distribute such a system following the terms of the GNU AGPL
 * for Usergrid Stack and the licenses of the other code concerned, provided that
 ******************************************************************************/
package org.usergrid.rest.management.users.organizations;

import java.util.Map;
import java.util.UUID;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.usergrid.management.OrganizationInfo;
import org.usergrid.management.UserInfo;
import org.usergrid.rest.AbstractContextResource;
import org.usergrid.rest.ApiResponse;
import org.usergrid.rest.security.annotations.RequireAdminUserAccess;
import org.usergrid.rest.security.annotations.RequireOrganizationAccess;
import org.usergrid.security.shiro.utils.SubjectUtils;

import com.google.common.collect.BiMap;

@Produces(MediaType.APPLICATION_JSON)
public class OrganizationsResource extends AbstractContextResource {

	UserInfo user;

	public OrganizationsResource(AbstractContextResource parent, UserInfo user) {
		super(parent);
		this.user = user;
	}

	@RequireAdminUserAccess
	@GET
	public ApiResponse getUserOrganizations(@Context UriInfo ui)
			throws Exception {

		ApiResponse response = new ApiResponse(ui);
		response.setAction("get user management");

		BiMap<UUID, String> userOrganizations = SubjectUtils.getOrganizations();
		response.setData(userOrganizations.inverse());

		return response;
	}

	@RequireAdminUserAccess
	@POST
	public ApiResponse newOrganizationForUser(@Context UriInfo ui,
			Map<String, Object> json) throws Exception {

		ApiResponse response = new ApiResponse(ui);
		response.setAction("new organization for user");

		String organizationName = (String) json.get("organization");
		OrganizationInfo organization = management.createOrganization(
				organizationName, user);
		response.setData(organization);

		management.activateOrganization(organization.getUuid());
		management.sendOrganizationActivationEmail(organization);

		return response;
	}

	@RequireOrganizationAccess
	@PUT
	@Path("{organizationId: [A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}}")
	public ApiResponse addUserToOrganization(@Context UriInfo ui,
			@PathParam("organizationId") String organizationIdStr)
			throws Exception {

		ApiResponse response = new ApiResponse(ui);
		response.setAction("add user to organization");

		OrganizationInfo organization = management.getOrganizationByUuid(UUID
				.fromString(organizationIdStr));
		management
				.addAdminUserToOrganization(user.getUuid(), organization.getUuid());
		response.setData(organization);
		return response;
	}

	@RequireOrganizationAccess
	@DELETE
	@Path("{organizationId: [A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}}")
	public ApiResponse removeUserFromOrganizationByOrganizationId(
			@Context UriInfo ui,
			@PathParam("organizationId") String organizationIdStr)
			throws Exception {

		ApiResponse response = new ApiResponse(ui);
		response.setAction("remove user from organization");

		OrganizationInfo organization = management.getOrganizationByUuid(UUID
				.fromString(organizationIdStr));
		management.removeAdminUserFromOrganization(user.getUuid(),
				organization.getUuid());
		response.setData(organization);
		return response;
	}

	@RequireOrganizationAccess
	@DELETE
	@Path("{organizationName}")
	public ApiResponse removeUserFromOrganizationByOrganizationName(
			@Context UriInfo ui,
			@PathParam("organizationName") String organizationName)
			throws Exception {

		ApiResponse response = new ApiResponse(ui);
		response.setAction("remove user from organization");
		OrganizationInfo organization = management
				.getOrganizationByName(organizationName);
		management.removeAdminUserFromOrganization(user.getUuid(),
				organization.getUuid());
		response.setData(organization);

		return response;
	}

}
