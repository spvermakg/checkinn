package com.konrad.checkinn.listingPhoto.service;

import com.konrad.checkinn.core.entity.User;
import com.konrad.checkinn.core.exception.InvalidRequestException;
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
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class ListingPhotoService {

    private final ListingPhotoRepository listingPhotoRepository;
    private final PhotoMapper listingPhotoMapper;
    private final ListingRepository listingRepository;
    private final StorageService storageService;
    private final UserRepository userRepository;

    @Transactional
    public PhotoResponseDTO uploadPhoto(
            MultipartFile file,
            PhotoUploadRequestDTO requestDTO,
            Long listingId,
            UserDetails currentUser) {
        if (!isValidImageFormat(file)) {
            throw new IllegalFileFormatException(IllegalFileFormatException.IMAGE_FILE_REQUIRED);
        }

        User host = getUserByEmailId(currentUser);
        Listing foundListing = getListingById(listingId);
        checkOwnershipOfListing(host, foundListing);

        String fileKey = storageService.uploadFile(file);

        List<ListingPhoto> existingPhotos = listingPhotoRepository.getAllListingPhotosByListingId(listingId);

        ListingPhoto listingPhoto = new ListingPhoto();
        listingPhoto.setListing(foundListing);
        listingPhoto.setPhotoTitle(requestDTO.getPhotoTitle());

        if (existingPhotos.isEmpty()) {
            listingPhoto.setPrimary(true);
        } else if (requestDTO.isPrimary()) {
            unsetCurrentPrimary(existingPhotos);
            listingPhoto.setPrimary(true);
        } else {
            listingPhoto.setPrimary(false);
        }

        listingPhoto.setDisplayOrder(existingPhotos.size() + 1);
        listingPhoto.setImageKey(fileKey);

        ListingPhoto savedPhoto = listingPhotoRepository.save(listingPhoto);

        PhotoResponseDTO responseDTO = listingPhotoMapper.listingPhotoToPhotoResponseDTO(savedPhoto);
        responseDTO.setImageUrl(storageService.getFileUrl(fileKey));
        return responseDTO;
    }

    @Transactional
    public void deletePhoto(Long photoId, Long listingId, UserDetails currentUser) {
        ListingPhoto foundPhoto = getPhotoById(photoId);

        checkImageBelongToListing(foundPhoto, listingId);

        User host = getUserByEmailId(currentUser);
        Listing foundListing = getListingById(listingId);
        checkOwnershipOfListing(host, foundListing);

        boolean wasPrimary = foundPhoto.isPrimary();
        String fileKey = foundPhoto.getImageKey();

        listingPhotoRepository.deleteById(foundPhoto.getId());


        if (wasPrimary) {
            promoteNextPrimary(foundListing.getId());
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                storageService.deleteFile(fileKey);
            }
        });
    }

    @Transactional
    public void setPhotoPrimary(Long photoId, Long listingId, UserDetails currentUser) {
        ListingPhoto foundPhoto = getPhotoById(photoId);

        checkImageBelongToListing(foundPhoto, listingId);

        User host = getUserByEmailId(currentUser);
        Listing foundListing = getListingById(listingId);
        checkOwnershipOfListing(host, foundListing);

        List<ListingPhoto> listingPhotos = listingPhotoRepository.getAllListingPhotosByListingId(foundListing.getId());

        unsetCurrentPrimary(listingPhotos);
        foundPhoto.setPrimary(true);
        listingPhotoRepository.save(foundPhoto);
    }

    // --- Private helpers ---

    private void unsetCurrentPrimary(List<ListingPhoto> photos) {
        photos.stream()
                .filter(ListingPhoto::isPrimary)
                .findFirst()
                .ifPresent(oldPrimary -> {
                    oldPrimary.setPrimary(false);
                    listingPhotoRepository.save(oldPrimary);
                });
    }

    private void promoteNextPrimary(Long listingId) {
        List<ListingPhoto> remainingPhotos = listingPhotoRepository.getAllListingPhotosByListingId(listingId);

        if (!remainingPhotos.isEmpty()) {
            remainingPhotos.sort(Comparator.comparing(ListingPhoto::getDisplayOrder));
            ListingPhoto promoted = remainingPhotos.get(0);
            promoted.setPrimary(true);
            listingPhotoRepository.save(promoted);
        }
    }

    private ListingPhoto getPhotoById(Long photoId) {
        return listingPhotoRepository.findById(photoId)
                .orElseThrow(() -> new ListingPhotoNotFoundException(ListingPhotoNotFoundException.PHOTO_NOT_FOUND));
    }

    private void checkOwnershipOfListing(User host, Listing listing) {
        if (!host.getId().equals(listing.getHost().getId())) {
            throw new InvalidRequestException("You are not the owner of this listing");
        }
    }

    private void checkImageBelongToListing(ListingPhoto photo, Long listingId) {
        if (!photo.getListing().getId().equals(listingId)) {
            throw new InvalidRequestException("Photo does not belong to this listing");
        }
    }

    private @NonNull User getUserByEmailId(UserDetails currentUser) {
        String userEmail = currentUser.getUsername();
        return userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(UserNotFoundException.NO_USER_FOUND));
    }

    private @NonNull Listing getListingById(Long listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException(ListingNotFoundException.LISTING_NOT_FOUND));
    }

    private static boolean isValidImageFormat(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();

        if (fileName == null || !fileName.contains(".")) {
            return false;
        }

        String extension = fileName.substring(fileName.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);

        Set<String> allowedExtensions = Set.of("jpg", "jpeg", "png");

        if (!allowedExtensions.contains(extension)) {
            return false;
        }

        return Set.of("image/jpeg", "image/png").contains(contentType);
    }
}