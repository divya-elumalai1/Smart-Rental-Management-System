package com.smartrental.service;

import com.smartrental.exception.ResourceNotFoundException;
import com.smartrental.model.Property;
import com.smartrental.model.PropertyStatus;
import com.smartrental.model.Role;
import com.smartrental.model.User;
import com.smartrental.model.dto.PropertyRequestDTO;
import com.smartrental.model.dto.PropertyResponseDTO;
import com.smartrental.repository.PropertyRepository;
import com.smartrental.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PropertyService}.
 */
@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PropertyService propertyService;

    private UUID landlordId;
    private User landlord;
    private UUID propertyId;
    private Property property;

    @BeforeEach
    void setUp() {
        landlordId = UUID.randomUUID();
        landlord = User.builder()
                .id(landlordId)
                .firstName("Jane")
                .lastName("Landlord")
                .email("jane@example.com")
                .role(Role.OWNER)
                .build();
        propertyId = UUID.randomUUID();
        property = Property.builder()
                .id(propertyId)
                .landlord(landlord)
                .address("123 Main St")
                .city("Pune")
                .state("MH")
                .postalCode("411001")
                .rentAmount(new BigDecimal("15000"))
                .deposit(new BigDecimal("30000"))
                .bedrooms(2)
                .bathrooms(new BigDecimal("1.5"))
                .areaSqft(800)
                .status(PropertyStatus.AVAILABLE)
                .build();
    }

    @Test
    void create_shouldSavePropertyAndReturnDTO() {
        PropertyRequestDTO request = PropertyRequestDTO.builder()
                .landlordId(landlordId)
                .address("123 Main St")
                .city("Pune")
                .rentAmount(new BigDecimal("15000"))
                .build();
        when(userRepository.findById(landlordId)).thenReturn(Optional.of(landlord));
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> {
            Property p = inv.getArgument(0);
            p.setId(propertyId);
            return p;
        });

        PropertyResponseDTO result = propertyService.create(request);

        ArgumentCaptor<Property> captor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(captor.capture());
        assertThat(captor.getValue().getLandlord()).isEqualTo(landlord);
        assertThat(captor.getValue().getStatus()).isEqualTo(PropertyStatus.AVAILABLE);
        assertThat(result.getAddress()).isEqualTo("123 Main St");
        assertThat(result.getLandlordName()).isEqualTo("Jane Landlord");
        assertThat(result.getStatus()).isEqualTo(PropertyStatus.AVAILABLE);
    }

    @Test
    void create_shouldDefaultStatusToAvailableWhenNull() {
        PropertyRequestDTO request = PropertyRequestDTO.builder()
                .landlordId(landlordId)
                .address("1 St")
                .city("Pune")
                .rentAmount(new BigDecimal("1000"))
                .status(null)
                .build();
        when(userRepository.findById(landlordId)).thenReturn(Optional.of(landlord));
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> {
            Property p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        PropertyResponseDTO result = propertyService.create(request);

        assertThat(result.getStatus()).isEqualTo(PropertyStatus.AVAILABLE);
    }

    @Test
    void create_shouldThrowWhenLandlordNotFound() {
        PropertyRequestDTO request = PropertyRequestDTO.builder()
                .landlordId(landlordId)
                .address("123 Main St")
                .city("Pune")
                .rentAmount(new BigDecimal("15000"))
                .build();
        when(userRepository.findById(landlordId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
        verify(propertyRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnProperty() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        PropertyResponseDTO result = propertyService.getById(propertyId);

        assertThat(result.getId()).isEqualTo(propertyId);
        assertThat(result.getRentAmount()).isEqualByComparingTo("15000");
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.getById(propertyId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldSoftDelete() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        propertyService.delete(propertyId);

        assertThat(property.getDeleted()).isTrue();
        assertThat(property.getDeletedAt()).isNotNull();
        verify(propertyRepository).save(property);
    }

    @Test
    void findByLandlord_shouldReturnPropertiesForLandlord() {
        when(propertyRepository.findByLandlordId(landlordId)).thenReturn(List.of(property));

        List<PropertyResponseDTO> results = propertyService.findByLandlord(landlordId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLandlordId()).isEqualTo(landlordId);
    }

    @Test
    void findAvailable_shouldFilterByAvailableStatus() {
        property.setStatus(PropertyStatus.AVAILABLE);
        when(propertyRepository.findByStatus(PropertyStatus.AVAILABLE)).thenReturn(List.of(property));

        List<PropertyResponseDTO> results = propertyService.findAvailable();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(PropertyStatus.AVAILABLE);
    }

    @Test
    void update_shouldModifyExistingProperty() {
        PropertyRequestDTO request = PropertyRequestDTO.builder()
                .landlordId(landlordId)
                .address("456 New St")
                .city("Mumbai")
                .rentAmount(new BigDecimal("20000"))
                .status(PropertyStatus.OCCUPIED)
                .build();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));

        PropertyResponseDTO result = propertyService.update(propertyId, request);

        assertThat(result.getAddress()).isEqualTo("456 New St");
        assertThat(result.getCity()).isEqualTo("Mumbai");
        assertThat(result.getRentAmount()).isEqualByComparingTo("20000");
        assertThat(result.getStatus()).isEqualTo(PropertyStatus.OCCUPIED);
    }
}
