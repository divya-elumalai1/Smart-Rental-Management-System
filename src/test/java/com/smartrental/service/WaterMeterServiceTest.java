package com.smartrental.service;

import com.smartrental.model.Property;
import com.smartrental.model.PropertyStatus;
import com.smartrental.model.WaterMeterReading;
import com.smartrental.model.dto.WaterMeterBillDTO;
import com.smartrental.model.dto.WaterMeterReadingDTO;
import com.smartrental.repository.PropertyRepository;
import com.smartrental.repository.WaterMeterReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaterMeterServiceTest {

    @Mock
    private WaterMeterReadingRepository waterMeterReadingRepository;
    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private WaterMeterService waterMeterService;

    private Property property;
    private UUID propertyId;
    private static final String UNIT_NUMBER = "101";

    @BeforeEach
    void setUp() {
        propertyId = UUID.randomUUID();
        property = Property.builder()
                .id(propertyId)
                .unitNumber(UNIT_NUMBER)
                .rentAmount(new BigDecimal("10000"))
                .status(PropertyStatus.OCCUPIED)
                .build();
    }

    @Test
    void calculateWaterBill_shouldCalculateCorrectly() {
        when(propertyRepository.findByUnitNumber(UNIT_NUMBER)).thenReturn(Optional.of(property));
        when(waterMeterReadingRepository.findTopByUnitNumberOrderByReadingDateDesc(UNIT_NUMBER))
                .thenReturn(Optional.empty());

        WaterMeterBillDTO bill = waterMeterService.calculateWaterBill(UNIT_NUMBER, new BigDecimal("500"));

        assertThat(bill.getUnitNumber()).isEqualTo(UNIT_NUMBER);
        assertThat(bill.getPreviousReading()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(bill.getCurrentReading()).isEqualByComparingTo("500");
        assertThat(bill.getUnitsConsumed()).isEqualByComparingTo("500");
        assertThat(bill.getWaterRate()).isEqualByComparingTo("8");
        assertThat(bill.getWaterBill()).isEqualByComparingTo("4000");
        assertThat(bill.getTotalBill()).isEqualByComparingTo("14000");
    }

    @Test
    void calculateWaterBill_shouldUsePreviousReading() {
        WaterMeterReading lastReading = WaterMeterReading.builder()
                .currentReading(new BigDecimal("300"))
                .build();
        when(propertyRepository.findByUnitNumber(UNIT_NUMBER)).thenReturn(Optional.of(property));
        when(waterMeterReadingRepository.findTopByUnitNumberOrderByReadingDateDesc(UNIT_NUMBER))
                .thenReturn(Optional.of(lastReading));

        WaterMeterBillDTO bill = waterMeterService.calculateWaterBill(UNIT_NUMBER, new BigDecimal("500"));

        assertThat(bill.getPreviousReading()).isEqualByComparingTo("300");
        assertThat(bill.getUnitsConsumed()).isEqualByComparingTo("200");
        assertThat(bill.getWaterBill()).isEqualByComparingTo("1600");
        assertThat(bill.getTotalBill()).isEqualByComparingTo("11600");
    }

    @Test
    void calculateWaterBill_shouldNotBeNegative() {
        WaterMeterReading lastReading = WaterMeterReading.builder()
                .currentReading(new BigDecimal("600"))
                .build();
        when(propertyRepository.findByUnitNumber(UNIT_NUMBER)).thenReturn(Optional.of(property));
        when(waterMeterReadingRepository.findTopByUnitNumberOrderByReadingDateDesc(UNIT_NUMBER))
                .thenReturn(Optional.of(lastReading));

        WaterMeterBillDTO bill = waterMeterService.calculateWaterBill(UNIT_NUMBER, new BigDecimal("500"));

        assertThat(bill.getUnitsConsumed()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(bill.getWaterBill()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateWaterBill_shouldThrowWhenUnitNotFound() {
        when(propertyRepository.findByUnitNumber("NONEXISTENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> waterMeterService.calculateWaterBill("NONEXISTENT", BigDecimal.TEN))
                .hasMessageContaining("Unit not found");
    }

    @Test
    void saveReading_shouldPersistAndReturnDTO() {
        when(propertyRepository.findByUnitNumber(UNIT_NUMBER)).thenReturn(Optional.of(property));
        when(waterMeterReadingRepository.findTopByUnitNumberOrderByReadingDateDesc(UNIT_NUMBER))
                .thenReturn(Optional.empty());

        WaterMeterReading saved = WaterMeterReading.builder()
                .id(UUID.randomUUID())
                .property(property)
                .unitNumber(UNIT_NUMBER)
                .previousReading(BigDecimal.ZERO)
                .currentReading(new BigDecimal("500"))
                .unitsConsumed(new BigDecimal("500"))
                .readingDate(LocalDate.now())
                .build();
        when(waterMeterReadingRepository.save(org.mockito.ArgumentMatchers.any(WaterMeterReading.class)))
                .thenReturn(saved);

        WaterMeterReadingDTO dto = waterMeterService.saveReading(UNIT_NUMBER, new BigDecimal("500"), null, null);

        assertThat(dto.getUnitNumber()).isEqualTo(UNIT_NUMBER);
        assertThat(dto.getCurrentReading()).isEqualByComparingTo("500");
        assertThat(dto.getUnitsConsumed()).isEqualByComparingTo("500");
    }

    @Test
    void getReadingsByUnit_shouldReturnOrderedReadings() {
        WaterMeterReading r1 = WaterMeterReading.builder()
                .id(UUID.randomUUID())
                .property(property)
                .unitNumber(UNIT_NUMBER)
                .previousReading(BigDecimal.ZERO)
                .currentReading(new BigDecimal("500"))
                .unitsConsumed(new BigDecimal("500"))
                .readingDate(LocalDate.now())
                .build();
        when(waterMeterReadingRepository.findByUnitNumberOrderByReadingDateDesc(UNIT_NUMBER))
                .thenReturn(List.of(r1));

        List<WaterMeterReadingDTO> readings = waterMeterService.getReadingsByUnit(UNIT_NUMBER);

        assertThat(readings).hasSize(1);
        assertThat(readings.get(0).getUnitNumber()).isEqualTo(UNIT_NUMBER);
    }

    @Test
    void getLatestReadingsForOccupiedUnits_shouldReturnReadings() {
        WaterMeterReading r1 = WaterMeterReading.builder()
                .id(UUID.randomUUID())
                .property(property)
                .unitNumber(UNIT_NUMBER)
                .previousReading(BigDecimal.ZERO)
                .currentReading(new BigDecimal("500"))
                .unitsConsumed(new BigDecimal("500"))
                .readingDate(LocalDate.now())
                .build();
        when(propertyRepository.findByStatus(PropertyStatus.OCCUPIED)).thenReturn(List.of(property));
        when(waterMeterReadingRepository.findTopByUnitNumberOrderByReadingDateDesc(UNIT_NUMBER))
                .thenReturn(Optional.of(r1));

        List<WaterMeterReadingDTO> result = waterMeterService.getLatestReadingsForOccupiedUnits();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUnitNumber()).isEqualTo(UNIT_NUMBER);
    }

    @Test
    void getLatestReadingsForOccupiedUnits_shouldSkipUnitsWithoutReading() {
        when(propertyRepository.findByStatus(PropertyStatus.OCCUPIED)).thenReturn(List.of(property));
        when(waterMeterReadingRepository.findTopByUnitNumberOrderByReadingDateDesc(UNIT_NUMBER))
                .thenReturn(Optional.empty());

        List<WaterMeterReadingDTO> result = waterMeterService.getLatestReadingsForOccupiedUnits();

        assertThat(result).isEmpty();
    }
}
