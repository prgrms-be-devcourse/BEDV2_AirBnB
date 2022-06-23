package com.prgrms.airbnb.domain.review.service;

import com.prgrms.airbnb.domain.reservation.entity.Reservation;
import com.prgrms.airbnb.domain.reservation.entity.ReservationStatus;
import com.prgrms.airbnb.domain.reservation.repository.ReservationRepository;
import com.prgrms.airbnb.domain.review.dto.CreateReviewRequest;
import com.prgrms.airbnb.domain.review.entity.Review;
import com.prgrms.airbnb.domain.review.repository.ReviewRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final ReservationRepository reservationRepository;

  public ReviewService(ReviewRepository reviewRepository,
      ReservationRepository reservationRepository) {
    this.reviewRepository = reviewRepository;
    this.reservationRepository = reservationRepository;
  }

  //여기서 리뷰가 존재하는지 확인 메서드
  //리뷰가 만들어지기 위한 dto

  public void save(String reservationId, CreateReviewRequest createReviewRequest){
    Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(IllegalArgumentException::new);
    if(reservation.canReviewed()){
      //리뷰를 남길수있음
      reviewRepository.save(new Review(createReviewRequest.getComment(),
          createReviewRequest.getRating(),
          reservationId,
          createReviewRequest.getVisible()
        )
      );
    }

  }

}