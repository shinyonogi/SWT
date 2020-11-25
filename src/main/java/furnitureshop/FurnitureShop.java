/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package furnitureshop;

import org.salespointframework.EnableSalespoint;
import org.salespointframework.SalespointSecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableSalespoint
public class FurnitureShop {

	public static void main(String[] args) { SpringApplication.run(FurnitureShop.class, args); }

	@Configuration
	static class FurnitureShopWebConfiguration implements WebMvcConfigurer {

		@Override
		public void addViewControllers(ViewControllerRegistry registry) {
			registry.addViewController("/").setViewName("index");
		}
	}

	@Configuration
	static class WebSecurityConfiguration extends SalespointSecurityConfiguration {

		public BCryptPasswordEncoder passwordEncoder() {
			return new BCryptPasswordEncoder();
		}

		@Override
		protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
			auth.inMemoryAuthentication()
					.withUser("admin").password(passwordEncoder().encode("admin")).roles("EMPLOYEE");
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.csrf().disable();  // for lab purposes, that's ok!
			http.authorizeRequests().antMatchers("/admin/**").hasRole("EMPLOYEE").and()
					.formLogin().loginPage("/login").permitAll()
					.and()
					.logout().permitAll();
		}
	}
}
