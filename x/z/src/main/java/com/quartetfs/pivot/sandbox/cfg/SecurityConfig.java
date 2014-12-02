/*
 * (C) Quartet FS 2012
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import com.quartetfs.biz.pivot.security.IRoleMapping;
import com.quartetfs.biz.pivot.security.impl.BasicRoleMapping;
import com.quartetfs.biz.pivot.security.impl.UserDetailsServiceWrapper;
import com.quartetfs.fwk.QuartetException;

/**
 * 
 * Spring configuration fragment for security.
 * 
 * <p>
 * You can import one of the following resources:
 * <ul>
 * <li>SecurityAnonymous.xml   (allow anonymous access)
 * <li>SecurityBasic.xml       (HTTP basic authentication)
 * <li>SecurityDigest.xml      (HTTP digest authentication)
 * </ul>
 * 
 * @author Quartet FS
 *
 */
@Configuration
@ImportResource(value={"classpath:SECURITY-INF/SecurityBasic.xml"})
public class SecurityConfig {
	
	
	/**
	 * Install a simple authentication manager with one single
	 * authentication provider.
	 * 
	 * The provider relies on a user details service.
	 * 
	 * @return authentication manager
	 */
	@Bean(name="org.springframework.security.authenticationManager")
	public AuthenticationManager authenticationManager() {
		ProviderManager manager = new ProviderManager(Collections.singletonList(authenticationProvider()));
		manager.setEraseCredentialsAfterAuthentication(false);
		return manager;
	}
	
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsManager());
		return provider;
	}


	/**
	 * Simple use details manager based on an hard coded user list,
	 * stored in memory.
	 * 
	 * For production usage this is at least replaced by a database store.
	 * 
	 * @return user details manager
	 */
	@Bean
	public UserDetailsManager userDetailsManager() {
		List<UserDetails> users = new ArrayList<UserDetails>();
		users.add(new SimpleUser("admin", "admin", "ROLE_USER", "ROLE_ADMIN"));
		users.add(new SimpleUser("user1", "user1", "ROLE_USER"));
		users.add(new SimpleUser("user2", "user2", "ROLE_USER"));
		users.add(new SimpleUser("live",  "live",  "ROLE_TECH"));  // Technical user for ActivePivot Live access
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager(users);
		return manager;
	}

	@Bean
	public UserDetailsServiceWrapper userDetailsServiceWrapper() {
		UserDetailsServiceWrapper wrapper = new UserDetailsServiceWrapper();
		wrapper.setUserDetailsService(userDetailsManager());
		return wrapper;
	}

	
	
	/**
	 * The role mapping bean associates ActivePivot roles
	 * with security groups or security users.
	 * 
	 * The roles are provided in XML resources.
	 * 
	 * @return role mapping
	 * @throws QuartetException
	 */
	@Bean
	public IRoleMapping roleMapping() throws QuartetException {
		BasicRoleMapping mapping = new BasicRoleMapping();
		Properties props = new Properties();
		props.setProperty("admin", "ROLE-INF/All.xml");
		props.setProperty("user1", "ROLE-INF/DeskA.xml");
		props.setProperty("user2", "ROLE-INF/EurUsd.xml");
		props.setProperty("guest", "ROLE-INF/EurUsd.xml");
		mapping.setMappings(props);

		return mapping;
	}

	
	
	/**
	 * 
	 * Convenient and simple Spring Security user implementation.
	 * 
	 * @author Quartet FS
	 *
	 */
	public static class SimpleUser extends User {
		
		/** serialVersionUID */
		private static final long serialVersionUID = -2171284605087844550L;

		public SimpleUser(String username, String password, String ... roles) {
			super(username, password, wrapRoles(roles));
		}
		
		static List<GrantedAuthority> wrapRoles(String ... roles) {
			if(roles != null) {
				List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
				for(String role : roles) {
					authorities.add(new SimpleGrantedAuthority(role));
				}
				return authorities;
			} else {
				return null;
			}
		}
		
	}
	
}