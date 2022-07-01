package com.prgrms.airbnb.domain.user.service;

import com.prgrms.airbnb.domain.common.entity.Email;
import com.prgrms.airbnb.domain.common.service.UploadService;
import com.prgrms.airbnb.domain.user.dto.UserDetailResponse;
import com.prgrms.airbnb.domain.user.dto.UserUpdateRequest;
import com.prgrms.airbnb.domain.user.entity.Group;
import com.prgrms.airbnb.domain.user.entity.User;
import com.prgrms.airbnb.domain.user.repository.GroupRepository;
import com.prgrms.airbnb.domain.user.repository.UserRepository;
import com.prgrms.airbnb.domain.user.util.UserConverter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserService {

  @Value("${cloud.aws.s3.bucket.url}")
  private String s3Cdn;

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final UploadService uploadService;

  public UserService(UserRepository userRepository, GroupRepository groupRepository,
      UploadService uploadService) {
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
    this.uploadService = uploadService;
  }

  @Transactional
  public User join(OAuth2User oauth2User, String authorizedClientRegistrationId) {
    String providerId = oauth2User.getName();
    return findByProviderAndProviderId(authorizedClientRegistrationId, providerId)
        .orElseGet(() -> {
          Map<String, Object> attributes = oauth2User.getAttributes();
          @SuppressWarnings("unchecked")
          Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
          Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

          String nickname = (String) properties.get("nickname");
          String profileImage = (String) properties.get("profile_image");
          String email = (String) kakaoAccount.get("email");
          Group group = groupRepository.findByName("USER_GROUP")
              .orElseThrow(() -> new IllegalStateException("그룹을 찾을 수 없습니다."));
          Email newEmail = Objects.isNull(email) ? null : new Email(email);
          return userRepository.save(
              new User(nickname, authorizedClientRegistrationId, providerId, profileImage, group,
                  newEmail));
        });
  }

  public Optional<UserDetailResponse> findById(Long userId) {
    return userRepository.findById(userId).map(UserConverter::from);
  }

  public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
    return userRepository.findByProviderAndProviderId(provider, providerId);
  }

  @Transactional
  public UserDetailResponse modify(Long userId, UserUpdateRequest request,
      MultipartFile multipartFile) {
    User user = userRepository.findById(userId).orElseThrow(RuntimeException::new);

    if (Objects.nonNull(multipartFile)) {
      if (Objects.nonNull(user.getProfileImage()) && user.getProfileImage().startsWith(s3Cdn)) {
        uploadService.delete(user.getProfileImage());
      }
      String url = uploadService.uploadImg(multipartFile);
      user.changeProfileImage(url);
    }
    user.changeName(request.getName());
    user.changeEmail(request.getEmail());
    user.changePhone(request.getPhone());
    userRepository.save(user);
    return UserConverter.from(user);
  }
}