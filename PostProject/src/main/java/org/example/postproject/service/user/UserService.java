package org.example.postproject.service.user;

import lombok.RequiredArgsConstructor;
import org.example.postproject.api.dtos.MailDto;
import org.example.postproject.api.dtos.RoleDto;
import org.example.postproject.api.dtos.request.*;
import org.example.postproject.api.dtos.response.JwtResponse;
import org.example.postproject.api.dtos.response.StandardResponse;
import org.example.postproject.api.dtos.response.Status;
import org.example.postproject.entities.gender.Gender;
import org.example.postproject.entities.role.PermissionEntity;
import org.example.postproject.entities.role.RoleEntity;
import org.example.postproject.entities.user.UserEntity;
import org.example.postproject.entities.user.UserState;
import org.example.postproject.entities.verification.VerificationEntity;
import org.example.postproject.exception.AuthenticationFailedException;
import org.example.postproject.exception.DataNotFoundException;
import org.example.postproject.exception.UniqueObjectException;
import org.example.postproject.exception.UserBadRequestException;
import org.example.postproject.mapper.UserMapper;
import org.example.postproject.repository.RoleRepository;
import org.example.postproject.repository.UserRepository;
import org.example.postproject.repository.VerificationRepository;
import org.example.postproject.service.jwt.JwtService;
import org.example.postproject.service.mail.MailService;
import org.example.postproject.service.role.RoleService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;
    private final ModelMapper modelMapper;
    private final MailService mailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Transactional
    public StandardResponse<JwtResponse> save(UserRequestDto userRequestDto) {
        checkUserEmailAndPhoneNumber(userRequestDto.getEmail(), userRequestDto.getPhoneNumber());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate dateOfBirth = LocalDate.parse(userRequestDto.getDateOfBirth(), formatter);

        UserEntity userEntity = modelMapper.map(userRequestDto, UserEntity.class);
        userEntity.setState(UserState.UNVERIFIED);
        userEntity.setDateOfBirth(dateOfBirth);
        userEntity.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));

        RoleEntity role = roleRepository.findRoleEntitiesByName("USER");
        if (role == null) {
            RoleDto roleDto = RoleDto.builder().name("USER").permissions(List.of("POST", "GET", "UPDATE", "DELETE")).build();
            role = roleService.save(roleDto).getData();
        }
        userEntity.setRoles(List.of(role));
        userEntity.setPermissions(List.of(role.getPermissions().toArray(new PermissionEntity[0])));

        if (!(Objects.equals(userRequestDto.getGender(), "MALE") || Objects.equals(userRequestDto.getGender(), "FEMALE"))) {
            throw new DataNotFoundException("Gender not found");
        }
        userEntity.setGender(Gender.valueOf(userRequestDto.getGender()));

        userEntity = userRepository.save(userEntity);
        sendVerificationCode(userEntity.getEmail());

        String accessToken = jwtService.generateAccessToken(userEntity);
        String refreshToken = jwtService.generateRefreshToken(userEntity);

        UserDetailsForFront user = mappingUser(userEntity);
        JwtResponse jwtResponse = JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(user)
                .build();

        return StandardResponse.<JwtResponse>builder()
                .status(Status.SUCCESS)
                .message("Successfully signed up")
                .data(jwtResponse)
                .build();
    }

    private UserDetailsForFront mappingUser(UserEntity userEntity) {
        UserDetailsForFront map = modelMapper.map(userEntity, UserDetailsForFront.class);
        List<String> roles = new ArrayList<>();
        for (RoleEntity role : userEntity.getRoles()) {
            roles.add(role.getName());
        }
        List<String> permissions = new ArrayList<>();
        for (PermissionEntity permission : userEntity.getPermissions()) {
            permissions.add(permission.getPermission());
        }
        map.setRoles(roles);
        map.setPermissions(permissions);
        return map;
    }

    @Transactional(readOnly = true)
    public StandardResponse<JwtResponse> signIn(LoginRequestDto loginRequestDto) {
        UserEntity userEntity = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new DataNotFoundException("Incorrect email or password"));

        if (userEntity.getState() == UserState.BLOCKED) {
            throw new AuthenticationFailedException("Your account is blocked. Please contact admin@gmail.com for further information");
        }

        if (passwordEncoder.matches(loginRequestDto.getPassword(), userEntity.getPassword())) {
            String accessToken = jwtService.generateAccessToken(userEntity);
            String refreshToken = jwtService.generateRefreshToken(userEntity);
            List<RoleEntity> roles = userEntity.getRoles();
            if (!roles.isEmpty()) {
                roles.sort(Comparator.comparing(RoleEntity::getName).reversed());
            }
            UserDetailsForFront user = mappingUser(userEntity);
            JwtResponse jwtResponse = JwtResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(user)
                    .build();
            return StandardResponse.<JwtResponse>builder().status(Status.SUCCESS).message("Successfully signed in").data(jwtResponse).build();
        } else {
            throw new AuthenticationFailedException("Incorrect username or password");
        }
    }

    @Transactional
    public StandardResponse<UserDetailsForFront> updateProfile(UserUpdateRequest update, Principal principal) {
        UserEntity userEntity = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UserBadRequestException("user not found"));

        modelMapper.map(update, userEntity);
        userEntity.setUpdatedDate(LocalDateTime.now());

        if (update.getDateOfBirth() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate dateOfBirth = LocalDate.parse(update.getDateOfBirth(), formatter);
            userEntity.setDateOfBirth(dateOfBirth);
        }

        if (update.getGender() != null) {
            if (!(Objects.equals(update.getGender(), "MALE") || Objects.equals(update.getGender(), "FEMALE"))) {
                throw new DataNotFoundException("Gender not found");
            }
        }

        userRepository.save(userEntity);

        return StandardResponse.<UserDetailsForFront>builder().status(Status.SUCCESS)
                .message("User updated successfully")
                .data(mappingUser(userEntity))
                .build();
    }

    @Transactional(readOnly = true)
    public StandardResponse<List<UserDetailsForFront>> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<UserEntity> userEntities = userRepository.findAll(pageable).getContent();

        List<UserDetailsForFront> userDetails = userMapper.toDtoList(userEntities);

        return StandardResponse.<List<UserDetailsForFront>>builder()
                .status(Status.SUCCESS)
                .message("User list " + page + "-page")
                .data(userDetails)
                .build();
    }

    @Async
    public StandardResponse<String> sendVerificationCode(String email) {
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(() -> new DataNotFoundException("User not found"));
        sendVerification(userEntity, email);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Verification code has been sent").build();
    }

    @Async
    public StandardResponse<String> sendVerificationCodeToChangeEmail(String email, Principal principal) {
        UserEntity user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new DataNotFoundException("User not found"));
        Optional<UserEntity> userEntity = userRepository.findByEmail(email);
        if (userEntity.isPresent()) throw new UniqueObjectException("Email already exists");
        sendVerification(user, email);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Verification code has been sent").build();
    }

    private void sendVerification(UserEntity userEntity, String email) {
        verificationRepository.findByUserEmail(userEntity.getEmail())
                .ifPresent(verificationRepository::delete);

        VerificationEntity verificationEntity = VerificationEntity.builder()
                .userId(userEntity)
                .code(generateVerificationCode())
                .build();

        MailDto mailDto = new MailDto();
        mailDto.setEmail(email);
        mailDto.setMessage(verificationEntity.getCode());

        verificationRepository.save(verificationEntity);
        mailService.sendMessage(mailDto);
    }

    @Transactional
    public StandardResponse<String> verify(Principal principal, String code) {
        VerificationEntity entity = verificationRepository.findByUserEmail(principal.getName())
                .orElseThrow(() -> new DataNotFoundException("Verification code Not Found!"));

        if (code.equals(entity.getCode())) {
            if (entity.getCreatedDate().plusMinutes(10).isAfter(LocalDateTime.now())) {
                UserEntity user = userRepository.findByEmail(principal.getName())
                        .orElseThrow(() -> new DataNotFoundException("User Not Found"));
                user.setState(UserState.ACTIVE);
                userRepository.save(user);
                verificationRepository.delete(entity);
                return StandardResponse.<String>builder().status(Status.SUCCESS).message("Successfully Verified!").build();
            }
            verificationRepository.delete(entity);
            throw new UserBadRequestException("Verification Code has Expired!");
        }
        throw new UserBadRequestException("Wrong Verification Code!");
    }

    @Transactional
    public StandardResponse<String> forgottenPassword(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        verificationRepository.findByUserEmail(email).ifPresent(verificationRepository::delete);
        sendVerificationCode(email);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Verification code has been sent").build();
    }

    @Transactional
    public StandardResponse<String> verifyPasswordForUpdatePassword(VerifyCodeDto verifyCodeDto) {
        checkVerificationCode(verifyCodeDto);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Successfully verified").build();
    }

    @Transactional
    public StandardResponse<String> verifyCodeForChangingEmail(VerifyCodeDto verifyCodeDto, Principal principal) {
        String newEmail = verifyCodeDto.getEmail();
        verifyCodeDto.setEmail(principal.getName());
        checkVerificationCode(verifyCodeDto);
        UserEntity userEntity = userRepository.findByEmail(verifyCodeDto.getEmail()).orElseThrow(() -> new DataNotFoundException("User not found"));
        userEntity.setEmail(newEmail);
        userRepository.save(userEntity);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Email successfully changed").build();
    }

    private void checkVerificationCode(VerifyCodeDto verifyCodeDto) {
        VerificationEntity entity = verificationRepository.findByUserEmail(verifyCodeDto.getEmail())
                .orElseThrow(() -> new DataNotFoundException("Verification code doesn't exist or expired"));
        if (verifyCodeDto.getCode().equals(entity.getCode())) {
            if (entity.getCreatedDate().plusMinutes(10).isAfter(LocalDateTime.now())) {
                verificationRepository.delete(entity);
                return;
            }
            verificationRepository.delete(entity);
            throw new UserBadRequestException("Verification Code has Expired!");
        }
        throw new UserBadRequestException("Wrong Verification Code!");
    }

    @Transactional
    public StandardResponse<String> updatePassword(UpdatePasswordDto updatePasswordDto) {
        UserEntity user = userRepository.findByEmail(updatePasswordDto.getEmail())
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(updatePasswordDto.getNewPassword()));
        user.setUpdatedDate(LocalDateTime.now());
        userRepository.save(user);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Successfully updated").build();
    }

    private String generateVerificationCode() {
        Random random = new Random(System.currentTimeMillis());
        int code = random.nextInt(1000000);
        return String.format("%06d", code);
    }

    private void checkUserEmailAndPhoneNumber(String email, String phoneNumber) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserBadRequestException("Email already exists");
        }
        if (userRepository.findUserEntityByPhoneNumber(phoneNumber).isPresent()) {
            throw new UserBadRequestException("Phone number already exists");
        }
    }

    @Transactional(readOnly = true)
    public StandardResponse<JwtResponse> getNewAccessToken(Principal principal) {
        UserEntity userEntity = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        String accessToken = jwtService.generateAccessToken(userEntity);
        JwtResponse jwtResponse = JwtResponse.builder().accessToken(accessToken).build();
        return StandardResponse.<JwtResponse>builder().status(Status.SUCCESS).message("Access token successfully sent").data(jwtResponse).build();
    }

    @Transactional(readOnly = true)
    public UUID sendId(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new DataNotFoundException("User not found")).getId();
    }

    @Transactional(readOnly = true)
    public StandardResponse<UserDetailsForFront> getMeByToken(String email) {
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(() -> new DataNotFoundException("User not found"));
        return StandardResponse.<UserDetailsForFront>builder().status(Status.SUCCESS).message("User entity").data(mappingUser(userEntity)).build();
    }

    @Transactional(readOnly = true)
    public String sendEmail(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found")).getEmail();
    }

    @Transactional(readOnly = true)
    public UserResponseForFront sendUser(UUID userId) {
        UserEntity user = userRepository.getUserById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));
        return UserResponseForFront.builder()
                .name(user.getFullName())
                .id(user.getId())
                .build();
    }

    @Transactional(readOnly = true)
    public StandardResponse<Boolean> checkPassword(CheckPasswordDto checkPasswordDto, Principal principal) {
        UserEntity userEntity = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new DataNotFoundException("User not found"));
        boolean matches = passwordEncoder.matches(checkPasswordDto.getPassword(), userEntity.getPassword());
        if (!matches) throw new UserBadRequestException("Password does not match");
        return StandardResponse.<Boolean>builder().status(Status.SUCCESS).message("Password matches").data(true).build();
    }

    public UserEntity getByPhoneNumber(String phoneNumber) {
        return userRepository.findUserEntityByPhoneNumber(phoneNumber)
                .orElse(null);
    }

}
