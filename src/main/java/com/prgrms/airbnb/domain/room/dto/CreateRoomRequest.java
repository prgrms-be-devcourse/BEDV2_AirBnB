package com.prgrms.airbnb.domain.room.dto;

import com.prgrms.airbnb.domain.common.entity.Address;
import com.prgrms.airbnb.domain.room.entity.RoomInfo;
import com.prgrms.airbnb.domain.room.entity.RoomType;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {

  private Address address;
  private Integer charge;
  private String name;
  private String description;
  private RoomInfo roomInfo;
  private RoomType roomType;
  private Long userId;

  public void setUserId(Long userId) {
    this.userId = userId;
  }
}
