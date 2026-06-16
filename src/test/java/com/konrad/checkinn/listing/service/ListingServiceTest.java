package com.konrad.checkinn.listing.service;

import com.konrad.checkinn.core.entity.User;
import com.konrad.checkinn.core.exception.UserNotFoundException;
import com.konrad.checkinn.core.repository.UserRepository;
import com.konrad.checkinn.core.service.StorageService;
import com.konrad.checkinn.listing.dto.*;
import com.konrad.checkinn.listing.entity.Amenity;
import com.konrad.checkinn.listing.entity.Listing;
import com.konrad.checkinn.listing.entity.ListingType;
import com.konrad.checkinn.listing.exception.ListingNotFoundException;
import com.konrad.checkinn.listing.mapper.ListingMapper;
import com.konrad.checkinn.listing.repository.AmenityRepository;
import com.konrad.checkinn.listing.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ListingMapper listingMapper;
    @Mock
    private AmenityRepository amenityRepository;
    @Mock
    private StorageService storageService;
    @InjectMocks
    private ListingService listingService;

    private User mockHost;
    private Listing mockListing;
    private UserDetails mockUserDetails;
    private CreateListingRequestDTO createRequestDTO;
    private UpdateListingRequestDTO updateRequestDTO;
    private ListingResponseDTO mockResponseDTO;
    private SearchListingRequestDTO searchRequestDTO;
    private Amenity mockAmenityWifi;
    private Amenity mockAmenityPool;



    @BeforeEach
    void setUp() {
        mockHost = new User();
        mockHost.setEmail("host@example.com");
        // Assuming Role enum exists or using mock values
        // mockHost.setRoles(Set.of(Role.HOST));

        mockListing = new Listing();
        mockListing.setId(1L);
        mockListing.setTitle("Beach House");
        mockListing.setHost(mockHost);
        mockListing.setDeleted(false);

        mockUserDetails = mock(UserDetails.class);
        createRequestDTO = new CreateListingRequestDTO();
        createRequestDTO.setTitle("Beach House");
        createRequestDTO.setPrice(new BigDecimal("100.00"));
        createRequestDTO.setMaxGuest(4);
        createRequestDTO.setAmenityIds(Set.of(1L, 2L));

        updateRequestDTO = new UpdateListingRequestDTO();
        updateRequestDTO.setTitle("Updated Beach House");

        mockResponseDTO = new ListingResponseDTO();
        mockResponseDTO.setId(1L);
        mockResponseDTO.setTitle("Beach House");

        searchRequestDTO = new SearchListingRequestDTO();

        mockAmenityWifi = new Amenity();
        mockAmenityWifi.setId(1L);
        mockAmenityWifi.setName("WiFi");

        mockAmenityPool = new Amenity();
        mockAmenityPool.setId(2L);
        mockAmenityPool.setName("Pool");
    }

    // =====================
    // createListing tests
    // =====================

    @Test
    void shouldCreateListingSuccessfully() {
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockHost));
        when(listingMapper.createListingRequestDtoToListing(createRequestDTO)).thenReturn(mockListing);
        when(amenityRepository.findAllById(createRequestDTO.getAmenityIds()))
                .thenReturn(List.of(mockAmenityWifi, mockAmenityPool));
        when(listingRepository.save(mockListing)).thenReturn(mockListing);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        ListingResponseDTO result = listingService.createListing(createRequestDTO, mockUserDetails);

        assertNotNull(result);
        assertEquals("Beach House", result.getTitle());
        verify(listingRepository, times(1)).save(mockListing);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenHostDoesNotExistOnCreate() {
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                listingService.createListing(createRequestDTO, mockUserDetails)
        );
        verifyNoInteractions(listingMapper, listingRepository);
    }

    @Test
    void shouldSetHostOnListingBeforeSaving() {
        Listing unmappedListing = new Listing();
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockHost));
        when(listingMapper.createListingRequestDtoToListing(createRequestDTO)).thenReturn(unmappedListing);
        when(amenityRepository.findAllById(createRequestDTO.getAmenityIds()))
                .thenReturn(List.of(mockAmenityWifi, mockAmenityPool));
        when(listingRepository.save(unmappedListing)).thenReturn(mockListing);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);
        listingService.createListing(createRequestDTO, mockUserDetails);

        assertEquals(mockHost, unmappedListing.getHost());
    }

    @Test
    void shouldReturnListingResponseDTOAfterCreate() {
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockHost));
        when(listingMapper.createListingRequestDtoToListing(createRequestDTO)).thenReturn(mockListing);
        when(amenityRepository.findAllById(createRequestDTO.getAmenityIds()))
                .thenReturn(List.of(mockAmenityWifi, mockAmenityPool));
        when(listingRepository.save(mockListing)).thenReturn(mockListing);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        ListingResponseDTO result = listingService.createListing(createRequestDTO, mockUserDetails);

        assertNotNull(result);
        verify(listingMapper, times(1)).listingToListingResponseDTO(mockListing);
    }

    // =====================
    // findListingById tests
    // =====================

    @Test
    void shouldReturnListingWhenValidIdProvided() {
        when(listingRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(mockListing));
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        ListingResponseDTO result = listingService.findListingById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrowListingNotFoundExceptionWhenListingDoesNotExist() {
        when(listingRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThrows(ListingNotFoundException.class, () -> listingService.findListingById(1L));
    }

    @Test
    void shouldThrowListingNotFoundExceptionWhenListingIsSoftDeleted() {
        // Because findListingById uses repository.findByIdAndIsDeletedFalse(listingId),
        // a soft-deleted item returns Optional.empty() from the DB layer.
        when(listingRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThrows(ListingNotFoundException.class, () -> listingService.findListingById(1L));
    }

    // =====================
    // findAllListings tests
    // =====================

    @Test
    void shouldReturnPagedListingsSuccessfully() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Listing> pagedListings = new PageImpl<>(List.of(mockListing));

        when(listingRepository.findAllByIsDeletedFalse(pageable)).thenReturn(pagedListings);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        Page<ListingResponseDTO> result = listingService.findAllListings(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Beach House", result.getContent().get(0).getTitle());
    }

    @Test
    void shouldReturnEmptyPageWhenNoListingsExist() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Listing> emptyPage = new PageImpl<>(Collections.emptyList());

        when(listingRepository.findAllByIsDeletedFalse(pageable)).thenReturn(emptyPage);

        Page<ListingResponseDTO> result = listingService.findAllListings(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(listingMapper);
    }

    @Test
    void shouldNotReturnSoftDeletedListingsInPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        // The service method relies entirely on findAllByIsDeletedFalse to filter out soft-deleted entries
        Page<Listing> activeOnlyPage = new PageImpl<>(List.of(mockListing));

        when(listingRepository.findAllByIsDeletedFalse(pageable)).thenReturn(activeOnlyPage);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        Page<ListingResponseDTO> result = listingService.findAllListings(pageable);

        assertNotNull(result);
        verify(listingRepository, times(1)).findAllByIsDeletedFalse(pageable);
    }

    // =====================
    // updateListing tests
    // =====================

    @Test
    void shouldUpdateListingSuccessfully() {
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockHost));
        when(listingRepository.findByIdAndHostAndIsDeletedFalse(1L, mockHost)).thenReturn(Optional.of(mockListing));
        when(listingRepository.save(mockListing)).thenReturn(mockListing);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        ListingResponseDTO result = listingService.updateListing(updateRequestDTO, 1L, mockUserDetails);

        assertNotNull(result);
        verify(listingMapper, times(1)).updateListingFromDTO(updateRequestDTO, mockListing);
        verify(listingRepository, times(1)).save(mockListing);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenHostDoesNotExistOnUpdate() {
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                listingService.updateListing(updateRequestDTO, 1L, mockUserDetails)
        );
        verifyNoInteractions(listingRepository, listingMapper);
    }

    @Test
    void shouldThrowListingNotFoundExceptionWhenListingDoesNotBelongToHost() {
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockHost));
        when(listingRepository.findByIdAndHostAndIsDeletedFalse(1L, mockHost)).thenReturn(Optional.empty());

        assertThrows(ListingNotFoundException.class, () ->
                listingService.updateListing(updateRequestDTO, 1L, mockUserDetails)
        );
        verify(listingRepository, never()).save(any());
    }

    @Test
    void shouldNotUpdateFieldsWhenDTOFieldsAreNull() {
        // Handled completely by mapstruct/mapper behavior, but we verify the interaction pattern
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockHost));
        when(listingRepository.findByIdAndHostAndIsDeletedFalse(1L, mockHost)).thenReturn(Optional.of(mockListing));
        when(listingRepository.save(mockListing)).thenReturn(mockListing);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        listingService.updateListing(updateRequestDTO, 1L, mockUserDetails);

        // Ensures the mapper is called to execute its internal null-checking strategy
        verify(listingMapper, times(1)).updateListingFromDTO(updateRequestDTO, mockListing);
    }

    @Test
    void shouldPartiallyUpdateListingWhenSomeFieldsAreNull() {
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockHost));
        when(listingRepository.findByIdAndHostAndIsDeletedFalse(1L, mockHost)).thenReturn(Optional.of(mockListing));
        when(listingRepository.save(mockListing)).thenReturn(mockListing);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        listingService.updateListing(updateRequestDTO, 1L, mockUserDetails);

        verify(listingMapper, times(1)).updateListingFromDTO(updateRequestDTO, mockListing);
    }

    // =====================
    // softDeleteListing tests
    // =====================

    @Test
    void shouldSoftDeleteListingSuccessfully() {
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockHost));
        when(listingRepository.findByIdAndHostAndIsDeletedFalse(1L, mockHost)).thenReturn(Optional.of(mockListing));

        listingService.softDeleteListingById(1L, mockUserDetails);

        verify(listingRepository, times(1)).save(mockListing);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenHostDoesNotExistOnDelete() {
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                listingService.softDeleteListingById(1L, mockUserDetails)
        );
        verifyNoInteractions(listingRepository);
    }

    @Test
    void shouldThrowListingNotFoundExceptionWhenListingDoesNotBelongToHostOnDelete() {
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockHost));
        when(listingRepository.findByIdAndHostAndIsDeletedFalse(1L, mockHost)).thenReturn(Optional.empty());

        assertThrows(ListingNotFoundException.class, () ->
                listingService.softDeleteListingById(1L, mockUserDetails)
        );
        verify(listingRepository, never()).save(any());
    }

    @Test
    void shouldSetIsDeletedTrueOnSoftDelete() {
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockHost));
        when(listingRepository.findByIdAndHostAndIsDeletedFalse(1L, mockHost)).thenReturn(Optional.of(mockListing));

        assertFalse(mockListing.isDeleted()); // Check initial state

        listingService.softDeleteListingById(1L, mockUserDetails);

        assertTrue(mockListing.isDeleted()); // Verify state changed to true
        verify(listingRepository).save(mockListing);
    }

    @Test
    void shouldNotHardDeleteListingOnSoftDelete() {
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockHost));
        when(listingRepository.findByIdAndHostAndIsDeletedFalse(1L, mockHost)).thenReturn(Optional.of(mockListing));

        listingService.softDeleteListingById(1L, mockUserDetails);

        // Ensure delete or deleteById methods are absolutely never called
        verify(listingRepository, never()).delete(any(Listing.class));
        verify(listingRepository, never()).deleteById(anyLong());
        verify(listingRepository, times(1)).save(mockListing);
    }

    // =====================
// searchListings tests
// =====================

    @Test
    void shouldReturnAllActiveListingsWhenNoFiltersApplied() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Listing> pagedListings = new PageImpl<>(List.of(mockListing));

        when(listingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagedListings);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        Page<ListingResponseDTO> result = listingService.searchListings(searchRequestDTO, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(listingRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldReturnEmptyPageWhenNoListingsMatchFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Listing> emptyPage = new PageImpl<>(Collections.emptyList());
        searchRequestDTO.setState("Atlantis");

        when(listingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        Page<ListingResponseDTO> result = listingService.searchListings(searchRequestDTO, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFilterByState() {
        Pageable pageable = PageRequest.of(0, 10);
        searchRequestDTO.setState("Paris");
        Page<Listing> pagedListings = new PageImpl<>(List.of(mockListing));

        when(listingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagedListings);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        Page<ListingResponseDTO> result = listingService.searchListings(searchRequestDTO, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void shouldFilterByCountry() {
        Pageable pageable = PageRequest.of(0, 10);
        searchRequestDTO.setCountry("France");
        Page<Listing> pagedListings = new PageImpl<>(List.of(mockListing));

        when(listingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagedListings);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        Page<ListingResponseDTO> result = listingService.searchListings(searchRequestDTO, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void shouldFilterByListingType() {
        Pageable pageable = PageRequest.of(0, 10);
        searchRequestDTO.setListingType(ListingType.FLAT);
        Page<Listing> pagedListings = new PageImpl<>(List.of(mockListing));

        when(listingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagedListings);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        Page<ListingResponseDTO> result = listingService.searchListings(searchRequestDTO, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void shouldFilterByPriceRange() {
        Pageable pageable = PageRequest.of(0, 10);
        searchRequestDTO.setMinPrice(new BigDecimal("50.00"));
        searchRequestDTO.setMaxPrice(new BigDecimal("200.00"));
        Page<Listing> pagedListings = new PageImpl<>(List.of(mockListing));

        when(listingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagedListings);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        Page<ListingResponseDTO> result = listingService.searchListings(searchRequestDTO, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void shouldFilterByMinGuests() {
        Pageable pageable = PageRequest.of(0, 10);
        searchRequestDTO.setMinGuests(2);
        Page<Listing> pagedListings = new PageImpl<>(List.of(mockListing));

        when(listingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagedListings);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        Page<ListingResponseDTO> result = listingService.searchListings(searchRequestDTO, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void shouldFilterByAmenities() {
        Pageable pageable = PageRequest.of(0, 10);
        searchRequestDTO.setAmenityIds(Set.of(1L, 2L));
        Page<Listing> pagedListings = new PageImpl<>(List.of(mockListing));

        when(listingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagedListings);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        Page<ListingResponseDTO> result = listingService.searchListings(searchRequestDTO, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void shouldFilterByMultipleCriteriaCombined() {
        Pageable pageable = PageRequest.of(0, 10);
        searchRequestDTO.setState("Paris");
        searchRequestDTO.setMinPrice(new BigDecimal("100.00"));
        searchRequestDTO.setMaxPrice(new BigDecimal("500.00"));
        searchRequestDTO.setListingType(ListingType.FLAT);
        searchRequestDTO.setMinGuests(2);
        searchRequestDTO.setAmenityIds(Set.of(1L));
        Page<Listing> pagedListings = new PageImpl<>(List.of(mockListing));

        when(listingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagedListings);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        Page<ListingResponseDTO> result = listingService.searchListings(searchRequestDTO, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void shouldReturnPagedSearchResults() {
        Pageable pageable = PageRequest.of(0, 2);
        Listing secondListing = new Listing();
        secondListing.setId(2L);
        ListingResponseDTO secondResponseDTO = new ListingResponseDTO();
        secondResponseDTO.setId(2L);

        Page<Listing> pagedListings = new PageImpl<>(List.of(mockListing, secondListing), pageable, 5);

        when(listingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagedListings);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);
        when(listingMapper.listingToListingResponseDTO(secondListing)).thenReturn(secondResponseDTO);

        Page<ListingResponseDTO> result = listingService.searchListings(searchRequestDTO, pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
    }

    @Test
    void shouldMapEachListingThroughMapper() {
        Pageable pageable = PageRequest.of(0, 10);
        Listing secondListing = new Listing();
        secondListing.setId(2L);
        ListingResponseDTO secondResponseDTO = new ListingResponseDTO();
        secondResponseDTO.setId(2L);

        Page<Listing> pagedListings = new PageImpl<>(List.of(mockListing, secondListing));

        when(listingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagedListings);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);
        when(listingMapper.listingToListingResponseDTO(secondListing)).thenReturn(secondResponseDTO);

        listingService.searchListings(searchRequestDTO, pageable);

        verify(listingMapper, times(1)).listingToListingResponseDTO(mockListing);
        verify(listingMapper, times(1)).listingToListingResponseDTO(secondListing);
    }

// =====================
// resolveAmenities tests (via createListing / updateListing)
// =====================

    @Test
    void shouldResolveAmenitiesOnCreate() {
        Listing unmappedListing = new Listing();
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockHost));
        when(listingMapper.createListingRequestDtoToListing(createRequestDTO)).thenReturn(unmappedListing);
        when(amenityRepository.findAllById(createRequestDTO.getAmenityIds()))
                .thenReturn(List.of(mockAmenityWifi, mockAmenityPool));
        when(listingRepository.save(unmappedListing)).thenReturn(mockListing);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        listingService.createListing(createRequestDTO, mockUserDetails);

        assertEquals(2, unmappedListing.getAmenities().size());
        assertTrue(unmappedListing.getAmenities().contains(mockAmenityWifi));
        assertTrue(unmappedListing.getAmenities().contains(mockAmenityPool));
    }

    @Test
    void shouldThrowExceptionWhenAmenityIdIsInvalidOnCreate() {
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockHost));
        when(listingMapper.createListingRequestDtoToListing(createRequestDTO)).thenReturn(mockListing);
        when(amenityRepository.findAllById(createRequestDTO.getAmenityIds()))
                .thenReturn(List.of(mockAmenityWifi)); // only 1 returned for 2 IDs

        assertThrows(IllegalArgumentException.class, () ->
                listingService.createListing(createRequestDTO, mockUserDetails)
        );
        verify(listingRepository, never()).save(any());
    }

    @Test
    void shouldUpdateAmenitiesWhenAmenityIdsProvidedOnUpdate() {
        updateRequestDTO.setAmenityIds(Set.of(1L));
        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockHost));
        when(listingRepository.findByIdAndHostAndIsDeletedFalse(1L, mockHost)).thenReturn(Optional.of(mockListing));
        when(amenityRepository.findAllById(Set.of(1L))).thenReturn(List.of(mockAmenityWifi));
        when(listingRepository.save(mockListing)).thenReturn(mockListing);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        listingService.updateListing(updateRequestDTO, 1L, mockUserDetails);

        assertEquals(1, mockListing.getAmenities().size());
        assertTrue(mockListing.getAmenities().contains(mockAmenityWifi));
    }

    @Test
    void shouldNotUpdateAmenitiesWhenAmenityIdsNullOnUpdate() {
        updateRequestDTO.setAmenityIds(null);
        mockListing.setAmenities(new HashSet<>(Set.of(mockAmenityWifi, mockAmenityPool)));

        when(mockUserDetails.getUsername()).thenReturn("host@example.com");
        when(userRepository.findUserByEmail("host@example.com")).thenReturn(Optional.of(mockHost));
        when(listingRepository.findByIdAndHostAndIsDeletedFalse(1L, mockHost)).thenReturn(Optional.of(mockListing));
        when(listingRepository.save(mockListing)).thenReturn(mockListing);
        when(listingMapper.listingToListingResponseDTO(mockListing)).thenReturn(mockResponseDTO);

        listingService.updateListing(updateRequestDTO, 1L, mockUserDetails);

        assertEquals(2, mockListing.getAmenities().size());
        verifyNoInteractions(amenityRepository);
    }

// =====================
// getAllAmenities tests
// =====================

    @Test
    void shouldReturnAllAmenities() {
        AmenityResponseDTO wifiDTO = new AmenityResponseDTO();
        wifiDTO.setId(1L);
        wifiDTO.setName("WiFi");
        AmenityResponseDTO poolDTO = new AmenityResponseDTO();
        poolDTO.setId(2L);
        poolDTO.setName("Pool");

        when(amenityRepository.findAll()).thenReturn(List.of(mockAmenityWifi, mockAmenityPool));
        when(listingMapper.amenityToAmenityResponseDTO(mockAmenityWifi)).thenReturn(wifiDTO);
        when(listingMapper.amenityToAmenityResponseDTO(mockAmenityPool)).thenReturn(poolDTO);

        List<AmenityResponseDTO> result = listingService.getAllAmenities();

        assertEquals(2, result.size());
        verify(amenityRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoAmenitiesExist() {
        when(amenityRepository.findAll()).thenReturn(Collections.emptyList());

        List<AmenityResponseDTO> result = listingService.getAllAmenities();

        assertTrue(result.isEmpty());
    }
}