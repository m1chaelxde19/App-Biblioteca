package com.WebBiblioteca.Service;

import com.WebBiblioteca.DTO.ReserveBook.ReserveBookRequest;
import com.WebBiblioteca.DTO.ReserveBook.ReserveBookResponse;
import com.WebBiblioteca.DTO.ReserveBook.ReserveBookUpdate;
import com.WebBiblioteca.Exception.ResourceNotFoundException;
import com.WebBiblioteca.Model.Book;
import com.WebBiblioteca.Model.ReserveBook;
import com.WebBiblioteca.Model.User;
import com.WebBiblioteca.Repository.ReserveBookRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Service
public class ReserveBookService {
    private final ReserveBookRepository reserveBookRepository;
    private final BookService bookService;
    private final UserService userService;
    public ReserveBookService(ReserveBookRepository reserveBookRepository,BookService bookService,UserService userService) {
        this.reserveBookRepository = reserveBookRepository;
        this.bookService = bookService;
        this.userService = userService;
    }
    public List<ReserveBookResponse> getReservationList() {
        return reserveBookRepository.findAll().stream().map(reserveBook -> new ReserveBookResponse(
                reserveBook.getCodeReserve(),
                reserveBook.getBook().getTitle(),
                reserveBook.getUser().getCode(),
                reserveBook.getUser().getName()+" "+reserveBook.getUser().getLastname(),
                reserveBook.getLibrarian().getCode(),
                reserveBook.getLibrarian().getName() +" "+reserveBook.getLibrarian().getLastname(),
                reserveBook.getState(),
                reserveBook.getDateReserve(),
                reserveBook.getStartTime(),
                reserveBook.getEndTime()))
                .toList();
    }

    public List<ReserveBookResponse> getReservationActives() {
        return reserveBookRepository.findByState(true).stream().map(reserveBook -> new ReserveBookResponse(
                reserveBook.getCodeReserve(),
                reserveBook.getBook().getTitle(),
                reserveBook.getUser().getCode(),
                reserveBook.getUser().getName()+" "+reserveBook.getUser().getLastname(),
                reserveBook.getLibrarian().getCode(),
                reserveBook.getLibrarian().getName() +" "+reserveBook.getLibrarian().getLastname(),
                reserveBook.getState(),
                reserveBook.getDateReserve(),
                reserveBook.getStartTime(),
                reserveBook.getEndTime()))
                .toList();
    }
    public List<ReserveBookResponse> gerReservationInactive() {
        return reserveBookRepository.findByState(false).stream().map(reserveBook -> new ReserveBookResponse(
                reserveBook.getCodeReserve(),
                reserveBook.getBook().getTitle(),
                reserveBook.getUser().getCode(),
                reserveBook.getUser().getName(),
                reserveBook.getLibrarian().getCode(),
                reserveBook.getLibrarian().getName() +" "+reserveBook.getLibrarian().getLastname(),
                reserveBook.getState(),
                reserveBook.getDateReserve(),
                reserveBook.getStartTime(),
                reserveBook.getEndTime()))
                .toList();
    }
    public ReserveBookResponse getReservationById(Long id){
        ReserveBook reserveBook = reserveBookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ReserveBook", "id", id));
        return new ReserveBookResponse(
                reserveBook.getCodeReserve(),
                reserveBook.getBook().getTitle(),
                reserveBook.getUser().getCode(),
                reserveBook.getUser().getName()+" "+reserveBook.getUser().getLastname(),
                reserveBook.getLibrarian().getCode(),
                reserveBook.getLibrarian().getName() +" "+reserveBook.getLibrarian().getLastname(),
                reserveBook.getState(),
                reserveBook.getDateReserve(),
                reserveBook.getStartTime(),
                reserveBook.getEndTime()
        );
    }
    public List<ReserveBookResponse> getReservationByUser(Long id) {
        return reserveBookRepository.findByUserId(id).stream().map(reserveBook -> new ReserveBookResponse(
                reserveBook.getCodeReserve(),
                reserveBook.getBook().getTitle(),
                reserveBook.getUser().getCode(),
                reserveBook.getUser().getName()+" "+reserveBook.getUser().getLastname(),
                reserveBook.getLibrarian().getCode(),
                reserveBook.getLibrarian().getName() +" "+reserveBook.getLibrarian().getLastname(),
                reserveBook.getState(),
                reserveBook.getDateReserve(),
                reserveBook.getStartTime(),
                reserveBook.getEndTime()))
                .toList();
    }
    public List<ReserveBookResponse> getReservationByDni(String dni) {
        return reserveBookRepository.findByUserDni(dni).stream().map(reserveBook -> new ReserveBookResponse(
                        reserveBook.getCodeReserve(),
                        reserveBook.getBook().getTitle(),
                        reserveBook.getUser().getCode(),
                        reserveBook.getUser().getName()+" "+reserveBook.getUser().getLastname(),
                        reserveBook.getLibrarian().getCode(),
                        reserveBook.getLibrarian().getName() +" "+reserveBook.getLibrarian().getLastname(),
                        reserveBook.getState(),
                        reserveBook.getDateReserve(),
                        reserveBook.getStartTime(),
                        reserveBook.getEndTime()))
                .toList();
    }
    public ReserveBookResponse saveReservation(ReserveBookRequest request){
        User user = userService.getUser(request.getUserId());
        User librarian = userService.getUser(request.getLibraryId());
        if (bookService.verifyBook(request.getBookId())) {
            Book book = bookService.updateBookStock(request.getBookId(), -1);
            ReserveBook reserveBook = new ReserveBook();
            reserveBook.setUser(user);
            reserveBook.setLibrarian(librarian);
            reserveBook.setBook(book);
            reserveBook.setState(true);
            reserveBook.setDateReserve(LocalDate.now());
            reserveBook.setStartTime(LocalTime.now());
            reserveBook.setEndTime(LocalTime.now().plusHours(4));
            ReserveBook reserveBookSave = reserveBookRepository.save(reserveBook);
            bookService.updateBookStock(request.getBookId(), -1);
            return new ReserveBookResponse(
                    reserveBookSave.getCodeReserve(),
                    reserveBookSave.getBook().getTitle(),
                    reserveBookSave.getUser().getCode(),
                    reserveBookSave.getUser().getName()+" "+reserveBookSave.getUser().getLastname(),
                    reserveBookSave.getLibrarian().getCode(),
                    reserveBookSave.getLibrarian().getName() +" "+reserveBookSave.getLibrarian().getLastname(),
                    reserveBookSave.getState(),
                    reserveBookSave.getDateReserve(),
                    reserveBookSave.getStartTime(),
                    reserveBookSave.getEndTime()
            );
        } else {
            throw new IllegalArgumentException("Some books are not available for loan.");
        }
    }
    public ReserveBookResponse updateReservation(ReserveBookUpdate request,Long id){
        ReserveBook reserveBook = reserveBookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reserve","id",id));
        if(request.getBookId() !=null){
            reserveBook.setBook(bookService.getBook(request.getId()));
        }
        if (request.getStartTime() !=null){
            reserveBook.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() !=null){
            reserveBook.setEndTime(request.getEndTime());
        }
        if(request.getReservationDate() != null){
            reserveBook.setDateReserve(request.getReservationDate());
        }
        if(request.getIsActive() != null){
            reserveBook.setState(request.getIsActive());
        }
        ReserveBook response = reserveBookRepository.save(reserveBook);
        return new ReserveBookResponse(
                response.getCodeReserve(),
                response.getBook().getTitle(),
                response.getUser().getCode(),
                response.getUser().getName()+" "+response.getUser().getLastname(),
                response.getLibrarian().getCode(),
                response.getLibrarian().getName() +" "+response.getLibrarian().getLastname(),
                response.getState(),
                response.getDateReserve(),
                response.getStartTime(),
                response.getEndTime()
        );
    }

    public void deleteReservation(Long id){
        ReserveBook reserveBook = reserveBookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reservation","id",id));
        reserveBook.setState(false);
        reserveBookRepository.save(reserveBook);
    }

}
