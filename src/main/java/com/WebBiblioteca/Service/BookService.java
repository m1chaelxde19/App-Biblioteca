package com.WebBiblioteca.Service;

import com.WebBiblioteca.DTO.Autor.AuthorRequest;
import com.WebBiblioteca.DTO.Autor.AuthorResponse;
import com.WebBiblioteca.DTO.Book.BookRequest;
import com.WebBiblioteca.DTO.Book.BookResponse;
import com.WebBiblioteca.Exception.DuplicateResourceException;
import com.WebBiblioteca.Exception.ResourceNotFoundException;
import com.WebBiblioteca.Model.Author;
import com.WebBiblioteca.Model.Book;
import com.WebBiblioteca.Model.BookState;
import com.WebBiblioteca.Model.Category;
import com.WebBiblioteca.Repository.BookReposity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookReposity bookReposity;
    private final AuthorService authorService;
    public BookService(BookReposity bookReposity, AuthorService authorService) {
        this.authorService = authorService;
        this.bookReposity = bookReposity;
    }
    public List<BookResponse> getBookList() {
        return bookReposity.findAll()
                .stream()
                .map(book -> new BookResponse(
                        book.getCodeBook(),
                        book.getTitle(),
                        book.getIsbn(),
                        book.getPublicationDate(),
                        book.getPublisher(),
                        book.getCategory(),
                        book.getStockTotal(),
                        book.getEstado(),
                        book.getAutores().stream()
                                .map(author -> new AuthorResponse(
                                        author.getIdAuthor(),
                                        author.getNames(),
                                        author.getLastname(),
                                        author.getNationality(),
                                        author.getBirthdate(),
                                        author.getGender()
                                )).collect(Collectors.toList())
                )).collect(Collectors.toList());
    }
    public List<BookResponse> getBookList(BookState bookState) {
        return bookReposity.findByEstado(bookState)
                .stream().
                map(book -> new BookResponse(
                        book.getCodeBook(),
                        book.getTitle(),
                        book.getIsbn(),
                        book.getPublicationDate(),
                        book.getPublisher(),
                        book.getCategory(),
                        book.getStockTotal(),
                        book.getEstado(),
                        book.getAutores().stream()
                                .map(author -> new AuthorResponse(
                                        author.getIdAuthor(),
                                        author.getNames(),
                                        author.getLastname(),
                                        author.getNationality(),
                                        author.getBirthdate(),
                                        author.getGender()
                                )).collect(Collectors.toList())
                )).collect(Collectors.toList());
    }

    public BookRequest getBookById(Long id) {
        Book book = bookReposity.findByCodeBook(id).orElseThrow(() -> new ResourceNotFoundException("Book not found", "id", id));
        return new BookRequest(
                book.getTitle(),
                book.getIsbn(),
                book.getPublicationDate(),
                book.getPublisher(),
                book.getCategory(),
                book.getStockTotal(),
                book.getEstado(),
                book.getAutores().stream()
                        .map(author -> new AuthorRequest(
                                author.getIdAuthor(),
                                author.getNames(),
                                author.getLastname(),
                                author.getNationality(),
                                author.getBirthdate(),
                                author.getGender()
                        )).collect(Collectors.toList())
        );
    }
    public List<Category> getAllCategories() {
        return bookReposity.findAll()
                .stream()
                .map(Book::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<BookResponse> getBooksByPublicationYear(Integer year) {
        return bookReposity.findByPublicationDateYear(year)
                .stream()
                .map(book -> new BookResponse(
                        book.getCodeBook(),
                        book.getTitle(),
                        book.getIsbn(),
                        book.getPublicationDate(),
                        book.getPublisher(),
                        book.getCategory(),
                        book.getStockTotal(),
                        book.getEstado(),
                        book.getAutores().stream()
                                .map(author -> new AuthorResponse(
                                        author.getIdAuthor(),
                                        author.getNames(),
                                        author.getLastname(),
                                        author.getNationality(),
                                        author.getBirthdate(),
                                        author.getGender()
                                )).collect(Collectors.toList())
                )).collect(Collectors.toList());
    }
    @Transactional
    public BookResponse addBook(BookRequest book) {
        if (bookReposity.findByIsbn(book.getIsbn()).isPresent()) {
            throw new DuplicateResourceException("Book with this ISBN already exists", "ISBN", book.getIsbn());
        }
        Book newBook = new Book();
        newBook.setTitle(book.getTitle());
        newBook.setPublisher(book.getPublisher());
        newBook.setPublicationDate(book.getPublicationDate());
        newBook.setCategory(book.getCategory());
        newBook.setStockTotal(book.getStockTotal());
        newBook.setIsbn(book.getIsbn());
        newBook.setEstado(BookState.ACTIVO);
        if (authorService.existsAuthors(new HashSet<>(book.getAuthor()))) {
            Set<Author> authors = book.getAuthor().stream()
                    .map(authorRequest -> authorService.getAuthorById(authorRequest.getIdAuthor()))
                    .collect(Collectors.toSet());
            newBook.setAutores(authors);
        }
        Book savedBook = bookReposity.save(newBook);
        return new BookResponse(
                savedBook.getCodeBook(),
                savedBook.getTitle(),
                savedBook.getIsbn(),
                savedBook.getPublicationDate(),
                savedBook.getPublisher(),
                savedBook.getCategory(),
                savedBook.getStockTotal(),
                savedBook.getEstado(),
                savedBook.getAutores().stream()
                        .map(author -> new AuthorResponse(
                                author.getIdAuthor(),
                                author.getNames(),
                                author.getLastname(),
                                author.getNationality(),
                                author.getBirthdate(),
                                author.getGender()
                        )).collect(Collectors.toList())
        );
    }

    public BookResponse updateBook(Long id, BookRequest book) {
        Book bookToUpdate = bookReposity.findByCodeBook(id).orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        if (book.getTitle() != null) {
            bookToUpdate.setTitle(book.getTitle());
        }
        if (book.getPublisher() != null) {
            bookToUpdate.setPublisher(book.getPublisher());
        }
        if (book.getPublicationDate() != null) {
            bookToUpdate.setPublicationDate(book.getPublicationDate());
        }
        if (book.getCategory() != null) {
            bookToUpdate.setCategory(book.getCategory());
        }
        if (book.getStockTotal() != null) {
            bookToUpdate.setStockTotal(book.getStockTotal());
        }
        if (book.getIsbn() != null) {
            bookToUpdate.setIsbn(book.getIsbn());
        }
        if (book.getState() != null) {
            bookToUpdate.setEstado(book.getState());
        }
        if(book.getAuthor() !=null && authorService.existsAuthors(new HashSet<>(book.getAuthor()))) {
            Set<Author> authors = book.getAuthor().stream()
                    .map(authorRequest -> authorService.getAuthorById(authorRequest.getIdAuthor()))
                    .collect(Collectors.toSet());
            bookToUpdate.setAutores(authors);
        }
        Book bookUpdate = bookReposity.save(bookToUpdate);
        return new BookResponse(
                bookUpdate.getCodeBook(),
                bookUpdate.getTitle(),
                bookUpdate.getIsbn(),
                bookUpdate.getPublicationDate(),
                bookUpdate.getPublisher(),
                bookUpdate.getCategory(),
                bookUpdate.getStockTotal(),
                bookUpdate.getEstado(),
                bookUpdate.getAutores().stream()
                        .map(author -> new AuthorResponse(
                                author.getIdAuthor(),
                                author.getNames(),
                                author.getLastname(),
                                author.getNationality(),
                                author.getBirthdate(),
                                author.getGender()
                        )).collect(Collectors.toList())
        );
    }
    public void deleteBook(Long id) {
        Book book = bookReposity.findByCodeBook(id).orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        book.setEstado(BookState.INACTIVO);
        bookReposity.save(book);
    }


    public List<BookResponse> filterBookCategory(Category category) {
        return bookReposity.findByCategory(category)
                .stream()
                .map(book -> new BookResponse(
                        book.getCodeBook(),
                        book.getTitle(),
                        book.getIsbn(),
                        book.getPublicationDate(),
                        book.getPublisher(),
                        book.getCategory(),
                        book.getStockTotal(),
                        book.getEstado(),
                        book.getAutores().stream()
                                .map(author -> new AuthorResponse(
                                        author.getIdAuthor(),
                                        author.getNames(),
                                        author.getLastname(),
                                        author.getNationality(),
                                        author.getBirthdate(),
                                        author.getGender()
                                )).collect(Collectors.toList())
                )).collect(Collectors.toList());
    }
    public boolean verifyBooks(List<Long> booklist){
        for(Long book : booklist){
            if(bookReposity.findById(book).isEmpty()){
                return false;
            }
            if(isAvailable(book)){
                return false;
            }
            if(getBookById(book).getStockTotal() <=0){
                return false;
            }
        }
        return true;
    }
    public boolean verifyBook(Long bookId) {
        if (bookReposity.findById(bookId).isEmpty()) {
            return false;
        }
        if (isAvailable(bookId)) {
            return false;
        }
        return getBookById(bookId).getStockTotal() > 0;
    }
    public boolean isAvailable(Long id){
        Book book = bookReposity.findByCodeBook(id).orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        return book.getEstado() != BookState.ACTIVO;
    }
    public Book getBook(Long id) {
        return bookReposity.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
    }
    public Set<Book> getBooksByIds(List<Long> bookIds) {
        return new HashSet<>(bookReposity.findAllById(bookIds));
    }
    public Set<Book> updateBooksStock(Set<Book> books) {
        Set<Book> updatedBooks = new HashSet<>();
        for (Book book : books) {
            Book existingBook = bookReposity.findById(book.getCodeBook())
                    .orElseThrow(() -> new ResourceNotFoundException("Book", "id", book.getCodeBook()));
            existingBook.setStockTotal(existingBook.getStockTotal() - 1);
            updatedBooks.add(bookReposity.save(existingBook));
        }
        return updatedBooks;
    }
    public Book updateBookStock(Long id, Integer stock) {
        Book book = bookReposity.findByCodeBook(id).orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        book.setStockTotal(book.getStockTotal() + stock);
        return bookReposity.save(book);
    }

    public List<BookResponse> getBooksByTitle(String title) {
        List<Book> bookList = Collections.emptyList();
        boolean titleValid = title != null && !title.isBlank();

        if (titleValid) {
            bookList = bookReposity.findByTitleContainingIgnoreCase(title);
        }

        return bookList.stream()
                .map(book -> new BookResponse(
                        book.getCodeBook(),
                        book.getTitle(),
                        book.getIsbn(),
                        book.getPublicationDate(),
                        book.getPublisher(),
                        book.getCategory(),
                        book.getStockTotal(),
                        book.getEstado())).collect(Collectors.toList());
    }

    public List<BookResponse> getBooksByCategoryName(String category) {
        List<Book> bookList = Collections.emptyList();
        boolean categoryValid = category != null && !category.isBlank();

        if (categoryValid) {
            bookList = bookReposity.findByCategory(Category.valueOf(category.toUpperCase()));
        }

        return bookList.stream()
                .map(book -> new BookResponse(
                        book.getCodeBook(),
                        book.getTitle(),
                        book.getIsbn(),
                        book.getPublicationDate(),
                        book.getPublisher(),
                        book.getCategory(),
                        book.getStockTotal(),
                        book.getEstado())).collect(Collectors.toList());
    }
}
