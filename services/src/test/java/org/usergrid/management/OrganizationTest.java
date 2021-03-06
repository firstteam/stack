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
package org.usergrid.management;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManagerFactory;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.usergrid.management.cassandra.ManagementTestHelperImpl;

public class OrganizationTest {

	private static final Logger logger = Logger
			.getLogger(OrganizationTest.class);

	static ManagementService management;

	static ManagementTestHelper helper;

	static EntityManagerFactory entityManagerFactory;

	@BeforeClass
	public static void setup() throws Exception {
		logger.info("setup");
		assertNull(helper);
		helper = new ManagementTestHelperImpl();
		// helper.setClient(this);
		helper.setup();
		entityManagerFactory = helper.getJpaEntityManagerFactory();
		management = helper.getManagementService();
	}

	@AfterClass
	public static void teardown() throws Exception {
		logger.info("teardown");
		helper.teardown();
	}

	@Test
	public void testCreateOrganization() throws Exception {

		UserInfo user = management.createAdminUser("edanuff", "Ed Anuff",
				"ed@anuff.com", "test", false, false, false);
		assertNotNull(user);

		OrganizationInfo organization = management.createOrganization("ed-organization",
				user);
		assertNotNull(organization);

		Map<UUID, String> userOrganizations = management
				.getOrganizationsForAdminUser(user.getUuid());
		assertEquals("wrong number of organizations", 1, userOrganizations.size());

		List<UserInfo> users = management.getAdminUsersForOrganization(organization
				.getUuid());
		assertEquals("wrong number of users", 1, users.size());

		UUID applicationId = management.createApplication(organization.getUuid(),
				"ed-application");
		assertNotNull(applicationId);

		Map<UUID, String> applications = management
				.getApplicationsForOrganization(organization.getUuid());
		assertEquals("wrong number of applications", 1, applications.size());

		OrganizationInfo organization2 = management
				.getOrganizationForApplication(applicationId);
		assertNotNull(organization2);
		assertEquals("wrong organization name", "ed-organization", organization2.getName());

		boolean verified = management.verifyAdminUserPassword(user.getUuid(), "test");
		assertTrue(verified);

		management.activateOrganization(user.getUuid());

		UserInfo u = management.verifyAdminUserPasswordCredentials(user.getUuid()
				.toString(), "test");
		assertNotNull(u);

		String token = management.getAccessTokenForAdminUser(user.getUuid());
		assertNotNull(token);

		UUID userId = management.getAdminUserIdFromAccessToken(token);
		assertEquals(user.getUuid(), userId);

	}
}
