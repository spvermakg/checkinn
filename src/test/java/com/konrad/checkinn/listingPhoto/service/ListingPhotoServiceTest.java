package com.konrad.checkinn.listingPhoto.service;

import com.konrad.checkinn.core.entity.User;
import com.konrad.checkinn.core.exception.InvalidRequestException;
import com.konrad.checkinn.core.exception.StorageOperationException;
import com.konrad.checkinn.core.exception.UserNotFoundException;
import com.konrad.checkinn.core.repository.UserRepository;
import com.konrad.checkinn.core.service.StorageService;
import com.konrad.checkinn.listing.entity.Listing;
import com.konrad.checkinn.listing.exception.ListingNotFoundException;
import com.konrad.checkinn.listing.repository.ListingRepository;
import com.konrad.checkinn.listingPhoto.dto.PhotoResponseDTO;
import com.konrad.checkinn.listingPhoto.dto.PhotoUploadRequestDTO;
import com.konrad.checkinn.listingPhoto.entity.ListingPhoto;
import com.konrad.checkinn.listingPhoto.exception.IllegalFileFormatException;
import com.konrad.checkinn.listingPhoto.exception.ListingPhotoNotFoundException;
import com.konrad.checkinn.listingPhoto.mapper.PhotoMapper;
import com.konrad.checkinn.listingPhoto.repository.ListingPhotoRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingPhotoServiceTest {

    @Mock private ListingPhotoRepository listingPhotoRepository;
    @Mock private PhotoMapper listingPhotoMapper;
    @Mock private ListingRepository listingRepository;
    @Mock private StorageService storageService;
    @Mock private UserRepository userRepository;

    @InjectMocks private ListingPhotoService listingPhotoService;

    // Standard test fixtures
    private UserDetails mockUserDetails;
    private User mockUser;
    private Listing mockListing;
    private MockMultipartFile validFile;
    private MockMultipartFile invalidFile;

    @BeforeEach
    void setUp() {
        mockUserDetails = mock(UserDetails.class);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("host@example.com");

        User listingOwner = new User();
        listingOwner.setId(1L); // Matches mockUser by default for happy paths

        mockListing = new Listing();
        mockListing.setId(100L);
        mockListing.setHost(listingOwner);

        validFile = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", "test image content".getBytes()
        );

        invalidFile = new MockMultipartFile(
                "file", "document.txt", "text/plain", "not an image".getBytes()
        );
    }

    // =========================================================================
    // uploadPhoto Tests
    // =========================================================================
    @Nested
    @DisplayName("Tests for uploadPhoto")
    class UploadPhotoTests {

        private PhotoUploadRequestDTO requestDTO;

        @BeforeEach
        void setupRequest() {
            requestDTO = new PhotoUploadRequestDTO();
            requestDTO.setPhotoTitle("Beautiful View");
            requestDTO.setPrimary(false);
        }

        @Test
        void shouldUploadPhotoSuccessfully() {
            // Arrange
            PhotoResponseDTO expectedResponse = new PhotoResponseDTO();
            expectedResponse.setPhotoTitle("Beautiful View");

            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockUser));
            when(listingRepository.findById(100L)).thenReturn(Optional.of(mockListing));
            when(storageService.uploadFile(validFile)).thenReturn("storage-key-123");
            when(listingPhotoRepository.getAllListingPhotosByListingId(100L)).thenReturn(new ArrayList<>());

            // Capture the saved entity to verify properties later
            when(listingPhotoRepository.save(any(ListingPhoto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(listingPhotoMapper.listingPhotoToPhotoResponseDTO(any(ListingPhoto.class))).thenReturn(expectedResponse);
            when(storageService.getFileUrl("storage-key-123")).thenReturn("https://s3.com/image.jpg");

            // Act
            PhotoResponseDTO result = listingPhotoService.uploadPhoto(validFile, requestDTO, 100L, mockUserDetails);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getImageUrl()).isEqualTo("https://s3.com/image.jpg");
            verify(listingPhotoRepository).save(any(ListingPhoto.class));
        }

        @Test
        void shouldThrowIllegalFileFormatExceptionWhenFileIsNotImage() {
            assertThatThrownBy(() -> listingPhotoService.uploadPhoto(invalidFile, requestDTO, 100L, mockUserDetails))
                    .isInstanceOf(IllegalFileFormatException.class);

            verifyNoInteractions(storageService, listingPhotoRepository);
        }

        @Test
        void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
            when(mockUserDetails.getUsername()).thenReturn("unknown@example.com");
            when(userRepository.findUserByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> listingPhotoService.uploadPhoto(validFile, requestDTO, 100L, mockUserDetails))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        void shouldThrowListingNotFoundExceptionWhenListingDoesNotExist() {
            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockUser));
            when(listingRepository.findById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> listingPhotoService.uploadPhoto(validFile, requestDTO, 100L, mockUserDetails))
                    .isInstanceOf(ListingNotFoundException.class);
        }

        @Test
        void shouldThrowInvalidRequestExceptionWhenUserIsNotListingOwner() {
            User stranger = new User();
            stranger.setId(999L); // Different ID from listing host

            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(stranger));
            when(listingRepository.findById(100L)).thenReturn(Optional.of(mockListing));

            assertThatThrownBy(() -> listingPhotoService.uploadPhoto(validFile, requestDTO, 100L, mockUserDetails))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("You are not the owner of this listing");
        }

        @Test
        void shouldSetPrimaryTrueWhenFirstPhotoForListing() {
            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockUser));
            when(listingRepository.findById(100L)).thenReturn(Optional.of(mockListing));
            when(storageService.uploadFile(validFile)).thenReturn("key");
            when(listingPhotoRepository.getAllListingPhotosByListingId(100L)).thenReturn(new ArrayList<>());
            when(listingPhotoMapper.listingPhotoToPhotoResponseDTO(any())).thenReturn(new PhotoResponseDTO());

            listingPhotoService.uploadPhoto(validFile, requestDTO, 100L, mockUserDetails);

            verify(listingPhotoRepository).save(argThat(ListingPhoto::isPrimary));
        }

        @Test
        void shouldSwapPrimaryWhenNewPhotoMarkedAsPrimary() {
            requestDTO.setPrimary(true);

            ListingPhoto oldPrimary = new ListingPhoto();
            oldPrimary.setId(49L);
            oldPrimary.setPrimary(true);
            List<ListingPhoto> existingPhotos = List.of(oldPrimary);

            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockUser));
            when(listingRepository.findById(100L)).thenReturn(Optional.of(mockListing));
            when(storageService.uploadFile(validFile)).thenReturn("key");
            when(listingPhotoRepository.getAllListingPhotosByListingId(100L)).thenReturn(existingPhotos);
            when(listingPhotoMapper.listingPhotoToPhotoResponseDTO(any())).thenReturn(new PhotoResponseDTO());

            listingPhotoService.uploadPhoto(validFile, requestDTO, 100L, mockUserDetails);

            assertThat(oldPrimary.isPrimary()).isFalse();
            verify(listingPhotoRepository).save(oldPrimary); // Verifies old primary got saved as false
            verify(listingPhotoRepository).save(argThat(ListingPhoto::isPrimary)); // Verifies new photo saved as true
        }

        @Test
        void shouldSetPrimaryFalseWhenNotRequestedAndPhotosExist() {
            ListingPhoto existingPhoto = new ListingPhoto();
            existingPhoto.setPrimary(true);

            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockUser));
            when(listingRepository.findById(100L)).thenReturn(Optional.of(mockListing));
            when(storageService.uploadFile(validFile)).thenReturn("key");
            when(listingPhotoRepository.getAllListingPhotosByListingId(100L)).thenReturn(List.of(existingPhoto));
            when(listingPhotoMapper.listingPhotoToPhotoResponseDTO(any())).thenReturn(new PhotoResponseDTO());

            listingPhotoService.uploadPhoto(validFile, requestDTO, 100L, mockUserDetails);

            verify(listingPhotoRepository).save(argThat(photo -> !photo.isPrimary()));
        }

        @Test
        void shouldThrowStorageOperationExceptionWhenUploadFails() {
            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockUser));
            when(listingRepository.findById(100L)).thenReturn(Optional.of(mockListing));

            // Simulating storage failure RuntimeException (e.g., Custom StorageOperationException)
            when(storageService.uploadFile(validFile)).thenThrow(new StorageOperationException(StorageOperationException.FILE_UPLOAD_FAILURE));

            assertThatThrownBy(() -> listingPhotoService.uploadPhoto(validFile, requestDTO, 100L, mockUserDetails))
                    .isInstanceOf(StorageOperationException.class)
                    .hasMessage(StorageOperationException.FILE_UPLOAD_FAILURE);

            verify(listingPhotoRepository, never()).save(any());
        }
    }

    // =========================================================================
    // deletePhoto Tests
    // =========================================================================
    @Nested
    @DisplayName("Tests for deletePhoto")
    class DeletePhotoTests {

        private ListingPhoto targetPhoto;

        @BeforeEach
        void setupPhoto() {
            TransactionSynchronizationManager.initSynchronization();

            targetPhoto = new ListingPhoto();
            targetPhoto.setId(50L);
            targetPhoto.setListing(mockListing);
            targetPhoto.setImageKey("delete-key");
            targetPhoto.setPrimary(false);
        }

        @AfterEach
        void tearDown() {
            TransactionSynchronizationManager.clearSynchronization();
        }

        @Test
        void shouldDeletePhotoFromDbAndDeferStorageDeletion() {
            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.of(targetPhoto));
            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockUser));
            when(listingRepository.findById(100L)).thenReturn(Optional.of(mockListing));

            listingPhotoService.deletePhoto(50L, 100L, mockUserDetails);

            verify(listingPhotoRepository).deleteById(50L);
            verify(storageService, never()).deleteFile(any());

            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);

            verify(storageService).deleteFile("delete-key");
        }

        @Test
        void shouldThrowListingPhotoNotFoundExceptionWhenPhotoDoesNotExist() {
            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> listingPhotoService.deletePhoto(50L, 100L, mockUserDetails))
                    .isInstanceOf(ListingPhotoNotFoundException.class);
        }

        @Test
        void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.of(targetPhoto));
            when(mockUserDetails.getUsername()).thenReturn("unknown@example.com");
            when(userRepository.findUserByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> listingPhotoService.deletePhoto(50L, 100L, mockUserDetails))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        void shouldThrowListingNotFoundExceptionWhenListingDoesNotExist() {
            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.of(targetPhoto));
            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockUser));
            when(listingRepository.findById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> listingPhotoService.deletePhoto(50L, 100L, mockUserDetails))
                    .isInstanceOf(ListingNotFoundException.class);
        }

        @Test
        void shouldThrowInvalidRequestExceptionWhenUserIsNotListingOwner() {
            User stranger = new User();
            stranger.setId(999L);

            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.of(targetPhoto));
            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(stranger));
            when(listingRepository.findById(100L)).thenReturn(Optional.of(mockListing));

            assertThatThrownBy(() -> listingPhotoService.deletePhoto(50L, 100L, mockUserDetails))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("You are not the owner of this listing");
        }

        @Test
        void shouldThrowInvalidRequestExceptionWhenPhotoDoesNotBelongToListing() {
            Listing wrongListing = new Listing();
            wrongListing.setId(200L); // Does not match path param 100L
            targetPhoto.setListing(wrongListing);

            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.of(targetPhoto));

            assertThatThrownBy(() -> listingPhotoService.deletePhoto(50L, 100L, mockUserDetails))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Photo does not belong to this listing");
        }

        @Test
        void shouldPromoteNextPhotoWhenDeletedPhotoWasPrimary() {
            targetPhoto.setPrimary(true);

            ListingPhoto photo2 = new ListingPhoto();
            photo2.setId(51L);
            photo2.setDisplayOrder(2);
            photo2.setPrimary(false);

            ListingPhoto photo3 = new ListingPhoto();
            photo3.setId(52L);
            photo3.setDisplayOrder(1); // Lowest display order should be promoted
            photo3.setPrimary(false);

            // Using ArrayList because logic may attempt sorting operations on the collection
            List<ListingPhoto> remainingPhotos = new ArrayList<>(List.of(photo2, photo3));

            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.of(targetPhoto));
            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockUser));
            when(listingRepository.findById(100L)).thenReturn(Optional.of(mockListing));
            when(listingPhotoRepository.getAllListingPhotosByListingId(100L)).thenReturn(remainingPhotos);

            listingPhotoService.deletePhoto(50L, 100L, mockUserDetails);

            assertThat(photo3.isPrimary()).isTrue();
            verify(listingPhotoRepository).save(photo3);
        }

        @Test
        void shouldNotPromoteWhenDeletedPhotoWasNotPrimary() {
            targetPhoto.setPrimary(false);

            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.of(targetPhoto));
            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockUser));
            when(listingRepository.findById(100L)).thenReturn(Optional.of(mockListing));

            listingPhotoService.deletePhoto(50L, 100L, mockUserDetails);

            verify(listingPhotoRepository, never()).getAllListingPhotosByListingId(anyLong());
            verify(listingPhotoRepository, never()).save(any());
        }

        @Test
        void shouldNotPromoteWhenNoPhotosRemainAfterDeletion() {
            targetPhoto.setPrimary(true);

            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.of(targetPhoto));
            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockUser));
            when(listingRepository.findById(100L)).thenReturn(Optional.of(mockListing));
            when(listingPhotoRepository.getAllListingPhotosByListingId(100L)).thenReturn(Collections.emptyList());

            listingPhotoService.deletePhoto(50L, 100L, mockUserDetails);

            verify(listingPhotoRepository, never()).save(any());
        }
    }

    // =========================================================================
    // setPhotoPrimary Tests
    // =========================================================================
    @Nested
    @DisplayName("Tests for setPhotoPrimary")
    class SetPhotoPrimaryTests {

        private ListingPhoto targetPhoto;

        @BeforeEach
        void setupPhoto() {
            targetPhoto = new ListingPhoto();
            targetPhoto.setId(50L);
            targetPhoto.setListing(mockListing);
            targetPhoto.setPrimary(false);
        }

        @Test
        void shouldSwapOldPrimaryWithNewPrimary() {
            ListingPhoto oldPrimary = new ListingPhoto();
            oldPrimary.setId(49L);
            oldPrimary.setPrimary(true);

            List<ListingPhoto> existingPhotos = List.of(oldPrimary, targetPhoto);

            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.of(targetPhoto));
            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockUser));
            when(listingRepository.findById(100L)).thenReturn(Optional.of(mockListing));
            when(listingPhotoRepository.getAllListingPhotosByListingId(100L)).thenReturn(existingPhotos);

            listingPhotoService.setPhotoPrimary(50L, 100L, mockUserDetails);

            assertThat(oldPrimary.isPrimary()).isFalse();
            assertThat(targetPhoto.isPrimary()).isTrue();

            verify(listingPhotoRepository).save(oldPrimary);
            verify(listingPhotoRepository).save(targetPhoto);
        }

        @Test
        void shouldSetPrimaryWhenNoPreviousPrimaryExists() {
            List<ListingPhoto> existingPhotos = List.of(targetPhoto); // None are primary

            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.of(targetPhoto));
            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockUser));
            when(listingRepository.findById(100L)).thenReturn(Optional.of(mockListing));
            when(listingPhotoRepository.getAllListingPhotosByListingId(100L)).thenReturn(existingPhotos);

            listingPhotoService.setPhotoPrimary(50L, 100L, mockUserDetails);

            assertThat(targetPhoto.isPrimary()).isTrue();
            verify(listingPhotoRepository, times(1)).save(targetPhoto);
        }

        @Test
        void shouldThrowListingPhotoNotFoundExceptionWhenPhotoDoesNotExist() {
            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> listingPhotoService.setPhotoPrimary(50L, 100L, mockUserDetails))
                    .isInstanceOf(ListingPhotoNotFoundException.class);
        }

        @Test
        void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.of(targetPhoto));
            when(mockUserDetails.getUsername()).thenReturn("unknown@example.com");
            when(userRepository.findUserByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> listingPhotoService.setPhotoPrimary(50L, 100L, mockUserDetails))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        void shouldThrowListingNotFoundExceptionWhenListingDoesNotExist() {
            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.of(targetPhoto));
            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockUser));
            when(listingRepository.findById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> listingPhotoService.setPhotoPrimary(50L, 100L, mockUserDetails))
                    .isInstanceOf(ListingNotFoundException.class);
        }

        @Test
        void shouldThrowInvalidRequestExceptionWhenUserIsNotListingOwner() {
            User stranger = new User();
            stranger.setId(999L);

            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.of(targetPhoto));
            when(mockUserDetails.getUsername()).thenReturn("host@example.com");
            when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(stranger));
            when(listingRepository.findById(100L)).thenReturn(Optional.of(mockListing));

            assertThatThrownBy(() -> listingPhotoService.setPhotoPrimary(50L, 100L, mockUserDetails))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("You are not the owner of this listing");
        }

        @Test
        void shouldThrowInvalidRequestExceptionWhenPhotoDoesNotBelongToListing() {
            Listing wrongListing = new Listing();
            wrongListing.setId(200L);
            targetPhoto.setListing(wrongListing);

            when(listingPhotoRepository.findById(50L)).thenReturn(Optional.of(targetPhoto));

            assertThatThrownBy(() -> listingPhotoService.setPhotoPrimary(50L, 100L, mockUserDetails))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Photo does not belong to this listing");
        }
    }
}