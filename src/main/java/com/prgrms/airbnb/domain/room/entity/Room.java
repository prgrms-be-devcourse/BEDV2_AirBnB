package com.prgrms.airbnb.domain.room.entity;

import com.prgrms.airbnb.domain.common.entity.Address;
import com.prgrms.airbnb.domain.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.util.ObjectUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room")
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE room SET is_deleted = true WHERE id = ?")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseEntity {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(name = "charge")
  private Integer charge;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Enumerated(value = EnumType.STRING)
  @Column(name = "room_type")
  private RoomType roomType;

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "is_deleted")
  private Boolean isDeleted = Boolean.FALSE;

  @Embedded
  private Address address;

  @Embedded
  private RoomInfo roomInfo;

  @Embedded
  private ReviewInfo reviewInfo;

  @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RoomImage> roomImages = new ArrayList<>();

  public Room(Address address, Integer charge, String name, String description,
      RoomInfo roomInfo, RoomType roomType, List<RoomImage> images, Long userId) {

    setAddress(address);
    setCharge(charge);
    setName(name);
    setDescription(description);
    this.roomInfo = roomInfo;
    setRoomType(roomType);
    images.forEach(this::setImage);
    setUserId(userId);
    this.isDeleted = Boolean.FALSE;
  }

  public Room(Long id, Address address, Integer charge, String name, String description,
      RoomInfo roomInfo, RoomType roomType, ReviewInfo reviewInfo, List<RoomImage> images,
      Long userId) {
    this.id = id;
    setAddress(address);
    setCharge(charge);
    setName(name);
    setDescription(description);
    this.roomInfo = roomInfo;
    setRoomType(roomType);
    this.reviewInfo = reviewInfo;
    images.forEach(this::setImage);
    setUserId(userId);
    this.isDeleted = Boolean.FALSE;
  }

  public void setCharge(Integer charge) {
    if (charge < 0) {
      throw new IllegalArgumentException("가격은 0보다 작을 수 없습니다.");
    }
    this.charge = charge;
  }

  public void setName(String newName) {
    if (StringUtils.isBlank(newName)) {
      throw new IllegalArgumentException("이름은 필수 입력사항입니다.");
    }
    this.name = newName;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setImage(RoomImage roomImage) {
    roomImage.setRoom(this);
  }

  public void deleteImage(RoomImage roomImage) {
    roomImage.deleteRoom();
  }

  private void setRoomType(RoomType roomType) {
    if (ObjectUtils.isEmpty(roomType)) {
      throw new IllegalArgumentException();
    }
    this.roomType = roomType;
  }

  private void setUserId(Long userId) {
    if (ObjectUtils.isEmpty(userId)) {
      throw new IllegalArgumentException();
    }
    this.userId = userId;
  }

  private void setAddress(Address address) {
    if (ObjectUtils.isEmpty(address)) {
      throw new IllegalArgumentException();
    }
    this.address = address;
  }

  @Embeddable
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  class ReviewInfo {

    private Double reviewRating;
    private Long reviewCount;

    public ReviewInfo(Double reviewRating, Long reviewCount) {
      if (reviewCount < 0) {
        throw new IllegalArgumentException();
      }
      this.reviewRating = reviewRating;
      this.reviewCount = reviewCount;
    }
  }
}
