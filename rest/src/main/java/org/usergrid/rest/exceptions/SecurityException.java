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
package org.usergrid.rest.exceptions;

import static org.usergrid.utils.JsonUtils.mapToJsonString;

import org.usergrid.rest.ApiResponse;

import com.sun.jersey.api.container.MappableContainerException;

/**
 * <p>
 * A runtime exception representing a failure to provide correct authentication
 * credentials. Will result in the browser presenting a password challenge if a
 * realm is provided.
 * </p>
 */
public class SecurityException extends RuntimeException {

	public static final String REALM = "Usergrid Authentication";

	private static final long serialVersionUID = 1L;

	private String realm = null;
	private String type = null;

	private SecurityException(String type, String message, String realm) {
		super(message);
		this.type = type;
		this.realm = realm;
	}

	public String getRealm() {
		return realm;
	}

	public String getType() {
		return type;
	}

	public String getJsonResponse() {
		ApiResponse response = new ApiResponse();
		response.setError(type, getMessage(), this);
		return mapToJsonString(response);
	}

	public static MappableContainerException mappableSecurityException(
			AuthErrorInfo errorInfo) {
		return mappableSecurityException(errorInfo.getType(),
				errorInfo.getMessage());
	}

	public static MappableContainerException mappableSecurityException(
			AuthErrorInfo errorInfo, String message) {
		return mappableSecurityException(errorInfo.getType(), message);
	}

	public static MappableContainerException mappableSecurityException(
			String type, String message) {
		return new MappableContainerException(new SecurityException(type,
				message, null));
	}

	public static MappableContainerException mappableSecurityException(
			AuthErrorInfo errorInfo, String message, String realm) {
		return mappableSecurityException(errorInfo.getType(), message, realm);
	}

	public static MappableContainerException mappableSecurityException(
			String type, String message, String realm) {
		return new MappableContainerException(new SecurityException(type,
				message, realm));
	}

}
