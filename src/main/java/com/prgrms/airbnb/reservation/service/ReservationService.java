package com.prgrms.airbnb.reservation.service;

import com.prgrms.airbnb.reservation.ReservationConverter;
import com.prgrms.airbnb.reservation.domain.Reservation;
import com.prgrms.airbnb.reservation.dto.CreateReservationRequest;
import com.prgrms.airbnb.reservation.dto.ReservationDetailResponseForGuest;
import com.prgrms.airbnb.reservation.repository.ReservationRepository;
import com.prgrms.airbnb.room.domain.Room;
import com.prgrms.airbnb.room.repository.RoomRepository;
import com.prgrms.airbnb.user.domain.User;
import com.prgrms.airbnb.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReservationDetailResponseForGuest save(CreateReservationRequest createReservationRequest) {
        User user = userRepository.findById(createReservationRequest.getUserId()).orElseThrow(IllegalArgumentException::new);
        Room room = roomRepository.findById(createReservationRequest.getRoomId()).orElseThrow(IllegalArgumentException::new);
        String reservationNo = reservationRepository.createReservationNo();
        Reservation reservation = ReservationConverter.toReservation(reservationNo, createReservationRequest);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationConverter.ofDetailForGuest(savedReservation, user, room);
    }
}
