package it.polito.ezgas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import exception.GPSDataException;
import exception.InvalidGasStationException;
import exception.PriceException;
import it.polito.ezgas.dto.GasStationDto;
import it.polito.ezgas.entity.GasStation;
import it.polito.ezgas.entity.User;
import it.polito.ezgas.repository.GasStationRepository;
import it.polito.ezgas.repository.UserRepository;
import it.polito.ezgas.service.GasStationService;
import it.polito.ezgas.service.UserService;
import it.polito.ezgas.service.impl.GasStationServiceimpl;
import it.polito.ezgas.service.impl.UserServiceimpl;
import it.polito.ezgas.utils.Constants;

@RunWith( SpringRunner.class )
@DataJpaTest
public class GasStationServiceimplTest {
	
	@Autowired
	private GasStationRepository gasStationRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private GasStationService gasStationService;
	
	@TestConfiguration
    static class UserServiceImplTestContextConfiguration {
  
        @Bean
        public UserService userService() {
            return new UserServiceimpl();
        }
    }
	
	@TestConfiguration
    static class GasStationImplTestContextConfiguration {
  
        @Bean
        public GasStationService gasStationService() {
            return new GasStationServiceimpl();
        }
    }
	
	private final int NUMBER_OF_GAS_STATIONS=15;
	private final int NUMBER_OF_USERS=15;
	private final int NUMBER_OF_CAR_SHARING=2;
	private final double MAX_PRICE=5.00;
	private final double MAX_DEPENDABILITY=5.00;
	
	private int validGasStationId;
	private int validUserId;
	
	@Before
	public void init() {
		Random random = new Random();
		GasStation gasStation=null;
		User user=null;
		for(int i=0;i<NUMBER_OF_GAS_STATIONS;i++) {
			gasStation = gasStationRepository.save(new GasStation(
					"gasstationname"+i, 
					"gasstationaddress"+i, 
					random.nextBoolean(),
					random.nextBoolean(),
					random.nextBoolean(),
					random.nextBoolean(),
					random.nextBoolean(),
					Integer.toString(random.nextInt(NUMBER_OF_CAR_SHARING)), 
					random.nextDouble()*Constants.MAX_LAT*2-Constants.MAX_LAT, 
					random.nextDouble()*Constants.MAX_LON*2-Constants.MAX_LON, 
					random.nextDouble()*MAX_PRICE,
					random.nextDouble()*MAX_PRICE,
					random.nextDouble()*MAX_PRICE,
					random.nextDouble()*MAX_PRICE,
					random.nextDouble()*MAX_PRICE,
					i,
					"reporttimestamp"+i,
					random.nextDouble()*MAX_DEPENDABILITY));
		}
		if(gasStation!=null) {
			this.validGasStationId = gasStation.getGasStationId();
		}
		for(int i=0;i<NUMBER_OF_USERS;i++) {
			user = userRepository.save(new User("user"+i, "password"+i, "user"+i+"@example.com", i));
		}
		if(user!=null) {
			validUserId = user.getUserId();
		}
	}
	
	@Test(expected = InvalidGasStationException.class)
	public void testGetGasStationByIdNegative() throws InvalidGasStationException {
		gasStationService.getGasStationById(-23);
	}
	
	@Test
	public void testGetGasStationByIdDoesNotExist() throws InvalidGasStationException {
		assertNull(gasStationService.getGasStationById(9999));
	}
	
	@Test
	public void testGetGasStationByIdPositiveAndExists() throws InvalidGasStationException {
		GasStationDto gasStationDto = gasStationService.getGasStationById(this.validGasStationId);
		assertEquals(validGasStationId,(int)gasStationDto.getGasStationId());
	}
	
	@Test(expected = PriceException.class)
	public void testSaveGasStationInvalidNegativePrice() throws PriceException,GPSDataException {
		GasStationDto gasStationDto = new GasStationDto();
		gasStationDto.setHasDiesel(true);
		gasStationDto.setDieselPrice(-12);
		gasStationService.saveGasStation(gasStationDto);
	}
	
	@Test(expected = GPSDataException.class)
	public void testSaveGasStationInvalidCoordinates() throws PriceException,GPSDataException {
		GasStationDto gasStationDto = new GasStationDto();
		gasStationDto.setLat(-500.11);
		gasStationDto.setLon(343.12);
		gasStationService.saveGasStation(gasStationDto);
	}
	
	@Test
	public void testSaveGasStationValid() throws PriceException,GPSDataException {
		GasStationDto gasStationDtoSent = new GasStationDto(99999, "gasStationName", "gasStationAddress", true, false, true, true, false, "carSharing", 12.3, 23.43, 1.23, 2.34, 3.45, 4.56, 5.67, 1234, "reportTimestamp", 5.43);
		GasStationDto gasStationDtoReceived = gasStationService.saveGasStation(gasStationDtoSent);
		assertTrue(new ReflectionEquals(gasStationDtoSent,"gasStationId","reportDependability").matches(gasStationDtoReceived));
		assertEquals(0.0,gasStationDtoReceived.getReportDependability(),0.0);
	}

}
