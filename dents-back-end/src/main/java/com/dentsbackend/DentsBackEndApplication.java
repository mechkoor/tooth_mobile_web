package com.dentsbackend;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.dentsbackend.entities.Role;
import com.dentsbackend.entities.User;
import com.dentsbackend.repositories.RoleRepository;
import com.dentsbackend.repositories.UserRepository;

@SpringBootApplication
public class DentsBackEndApplication {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;

	public static void main(String[] args) {
		SpringApplication.run(DentsBackEndApplication.class, args);
	}

	// @Bean
	// CommandLineRunner init() {
	// 	return args -> {
	// 		Role admin = Role.builder().name("ROLE_ADMIN").build();
	// 		Role professor = Role.builder().name("ROLE_PROFESSOR").build();
	// 		// Role manager = Role.builder().name("ROLE_MANAGER").build();
	// 		admin = roleRepository.save(admin);
	// 		professor = roleRepository.save(professor);
	// 		// manager = roleRepository.save(manager);
	// 		System.out.println(admin);
	// 		System.out.println(roleRepository.findAll());
	// 		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	// 		var user1 = User.builder()
	// 				.firstName("admin")
	// 				.lastName("admin")
	// 				.userName("admin").email("lachgar.m@gmail.com")
	// 				.password(passwordEncoder.encode("admin"))
	// 				.roles(Set.of(admin))
	// 				.build();
		
	// 		userRepository.save(user1);
			
	// 	};
	}

}
