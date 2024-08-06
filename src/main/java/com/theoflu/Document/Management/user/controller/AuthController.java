package com.theoflu.Document.Management.user.controller;

import com.theoflu.Document.Management.user.configs.JwtUtils;
import com.theoflu.Document.Management.user.entity.*;
import com.theoflu.Document.Management.user.repository.RoleRepository;

import com.theoflu.Document.Management.user.repository.UserRepository;

import com.theoflu.Document.Management.user.response.JwtResponse;
import com.theoflu.Document.Management.user.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.Console;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600,allowedHeaders = "*")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private  final UserService userService;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @GetMapping("/dene")
    public ResponseEntity<?> dene(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok("Oldu ENAİ");
    }

    @PostMapping("/signin")
    public JwtResponse authenticateUser(@RequestBody UserEntity loginRequest ) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item ->item.getAuthority())
                .collect(Collectors.toList());
        UserEntity user= userRepository.findUserEntityByUsername(userDetails.getUsername());

        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }

    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<?> registerUser(@RequestBody UserEntity signUpRequest) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(ERole.ROLE_Document_Manager).orElseThrow(() -> new RuntimeException("Error: Role is not found.")));
        UserEntity user = UserEntity.builder().email(signUpRequest.getEmail())
                .username(signUpRequest.getUsername())
                .password(encoder.encode(signUpRequest.getPassword()))
                .roles(roles)
                .enabled(false)
                .build();

        userRepository.save(user);


        return ResponseEntity.ok("User registered successfully!");
    }
}