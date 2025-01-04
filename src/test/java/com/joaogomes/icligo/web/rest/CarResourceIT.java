package com.joaogomes.icligo.web.rest;

import static com.joaogomes.icligo.domain.CarAsserts.*;
import static com.joaogomes.icligo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joaogomes.icligo.IntegrationTest;
import com.joaogomes.icligo.domain.Car;
import com.joaogomes.icligo.repository.CarRepository;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link CarResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CarResourceIT {

    private static final String DEFAULT_MODEL = "AAAAAAAAAA";
    private static final String UPDATED_MODEL = "BBBBBBBBBB";

    private static final String DEFAULT_BRAND = "AAAAAAAAAA";
    private static final String UPDATED_BRAND = "BBBBBBBBBB";

    private static final Integer DEFAULT_CYEAR = 1;
    private static final Integer UPDATED_CYEAR = 2;

    private static final Boolean DEFAULT_AVAILABLE = false;
    private static final Boolean UPDATED_AVAILABLE = true;

    private static final String ENTITY_API_URL = "/api/cars";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCarMockMvc;

    private Car car;

    private Car insertedCar;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Car createEntity() {
        return new Car().model(DEFAULT_MODEL).brand(DEFAULT_BRAND).cyear(DEFAULT_CYEAR).available(DEFAULT_AVAILABLE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Car createUpdatedEntity() {
        return new Car().model(UPDATED_MODEL).brand(UPDATED_BRAND).cyear(UPDATED_CYEAR).available(UPDATED_AVAILABLE);
    }

    @BeforeEach
    public void initTest() {
        car = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedCar != null) {
            carRepository.delete(insertedCar);
            insertedCar = null;
        }
    }

    @Test
    @Transactional
    void createCar() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Car
        var returnedCar = om.readValue(
            restCarMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(car)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Car.class
        );

        // Validate the Car in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertCarUpdatableFieldsEquals(returnedCar, getPersistedCar(returnedCar));

        insertedCar = returnedCar;
    }

    @Test
    @Transactional
    void createCarWithExistingId() throws Exception {
        // Create the Car with an existing ID
        car.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCarMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(car)))
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllCars() throws Exception {
        // Initialize the database
        insertedCar = carRepository.saveAndFlush(car);

        // Get all the carList
        restCarMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(car.getId().intValue())))
            .andExpect(jsonPath("$.[*].model").value(hasItem(DEFAULT_MODEL)))
            .andExpect(jsonPath("$.[*].brand").value(hasItem(DEFAULT_BRAND)))
            .andExpect(jsonPath("$.[*].cyear").value(hasItem(DEFAULT_CYEAR)))
            .andExpect(jsonPath("$.[*].available").value(hasItem(DEFAULT_AVAILABLE)));
    }

    @Test
    @Transactional
    void getCar() throws Exception {
        // Initialize the database
        insertedCar = carRepository.saveAndFlush(car);

        // Get the car
        restCarMockMvc
            .perform(get(ENTITY_API_URL_ID, car.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(car.getId().intValue()))
            .andExpect(jsonPath("$.model").value(DEFAULT_MODEL))
            .andExpect(jsonPath("$.brand").value(DEFAULT_BRAND))
            .andExpect(jsonPath("$.cyear").value(DEFAULT_CYEAR))
            .andExpect(jsonPath("$.available").value(DEFAULT_AVAILABLE));
    }

    @Test
    @Transactional
    void getNonExistingCar() throws Exception {
        // Get the car
        restCarMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCar() throws Exception {
        // Initialize the database
        insertedCar = carRepository.saveAndFlush(car);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the car
        Car updatedCar = carRepository.findById(car.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCar are not directly saved in db
        em.detach(updatedCar);
        updatedCar.model(UPDATED_MODEL).brand(UPDATED_BRAND).cyear(UPDATED_CYEAR).available(UPDATED_AVAILABLE);

        restCarMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedCar.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedCar))
            )
            .andExpect(status().isOk());

        // Validate the Car in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCarToMatchAllProperties(updatedCar);
    }

    @Test
    @Transactional
    void putNonExistingCar() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        car.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCarMockMvc
            .perform(
                put(ENTITY_API_URL_ID, car.getId()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(car))
            )
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCar() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        car.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(car))
            )
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCar() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        car.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(car)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Car in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCarWithPatch() throws Exception {
        // Initialize the database
        insertedCar = carRepository.saveAndFlush(car);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the car using partial update
        Car partialUpdatedCar = new Car();
        partialUpdatedCar.setId(car.getId());

        partialUpdatedCar.model(UPDATED_MODEL).available(UPDATED_AVAILABLE);

        restCarMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCar.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCar))
            )
            .andExpect(status().isOk());

        // Validate the Car in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCarUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedCar, car), getPersistedCar(car));
    }

    @Test
    @Transactional
    void fullUpdateCarWithPatch() throws Exception {
        // Initialize the database
        insertedCar = carRepository.saveAndFlush(car);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the car using partial update
        Car partialUpdatedCar = new Car();
        partialUpdatedCar.setId(car.getId());

        partialUpdatedCar.model(UPDATED_MODEL).brand(UPDATED_BRAND).cyear(UPDATED_CYEAR).available(UPDATED_AVAILABLE);

        restCarMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCar.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCar))
            )
            .andExpect(status().isOk());

        // Validate the Car in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCarUpdatableFieldsEquals(partialUpdatedCar, getPersistedCar(partialUpdatedCar));
    }

    @Test
    @Transactional
    void patchNonExistingCar() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        car.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCarMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, car.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(car))
            )
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCar() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        car.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(car))
            )
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCar() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        car.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(car)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Car in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCar() throws Exception {
        // Initialize the database
        insertedCar = carRepository.saveAndFlush(car);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the car
        restCarMockMvc
            .perform(delete(ENTITY_API_URL_ID, car.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return carRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Car getPersistedCar(Car car) {
        return carRepository.findById(car.getId()).orElseThrow();
    }

    protected void assertPersistedCarToMatchAllProperties(Car expectedCar) {
        assertCarAllPropertiesEquals(expectedCar, getPersistedCar(expectedCar));
    }

    protected void assertPersistedCarToMatchUpdatableProperties(Car expectedCar) {
        assertCarAllUpdatablePropertiesEquals(expectedCar, getPersistedCar(expectedCar));
    }
}
