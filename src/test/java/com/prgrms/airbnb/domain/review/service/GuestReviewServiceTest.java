package com.prgrms.airbnb.domain.review.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prgrms.airbnb.domain.common.entity.Address;
import com.prgrms.airbnb.domain.common.exception.BadRequestException;
import com.prgrms.airbnb.domain.common.exception.NotFoundException;
import com.prgrms.airbnb.domain.common.exception.UnAuthorizedAccessException;
import com.prgrms.airbnb.domain.common.service.UploadService;
import com.prgrms.airbnb.domain.reservation.entity.Reservation;
import com.prgrms.airbnb.domain.reservation.entity.ReservationStatus;
import com.prgrms.airbnb.domain.reservation.repository.ReservationRepository;
import com.prgrms.airbnb.domain.review.dto.CreateReviewRequest;
import com.prgrms.airbnb.domain.review.dto.ReviewResponse;
import com.prgrms.airbnb.domain.review.dto.UpdateReviewRequest;
import com.prgrms.airbnb.domain.review.entity.Review;
import com.prgrms.airbnb.domain.review.entity.ReviewImage;
import com.prgrms.airbnb.domain.review.event.AddReviewInfoEvent;
import com.prgrms.airbnb.domain.review.event.ChangeReviewInfoEvent;
import com.prgrms.airbnb.domain.review.repository.ReviewRepository;
import com.prgrms.airbnb.domain.room.entity.Room;
import com.prgrms.airbnb.domain.room.entity.Room.ReviewInfo;
import com.prgrms.airbnb.domain.room.entity.RoomImage;
import com.prgrms.airbnb.domain.room.entity.RoomInfo;
import com.prgrms.airbnb.domain.room.entity.RoomType;
import com.prgrms.airbnb.domain.room.repository.RoomRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class GuestReviewServiceTest {

  ReviewInfo reviewInfo;
  Room room;
  Reservation reservation1;
  Review review1, review2;
  PageRequest pageRequest;
  @InjectMocks
  private GuestReviewService guestReviewService;
  @Mock
  private UploadService uploadService;
  @Mock
  private ReviewRepository reviewRepository;
  @Mock
  private RoomRepository roomRepository;
  @Mock
  private ReservationRepository reservationRepository;
  @Mock
  ApplicationEventPublisher publisher;

  @BeforeEach
  void setUp() {
    reviewInfo = new ReviewInfo(4.4, 5L);
    room = new Room(10L, new Address("1", "2"), 30000, "담양 떡갈비", "뷰가 좋습니다",
        new RoomInfo(1, 2, 3, 4), RoomType.HOUSE, reviewInfo, List.of(new RoomImage("room path1")),
        1L);
    reservation1 = new Reservation("reservationRepository.createReservationId()",
        ReservationStatus.WAITED_OK, LocalDate.now().minusDays(5), LocalDate.now().minusDays(3), 3,
        30000, 1L, 10L);
    review1 = new Review("comment", 5, "245325", true,
        List.of(new ReviewImage(11L, "Path 1"), new ReviewImage(12L, "Path 2")));
    review2 = new Review("comment", 5, "245325", false, null);
    pageRequest = PageRequest.of(0, 2);
  }

  @Nested
  @DisplayName("리뷰 저장 테스트")
  class Save {

    @Test
    @DisplayName("성공: 리뷰를 저장합니다.")
    public void save() {
      //given
      CreateReviewRequest request = new CreateReviewRequest("hello", 5, true);
      MockMultipartFile multipartFile = new MockMultipartFile("img", "img", "text/plain",
          "img".getBytes());
      List<MultipartFile> multipartFiles = new ArrayList<>();
      multipartFiles.add(multipartFile);
      reservation1.changeStatus(ReservationStatus.ACCEPTED);
      reservation1.changeStatus(ReservationStatus.WAIT_REVIEW);
      when(reservationRepository.findById(reservation1.getId())).thenReturn(
          Optional.of(reservation1));
      when(uploadService.uploadImg(any())).thenReturn("path");
      doNothing().when(publisher).publishEvent(any(AddReviewInfoEvent.class));
      when(reviewRepository.save(any())).thenReturn(review1);
      //when
      ReviewResponse reviewResponse = guestReviewService.save(1L, reservation1.getId(), request,
          multipartFiles);
      //then
      Assertions.assertThat(reservation1.getReservationStatus())
          .isEqualTo(ReservationStatus.COMPLETE);
      Assertions.assertThat(room.getReviewInfo().getReviewCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("실패: 리뷰를 남길수 없는 상태입니다.")
    public void failCantReviewed() {
      //given
      CreateReviewRequest request = new CreateReviewRequest("hello", 5, true);
      MockMultipartFile multipartFile = new MockMultipartFile("img", "img", "text/plain",
          "img".getBytes());
      List<MultipartFile> multipartFiles = new ArrayList<>();
      multipartFiles.add(multipartFile);
      when(reservationRepository.findById(reservation1.getId())).thenReturn(
          Optional.of(reservation1));
      //when
      //then
      Assertions.assertThatThrownBy(
              () -> guestReviewService.save(1L, reservation1.getId(), request, multipartFiles))
          .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("실패: 리뷰 저장 권한이 없습니다.")
    public void failAuthentication() {
      //given
      CreateReviewRequest request = new CreateReviewRequest("hello", 5, true);
      MockMultipartFile multipartFile = new MockMultipartFile("img", "img", "text/plain",
          "img".getBytes());
      List<MultipartFile> multipartFiles = new ArrayList<>();
      multipartFiles.add(multipartFile);
      reservation1.changeStatus(ReservationStatus.ACCEPTED);
      reservation1.changeStatus(ReservationStatus.WAIT_REVIEW);
      when(reservationRepository.findById(reservation1.getId())).thenReturn(
          Optional.of(reservation1));
      //when
      //then
      Assertions.assertThatThrownBy(
              () -> guestReviewService.save(2L, reservation1.getId(), request, multipartFiles))
          .isInstanceOf(UnAuthorizedAccessException.class);
    }
  }

  @Nested
  @DisplayName("리뷰 수정 테스트")
  class Modify {

    @Test
    @DisplayName("성공: 리뷰를 수정합니다.")
    public void modify() {
      //given
      List<MultipartFile> multipartFiles = new ArrayList<>();
      multipartFiles.add(new MockMultipartFile("img", "img", "text/plain", "img".getBytes()));
      UpdateReviewRequest request = new UpdateReviewRequest("hello", 0, true,
          List.of(new ReviewImage(12L, "PATH")));
      reservation1.changeStatus(ReservationStatus.ACCEPTED);
      reservation1.changeStatus(ReservationStatus.WAIT_REVIEW);
      reservation1.changeStatus(ReservationStatus.COMPLETE);
      when(uploadService.uploadImg(any())).thenReturn("path");
      when(reviewRepository.findById(any())).thenReturn(Optional.of(review1));
      when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation1));
      doNothing().when(publisher).publishEvent(any(ChangeReviewInfoEvent.class));
      doNothing().when(uploadService).delete(any());
      //when
      ReviewResponse reviewResponse = guestReviewService.modify(1L, 3L, request, multipartFiles);
      //then
      Assertions.assertThat(reservation1.getReservationStatus())
          .isEqualTo(ReservationStatus.COMPLETE);
      Assertions.assertThat(reviewResponse.getRating()).isEqualTo(0);
    }

    @Test
    @DisplayName("성공: 삭제될 이미지가 없어도 리뷰를 수정할 수 있습니다.")
    public void noDeletedImage() {
      //given
      List<MultipartFile> multipartFiles = new ArrayList<>();
      multipartFiles.add(new MockMultipartFile("img", "img", "text/plain", "img".getBytes()));
      UpdateReviewRequest request = new UpdateReviewRequest("hello", 0, true,
          List.of(new ReviewImage(11L, "PATH"), new ReviewImage(12L, "PATH")));
      reservation1.changeStatus(ReservationStatus.ACCEPTED);
      reservation1.changeStatus(ReservationStatus.WAIT_REVIEW);
      reservation1.changeStatus(ReservationStatus.COMPLETE);
      when(uploadService.uploadImg(any())).thenReturn("path");
      when(reviewRepository.findById(any())).thenReturn(Optional.of(review1));
      when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation1));
      //when
      ReviewResponse reviewResponse = guestReviewService.modify(1L, 3L, request, multipartFiles);
      //then
      Assertions.assertThat(reservation1.getReservationStatus())
          .isEqualTo(ReservationStatus.COMPLETE);
      Assertions.assertThat(reviewResponse.getRating()).isEqualTo(0);
    }

    @Test
    @DisplayName("성공: 추가된 이미지가 없어도 리뷰를 수정할 수 있습니다.")
    public void noAddImage() {
      //given
      List<MultipartFile> multipartFiles = new ArrayList<>();
      multipartFiles.add(new MockMultipartFile("img", "img", "text/plain", "img".getBytes()));
      UpdateReviewRequest request = new UpdateReviewRequest("hello", 0, true,
          List.of(new ReviewImage(12L, "PATH")));
      reservation1.changeStatus(ReservationStatus.ACCEPTED);
      reservation1.changeStatus(ReservationStatus.WAIT_REVIEW);
      reservation1.changeStatus(ReservationStatus.COMPLETE);
      when(reviewRepository.findById(any())).thenReturn(Optional.of(review1));
      when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation1));
      //when
      ReviewResponse reviewResponse = guestReviewService.modify(1L, 3L, request, null);
      //then
      Assertions.assertThat(reservation1.getReservationStatus())
          .isEqualTo(ReservationStatus.COMPLETE);
      Assertions.assertThat(reviewResponse.getRating()).isEqualTo(0);
    }

    @Test
    @DisplayName("실패: 리뷰 수정 권한이 없습니다.")
    public void failAuthentication() {
      //given
      List<MultipartFile> multipartFiles = new ArrayList<>();
      multipartFiles.add(new MockMultipartFile("img", "img", "text/plain", "img".getBytes()));
      UpdateReviewRequest request = new UpdateReviewRequest("hello", 0, true,
          List.of(new ReviewImage(12L, "PATH")));
      //when
      //then
      Assertions.assertThatThrownBy(
              () -> guestReviewService.modify(2L, 3L, request, multipartFiles))
          .isInstanceOf(NotFoundException.class);
    }
  }

  @Nested
  @DisplayName("리뷰 조회 테스트")
  class Find {

    @Test
    @DisplayName("성공: 리뷰를 조회합니다.")
    public void findAllByRoomIdWithReview() {
      //given
      Slice<Review> reviews = new SliceImpl<>(List.of(review2, review1), PageRequest.of(0, 2),
          false);
      when(reviewRepository.findAllByRoomIdForGuest(room.getId(), 1L,
          PageRequest.of(0, 2))).thenReturn(reviews);
      //when
      Slice<ReviewResponse> reviewResponses = guestReviewService.findAllByRoomId(1L, 10L,
          PageRequest.of(0, 2));
      //then
      Assertions.assertThat(reviewResponses.getContent().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("성공: 익명글인 타인의 리뷰를 조회합니다.")
    public void findAllByRoomIdWithOtherReviewAndVisibleFalse() {
      //given
      Slice<Review> reviews = new SliceImpl<>(List.of(review2), PageRequest.of(0, 2), false);
      when(reviewRepository.findAllByRoomIdForGuest(room.getId(), 2L,
          PageRequest.of(0, 2))).thenReturn(reviews);
      //when
      Slice<ReviewResponse> reviewResponses = guestReviewService.findAllByRoomId(2L, 10L,
          PageRequest.of(0, 2));
      //then
      Assertions.assertThat(reviewResponses.getContent().size()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("리뷰 삭제 테스트")
  class Remove {

    @Test
    @DisplayName("성공: 리뷰를 삭제합니다.")
    public void remove() {
      //given
      when(reviewRepository.findById(any())).thenReturn(Optional.of(review2));
      when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation1));
      //when
      guestReviewService.remove(1L, 3L);
      //then
      verify(reviewRepository, times(1)).delete(any(Review.class));
    }

    @Test
    @DisplayName("실패: 리뷰 삭제 권한이 없습니다.")
    public void failAuthentication() {
      //given
      when(reviewRepository.findById(any())).thenReturn(Optional.of(review2));
      when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation1));
      //when
      //then
      Assertions.assertThatThrownBy(() -> guestReviewService.remove(2L, 3L))
          .isInstanceOf(UnAuthorizedAccessException.class);
    }
  }
}